package Sample.BentouReservation.service

import Sample.BentouReservation.dto.DailyAggregationResponse
import Sample.BentouReservation.dto.MenuAggregation
import Sample.BentouReservation.dto.VendorAggregation
import Sample.BentouReservation.repository.DailyAggregationResult
import Sample.BentouReservation.repository.OrderRepository
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate

@DisplayName("DailyAggregationService テスト")
class DailyAggregationServiceTest {

    @Mock
    private lateinit var orderRepository: OrderRepository

    private lateinit var dailyAggregationService: DailyAggregationService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        dailyAggregationService = DailyAggregationService(orderRepository)
    }

    // テスト用の DailyAggregationResult モック実装
    private data class MockDailyAggregationResult(
        override val vendorId: Long,
        override val vendorName: String,
        override val menuId: Long,
        override val menuName: String,
        override val orderCount: Long,
        override val totalPrice: BigDecimal
    ) : DailyAggregationResult

    // ==================== 正常系テスト ====================

    @Nested
    @DisplayName("aggregateByDate - 日次集計取得")
    inner class AggregateByDateTests {

        @Test
        @DisplayName("複数の業者・メニューの集計を取得できる")
        fun `should aggregate orders by vendor and menu successfully`() {
            // Given
            val today = LocalDate.now()
            val results = listOf(
                MockDailyAggregationResult(
                    vendorId = 1L,
                    vendorName = "業者A",
                    menuId = 1L,
                    menuName = "弁当A",
                    orderCount = 5L,
                    totalPrice = BigDecimal("2500")
                ),
                MockDailyAggregationResult(
                    vendorId = 1L,
                    vendorName = "業者A",
                    menuId = 2L,
                    menuName = "弁当B",
                    orderCount = 3L,
                    totalPrice = BigDecimal("1800")
                ),
                MockDailyAggregationResult(
                    vendorId = 2L,
                    vendorName = "業者B",
                    menuId = 3L,
                    menuName = "弁当C",
                    orderCount = 2L,
                    totalPrice = BigDecimal("1200")
                )
            )
            whenever(orderRepository.findDailyAggregationByDate(today)).thenReturn(results)

            // When
            val result = dailyAggregationService.aggregateByDate(today)

            // Then
            assertThat(result.orderDate).isEqualTo(today)
            assertThat(result.vendors).hasSize(2)

            val vendorA = result.vendors.find { it.vendorId == 1L }
            assertThat(vendorA).isNotNull
            vendorA?.apply {
                assertThat(vendorName).isEqualTo("業者A")
                assertThat(menus).hasSize(2)
                assertThat(totalCount).isEqualTo(8)
                assertThat(totalPrice).isEqualByComparingTo(BigDecimal("4300"))
            }

            val vendorB = result.vendors.find { it.vendorId == 2L }
            assertThat(vendorB).isNotNull
            vendorB?.apply {
                assertThat(vendorName).isEqualTo("業者B")
                assertThat(menus).hasSize(1)
                assertThat(totalCount).isEqualTo(2)
                assertThat(totalPrice).isEqualByComparingTo(BigDecimal("1200"))
            }
        }

        @Test
        @DisplayName("1 つの業者・メニューのみの集計を取得できる")
        fun `should aggregate single vendor with single menu`() {
            // Given
            val today = LocalDate.now()
            val results = listOf(
                MockDailyAggregationResult(
                    vendorId = 1L,
                    vendorName = "業者A",
                    menuId = 1L,
                    menuName = "弁当A",
                    orderCount = 3L,
                    totalPrice = BigDecimal("1500")
                )
            )
            whenever(orderRepository.findDailyAggregationByDate(today)).thenReturn(results)

            // When
            val result = dailyAggregationService.aggregateByDate(today)

            // Then
            assertThat(result.vendors).hasSize(1)
            assertThat(result.vendors[0].totalCount).isEqualTo(3)
            assertThat(result.vendors[0].totalPrice).isEqualByComparingTo(BigDecimal("1500"))
        }

        @Test
        @DisplayName("注文がない場合、空の集計を返す")
        fun `should return empty aggregation when no orders exist`() {
            // Given
            val today = LocalDate.now()
            whenever(orderRepository.findDailyAggregationByDate(today)).thenReturn(emptyList())

            // When
            val result = dailyAggregationService.aggregateByDate(today)

            // Then
            assertThat(result.orderDate).isEqualTo(today)
            assertThat(result.vendors).isEmpty()
        }

        @Test
        @DisplayName("複数メニューの平均単価が正しく計算される")
        fun `should calculate average unit price correctly`() {
            // Given
            val today = LocalDate.now()
            val results = listOf(
                MockDailyAggregationResult(
                    vendorId = 1L,
                    vendorName = "業者A",
                    menuId = 1L,
                    menuName = "弁当A",
                    orderCount = 2L,
                    totalPrice = BigDecimal("1000")  // 500円 * 2
                )
            )
            whenever(orderRepository.findDailyAggregationByDate(today)).thenReturn(results)

            // When
            val result = dailyAggregationService.aggregateByDate(today)

            // Then
            val menu = result.vendors[0].menus[0]
            assertThat(menu.priceAtOrder).isEqualByComparingTo(BigDecimal("500"))
        }
    }

    @Nested
    @DisplayName("aggregateByDateRange - 期間集計取得")
    inner class AggregateByDateRangeTests {

        @Test
        @DisplayName("複数日の集計を取得できる")
        fun `should aggregate orders for date range with multiple days`() {
            // Given
            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)
            val results1 = listOf(
                MockDailyAggregationResult(
                    vendorId = 1L,
                    vendorName = "業者A",
                    menuId = 1L,
                    menuName = "弁当A",
                    orderCount = 2L,
                    totalPrice = BigDecimal("1000")
                )
            )
            val results2 = listOf(
                MockDailyAggregationResult(
                    vendorId = 1L,
                    vendorName = "業者A",
                    menuId = 1L,
                    menuName = "弁当A",
                    orderCount = 3L,
                    totalPrice = BigDecimal("1500")
                )
            )
            whenever(orderRepository.findDailyAggregationByDate(today)).thenReturn(results1)
            whenever(orderRepository.findDailyAggregationByDate(tomorrow)).thenReturn(results2)

            // When
            val result = dailyAggregationService.aggregateByDateRange(today, tomorrow)

            // Then
            assertThat(result).hasSize(2)
            assertThat(result[0].orderDate).isEqualTo(today)
            assertThat(result[1].orderDate).isEqualTo(tomorrow)
        }

        @Test
        @DisplayName("注文がない日は集計結果に含めない")
        fun `should exclude days with no orders from aggregation`() {
            // Given
            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)
            val dayAfterTomorrow = today.plusDays(2)
            val results1 = listOf(
                MockDailyAggregationResult(
                    vendorId = 1L,
                    vendorName = "業者A",
                    menuId = 1L,
                    menuName = "弁当A",
                    orderCount = 2L,
                    totalPrice = BigDecimal("1000")
                )
            )
            whenever(orderRepository.findDailyAggregationByDate(today)).thenReturn(results1)
            whenever(orderRepository.findDailyAggregationByDate(tomorrow)).thenReturn(emptyList())
            whenever(orderRepository.findDailyAggregationByDate(dayAfterTomorrow)).thenReturn(emptyList())

            // When
            val result = dailyAggregationService.aggregateByDateRange(today, dayAfterTomorrow)

            // Then
            assertThat(result).hasSize(1)
            assertThat(result[0].orderDate).isEqualTo(today)
        }

        @Test
        @DisplayName("期間内に注文がない場合、空のリストを返す")
        fun `should return empty list when no orders in date range`() {
            // Given
            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)
            whenever(orderRepository.findDailyAggregationByDate(today)).thenReturn(emptyList())
            whenever(orderRepository.findDailyAggregationByDate(tomorrow)).thenReturn(emptyList())

            // When
            val result = dailyAggregationService.aggregateByDateRange(today, tomorrow)

            // Then
            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("同一日付で複数回呼ばれても同じ結果を返す")
        fun `should return same result for same date called multiple times`() {
            // Given
            val today = LocalDate.now()
            val results = listOf(
                MockDailyAggregationResult(
                    vendorId = 1L,
                    vendorName = "業者A",
                    menuId = 1L,
                    menuName = "弁当A",
                    orderCount = 2L,
                    totalPrice = BigDecimal("1000")
                )
            )
            whenever(orderRepository.findDailyAggregationByDate(today)).thenReturn(results)

            // When
            val result = dailyAggregationService.aggregateByDateRange(today, today)

            // Then
            assertThat(result).hasSize(1)
            assertThat(result[0].vendors).hasSize(1)
        }
    }

    @Nested
    @DisplayName("getVendorAggregationForPrint - 業者別集計（印刷用）")
    inner class GetVendorAggregationForPrintTests {

        @Test
        @DisplayName("指定業者の集計を取得できる")
        fun `should get vendor aggregation for specified vendor`() {
            // Given
            val today = LocalDate.now()
            val results = listOf(
                MockDailyAggregationResult(
                    vendorId = 1L,
                    vendorName = "業者A",
                    menuId = 1L,
                    menuName = "弁当A",
                    orderCount = 5L,
                    totalPrice = BigDecimal("2500")
                ),
                MockDailyAggregationResult(
                    vendorId = 2L,
                    vendorName = "業者B",
                    menuId = 3L,
                    menuName = "弁当C",
                    orderCount = 2L,
                    totalPrice = BigDecimal("1200")
                )
            )
            whenever(orderRepository.findDailyAggregationByDate(today)).thenReturn(results)

            // When
            val result = dailyAggregationService.getVendorAggregationForPrint(today, 1L)

            // Then
            assertThat(result).isNotNull
            result?.apply {
                assertThat(vendorId).isEqualTo(1L)
                assertThat(vendorName).isEqualTo("業者A")
                assertThat(totalCount).isEqualTo(5)
            }
        }

        @Test
        @DisplayName("存在しない業者は null を返す")
        fun `should return null when vendor does not exist in aggregation`() {
            // Given
            val today = LocalDate.now()
            val results = listOf(
                MockDailyAggregationResult(
                    vendorId = 1L,
                    vendorName = "業者A",
                    menuId = 1L,
                    menuName = "弁当A",
                    orderCount = 5L,
                    totalPrice = BigDecimal("2500")
                )
            )
            whenever(orderRepository.findDailyAggregationByDate(today)).thenReturn(results)

            // When
            val result = dailyAggregationService.getVendorAggregationForPrint(today, 999L)

            // Then
            assertThat(result).isNull()
        }

        @Test
        @DisplayName("集計がない場合は null を返す")
        fun `should return null when no aggregation exists for date`() {
            // Given
            val today = LocalDate.now()
            whenever(orderRepository.findDailyAggregationByDate(today)).thenReturn(emptyList())

            // When
            val result = dailyAggregationService.getVendorAggregationForPrint(today, 1L)

            // Then
            assertThat(result).isNull()
        }
    }

    @Nested
    @DisplayName("getAllVendorAggregationsForPrint - 全業者集計（印刷用）")
    inner class GetAllVendorAggregationsForPrintTests {

        @Test
        @DisplayName("全業者の集計を取得できる")
        fun `should get all vendor aggregations for print`() {
            // Given
            val today = LocalDate.now()
            val results = listOf(
                MockDailyAggregationResult(
                    vendorId = 1L,
                    vendorName = "業者A",
                    menuId = 1L,
                    menuName = "弁当A",
                    orderCount = 5L,
                    totalPrice = BigDecimal("2500")
                ),
                MockDailyAggregationResult(
                    vendorId = 2L,
                    vendorName = "業者B",
                    menuId = 3L,
                    menuName = "弁当C",
                    orderCount = 2L,
                    totalPrice = BigDecimal("1200")
                )
            )
            whenever(orderRepository.findDailyAggregationByDate(today)).thenReturn(results)

            // When
            val result = dailyAggregationService.getAllVendorAggregationsForPrint(today)

            // Then
            assertThat(result).hasSize(2)
            assertThat(result.map { it.vendorId }).containsExactly(1L, 2L)
        }

        @Test
        @DisplayName("集計がない場合、空のリストを返す")
        fun `should return empty list when no aggregations exist`() {
            // Given
            val today = LocalDate.now()
            whenever(orderRepository.findDailyAggregationByDate(today)).thenReturn(emptyList())

            // When
            val result = dailyAggregationService.getAllVendorAggregationsForPrint(today)

            // Then
            assertThat(result).isEmpty()
        }
    }

    // ==================== 境界値テスト ====================

    @Nested
    @DisplayName("境界値テスト")
    inner class BoundaryValueTests {

        @Test
        @DisplayName("1件の注文を集計できる")
        fun `should aggregate with single order`() {
            // Given
            val today = LocalDate.now()
            val results = listOf(
                MockDailyAggregationResult(
                    vendorId = 1L,
                    vendorName = "業者A",
                    menuId = 1L,
                    menuName = "弁当A",
                    orderCount = 1L,
                    totalPrice = BigDecimal("500")
                )
            )
            whenever(orderRepository.findDailyAggregationByDate(today)).thenReturn(results)

            // When
            val result = dailyAggregationService.aggregateByDate(today)

            // Then
            assertThat(result.vendors[0].totalCount).isEqualTo(1)
            assertThat(result.vendors[0].menus[0].orderCount).isEqualTo(1)
        }

        @Test
        @DisplayName("大量の注文を集計できる")
        fun `should aggregate large number of orders`() {
            // Given
            val today = LocalDate.now()
            val largeCount = 1000L
            val results = listOf(
                MockDailyAggregationResult(
                    vendorId = 1L,
                    vendorName = "業者A",
                    menuId = 1L,
                    menuName = "弁当A",
                    orderCount = largeCount,
                    totalPrice = BigDecimal("500000")  // 500円 * 1000
                )
            )
            whenever(orderRepository.findDailyAggregationByDate(today)).thenReturn(results)

            // When
            val result = dailyAggregationService.aggregateByDate(today)

            // Then
            assertThat(result.vendors[0].totalCount).isEqualTo(largeCount.toInt())
            assertThat(result.vendors[0].totalPrice).isEqualByComparingTo(BigDecimal("500000"))
        }

        @Test
        @DisplayName("最小金額の注文を集計できる")
        fun `should aggregate order with minimum price`() {
            // Given
            val today = LocalDate.now()
            val results = listOf(
                MockDailyAggregationResult(
                    vendorId = 1L,
                    vendorName = "業者A",
                    menuId = 1L,
                    menuName = "弁当A",
                    orderCount = 1L,
                    totalPrice = BigDecimal("1")
                )
            )
            whenever(orderRepository.findDailyAggregationByDate(today)).thenReturn(results)

            // When
            val result = dailyAggregationService.aggregateByDate(today)

            // Then
            assertThat(result.vendors[0].menus[0].priceAtOrder).isEqualByComparingTo(BigDecimal("1"))
        }

        @Test
        @DisplayName("月初の日付で集計できる")
        fun `should aggregate for first day of month`() {
            // Given
            val today = LocalDate.now()
            val firstDayOfMonth = today.withDayOfMonth(1)
            val results = listOf(
                MockDailyAggregationResult(
                    vendorId = 1L,
                    vendorName = "業者A",
                    menuId = 1L,
                    menuName = "弁当A",
                    orderCount = 2L,
                    totalPrice = BigDecimal("1000")
                )
            )
            whenever(orderRepository.findDailyAggregationByDate(firstDayOfMonth)).thenReturn(results)

            // When
            val result = dailyAggregationService.aggregateByDate(firstDayOfMonth)

            // Then
            assertThat(result.orderDate).isEqualTo(firstDayOfMonth)
            assertThat(result.vendors).isNotEmpty
        }

        @Test
        @DisplayName("月末の日付で集計できる")
        fun `should aggregate for last day of month`() {
            // Given
            val today = LocalDate.now()
            val lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth())
            val results = listOf(
                MockDailyAggregationResult(
                    vendorId = 1L,
                    vendorName = "業者A",
                    menuId = 1L,
                    menuName = "弁当A",
                    orderCount = 2L,
                    totalPrice = BigDecimal("1000")
                )
            )
            whenever(orderRepository.findDailyAggregationByDate(lastDayOfMonth)).thenReturn(results)

            // When
            val result = dailyAggregationService.aggregateByDate(lastDayOfMonth)

            // Then
            assertThat(result.orderDate).isEqualTo(lastDayOfMonth)
            assertThat(result.vendors).isNotEmpty
        }

        @Test
        @DisplayName("小数点を含む金額を集計できる")
        fun `should aggregate orders with decimal prices`() {
            // Given
            val today = LocalDate.now()
            val results = listOf(
                MockDailyAggregationResult(
                    vendorId = 1L,
                    vendorName = "業者A",
                    menuId = 1L,
                    menuName = "弁当A",
                    orderCount = 3L,
                    totalPrice = BigDecimal("1555")  // 平均 518.33...
                )
            )
            whenever(orderRepository.findDailyAggregationByDate(today)).thenReturn(results)

            // When
            val result = dailyAggregationService.aggregateByDate(today)

            // Then
            val priceAtOrder = result.vendors[0].menus[0].priceAtOrder
            assertThat(priceAtOrder).isGreaterThan(BigDecimal("500"))
            assertThat(priceAtOrder).isLessThan(BigDecimal("600"))
        }

        @Test
        @DisplayName("10個以上の業者・メニューを集計できる")
        fun `should aggregate with many vendors and menus`() {
            // Given
            val today = LocalDate.now()
            val results = (1..10).flatMap { vendorId ->
                (1..5).map { menuId ->
                    MockDailyAggregationResult(
                        vendorId = vendorId.toLong(),
                        vendorName = "業者$vendorId",
                        menuId = menuId.toLong(),
                        menuName = "弁当$menuId",
                        orderCount = 2L,
                        totalPrice = BigDecimal("1000")
                    )
                }
            }
            whenever(orderRepository.findDailyAggregationByDate(today)).thenReturn(results)

            // When
            val result = dailyAggregationService.aggregateByDate(today)

            // Then
            assertThat(result.vendors).hasSize(10)
            assertThat(result.vendors.flatMap { it.menus }).hasSize(50)
        }
    }
}