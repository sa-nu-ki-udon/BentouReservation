package Sample.BentouReservation.service

import Sample.BentouReservation.dto.*
import Sample.BentouReservation.entity.BillingClosingDate
import Sample.BentouReservation.entity.ClosingDateType
import Sample.BentouReservation.repository.BillingClosingDateRepository
import Sample.BentouReservation.repository.EmployeeRepository
import Sample.BentouReservation.repository.VendorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 締め日設定サービス
 */
@Service
@Transactional
class BillingClosingDateService(
    private val billingClosingDateRepository: BillingClosingDateRepository,
    private val vendorRepository: VendorRepository,
    private val employeeRepository: EmployeeRepository
) {

    /**
     * 全締め日設定を取得
     */
    @Transactional(readOnly = true)
    fun findAll(): List<BillingClosingDateResponse> {
        return billingClosingDateRepository.findAll()
            .map { it.toResponse() }
    }

    /**
     * タイプ別に締め日設定を取得
     */
    @Transactional(readOnly = true)
    fun findByType(type: ClosingDateType): List<BillingClosingDateResponse> {
        return billingClosingDateRepository.findByTypeOrderByClosingDayAsc(type)
            .map { it.toResponse() }
    }

    /**
     * 業者別締め日設定を全件取得
     */
    @Transactional(readOnly = true)
    fun findVendorClosingDates(): List<BillingClosingDateResponse> {
        return findByType(ClosingDateType.VENDOR)
    }

    /**
     * 個人別締め日設定を全件取得
     */
    @Transactional(readOnly = true)
    fun findEmployeeClosingDates(): List<BillingClosingDateResponse> {
        return findByType(ClosingDateType.EMPLOYEE)
    }

    /**
     * IDで締め日設定を取得
     */
    @Transactional(readOnly = true)
    fun findById(id: Long): BillingClosingDateResponse {
        val closingDate = billingClosingDateRepository.findById(id).orElseThrow {
            IllegalArgumentException("締め日設定が見つかりません。ID: $id")
        }
        return closingDate.toResponse()
    }

    /**
     * タイプとターゲットIDで締め日設定を取得
     */
    @Transactional(readOnly = true)
    fun findByTypeAndTargetId(type: ClosingDateType, targetId: Long): BillingClosingDateResponse? {
        val closingDate = billingClosingDateRepository.findByTypeAndTargetId(type, targetId)
        return closingDate?.toResponse()
    }

    /**
     * 締め日設定を作成
     */
    fun create(request: BillingClosingDateCreateRequest): BillingClosingDateResponse {
        // ターゲットの存在確認
        val targetName = validateAndGetTargetName(request.type, request.targetId)

        // 既に同じタイプとターゲットIDの設定があるかチェック
        val existing = request.targetId?.let {
            billingClosingDateRepository.findByTypeAndTargetId(request.type, it)
        }
        if (existing != null) {
            throw IllegalArgumentException("既に設定が存在します")
        }

        val closingDate = BillingClosingDate(
            type = request.type,
            targetId = request.targetId,
            closingDay = request.closingDay,
            description = request.description
        )

        val saved = billingClosingDateRepository.save(closingDate)
        return saved.toResponse()
    }

    /**
     * 締め日設定を更新
     */
    fun update(id: Long, request: BillingClosingDateUpdateRequest): BillingClosingDateResponse {
        val closingDate = billingClosingDateRepository.findById(id).orElseThrow {
            IllegalArgumentException("締め日設定が見つかりません。ID: $id")
        }

        closingDate.closingDay = request.closingDay
        closingDate.description = request.description

        val updated = billingClosingDateRepository.save(closingDate)
        return updated.toResponse()
    }

    /**
     * 締め日設定を削除
     */
    fun delete(id: Long) {
        val closingDate = billingClosingDateRepository.findById(id).orElseThrow {
            IllegalArgumentException("締め日設定が見つかりません。ID: $id")
        }

        billingClosingDateRepository.delete(closingDate)
    }

    /**
     * ターゲットの存在を確認し、名前を取得
     */
    private fun validateAndGetTargetName(type: ClosingDateType, targetId: Long?): String? {
        return when (type) {
            ClosingDateType.VENDOR -> {
                if (targetId == null) {
                    throw IllegalArgumentException("業者別締め日設定には業者IDが必要です")
                }
                val vendor = vendorRepository.findByIdAndDeletedFalse(targetId)
                    ?: throw IllegalArgumentException("業者が見つかりません。ID: $targetId")
                vendor.name
            }
            ClosingDateType.EMPLOYEE -> {
                // 個人請求の場合はターゲットIDは不要（全社員共通）
                null
            }
        }
    }

    /**
     * BillingClosingDateエンティティをBillingClosingDateResponseに変換
     */
    private fun BillingClosingDate.toResponse(): BillingClosingDateResponse {
        val targetName = when (this.type) {
            ClosingDateType.VENDOR -> {
                this.targetId?.let { id ->
                    vendorRepository.findById(id).orElse(null)?.name
                }
            }
            ClosingDateType.EMPLOYEE -> {
                this.targetId?.let { id ->
                    employeeRepository.findById(id).orElse(null)?.name
                }
            }
        }

        return BillingClosingDateResponse(
            id = this.id!!,
            type = this.type,
            targetId = this.targetId,
            targetName = targetName,
            closingDay = this.closingDay,
            description = this.description
        )
    }
}
