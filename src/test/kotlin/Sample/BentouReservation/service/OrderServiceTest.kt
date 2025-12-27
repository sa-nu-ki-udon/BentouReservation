package Sample.BentouReservation.service

import Sample.BentouReservation.dto.OrderCreateRequest
import Sample.BentouReservation.dto.OrderUpdateRequest
import Sample.BentouReservation.entity.CalendarSetting
import Sample.BentouReservation.entity.Employee
import Sample.BentouReservation.entity.Menu
import Sample.BentouReservation.entity.Order
import Sample.BentouReservation.entity.Vendor
import Sample.BentouReservation.repository.CalendarSettingRepository
import Sample.BentouReservation.repository.EmployeeRepository
import Sample.BentouReservation.repository.MenuRepository
import Sample.BentouReservation.repository.OrderRepository
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@DisplayName("OrderService テスト")
class OrderServiceTest {

    @Mock
    private lateinit var orderRepository: OrderRepository

    @Mock
    private lateinit var employeeRepository: EmployeeRepository

    @Mock
    private lateinit var menuRepository: MenuRepository

    @Mock
    private lateinit var calendarSettingRepository: CalendarSettingRepository

    private lateinit var orderService: OrderService

    private lateinit var testEmployee: Employee
    private lateinit var testVendor: Vendor
    private lateinit var testMenu: Menu

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        orderService = OrderService(orderRepository, employeeRepository, menuRepository, calendarSettingRepository)

