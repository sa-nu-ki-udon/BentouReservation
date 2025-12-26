package Sample.BentouReservation.repository

import Sample.BentouReservation.entity.MenuSet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * メニューセットリポジトリ
 */
@Repository
interface MenuSetRepository : JpaRepository<MenuSet, Long> {

    /**
     * 削除されていないメニューセットを全件取得
     */
    fun findByDeletedFalse(): List<MenuSet>

    /**
     * メニューセット名で検索（削除されていないもののみ）
     */
    fun findByNameContainingAndDeletedFalse(name: String): List<MenuSet>

    /**
     * IDで検索（削除されていないもののみ）
     */
    fun findByIdAndDeletedFalse(id: Long): MenuSet?

    /**
     * メニューセットとそのメニューを一括取得（N+1問題対策）
     */
    @Query("""
        SELECT DISTINCT ms FROM MenuSet ms
        LEFT JOIN FETCH ms.menuSetItems msi
        LEFT JOIN FETCH msi.menu
        WHERE ms.deleted = false
        ORDER BY ms.name
    """)
    fun findAllWithMenus(): List<MenuSet>

    /**
     * IDでメニューセットとそのメニューを一括取得
     */
    @Query("""
        SELECT ms FROM MenuSet ms
        LEFT JOIN FETCH ms.menuSetItems msi
        LEFT JOIN FETCH msi.menu
        WHERE ms.id = :id AND ms.deleted = false
    """)
    fun findByIdWithMenus(id: Long): MenuSet?
}
