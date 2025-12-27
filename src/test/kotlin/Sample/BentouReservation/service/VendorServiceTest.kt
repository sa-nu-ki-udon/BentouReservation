package Sample.BentouReservation.service

import Sample.BentouReservation.createTestVendor
import Sample.BentouReservation.dto.VendorCreateRequest
import Sample.BentouReservation.dto.VendorResponse
import Sample.BentouReservation.dto.VendorUpdateRequest
import Sample.BentouReservation.entity.Vendor
import Sample.BentouReservation.repository.VendorRepository
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

@SpringBootTest
@DisplayName("VendorService テスト")
class VendorServiceTest {

    @MockitoBean
    private lateinit var vendorRepository: VendorRepository

    @Autowired
    private lateinit var vendorService: VendorService


    // ==================== 正常系テスト ====================

    @Nested
    @DisplayName("findAll - 全業者取得")
    inner class FindAllTests {

        @Test
        @DisplayName("複数の業者が存在する場合、全て取得できる")
        fun `should return all active vendors when multiple vendors exist`() {
            // Given
            val vendors = listOf(
                createTestVendor(id = 1L, name = "弁当業者A", contactInfo = "123-4567"),
                createTestVendor(id = 2L, name = "弁当業者B", contactInfo = "123-4568"),
                createTestVendor(id = 3L, name = "弁当業者C", contactInfo = null)
            )
            whenever(vendorRepository.findByDeletedFalse()).thenReturn(vendors)

            // When
            val result = vendorService.findAll()

            // Then
            assertThat(result).hasSize(3)
            assertThat(result.map { it.name }).containsExactly(
                "弁当業者A", "弁当業者B", "弁当業者C"
            )
            assertThat(result.all { !it.deleted }).isTrue
        }

        @Test
        @DisplayName("業者が存在しない場合、空のリストを返す")
        fun `should return empty list when no vendors exist`() {
            // Given
            whenever(vendorRepository.findByDeletedFalse()).thenReturn(emptyList())

            // When
            val result = vendorService.findAll()

            // Then
            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("削除済み業者は含めない")
        fun `should only return non-deleted vendors`() {
            // Given
            val now = LocalDateTime.now()
            val vendors = listOf(
                createTestVendor(id = 1L, name = "弁当業者A", deleted = false),
                createTestVendor(id = 2L, name = "弁当業者B", deleted = false)
            ).onEach {
            }
            whenever(vendorRepository.findByDeletedFalse()).thenReturn(vendors)

            // When
            val result = vendorService.findAll()

            // Then
            assertThat(result).allMatch { !it.deleted }
            assertThat(result).hasSize(2)
        }
    }

    @Nested
    @DisplayName("findById - ID で業者取得")
    inner class FindByIdTests {

        @Test
        @DisplayName("存在する業者を ID で取得できる")
        fun `should return vendor when vendor with given id exists`() {
            // Given
            val now = LocalDateTime.now()
            val vendor = createTestVendor(id = 1L, name = "弁当業者A", contactInfo = "123-4567").apply {
                deleted = false
            }
            whenever(vendorRepository.findByIdAndDeletedFalse(1L)).thenReturn(vendor)

            // When
            val result = vendorService.findById(1L)

            // Then
            assertThat(result.id).isEqualTo(1L)
            assertThat(result.name).isEqualTo("弁当業者A")
            assertThat(result.contactInfo).isEqualTo("123-4567")
            assertThat(result.deleted).isFalse
        }

        @Test
        @DisplayName("存在しない ID で例外が発生する")
        fun `should throw IllegalArgumentException when vendor not found`() {
            // Given
            whenever(vendorRepository.findByIdAndDeletedFalse(999L)).thenReturn(null)

            // When & Then
            assertThrows<IllegalArgumentException> {
                vendorService.findById(999L)
            }.also {
                assertThat(it.message).contains("業者が見つかりません")
                assertThat(it.message).contains("999")
            }
        }

        @Test
        @DisplayName("削除済み業者は取得できない")
        fun `should not return deleted vendor`() {
            // Given
            whenever(vendorRepository.findByIdAndDeletedFalse(1L)).thenReturn(null)

            // When & Then
            assertThrows<IllegalArgumentException> {
                vendorService.findById(1L)
            }
        }
    }

    @Nested
    @DisplayName("searchByName - 業者名検索")
    inner class SearchByNameTests {

        @Test
        @DisplayName("検索文字列で業者を検索できる")
        fun `should find vendors by name containing search string`() {
            // Given
            val now = LocalDateTime.now()
            val vendors = listOf(
                createTestVendor(id = 1L, name = "弁当業者A", deleted = false),
                createTestVendor(id = 2L, name = "弁当業者AB", deleted = false)
            ).onEach {
            }
            whenever(vendorRepository.findByNameContainingAndDeletedFalse("業者A")).thenReturn(vendors)

            // When
            val result = vendorService.searchByName("業者A")

            // Then
            assertThat(result).hasSize(2)
            assertThat(result.map { it.name }).containsExactly("弁当業者A", "弁当業者AB")
        }

        @Test
        @DisplayName("検索結果がない場合、空のリストを返す")
        fun `should return empty list when no vendors match search criteria`() {
            // Given
            whenever(vendorRepository.findByNameContainingAndDeletedFalse("存在しない業者")).thenReturn(emptyList())

            // When
            val result = vendorService.searchByName("存在しない業者")

            // Then
            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("部分一致で検索できる")
        fun `should find vendors with partial name match`() {
            // Given
            val now = LocalDateTime.now()
            val vendor = createTestVendor(id = 1L, name = "弁当業者A", deleted = false).apply {
            }
            whenever(vendorRepository.findByNameContainingAndDeletedFalse("業者")).thenReturn(listOf(vendor))

            // When
            val result = vendorService.searchByName("業者")

            // Then
            assertThat(result).hasSize(1)
            assertThat(result[0].name).isEqualTo("弁当業者A")
        }
    }

    @Nested
    @DisplayName("create - 業者作成")
    inner class CreateTests {

        @Test
        @DisplayName("新規業者を作成できる")
        fun `should create a new vendor successfully`() {
            // Given
            val request = VendorCreateRequest(name = "新規業者", contactInfo = "090-1234-5678")
            val now = LocalDateTime.now()
            val savedVendor = createTestVendor(id = 1L, name = "新規業者", contactInfo = "090-1234-5678")
            whenever(vendorRepository.save(any())).thenReturn(savedVendor)

            // When
            val result = vendorService.create(request)

            // Then
            assertThat(result.id).isEqualTo(1L)
            assertThat(result.name).isEqualTo("新規業者")
            assertThat(result.contactInfo).isEqualTo("090-1234-5678")
        }

        @Test
        @DisplayName("連絡先なしで業者を作成できる")
        fun `should create vendor without contact info`() {
            // Given
            val request = VendorCreateRequest(name = "新規業者", contactInfo = null)
            val now = LocalDateTime.now()
            val savedVendor = createTestVendor(id = 1L, name = "新規業者", contactInfo = null).apply {
                deleted = false
            }
            whenever(vendorRepository.save(any())).thenReturn(savedVendor)

            // When
            val result = vendorService.create(request)

            // Then
            assertThat(result.name).isEqualTo("新規業者")
            assertThat(result.contactInfo).isNull()
        }
    }

    @Nested
    @DisplayName("update - 業者更新")
    inner class UpdateTests {

        @Test
        @DisplayName("業者情報を更新できる")
        fun `should update vendor successfully`() {
            // Given
            val now = LocalDateTime.now()
            val existingVendor = createTestVendor(id = 1L, name = "旧業者名", contactInfo = "123-4567").apply {
                deleted = false
            }
            val request = VendorUpdateRequest(name = "新業者名", contactInfo = "098-7654-3210")
            whenever(vendorRepository.findByIdAndDeletedFalse(1L)).thenReturn(existingVendor)
            whenever(vendorRepository.save(any())).thenReturn(existingVendor.apply {
                name = "新業者名"
                contactInfo = "098-7654-3210"
            })

            // When
            val result = vendorService.update(1L, request)

            // Then
            assertThat(result.name).isEqualTo("新業者名")
            assertThat(result.contactInfo).isEqualTo("098-7654-3210")
        }

        @Test
        @DisplayName("存在しない業者を更新しようとすると例外が発生")
        fun `should throw exception when updating non-existent vendor`() {
            // Given
            val request = VendorUpdateRequest(name = "新業者名", contactInfo = "098-7654-3210")
            whenever(vendorRepository.findByIdAndDeletedFalse(999L)).thenReturn(null)

            // When & Then
            assertThrows<IllegalArgumentException> {
                vendorService.update(999L, request)
            }.also {
                assertThat(it.message).contains("業者が見つかりません")
            }
        }

        @Test
        @DisplayName("連絡先を更新できる")
        fun `should update only contact info`() {
            // Given
            val now = LocalDateTime.now()
            val vendor = createTestVendor(id = 1L, name = "業者A", contactInfo = "旧連絡先").apply {
            }
            val request = VendorUpdateRequest(name = "業者A", contactInfo = "新連絡先")
            whenever(vendorRepository.findByIdAndDeletedFalse(1L)).thenReturn(vendor)
            whenever(vendorRepository.save(any())).thenReturn(vendor.apply { contactInfo = "新連絡先" })

            // When
            val result = vendorService.update(1L, request)

            // Then
            assertThat(result.contactInfo).isEqualTo("新連絡先")
        }
    }

    @Nested
    @DisplayName("delete - 業者削除")
    inner class DeleteTests {

        @Test
        @DisplayName("業者を論理削除できる")
        fun `should soft delete vendor successfully`() {
            // Given
            val now = LocalDateTime.now()
            val vendor = createTestVendor(id = 1L, name = "削除対象業者", deleted = false).apply {
            }
            whenever(vendorRepository.findByIdAndDeletedFalse(1L)).thenReturn(vendor)
            whenever(vendorRepository.save(any())).thenReturn(vendor.apply { deleted = true })

            // When
            vendorService.delete(1L)

            // Then
            assertThat(vendor.deleted).isTrue
        }

        @Test
        @DisplayName("存在しない業者を削除しようとすると例外が発生")
        fun `should throw exception when deleting non-existent vendor`() {
            // Given
            whenever(vendorRepository.findByIdAndDeletedFalse(999L)).thenReturn(null)

            // When & Then
            assertThrows<IllegalArgumentException> {
                vendorService.delete(999L)
            }.also {
                assertThat(it.message).contains("業者が見つかりません")
            }
        }

        @Test
        @DisplayName("削除済み業者は削除できない")
        fun `should not delete already deleted vendor`() {
            // Given
            whenever(vendorRepository.findByIdAndDeletedFalse(1L)).thenReturn(null)

            // When & Then
            assertThrows<IllegalArgumentException> {
                vendorService.delete(1L)
            }
        }
    }

    // ==================== 境界値テスト ====================

    @Nested
    @DisplayName("境界値テスト")
    inner class BoundaryValueTests {

        @Test
        @DisplayName("空の検索文字列で検索できる")
        fun `should search with empty string`() {
            // Given
            val now = LocalDateTime.now()
            val vendors = listOf(
                createTestVendor(id = 1L, name = "弁当業者A", deleted = false),
                createTestVendor(id = 2L, name = "弁当業者B", deleted = false)
            ).onEach {
            }
            whenever(vendorRepository.findByNameContainingAndDeletedFalse("")).thenReturn(vendors)

            // When
            val result = vendorService.searchByName("")

            // Then
            assertThat(result).hasSize(2)
        }

        @Test
        @DisplayName("最大長の業者名を作成できる")
        fun `should create vendor with maximum name length`() {
            // Given
            val maxLengthName = "a".repeat(100)
            val request = VendorCreateRequest(name = maxLengthName)
            val now = LocalDateTime.now()
            val savedVendor = createTestVendor(id = 1L, name = maxLengthName).apply {
            }
            whenever(vendorRepository.save(any())).thenReturn(savedVendor)

            // When
            val result = vendorService.create(request)

            // Then
            assertThat(result.name).hasSize(100)
            assertThat(result.name).isEqualTo(maxLengthName)
        }

        @Test
        @DisplayName("特殊文字を含む業者名で検索できる")
        fun `should search with special characters in vendor name`() {
            // Given
            val now = LocalDateTime.now()
            val vendor = createTestVendor(id = 1L, name = "弁当業者 (株) A", deleted = false).apply {
            }
            whenever(vendorRepository.findByNameContainingAndDeletedFalse("(株)")).thenReturn(listOf(vendor))

            // When
            val result = vendorService.searchByName("(株)")

            // Then
            assertThat(result).hasSize(1)
            assertThat(result[0].name).contains("(株)")
        }

        @Test
        @DisplayName("ID が 1 の業者を取得できる")
        fun `should retrieve vendor with id 1`() {
            // Given
            val now = LocalDateTime.now()
            val vendor = createTestVendor(id = 1L, name = "最初の業者").apply {
            }
            whenever(vendorRepository.findByIdAndDeletedFalse(1L)).thenReturn(vendor)

            // When
            val result = vendorService.findById(1L)

            // Then
            assertThat(result.id).isEqualTo(1L)
        }

        @Test
        @DisplayName("大きい ID の業者を取得できる")
        fun `should retrieve vendor with large id`() {
            // Given
            val largeId = Long.MAX_VALUE - 1
            val now = LocalDateTime.now()
            val vendor = createTestVendor(id = largeId, name = "大きいID業者").apply {
            }
            whenever(vendorRepository.findByIdAndDeletedFalse(largeId)).thenReturn(vendor)

            // When
            val result = vendorService.findById(largeId)

            // Then
            assertThat(result.id).isEqualTo(largeId)
        }
    }
}
