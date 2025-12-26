package Sample.BentouReservation.repository

import Sample.BentouReservation.entity.Menu
import Sample.BentouReservation.entity.Vendor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * メニューマスタリポジトリ
 */
@Repository
interface MenuRepository : JpaRepository<Menu, Long> {

    /**
     * 削除されていないメニューを全件取得
     */
    fun findByDeletedFalse(): List<Menu>

    /**
     * 業者IDで検索（削除されていないもののみ）
     */
    fun findByVendorAndDeletedFalse(vendor: Vendor): List<Menu>

    /**
     * 業者IDで検索（削除されていないもののみ）
     */
    fun findByVendorIdAndDeletedFalse(vendorId: Long): List<Menu>

    /**
     * メニュー名で検索（削除されていないもののみ）
     */
    fun findByNameContainingAndDeletedFalse(name: String): List<Menu>

    /**
     * 指定日時点で有効なメニューを取得（業者別）
     * 同じメニュー名で複数の価格変更履歴がある場合、最新のものを取得
     */
    @Query("""
        SELECT m FROM Menu m
        WHERE m.vendor.id = :vendorId
        AND m.deleted = false
        AND m.effectiveFrom <= :date
        AND m.id IN (
            SELECT MAX(m2.id) FROM Menu m2
            WHERE m2.vendor.id = :vendorId
            AND m2.name = m.name
            AND m2.deleted = false
            AND m2.effectiveFrom <= :date
            GROUP BY m2.name
        )
        ORDER BY m.name
    """)
    fun findCurrentMenusByVendorAndDate(
        @Param("vendorId") vendorId: Long,
        @Param("date") date: LocalDate
    ): List<Menu>

    /**
     * 指定日時点で有効なメニューを全件取得
     */
    @Query("""
        SELECT m FROM Menu m
        WHERE m.deleted = false
        AND m.effectiveFrom <= :date
        AND m.id IN (
            SELECT MAX(m2.id) FROM Menu m2
            WHERE m2.vendor.id = m.vendor.id
            AND m2.name = m.name
            AND m2.deleted = false
            AND m2.effectiveFrom <= :date
            GROUP BY m2.vendor.id, m2.name
        )
        ORDER BY m.vendor.name, m.name
    """)
    fun findCurrentMenusByDate(@Param("date") date: LocalDate): List<Menu>

    /**
     * メニューIDで検索（削除されていないもののみ）
     */
    fun findByIdAndDeletedFalse(id: Long): Menu?
}
