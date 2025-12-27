package Sample.BentouReservation.service

import Sample.BentouReservation.dto.MenuCreateRequest
import Sample.BentouReservation.dto.MenuPriceChangeRequest
import Sample.BentouReservation.dto.MenuUpdateRequest
import Sample.BentouReservation.entity.Menu
import Sample.BentouReservation.entity.Vendor
import Sample.BentouReservation.repository.MenuRepository
import Sample.BentouReservation.repository.VendorRepository
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

@DisplayName("MenuService テスト")
class MenuServiceTest {

    @Mock
    private lateinit var menuRepository: MenuRepository

    @Mock
    private lateinit var vendorRepository: VendorRepository

    private lateinit var menuService: MenuService

    private lateinit var testVendor: Vendor

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        menuService = MenuService(menuRepository, vendorRepository)

        val now = LocalDateTime.now()
        testVendor = Vendor(id = 1L, name = "テスト業者", contactInfo = "123-4567").apply {
            deleted = false
        }
    }

    // ==================== 正常系テスト ====================

    @Nested
    @DisplayName("findAll - 全メニュー取得")
    inner class FindAllTests {

        @Test
        @DisplayName("複数のメニューが存在する場合、全て取得できる")
        fun `should return all active menus when multiple menus exist`() {
            // Given
            val now = LocalDateTime.now()
            val menus = listOf(
                Menu(id = 1L, vendor = testVendor, name = "弁当A", price = BigDecimal("500"), effectiveFrom = LocalDate.now()),
                Menu(id = 2L, vendor = testVendor, name = "弁当B", price = BigDecimal("600"), effectiveFrom = LocalDate.now()),
                Menu(id = 3L, vendor = testVendor, name = "弁当C", price = BigDecimal("700"), effectiveFrom = LocalDate.now())
            ).onEach {
            }
            whenever(menuRepository.findByDeletedFalse()).thenReturn(menus)

            // When
            val result = menuService.findAll()

            // Then
            assertThat(result).hasSize(3)
            assertThat(result.map { it.name }).containsExactly("弁当A", "弁当B", "弁当C")
            assertThat(result.all { !it.deleted }).isTrue
        }

        @Test
        @DisplayName("メニューが存在しない場合、空のリストを返す")
        fun `should return empty list when no menus exist`() {
            // Given
            whenever(menuRepository.findByDeletedFalse()).thenReturn(emptyList())

            // When
            val result = menuService.findAll()

            // Then
            assertThat(result).isEmpty()
        }
    }

    @Nested
    @DisplayName("findById - ID でメニュー取得")
    inner class FindByIdTests {

        @Test
        @DisplayName("存在するメニューを ID で取得できる")
        fun `should return menu when menu with given id exists`() {
            // Given
            val now = LocalDateTime.now()
            val menu = Menu(
                id = 1L,
                vendor = testVendor,
                name = "弁当A",
                price = BigDecimal("500"),
                effectiveFrom = LocalDate.now()
            ).apply {
                deleted = false
            }
            whenever(menuRepository.findByIdAndDeletedFalse(1L)).thenReturn(menu)

            // When
            val result = menuService.findById(1L)

            // Then
            assertThat(result.id).isEqualTo(1L)
            assertThat(result.name).isEqualTo("弁当A")
            assertThat(result.price).isEqualByComparingTo(BigDecimal("500"))
            assertThat(result.deleted).isFalse
        }

        @Test
        @DisplayName("存在しない ID で例外が発生する")
        fun `should throw IllegalArgumentException when menu not found`() {
            // Given
            whenever(menuRepository.findByIdAndDeletedFalse(999L)).thenReturn(null)

            // When & Then
            assertThrows<IllegalArgumentException> {
                menuService.findById(999L)
            }.also {
                assertThat(it.message).contains("メニューが見つかりません")
                assertThat(it.message).contains("999")
            }
        }
    }

    @Nested
    @DisplayName("findByVendorId - 業者別メニュー取得")
    inner class FindByVendorIdTests {

        @Test
        @DisplayName("指定業者のメニュー一覧を取得できる")
        fun `should return all menus for specified vendor`() {
            // Given
            val now = LocalDateTime.now()
            val menus = listOf(
                Menu(id = 1L, vendor = testVendor, name = "弁当A", price = BigDecimal("500"), effectiveFrom = LocalDate.now()),
                Menu(id = 2L, vendor = testVendor, name = "弁当B", price = BigDecimal("600"), effectiveFrom = LocalDate.now())
            ).onEach {
            }
            whenever(menuRepository.findByVendorIdAndDeletedFalse(1L)).thenReturn(menus)

            // When
            val result = menuService.findByVendorId(1L)

            // Then
            assertThat(result).hasSize(2)
            assertThat(result.all { it.vendorId == 1L }).isTrue
        }

        @Test
        @DisplayName("メニューがない業者は空のリストを返す")
        fun `should return empty list when vendor has no menus`() {
            // Given
            whenever(menuRepository.findByVendorIdAndDeletedFalse(999L)).thenReturn(emptyList())

            // When
            val result = menuService.findByVendorId(999L)

            // Then
            assertThat(result).isEmpty()
        }
    }

    @Nested
    @DisplayName("searchByName - メニュー名検索")
    inner class SearchByNameTests {

        @Test
        @DisplayName("メニュー名で検索できる")
        fun `should find menus by name containing search string`() {
            // Given
            val now = LocalDateTime.now()
            val menus = listOf(
                Menu(id = 1L, vendor = testVendor, name = "唐揚げ弁当", price = BigDecimal("500"), effectiveFrom = LocalDate.now()),
                Menu(id = 2L, vendor = testVendor, name = "唐揚げ定食", price = BigDecimal("600"), effectiveFrom = LocalDate.now())
            ).onEach {
            }
            whenever(menuRepository.findByNameContainingAndDeletedFalse("唐揚げ")).thenReturn(menus)

            // When
            val result = menuService.searchByName("唐揚げ")

            // Then
            assertThat(result).hasSize(2)
            assertThat(result.all { it.name.contains("唐揚げ") }).isTrue
        }

        @Test
        @DisplayName("検索結果がない場合、空のリストを返す")
        fun `should return empty list when no menus match`() {
            // Given
            whenever(menuRepository.findByNameContainingAndDeletedFalse("存在しないメニュー")).thenReturn(emptyList())

            // When
            val result = menuService.searchByName("存在しないメニュー")

            // Then
            assertThat(result).isEmpty()
        }
    }

    @Nested
    @DisplayName("findCurrentMenusByVendor - 指定日の業者メニュー取得")
    inner class FindCurrentMenusByVendorTests {

        @Test
        @DisplayName("指定日時点で有効なメニューを取得できる")
        fun `should return current menus for vendor on specified date`() {
            // Given
            val today = LocalDate.now()
            val now = LocalDateTime.now()
            val menus = listOf(
                Menu(id = 1L, vendor = testVendor, name = "弁当A", price = BigDecimal("500"), effectiveFrom = today)
            ).onEach {
            }
            whenever(menuRepository.findCurrentMenusByVendorAndDate(1L, today)).thenReturn(menus)

            // When
            val result = menuService.findCurrentMenusByVendor(1L, today)

            // Then
            assertThat(result).hasSize(1)
            assertThat(result[0].name).isEqualTo("弁当A")
        }

        @Test
        @DisplayName("デフォルト日付（今日）でメニューを取得できる")
        fun `should use today as default date when not specified`() {
            // Given
            val today = LocalDate.now()
            val now = LocalDateTime.now()
            val menu = Menu(id = 1L, vendor = testVendor, name = "弁当A", price = BigDecimal("500"), effectiveFrom = today).apply {
            }
            whenever(menuRepository.findCurrentMenusByVendorAndDate(1L, today)).thenReturn(listOf(menu))

            // When
            val result = menuService.findCurrentMenusByVendor(1L)

            // Then
            assertThat(result).isNotEmpty
        }

        @Test
        @DisplayName("メニューがない場合、空のリストを返す")
        fun `should return empty list when no current menus exist`() {
            // Given
            val today = LocalDate.now()
            whenever(menuRepository.findCurrentMenusByVendorAndDate(1L, today)).thenReturn(emptyList())

            // When
            val result = menuService.findCurrentMenusByVendor(1L, today)

            // Then
            assertThat(result).isEmpty()
        }
    }

    @Nested
    @DisplayName("findCurrentMenus - 全業者メニュー取得")
    inner class FindCurrentMenusTests {

        @Test
        @DisplayName("指定日時点で有効な全メニューを取得できる")
        fun `should return all current menus on specified date`() {
            // Given
            val today = LocalDate.now()
            val now = LocalDateTime.now()
            val vendor2 = Vendor(id = 2L, name = "業者2").apply {
            }
            val menus = listOf(
                Menu(id = 1L, vendor = testVendor, name = "弁当A", price = BigDecimal("500"), effectiveFrom = today),
                Menu(id = 2L, vendor = vendor2, name = "弁当B", price = BigDecimal("600"), effectiveFrom = today)
            ).onEach {
            }
            whenever(menuRepository.findCurrentMenusByDate(today)).thenReturn(menus)

            // When
            val result = menuService.findCurrentMenus(today)

            // Then
            assertThat(result).hasSize(2)
        }
    }

    @Nested
    @DisplayName("create - メニュー作成")
    inner class CreateTests {

        @Test
        @DisplayName("新規メニューを作成できる")
        fun `should create a new menu successfully`() {
            // Given
            val today = LocalDate.now()
            val request = MenuCreateRequest(
                vendorId = 1L,
                name = "新規弁当",
                price = BigDecimal("500"),
                effectiveFrom = today,
                description = "説明"
            )
            val now = LocalDateTime.now()
            val savedMenu = Menu(
                id = 1L,
                vendor = testVendor,
                name = "新規弁当",
                price = BigDecimal("500"),
                effectiveFrom = today,
                description = "説明"
            ).apply {
            }
            whenever(vendorRepository.findByIdAndDeletedFalse(1L)).thenReturn(testVendor)
            whenever(menuRepository.save(any())).thenReturn(savedMenu)

            // When
            val result = menuService.create(request)

            // Then
            assertThat(result.id).isEqualTo(1L)
            assertThat(result.name).isEqualTo("新規弁当")
            assertThat(result.price).isEqualByComparingTo(BigDecimal("500"))
            assertThat(result.vendorId).isEqualTo(1L)
        }

        @Test
        @DisplayName("存在しない業者で作成しようとすると例外が発生")
        fun `should throw exception when vendor does not exist`() {
            // Given
            val today = LocalDate.now()
            val request = MenuCreateRequest(
                vendorId = 999L,
                name = "新規弁当",
                price = BigDecimal("500"),
                effectiveFrom = today
            )
            whenever(vendorRepository.findByIdAndDeletedFalse(999L)).thenReturn(null)

            // When & Then
            assertThrows<IllegalArgumentException> {
                menuService.create(request)
            }.also {
                assertThat(it.message).contains("業者が見つかりません")
            }
        }

        @Test
        @DisplayName("説明なしでメニューを作成できる")
        fun `should create menu without description`() {
            // Given
            val today = LocalDate.now()
            val request = MenuCreateRequest(
                vendorId = 1L,
                name = "新規弁当",
                price = BigDecimal("500"),
                effectiveFrom = today,
                description = null
            )
            val now = LocalDateTime.now()
            val savedMenu = Menu(
                id = 1L,
                vendor = testVendor,
                name = "新規弁当",
                price = BigDecimal("500"),
                effectiveFrom = today,
                description = null
            ).apply {
            }
            whenever(vendorRepository.findByIdAndDeletedFalse(1L)).thenReturn(testVendor)
            whenever(menuRepository.save(any())).thenReturn(savedMenu)

            // When
            val result = menuService.create(request)

            // Then
            assertThat(result.name).isEqualTo("新規弁当")
            assertThat(result.description).isNull()
        }
    }

    @Nested
    @DisplayName("update - メニュー更新")
    inner class UpdateTests {

        @Test
        @DisplayName("メニュー名と説明を更新できる")
        fun `should update menu name and description successfully`() {
            // Given
            val now = LocalDateTime.now()
            val menu = Menu(
                id = 1L,
                vendor = testVendor,
                name = "旧弁当名",
                price = BigDecimal("500"),
                effectiveFrom = LocalDate.now(),
                description = "旧説明"
            ).apply {
            }
            val request = MenuUpdateRequest(name = "新弁当名", description = "新説明")
            whenever(menuRepository.findByIdAndDeletedFalse(1L)).thenReturn(menu)
            whenever(menuRepository.save(any())).thenReturn(menu.apply {
                name = "新弁当名"
                description = "新説明"
            })

            // When
            val result = menuService.update(1L, request)

            // Then
            assertThat(result.name).isEqualTo("新弁当名")
            assertThat(result.description).isEqualTo("新説明")
        }

        @Test
        @DisplayName("存在しないメニューを更新しようとすると例外が発生")
        fun `should throw exception when menu does not exist`() {
            // Given
            val request = MenuUpdateRequest(name = "新弁当名", description = "新説明")
            whenever(menuRepository.findByIdAndDeletedFalse(999L)).thenReturn(null)

            // When & Then
            assertThrows<IllegalArgumentException> {
                menuService.update(999L, request)
            }.also {
                assertThat(it.message).contains("メニューが見つかりません")
            }
        }
    }

    @Nested
    @DisplayName("changePrice - メニュー価格変更")
    inner class ChangePriceTests {

        @Test
        @DisplayName("メニューの価格を変更できる")
        fun `should change menu price by creating new record`() {
            // Given
            val now = LocalDateTime.now()
            val originalMenu = Menu(
                id = 1L,
                vendor = testVendor,
                name = "弁当A",
                price = BigDecimal("500"),
                effectiveFrom = LocalDate.now()
            ).apply {
            }
            val tomorrow = LocalDate.now().plusDays(1)
            val request = MenuPriceChangeRequest(newPrice = BigDecimal("600"), effectiveFrom = tomorrow)
            val newMenu = Menu(
                id = 2L,
                vendor = testVendor,
                name = "弁当A",
                price = BigDecimal("600"),
                effectiveFrom = tomorrow
            ).apply {
            }
            whenever(menuRepository.findByIdAndDeletedFalse(1L)).thenReturn(originalMenu)
            whenever(menuRepository.save(any())).thenReturn(newMenu)

            // When
            val result = menuService.changePrice(1L, request)

            // Then
            assertThat(result.price).isEqualByComparingTo(BigDecimal("600"))
            assertThat(result.effectiveFrom).isEqualTo(tomorrow)
        }

        @Test
        @DisplayName("存在しないメニューの価格を変更しようとすると例外が発生")
        fun `should throw exception when menu to change price does not exist`() {
            // Given
            val tomorrow = LocalDate.now().plusDays(1)
            val request = MenuPriceChangeRequest(newPrice = BigDecimal("600"), effectiveFrom = tomorrow)
            whenever(menuRepository.findByIdAndDeletedFalse(999L)).thenReturn(null)

            // When & Then
            assertThrows<IllegalArgumentException> {
                menuService.changePrice(999L, request)
            }.also {
                assertThat(it.message).contains("メニューが見つかりません")
            }
        }
    }

    @Nested
    @DisplayName("delete - メニュー削除")
    inner class DeleteTests {

        @Test
        @DisplayName("メニューを論理削除できる")
        fun `should soft delete menu successfully`() {
            // Given
            val now = LocalDateTime.now()
            val menu = Menu(
                id = 1L,
                vendor = testVendor,
                name = "削除対象メニュー",
                price = BigDecimal("500"),
                effectiveFrom = LocalDate.now(),
                deleted = false
            ).apply {
            }
            whenever(menuRepository.findByIdAndDeletedFalse(1L)).thenReturn(menu)
            whenever(menuRepository.save(any())).thenReturn(menu.apply { deleted = true })

            // When
            menuService.delete(1L)

            // Then
            assertThat(menu.deleted).isTrue
        }

        @Test
        @DisplayName("存在しないメニューを削除しようとすると例外が発生")
        fun `should throw exception when deleting non-existent menu`() {
            // Given
            whenever(menuRepository.findByIdAndDeletedFalse(999L)).thenReturn(null)

            // When & Then
            assertThrows<IllegalArgumentException> {
                menuService.delete(999L)
            }.also {
                assertThat(it.message).contains("メニューが見つかりません")
            }
        }
    }

    // ==================== 境界値テスト ====================

    @Nested
    @DisplayName("境界値テスト")
    inner class BoundaryValueTests {

        @Test
        @DisplayName("最小金額 1 円で作成できる")
        fun `should create menu with minimum price of 1 yen`() {
            // Given
            val today = LocalDate.now()
            val request = MenuCreateRequest(
                vendorId = 1L,
                name = "最安弁当",
                price = BigDecimal("1"),
                effectiveFrom = today
            )
            val now = LocalDateTime.now()
            val savedMenu = Menu(
                id = 1L,
                vendor = testVendor,
                name = "最安弁当",
                price = BigDecimal("1"),
                effectiveFrom = today
            ).apply {
            }
            whenever(vendorRepository.findByIdAndDeletedFalse(1L)).thenReturn(testVendor)
            whenever(menuRepository.save(any())).thenReturn(savedMenu)

            // When
            val result = menuService.create(request)

            // Then
            assertThat(result.price).isEqualByComparingTo(BigDecimal("1"))
        }

        @Test
        @DisplayName("大きな金額で作成できる")
        fun `should create menu with large price`() {
            // Given
            val today = LocalDate.now()
            val largePrice = BigDecimal("99999")
            val request = MenuCreateRequest(
                vendorId = 1L,
                name = "高級弁当",
                price = largePrice,
                effectiveFrom = today
            )
            val now = LocalDateTime.now()
            val savedMenu = Menu(
                id = 1L,
                vendor = testVendor,
                name = "高級弁当",
                price = largePrice,
                effectiveFrom = today
            ).apply {
            }
            whenever(vendorRepository.findByIdAndDeletedFalse(1L)).thenReturn(testVendor)
            whenever(menuRepository.save(any())).thenReturn(savedMenu)

            // When
            val result = menuService.create(request)

            // Then
            assertThat(result.price).isEqualByComparingTo(largePrice)
        }

        @Test
        @DisplayName("過去の日付から有効になるメニューを作成できる")
        fun `should create menu with past date as effective date`() {
            // Given
            val yesterday = LocalDate.now().minusDays(1)
            val request = MenuCreateRequest(
                vendorId = 1L,
                name = "過去から有効な弁当",
                price = BigDecimal("500"),
                effectiveFrom = yesterday
            )
            val now = LocalDateTime.now()
            val savedMenu = Menu(
                id = 1L,
                vendor = testVendor,
                name = "過去から有効な弁当",
                price = BigDecimal("500"),
                effectiveFrom = yesterday
            ).apply {
            }
            whenever(vendorRepository.findByIdAndDeletedFalse(1L)).thenReturn(testVendor)
            whenever(menuRepository.save(any())).thenReturn(savedMenu)

            // When
            val result = menuService.create(request)

            // Then
            assertThat(result.effectiveFrom).isEqualTo(yesterday)
        }

        @Test
        @DisplayName("将来の日付から有効になるメニューを作成できる")
        fun `should create menu with future date as effective date`() {
            // Given
            val nextMonth = LocalDate.now().plusMonths(1)
            val request = MenuCreateRequest(
                vendorId = 1L,
                name = "将来から有効な弁当",
                price = BigDecimal("500"),
                effectiveFrom = nextMonth
            )
            val now = LocalDateTime.now()
            val savedMenu = Menu(
                id = 1L,
                vendor = testVendor,
                name = "将来から有効な弁当",
                price = BigDecimal("500"),
                effectiveFrom = nextMonth
            ).apply {
            }
            whenever(vendorRepository.findByIdAndDeletedFalse(1L)).thenReturn(testVendor)
            whenever(menuRepository.save(any())).thenReturn(savedMenu)

            // When
            val result = menuService.create(request)

            // Then
            assertThat(result.effectiveFrom).isEqualTo(nextMonth)
        }

        @Test
        @DisplayName("空の説明でメニューを作成できる")
        fun `should create menu with empty description`() {
            // Given
            val today = LocalDate.now()
            val request = MenuCreateRequest(
                vendorId = 1L,
                name = "説明なし弁当",
                price = BigDecimal("500"),
                effectiveFrom = today,
                description = ""
            )
            val now = LocalDateTime.now()
            val savedMenu = Menu(
                id = 1L,
                vendor = testVendor,
                name = "説明なし弁当",
                price = BigDecimal("500"),
                effectiveFrom = today,
                description = ""
            ).apply {
            }
            whenever(vendorRepository.findByIdAndDeletedFalse(1L)).thenReturn(testVendor)
            whenever(menuRepository.save(any())).thenReturn(savedMenu)

            // When
            val result = menuService.create(request)

            // Then
            assertThat(result.description).isEmpty()
        }
    }
}