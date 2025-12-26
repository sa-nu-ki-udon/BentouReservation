package Sample.BentouReservation.service

import Sample.BentouReservation.dto.*
import Sample.BentouReservation.entity.Vendor
import Sample.BentouReservation.repository.VendorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 業者サービス
 */
@Service
@Transactional
class VendorService(
    private val vendorRepository: VendorRepository
) {

    /**
     * 全業者を取得（削除されていないもののみ）
     */
    @Transactional(readOnly = true)
    fun findAll(): List<VendorResponse> {
        return vendorRepository.findByDeletedFalse()
            .map { it.toResponse() }
    }

    /**
     * IDで業者を取得
     */
    @Transactional(readOnly = true)
    fun findById(id: Long): VendorResponse {
        val vendor = vendorRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("業者が見つかりません。ID: $id")
        return vendor.toResponse()
    }

    /**
     * 業者名で検索
     */
    @Transactional(readOnly = true)
    fun searchByName(name: String): List<VendorResponse> {
        return vendorRepository.findByNameContainingAndDeletedFalse(name)
            .map { it.toResponse() }
    }

    /**
     * 業者を作成
     */
    fun create(request: VendorCreateRequest): VendorResponse {
        val vendor = Vendor(
            name = request.name,
            contactInfo = request.contactInfo
        )
        val saved = vendorRepository.save(vendor)
        return saved.toResponse()
    }

    /**
     * 業者を更新
     */
    fun update(id: Long, request: VendorUpdateRequest): VendorResponse {
        val vendor = vendorRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("業者が見つかりません。ID: $id")

        vendor.name = request.name
        vendor.contactInfo = request.contactInfo

        val updated = vendorRepository.save(vendor)
        return updated.toResponse()
    }

    /**
     * 業者を論理削除
     */
    fun delete(id: Long) {
        val vendor = vendorRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("業者が見つかりません。ID: $id")

        vendor.deleted = true
        vendorRepository.save(vendor)
    }

    /**
     * VendorエンティティをVendorResponseに変換
     */
    private fun Vendor.toResponse(): VendorResponse {
        return VendorResponse(
            id = this.id!!,
            name = this.name,
            contactInfo = this.contactInfo,
            deleted = this.deleted,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}
