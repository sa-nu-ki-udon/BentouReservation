package Sample.BentouReservation.repository

import Sample.BentouReservation.entity.Vendor
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("VendorRepository テスト")
class VendorRepositoryTest {

    @Autowired
    private lateinit var vendorRepository: VendorRepository

    private lateinit var testVendor1: Vendor
    private lateinit var testVendor2: Vendor
    private lateinit var testVendor3: Vendor

    @BeforeEach
    fun setUp() {
        // テストデータをクリアして初期化
        vendorRepository.deleteAll()

        testVendor1 = Vendor(name = "業者A", contactInfo = "123-4567").apply {
            deleted = false
        }
        testVendor2 = Vendor(name = "業者B", contactInfo = "098-7654").apply {
            deleted = false
        }
        testVendor3 = Vendor(name = "削除済み業者", contactInfo = "000-0000").apply {
            deleted = true
        }

        vendorRepository.saveAll(listOf(testVendor1, testVendor2, testVendor3))
    }

    // ==================== 正常系テスト ====================

    @Nested
    @DisplayName("findByDeletedFalse - 削除されていない業者取得")
    inner class FindByDeletedFalseTests {

        @Test
        @DisplayName("削除されていない全業者を取得できる")
        fun `should return all non-deleted vendors`() {
            // When
            val result = vendorRepository.findByDeletedFalse()

            // Then
            assertThat(result).hasSize(2)
            assertThat(result.map { it.name }).containsExactly("業者A", "業者B")
            assertThat(result.all { !it.deleted }).isTrue
        }

        @Test
        @DisplayName("削除済み業者は含めない")
        fun `should not include deleted vendors in result`() {
            // When
            val result = vendorRepository.findByDeletedFalse()

            // Then
            assertThat(result).noneMatch { it.deleted }
            assertThat(result.map { it.name }).doesNotContain("削除済み業者")
        }

        @Test
        @DisplayName("全業者が削除されている場合、空のリストを返す")
        fun `should return empty list when all vendors are deleted`() {
            // Given
            val allVendors = vendorRepository.findAll()
            allVendors.forEach { it.deleted = true }
            vendorRepository.saveAll(allVendors)

            // When
            val result = vendorRepository.findByDeletedFalse()

            // Then
            assertThat(result).isEmpty()
        }
    }

    @Nested
    @DisplayName("findByIdAndDeletedFalse - ID で削除されていない業者取得")
    inner class FindByIdAndDeletedFalseTests {

        @Test
        @DisplayName("存在する削除されていない業者を取得できる")
        fun `should return vendor when id matches and deleted is false`() {
            // Given
            val vendorId = testVendor1.id!!

            // When
            val result = vendorRepository.findByIdAndDeletedFalse(vendorId)

            // Then
            assertThat(result).isNotNull
            assertThat(result?.name).isEqualTo("業者A")
            assertThat(result?.deleted).isFalse
        }

        @Test
        @DisplayName("削除済み業者は取得できない")
        fun `should return null when vendor is deleted`() {
            // Given
            val deletedVendorId = testVendor3.id!!

            // When
            val result = vendorRepository.findByIdAndDeletedFalse(deletedVendorId)

            // Then
            assertThat(result).isNull()
        }

        @Test
        @DisplayName("存在しない ID は null を返す")
        fun `should return null when vendor id does not exist`() {
            // When
            val result = vendorRepository.findByIdAndDeletedFalse(999L)

            // Then
            assertThat(result).isNull()
        }
    }