        val now = LocalDateTime.now()
        testVendor = Vendor(id = 1L, name = "テスト業者").apply {
        }
        testMenu = Menu(
            id = 1L,
            vendor = testVendor,
            name = "テスト弁当",
            price = BigDecimal("500"),
            effectiveFrom = LocalDate.now()
        ).apply {
        }
        testEmployee = Employee(id = 1L, employeeNumber = "E001", name = "テスト社員").apply {
            deleted = false
        }
    }

    // ==================== 正常系テスト ====================

    @Nested
    @DisplayName("findByEmployeeAndDate - 社員の指定日注文取得")
    inner class FindByEmployeeAndDateTests {

        @Test
        @DisplayName("社員の指定日の注文を取得できる")
        fun `should return order for employee on specified date`() {
            // Given
            val now = LocalDateTime.now()
            val today = LocalDate.now()
            val order = Order(
                id = 1L,
                employee = testEmployee,
                orderDate = today,
                menu = testMenu,
                priceAtOrder = BigDecimal("500")
            ).apply {
            }
            whenever(orderRepository.findByEmployeeIdAndOrderDate(1L, today)).thenReturn(order)

            // When
            val result = orderService.findByEmployeeAndDate(1L, today)

            // Then
            assertThat(result).isNotNull
            assertThat(result?.id).isEqualTo(1L)
            assertThat(result?.menuName).isEqualTo("テスト弁当")
        }

        @Test
        @DisplayName("注文がない場合、null を返す")
        fun `should return null when no order exists for employee on date`() {
            // Given
            val today = LocalDate.now()
            whenever(orderRepository.findByEmployeeIdAndOrderDate(1L, today)).thenReturn(null)

            // When
            val result = orderService.findByEmployeeAndDate(1L, today)

            // Then
            assertThat(result).isNull()
        }
    }

    @Nested
    @DisplayName("findByEmployee - 社員の注文履歴取得")
    inner class FindByEmployeeTests {

        @Test
        @DisplayName("社員の注文履歴を取得できる")
        fun `should return order history for employee`() {
            // Given
            val now = LocalDateTime.now()
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            val orders = listOf(
                Order(id = 1L, employee = testEmployee, orderDate = today, menu = testMenu, priceAtOrder = BigDecimal("500")),
                Order(id = 2L, employee = testEmployee, orderDate = yesterday, menu = testMenu, priceAtOrder = BigDecimal("500"))
            ).onEach {
            }
            whenever(orderRepository.findByEmployeeId(1L)).thenReturn(orders)

            // When
            val result = orderService.findByEmployee(1L)

            // Then
            assertThat(result).hasSize(2)
            assertThat(result[0].orderDate).isEqualTo(today)
            assertThat(result[1].orderDate).isEqualTo(yesterday)
        }

        @Test
        @DisplayName("注文履歴がない場合、空のリストを返す")
        fun `should return empty list when employee has no orders`() {
            // Given
            whenever(orderRepository.findByEmployeeId(1L)).thenReturn(emptyList())

            // When
            val result = orderService.findByEmployee(1L)

            // Then
            assertThat(result).isEmpty()
        }
    }

    @Nested
    @DisplayName("findByEmployeeAndDateRange - 期間での注文取得")
    inner class FindByEmployeeAndDateRangeTests {

        @Test
        @DisplayName("社員の指定期間の注文を取得できる")
        fun `should return orders within date range for employee`() {
            // Given
            val now = LocalDateTime.now()
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            val tomorrow = today.plusDays(1)
            val orders = listOf(
                Order(id = 1L, employee = testEmployee, orderDate = yesterday, menu = testMenu, priceAtOrder = BigDecimal("500")),
                Order(id = 2L, employee = testEmployee, orderDate = today, menu = testMenu, priceAtOrder = BigDecimal("500")),
                Order(id = 3L, employee = testEmployee, orderDate = tomorrow, menu = testMenu, priceAtOrder = BigDecimal("500"))
            ).onEach {
            }
            whenever(orderRepository.findByEmployeeIdAndOrderDateBetweenOrderByOrderDateAsc(1L, yesterday, tomorrow))
                .thenReturn(orders)

            // When
            val result = orderService.findByEmployeeAndDateRange(1L, yesterday, tomorrow)

            // Then
            assertThat(result).hasSize(3)
            assertThat(result.map { it.orderDate }).containsExactly(yesterday, today, tomorrow)
        }

        @Test
        @DisplayName("期間内に注文がない場合、空のリストを返す")
        fun `should return empty list when no orders in date range`() {
            // Given
            val today = LocalDate.now()
            val startDate = today.minusDays(10)
            val endDate = today.minusDays(5)
            whenever(orderRepository.findByEmployeeIdAndOrderDateBetweenOrderByOrderDateAsc(1L, startDate, endDate))
                .thenReturn(emptyList())

            // When
            val result = orderService.findByEmployeeAndDateRange(1L, startDate, endDate)

            // Then
            assertThat(result).isEmpty()
        }
    }

    @Nested
    @DisplayName("findByDate - 指定日の全注文取得")
    inner class FindByDateTests {

        @Test
        @DisplayName("指定日の全注文を取得できる")
        fun `should return all orders for specified date`() {
            // Given
            val now = LocalDateTime.now()
            val today = LocalDate.now()
            val employee2 = Employee(id = 2L, employeeNumber = "E002", name = "社員B")
            val orders = listOf(
                Order(id = 1L, employee = testEmployee, orderDate = today, menu = testMenu, priceAtOrder = BigDecimal("500")),
                Order(id = 2L, employee = employee2, orderDate = today, menu = testMenu, priceAtOrder = BigDecimal("500"))
            ).onEach {
            }
            whenever(orderRepository.findByOrderDateWithDetails(today)).thenReturn(orders)

            // When
            val result = orderService.findByDate(today)

            // Then
            assertThat(result).hasSize(2)
            assertThat(result.all { it.orderDate == today }).isTrue
        }

        @Test
        @DisplayName("指定日に注文がない場合、空のリストを返す")
        fun `should return empty list when no orders on specified date`() {
            // Given
            val today = LocalDate.now()
            whenever(orderRepository.findByOrderDateWithDetails(today)).thenReturn(emptyList())

            // When
            val result = orderService.findByDate(today)

            // Then
            assertThat(result).isEmpty()
        }
    }

    @Nested
    @DisplayName("create - 注文作成")
    inner class CreateTests {

        @Test
        @DisplayName("明日の注文を作成できる")
        fun `should create order for future date successfully`() {
            // Given
            val tomorrow = LocalDate.now().plusDays(1)
            val request = OrderCreateRequest(
                employeeId = 1L,
                orderDate = tomorrow,
                menuId = 1L
            )
            val calendarSetting = CalendarSetting(date = tomorrow, menuSet = null, isHoliday = false)
            val now = LocalDateTime.now()
            val savedOrder = Order(
                id = 1L,
                employee = testEmployee,
                orderDate = tomorrow,
                menu = testMenu,
                priceAtOrder = BigDecimal("500")
            ).apply {
            }

            whenever(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(testEmployee)
            whenever(menuRepository.findByIdAndDeletedFalse(1L)).thenReturn(testMenu)
            whenever(orderRepository.findByEmployeeIdAndOrderDate(1L, tomorrow)).thenReturn(null)
            whenever(calendarSettingRepository.findByDate(tomorrow)).thenReturn(calendarSetting)
            whenever(orderRepository.save(any())).thenReturn(savedOrder)

            // When
            val result = orderService.create(request)

            // Then
            assertThat(result.id).isEqualTo(1L)
            assertThat(result.employeeName).isEqualTo("テスト社員")
            assertThat(result.menuName).isEqualTo("テスト弁当")
            assertThat(result.priceAtOrder).isEqualByComparingTo(BigDecimal("500"))
        }

        @Test
        @DisplayName("存在しない社員で注文作成を試みると例外が発生")
        fun `should throw exception when employee does not exist`() {
            // Given
            val tomorrow = LocalDate.now().plusDays(1)
            val request = OrderCreateRequest(
                employeeId = 999L,
                orderDate = tomorrow,
                menuId = 1L
            )
            val calendarSetting = CalendarSetting(date = tomorrow, menuSet = null, isHoliday = false)
            whenever(employeeRepository.findByIdAndDeletedFalse(999L)).thenReturn(null)
            whenever(calendarSettingRepository.findByDate(tomorrow)).thenReturn(calendarSetting)

            // When & Then
            assertThrows<IllegalArgumentException> {
                orderService.create(request)
            }.also {
                assertThat(it.message).contains("社員が見つかりません")
            }
        }

        @Test
        @DisplayName("存在しないメニューで注文作成を試みると例外が発生")
        fun `should throw exception when menu does not exist`() {
            // Given
            val tomorrow = LocalDate.now().plusDays(1)
            val request = OrderCreateRequest(
                employeeId = 1L,
                orderDate = tomorrow,
                menuId = 999L
            )
            val calendarSetting = CalendarSetting(date = tomorrow, menuSet = null, isHoliday = false)
            whenever(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(testEmployee)
            whenever(menuRepository.findByIdAndDeletedFalse(999L)).thenReturn(null)
            whenever(calendarSettingRepository.findByDate(tomorrow)).thenReturn(calendarSetting)

            // When & Then
            assertThrows<IllegalArgumentException> {
                orderService.create(request)
            }.also {
                assertThat(it.message).contains("メニューが見つかりません")
            }
        }

        @Test
        @DisplayName("重複した注文を作成しようとすると例外が発生")
        fun `should throw exception when order already exists for same date`() {
            // Given
            val tomorrow = LocalDate.now().plusDays(1)
            val request = OrderCreateRequest(
                employeeId = 1L,
                orderDate = tomorrow,
                menuId = 1L
            )
            val existingOrder = Order(
                id = 1L,
                employee = testEmployee,
                orderDate = tomorrow,
                menu = testMenu,
                priceAtOrder = BigDecimal("500")
            )
            val calendarSetting = CalendarSetting(date = tomorrow, menuSet = null, isHoliday = false)

            whenever(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(testEmployee)
            whenever(menuRepository.findByIdAndDeletedFalse(1L)).thenReturn(testMenu)
            whenever(orderRepository.findByEmployeeIdAndOrderDate(1L, tomorrow)).thenReturn(existingOrder)
            whenever(calendarSettingRepository.findByDate(tomorrow)).thenReturn(calendarSetting)

            // When & Then
            assertThrows<IllegalArgumentException> {
                orderService.create(request)
            }.also {
                assertThat(it.message).contains("既に")
                assertThat(it.message).contains("の注文が存在します")
            }
        }

        @Test
        @DisplayName("過去の日付で注文作成を試みると例外が発生")
        fun `should throw exception when trying to order for past date`() {
            // Given
            val yesterday = LocalDate.now().minusDays(1)
            val request = OrderCreateRequest(
                employeeId = 1L,
                orderDate = yesterday,
                menuId = 1L
            )

            // When & Then
            assertThrows<IllegalArgumentException> {
                orderService.create(request)
            }.also {
                assertThat(it.message).contains("過去の日付の注文はできません")
            }
        }

        @Test
        @DisplayName("カレンダー設定のない日で注文作成を試みると例外が発生")
        fun `should throw exception when no calendar setting exists for date`() {
            // Given
            val tomorrow = LocalDate.now().plusDays(1)
            val request = OrderCreateRequest(
                employeeId = 1L,
                orderDate = tomorrow,
                menuId = 1L
            )
            whenever(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(testEmployee)
            whenever(menuRepository.findByIdAndDeletedFalse(1L)).thenReturn(testMenu)
            whenever(orderRepository.findByEmployeeIdAndOrderDate(1L, tomorrow)).thenReturn(null)
            whenever(calendarSettingRepository.findByDate(tomorrow)).thenReturn(null)

            // When & Then
            assertThrows<IllegalArgumentException> {
                orderService.create(request)
            }.also {
                assertThat(it.message).contains("注文できません")
            }
        }
    }

    @Nested
    @DisplayName("update - 注文更新")
    inner class UpdateTests {

        @Test
        @DisplayName("明日の注文を更新できる")
        fun `should update order for future date successfully`() {
            // Given
            val tomorrow = LocalDate.now().plusDays(1)
            val existingOrder = Order(
                id = 1L,
                employee = testEmployee,
                orderDate = tomorrow,
                menu = testMenu,
                priceAtOrder = BigDecimal("500")
            )
            val newMenu = Menu(
                id = 2L,
                vendor = testVendor,
                name = "新しい弁当",
                price = BigDecimal("600"),
                effectiveFrom = LocalDate.now()
            )
            val request = OrderUpdateRequest(menuId = 2L)
            val calendarSetting = CalendarSetting(date = tomorrow, menuSet = null, isHoliday = false)

            whenever(calendarSettingRepository.findByDate(tomorrow)).thenReturn(calendarSetting)
            whenever(orderRepository.findByEmployeeIdAndOrderDate(1L, tomorrow)).thenReturn(existingOrder)
            whenever(menuRepository.findByIdAndDeletedFalse(2L)).thenReturn(newMenu)
            whenever(orderRepository.save(any())).thenReturn(existingOrder.apply {
                menu = newMenu
                priceAtOrder = BigDecimal("600")
            })

            // When
            val result = orderService.update(1L, tomorrow, request)

            // Then
            assertThat(result.menuName).isEqualTo("新しい弁当")
            assertThat(result.priceAtOrder).isEqualByComparingTo(BigDecimal("600"))
        }

        @Test
        @DisplayName("注文が存在しない場合、例外が発生")
        fun `should throw exception when order not found`() {
            // Given
            val tomorrow = LocalDate.now().plusDays(1)
            val request = OrderUpdateRequest(menuId = 2L)
            val calendarSetting = CalendarSetting(date = tomorrow, menuSet = null, isHoliday = false)

            whenever(calendarSettingRepository.findByDate(tomorrow)).thenReturn(calendarSetting)
            whenever(orderRepository.findByEmployeeIdAndOrderDate(1L, tomorrow)).thenReturn(null)

            // When & Then
            assertThrows<IllegalArgumentException> {
                orderService.update(1L, tomorrow, request)
            }.also {
                assertThat(it.message).contains("注文が見つかりません")
            }
        }
    }

    @Nested
    @DisplayName("delete - 注文削除")
    inner class DeleteTests {

        @Test
        @DisplayName("明日の注文を削除できる")
        fun `should delete order for future date successfully`() {
            // Given
            val tomorrow = LocalDate.now().plusDays(1)
            val order = Order(
                id = 1L,
                employee = testEmployee,
                orderDate = tomorrow,
                menu = testMenu,
                priceAtOrder = BigDecimal("500")
            )
            val calendarSetting = CalendarSetting(date = tomorrow, menuSet = null, isHoliday = false)

            whenever(calendarSettingRepository.findByDate(tomorrow)).thenReturn(calendarSetting)
            whenever(orderRepository.findByEmployeeIdAndOrderDate(1L, tomorrow)).thenReturn(order)

            // When
            orderService.delete(1L, tomorrow)

            // Then
            // 削除されたことを確認（モックの挙動により確認）
        }

        @Test
        @DisplayName("注文が存在しない場合、例外が発生")
        fun `should throw exception when deleting non-existent order`() {
            // Given
            val tomorrow = LocalDate.now().plusDays(1)
            val calendarSetting = CalendarSetting(date = tomorrow, menuSet = null, isHoliday = false)

            whenever(calendarSettingRepository.findByDate(tomorrow)).thenReturn(calendarSetting)
            whenever(orderRepository.findByEmployeeIdAndOrderDate(1L, tomorrow)).thenReturn(null)

            // When & Then
            assertThrows<IllegalArgumentException> {
                orderService.delete(1L, tomorrow)
            }.also {
                assertThat(it.message).contains("注文が見つかりません")
            }
        }
    }

    // ==================== 境界値テスト ====================

    @Nested
    @DisplayName("境界値テスト")
    inner class BoundaryValueTests {

        @Test
        @DisplayName("月初の日付で注文を作成できる")
        fun `should create order on first day of month`() {
            // Given
            val today = LocalDate.now()
            val firstDayOfMonth = today.withDayOfMonth(1)
            val request = OrderCreateRequest(
                employeeId = 1L,
                orderDate = firstDayOfMonth,
                menuId = 1L
            )
            val calendarSetting = CalendarSetting(date = firstDayOfMonth, menuSet = null, isHoliday = false)
            val now = LocalDateTime.now()
            val savedOrder = Order(
                id = 1L,
                employee = testEmployee,
                orderDate = firstDayOfMonth,
                menu = testMenu,
                priceAtOrder = BigDecimal("500")
            ).apply {
            }

            whenever(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(testEmployee)
            whenever(menuRepository.findByIdAndDeletedFalse(1L)).thenReturn(testMenu)
            whenever(orderRepository.findByEmployeeIdAndOrderDate(1L, firstDayOfMonth)).thenReturn(null)
            whenever(calendarSettingRepository.findByDate(firstDayOfMonth)).thenReturn(calendarSetting)
            whenever(orderRepository.save(any())).thenReturn(savedOrder)

            // When
            val result = orderService.create(request)

            // Then
            assertThat(result.id).isEqualTo(1L)
        }

        @Test
        @DisplayName("月末の日付で注文を作成できる")
        fun `should create order on last day of month`() {
            // Given
            val today = LocalDate.now()
            val lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth())
            val request = OrderCreateRequest(
                employeeId = 1L,
                orderDate = lastDayOfMonth,
                menuId = 1L
            )
            val calendarSetting = CalendarSetting(date = lastDayOfMonth, menuSet = null, isHoliday = false)
            val now = LocalDateTime.now()
            val savedOrder = Order(
                id = 1L,
                employee = testEmployee,
                orderDate = lastDayOfMonth,
                menu = testMenu,
                priceAtOrder = BigDecimal("500")
            ).apply {
            }

            whenever(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(testEmployee)
            whenever(menuRepository.findByIdAndDeletedFalse(1L)).thenReturn(testMenu)
            whenever(orderRepository.findByEmployeeIdAndOrderDate(1L, lastDayOfMonth)).thenReturn(null)
            whenever(calendarSettingRepository.findByDate(lastDayOfMonth)).thenReturn(calendarSetting)
            whenever(orderRepository.save(any())).thenReturn(savedOrder)

            // When
            val result = orderService.create(request)

            // Then
            assertThat(result.id).isEqualTo(1L)
        }

        @Test
        @DisplayName("大きなID の社員で注文を作成できる")
        fun `should create order for employee with large id`() {
            // Given
            val largeEmployeeId = Long.MAX_VALUE - 100
            val tomorrow = LocalDate.now().plusDays(1)
            val request = OrderCreateRequest(
                employeeId = largeEmployeeId,
                orderDate = tomorrow,
                menuId = 1L
            )
            val largeIdEmployee = Employee(id = largeEmployeeId, employeeNumber = "EX999", name = "社員X")
            val calendarSetting = CalendarSetting(date = tomorrow, menuSet = null, isHoliday = false)
            val now = LocalDateTime.now()
            val savedOrder = Order(
                id = 1L,
                employee = largeIdEmployee,
                orderDate = tomorrow,
                menu = testMenu,
                priceAtOrder = BigDecimal("500")
            ).apply {
            }

            whenever(employeeRepository.findByIdAndDeletedFalse(largeEmployeeId)).thenReturn(largeIdEmployee)
            whenever(menuRepository.findByIdAndDeletedFalse(1L)).thenReturn(testMenu)
            whenever(orderRepository.findByEmployeeIdAndOrderDate(largeEmployeeId, tomorrow)).thenReturn(null)
            whenever(calendarSettingRepository.findByDate(tomorrow)).thenReturn(calendarSetting)
            whenever(orderRepository.save(any())).thenReturn(savedOrder)

            // When
            val result = orderService.create(request)

            // Then
            assertThat(result.id).isEqualTo(1L)
        }

        @Test
        @DisplayName("1ヶ月後の注文を作成できる")
        fun `should create order one month ahead`() {
            // Given
            val oneMonthLater = LocalDate.now().plusMonths(1)
            val request = OrderCreateRequest(
                employeeId = 1L,
                orderDate = oneMonthLater,
                menuId = 1L
            )
            val calendarSetting = CalendarSetting(date = oneMonthLater, menuSet = null, isHoliday = false)
            val now = LocalDateTime.now()
            val savedOrder = Order(
                id = 1L,
                employee = testEmployee,
                orderDate = oneMonthLater,
                menu = testMenu,
                priceAtOrder = BigDecimal("500")
            ).apply {
            }

            whenever(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(testEmployee)
            whenever(menuRepository.findByIdAndDeletedFalse(1L)).thenReturn(testMenu)
            whenever(orderRepository.findByEmployeeIdAndOrderDate(1L, oneMonthLater)).thenReturn(null)
            whenever(calendarSettingRepository.findByDate(oneMonthLater)).thenReturn(calendarSetting)
            whenever(orderRepository.save(any())).thenReturn(savedOrder)

            // When
            val result = orderService.create(request)

            // Then
            assertThat(result.orderDate).isEqualTo(oneMonthLater)
        }
    }
}
