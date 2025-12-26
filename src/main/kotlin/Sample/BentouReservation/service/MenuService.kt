package Sample.BentouReservation.service

import Sample.BentouReservation.dto.*
import Sample.BentouReservation.entity.Menu
import Sample.BentouReservation.repository.MenuRepository
import Sample.BentouReservation.repository.VendorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * メニューサービス
 */
@Service
@Transactional
class MenuService(
    private val menuRepository: MenuRepository,
    private val vendorRepository: VendorRepository
) {

    /**
     * 全メニューを取得（削除されていないもののみ）
     */
    @Transactional(readOnly = true)
    fun findAll(): List<MenuResponse> {
        return menuRepository.findByDeletedFalse()
            .map { it.toResponse() }
    }

    /**
     * IDでメニューを取得
     */
    @Transactional(readOnly = true)
    fun findById(id: Long): MenuResponse {
        val menu = menuRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("メニューが見つかりません。ID: $id")
        return menu.toResponse()
    }

    /**
     * 業者IDでメニューを取得
     */
    @Transactional(readOnly = true)
    fun findByVendorId(vendorId: Long): List<MenuResponse> {
        return menuRepository.findByVendorIdAndDeletedFalse(vendorId)
            .map { it.toResponse() }
    }

    /**
     * メニュー名で検索
     */
    @Transactional(readOnly = true)
    fun searchByName(name: String): List<MenuResponse> {
        return menuRepository.findByNameContainingAndDeletedFalse(name)
            .map { it.toResponse() }
    }

    /**
     * 指定日時点で有効なメニューを取得（業者別）
     */
    @Transactional(readOnly = true)
    fun findCurrentMenusByVendor(vendorId: Long, date: LocalDate = LocalDate.now()): List<MenuResponse> {
        return menuRepository.findCurrentMenusByVendorAndDate(vendorId, date)
            .map { it.toResponse() }
    }

    /**
     * 指定日時点で有効なメニューを全件取得
     */
    @Transactional(readOnly = true)
    fun findCurrentMenus(date: LocalDate = LocalDate.now()): List<MenuResponse> {
        return menuRepository.findCurrentMenusByDate(date)
            .map { it.toResponse() }
    }

    /**
     * メニューを作成
     */
    fun create(request: MenuCreateRequest): MenuResponse {
        val vendor = vendorRepository.findByIdAndDeletedFalse(request.vendorId)
            ?: throw IllegalArgumentException("業者が見つかりません。ID: ${request.vendorId}")

        val menu = Menu(
            vendor = vendor,
            name = request.name,
            price = request.price,
            effectiveFrom = request.effectiveFrom,
            description = request.description
        )

        val saved = menuRepository.save(menu)
        return saved.toResponse()
    }

    /**
     * メニューを更新（名前と説明のみ）
     */
    fun update(id: Long, request: MenuUpdateRequest): MenuResponse {
        val menu = menuRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("メニューが見つかりません。ID: $id")

        menu.name = request.name
        menu.description = request.description

        val updated = menuRepository.save(menu)
        return updated.toResponse()
    }

    /**
     * メニューの価格を変更（新しいレコードとして保存）
     */
    fun changePrice(id: Long, request: MenuPriceChangeRequest): MenuResponse {
        val originalMenu = menuRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("メニューが見つかりません。ID: $id")

        // 新しい価格のメニューレコードを作成
        val newMenu = Menu(
            vendor = originalMenu.vendor,
            name = originalMenu.name,
            price = request.newPrice,
            effectiveFrom = request.effectiveFrom,
            description = originalMenu.description
        )

        val saved = menuRepository.save(newMenu)
        return saved.toResponse()
    }

    /**
     * メニューを論理削除
     */
    fun delete(id: Long) {
        val menu = menuRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("メニューが見つかりません。ID: $id")

        menu.deleted = true
        menuRepository.save(menu)
    }

    /**
     * MenuエンティティをMenuResponseに変換
     */
    private fun Menu.toResponse(): MenuResponse {
        return MenuResponse(
            id = this.id!!,
            vendorId = this.vendor.id!!,
            vendorName = this.vendor.name,
            name = this.name,
            price = this.price,
            effectiveFrom = this.effectiveFrom,
            description = this.description,
            deleted = this.deleted,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}