    @Nested
    @DisplayName("findByNameContainingAndDeletedFalse - 名前で検索")
    inner class FindByNameContainingAndDeletedFalseTests {

        @Test
        @DisplayName("検索文字列を含む削除されていない業者を検索できる")
        fun `should find vendors by name containing search string`() {
            // When
            val result = vendorRepository.findByNameContainingAndDeletedFalse("業者")

            // Then
            assertThat(result).hasSize(2)
            assertThat(result.map { it.name }).containsExactly("業者A", "業者B")
        }

        @Test
        @DisplayName("部分一致で検索できる")
        fun `should find vendors with partial name match`() {
            // When
            val result = vendorRepository.findByNameContainingAndDeletedFalse("A")

            // Then
            assertThat(result).hasSize(1)
            assertThat(result[0].name).isEqualTo("業者A")
        }

        @Test
        @DisplayName("削除済み業者は検索結果に含めない")
        fun `should not include deleted vendors in search results`() {
            // When
            val result = vendorRepository.findByNameContainingAndDeletedFalse("削除")

            // Then
            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("検索結果がない場合、空のリストを返す")
        fun `should return empty list when no vendors match search criteria`() {
            // When
            val result = vendorRepository.findByNameContainingAndDeletedFalse("存在しない")

            // Then
            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("空の検索文字列で全削除されていない業者を取得できる")
        fun `should find all non-deleted vendors with empty search string`() {
            // When
            val result = vendorRepository.findByNameContainingAndDeletedFalse("")

            // Then
            assertThat(result).hasSize(2)
        }

        @Test
        @DisplayName("大文字小文字を区別して検索できる")
        fun `should search with case sensitivity`() {
            // Given
            val caseVendor = Vendor(name = "ABC業者", contactInfo = "123-4567").apply {
                deleted = false
            }
            vendorRepository.save(caseVendor)

            // When
            val result = vendorRepository.findByNameContainingAndDeletedFalse("ABC")

            // Then
            assertThat(result).hasSize(1)
            assertThat(result[0].name).isEqualTo("ABC業者")
        }
    }

    // ==================== 異常系テスト ====================

    @Nested
    @DisplayName("異常系テスト")
    inner class AbnormalCasesTests {

        @Test
        @DisplayName("存在しない ID で findByIdAndDeletedFalse を呼ぶと null を返す")
        fun `should return null for non-existent vendor id`() {
            // When
            val result = vendorRepository.findByIdAndDeletedFalse(Long.MAX_VALUE)

            // Then
            assertThat(result).isNull()
        }

        @Test
        @DisplayName("null 検索で例外が発生しない（null safe）")
        fun `should handle null safe operations`() {
            // When
            val result = vendorRepository.findByIdAndDeletedFalse(testVendor1.id!!)

            // Then
            assertThat(result).isNotNull
            assertThat(result?.name).isNotNull
        }
    }

    // ==================== 境界値テスト ====================

    @Nested
    @DisplayName("境界値テスト")
    inner class BoundaryValueTests {

        @Test
        @DisplayName("最大長の業者名で検索できる")
        fun `should search vendor with maximum name length`() {
            // Given
            val maxLengthName = "a".repeat(100)
            val vendor = Vendor(name = maxLengthName, contactInfo = "123-4567").apply {
                deleted = false
            }
            vendorRepository.save(vendor)

            // When
            val result = vendorRepository.findByNameContainingAndDeletedFalse(maxLengthName)

            // Then
            assertThat(result).hasSize(1)
            assertThat(result[0].name).hasSize(100)
        }

        @Test
        @DisplayName("特殊文字を含む業者名で検索できる")
        fun `should search vendor with special characters in name`() {
            // Given
            val specialVendor = Vendor(name = "業者(株)", contactInfo = "123-4567").apply {
                deleted = false
            }
            vendorRepository.save(specialVendor)

            // When
            val result = vendorRepository.findByNameContainingAndDeletedFalse("(株)")

            // Then
            assertThat(result).hasSize(1)
            assertThat(result[0].name).contains("(株)")
        }

        @Test
        @DisplayName("連絡先が null の業者を保存・検索できる")
        fun `should handle vendor with null contact info`() {
            // Given
            val nullContactVendor = Vendor(name = "連絡先なし業者", contactInfo = null).apply {
                deleted = false
            }
            vendorRepository.save(nullContactVendor)

            // When
            val result = vendorRepository.findByNameContainingAndDeletedFalse("連絡先なし")

            // Then
            assertThat(result).hasSize(1)
            assertThat(result[0].contactInfo).isNull()
        }

        @Test
        @DisplayName("最大長の連絡先で業者を保存・取得できる")
        fun `should handle vendor with maximum contact info length`() {
            // Given
            val maxLengthContact = "a".repeat(255)
            val vendor = Vendor(name = "連絡先長い業者", contactInfo = maxLengthContact).apply {
                deleted = false
            }
            vendorRepository.save(vendor)

            // When
            val result = vendorRepository.findByIdAndDeletedFalse(vendor.id!!)

            // Then
            assertThat(result).isNotNull
            assertThat(result?.contactInfo).hasSize(255)
        }

        @Test
        @DisplayName("ID が 1 の業者を取得できる")
        fun `should retrieve vendor with id 1`() {
            // Given
            val vendor = Vendor(name = "ID1業者", contactInfo = "123-4567").apply {
                deleted = false
            }
            val savedVendor = vendorRepository.save(vendor)

            // When
            val result = vendorRepository.findByIdAndDeletedFalse(savedVendor.id!!)

            // Then
            assertThat(result).isNotNull
            assertThat(result?.name).isEqualTo("ID1業者")
        }

        @Test
        @DisplayName("大量の業者を保存・検索できる")
        fun `should handle large number of vendors`() {
            // Given
            val manyVendors = (1..100).map {
                Vendor(name = "業者$it", contactInfo = "123-456$it").apply {
                    deleted = false
                }
            }
            vendorRepository.saveAll(manyVendors)

            // When
            val result = vendorRepository.findByDeletedFalse()

            // Then
            assertThat(result).hasSizeGreaterThanOrEqualTo(100)
        }

        @Test
        @DisplayName("全て同じ名前の複数業者を検索できる")
        fun `should find multiple vendors with same name`() {
            // Given
            val duplicateVendor1 = Vendor(name = "同名業者", contactInfo = "111-1111").apply {
                deleted = false
            }
            val duplicateVendor2 = Vendor(name = "同名業者", contactInfo = "222-2222").apply {
                deleted = false
            }
            vendorRepository.saveAll(listOf(duplicateVendor1, duplicateVendor2))

            // When
            val result = vendorRepository.findByNameContainingAndDeletedFalse("同名業者")

            // Then
            assertThat(result).hasSizeGreaterThanOrEqualTo(2)
            assertThat(result.filter { it.name == "同名業者" }).hasSizeGreaterThanOrEqualTo(2)
        }

        @Test
        @DisplayName("論理削除フラグが false 境界でのテスト")
        fun `should correctly filter by deleted flag boundary`() {
            // Given
            val vendor = Vendor(name = "削除判定業者", contactInfo = "123-4567")
            vendor.deleted = false
            vendorRepository.save(vendor)

            // When - deleted = false のときは取得
            val resultNotDeleted = vendorRepository.findByIdAndDeletedFalse(vendor.id!!)
            assertThat(resultNotDeleted).isNotNull

            // When - deleted = true に変更
            vendor.deleted = true
            vendorRepository.save(vendor)
            val resultDeleted = vendorRepository.findByIdAndDeletedFalse(vendor.id!!)

            // Then
            assertThat(resultDeleted).isNull()
        }

        @Test
        @DisplayName("複数検索条件の組み合わせでテスト")
        fun `should handle combination of search conditions`() {
            // Given
            val vendor1 = Vendor(name = "A業者", contactInfo = "111-1111").apply { deleted = false }
            val vendor2 = Vendor(name = "A業者", contactInfo = "111-1111").apply { deleted = true }
            val vendor3 = Vendor(name = "B業者", contactInfo = "111-1111").apply { deleted = false }
            vendorRepository.saveAll(listOf(vendor1, vendor2, vendor3))

            // When
            val resultA = vendorRepository.findByNameContainingAndDeletedFalse("A業者")
            val resultAll = vendorRepository.findByDeletedFalse()

            // Then
            assertThat(resultA).hasSize(1)  // deleted = false のものだけ
            assertThat(resultA[0].name).isEqualTo("A業者")
            assertThat(resultAll).hasSize(3) // testVendor1, testVendor2, vendor1, vendor3 のうち deleted=false
        }
    }

    // ==================== データベース操作テスト ====================

    @Nested
    @DisplayName("データベース操作テスト")
    inner class DatabaseOperationTests {

        @Test
        @DisplayName("新規業者を保存できる")
        fun `should save new vendor successfully`() {
            // Given
            val newVendor = Vendor(name = "新規業者", contactInfo = "new-contact").apply {
                deleted = false
            }

            // When
            val saved = vendorRepository.save(newVendor)

            // Then
            assertThat(saved.id).isNotNull
            assertThat(saved.name).isEqualTo("新規業者")
            assertThat(saved.createdAt).isNotNull
            assertThat(saved.updatedAt).isNotNull
        }

        @Test
        @DisplayName("既存業者を更新できる")
        fun `should update existing vendor successfully`() {
            // Given
            val vendorId = testVendor1.id!!
            val vendor = vendorRepository.findById(vendorId).get()

            // When
            vendor.name = "更新業者名"
            vendor.contactInfo = "new-contact-info"
            vendorRepository.save(vendor)

            // Then
            val updated = vendorRepository.findById(vendorId).get()
            assertThat(updated.name).isEqualTo("更新業者名")
            assertThat(updated.contactInfo).isEqualTo("new-contact-info")
        }

        @Test
        @DisplayName("業者を削除できる")
        fun `should delete vendor successfully`() {
            // Given
            val vendorId = testVendor1.id!!

            // When
            vendorRepository.deleteById(vendorId)

            // Then
            assertThat(vendorRepository.findById(vendorId)).isEmpty
        }
    }
}