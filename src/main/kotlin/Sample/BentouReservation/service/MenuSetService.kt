package Sample.BentouReservation.service

import Sample.BentouReservation.dto.*
import Sample.BentouReservation.entity.MenuSet
import Sample.BentouReservation.entity.MenuSetItem
import Sample.BentouReservation.repository.MenuRepository
import Sample.BentouReservation.repository.MenuSetItemRepository
import Sample.BentouReservation.repository.MenuSetRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * メニューセットサービス
 */
@Service
@Transactional
class MenuSetService(
    private val menuSetRepository: MenuSetRepository,
    private val menuRepository: MenuRepository,
    private val menuSetItemRepository: MenuSetItemRepository
) {

    /**
     * 全メニューセットを取得（削除されていないもののみ）
     */
    @Transactional(readOnly = true)
    fun findAll(): List<MenuSetSummaryResponse> {
        return menuSetRepository.findByDeletedFalse()
            .map { it.toSummaryResponse() }
    }

    /**
     * 全メニューセットを詳細情報付きで取得
     */
    @Transactional(readOnly = true)
    fun findAllWithMenus(): List<MenuSetResponse> {
        return menuSetRepository.findAllWithMenus()
            .map { it.toResponse() }
    }

    /**
     * IDでメニューセットを取得
     */
    @Transactional(readOnly = true)
    fun findById(id: Long): MenuSetResponse {
        val menuSet = menuSetRepository.findByIdWithMenus(id)
            ?: throw IllegalArgumentException("メニューセットが見つかりません。ID: $id")
        return menuSet.toResponse()
    }

    /**
     * メニューセット名で検索
     */
    @Transactional(readOnly = true)
    fun searchByName(name: String): List<MenuSetSummaryResponse> {
        return menuSetRepository.findByNameContainingAndDeletedFalse(name)
            .map { it.toSummaryResponse() }
    }

    /**
     * メニューセットを作成
     */
    fun create(request: MenuSetCreateRequest): MenuSetResponse {
        // メニューが存在するか確認
        val menus = request.menuIds.map { menuId ->
            menuRepository.findByIdAndDeletedFalse(menuId)
                ?: throw IllegalArgumentException("メニューが見つかりません。ID: $menuId")
        }

        val menuSet = MenuSet(
            name = request.name,
            description = request.description
        )

        val savedMenuSet = menuSetRepository.save(menuSet)

        // メニューセットアイテムを作成
        menus.forEachIndexed { index, menu ->
            val item = MenuSetItem(
                menuSet = savedMenuSet,
                menu = menu,
                displayOrder = index
            )
            menuSetItemRepository.save(item)
        }

        return findById(savedMenuSet.id!!)
    }

    /**
     * メニューセットを更新
     */
    fun update(id: Long, request: MenuSetUpdateRequest): MenuSetResponse {
        val menuSet = menuSetRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("メニューセットが見つかりません。ID: $id")

        // メニューが存在するか確認
        val menus = request.menuIds.map { menuId ->
            menuRepository.findByIdAndDeletedFalse(menuId)
                ?: throw IllegalArgumentException("メニューが見つかりません。ID: $menuId")
        }

        menuSet.name = request.name
        menuSet.description = request.description

        menuSetRepository.save(menuSet)

        // 既存のメニューセットアイテムを削除
        menuSetItemRepository.deleteByMenuSetId(id)

        // 新しいメニューセットアイテムを作成
        menus.forEachIndexed { index, menu ->
            val item = MenuSetItem(
                menuSet = menuSet,
                menu = menu,
                displayOrder = index
            )
            menuSetItemRepository.save(item)
        }

        return findById(id)
    }

    /**
     * メニューセットを論理削除
     */
    fun delete(id: Long) {
        val menuSet = menuSetRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("メニューセットが見つかりません。ID: $id")

        menuSet.deleted = true
        menuSetRepository.save(menuSet)
    }

    /**
     * MenuSetエンティティをMenuSetResponseに変換
     */
    private fun MenuSet.toResponse(): MenuSetResponse {
        val menuResponses = this.menuSetItems
            .sortedBy { it.displayOrder }
            .map { item ->
                MenuInSetResponse(
                    menuId = item.menu.id!!,
                    menuName = item.menu.name,
                    vendorName = item.menu.vendor.name,
                    price = item.menu.price,
                    displayOrder = item.displayOrder
                )
            }

        return MenuSetResponse(
            id = this.id!!,
            name = this.name,
            description = this.description,
            menus = menuResponses,
            deleted = this.deleted,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    /**
     * MenuSetエンティティをMenuSetSummaryResponseに変換
     */
    private fun MenuSet.toSummaryResponse(): MenuSetSummaryResponse {
        return MenuSetSummaryResponse(
            id = this.id!!,
            name = this.name,
            description = this.description,
            menuCount = this.menuSetItems.size,
            deleted = this.deleted
        )
    }
}
