package Sample.BentouReservation.repository

import Sample.BentouReservation.entity.Vendor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 業者マスタリポジトリ
 */
@Repository
interface VendorRepository : JpaRepository<Vendor, Long> {

    /**
     * 削除されていない業者を全件取得
     */
    fun findByDeletedFalse(): List<Vendor>

    /**
     * 業者名で検索（削除されていないもののみ）
     */
    fun findByNameContainingAndDeletedFalse(name: String): List<Vendor>

    /**
     * IDで検索（削除されていないもののみ）
     */
    fun findByIdAndDeletedFalse(id: Long): Vendor?
}
