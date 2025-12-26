package Sample.BentouReservation.repository

import Sample.BentouReservation.entity.MenuSetItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * メニューセット詳細リポジトリ
 */
@Repository
interface MenuSetItemRepository : JpaRepository<MenuSetItem, Long> {

    /**
     * メニューセットIDで検索
     */
    fun findByMenuSetId(menuSetId: Long): List<MenuSetItem>

    /**
     * メニューセットIDで削除
     */
    fun deleteByMenuSetId(menuSetId: Long)

    /**
     * メニューセットIDとメニューIDで検索
     */
    fun findByMenuSetIdAndMenuId(menuSetId: Long, menuId: Long): MenuSetItem?
}
