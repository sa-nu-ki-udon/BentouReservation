package Sample.BentouReservation.dto

import Sample.BentouReservation.entity.ClosingDateType
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * 締め日設定レスポンスDTO
 */
data class BillingClosingDateResponse(
    val id: Long,
    val type: ClosingDateType,
    val targetId: Long?,
    val targetName: String?,  // 業者名または社員名
    val closingDay: Int,
    val description: String?
)

/**
 * 締め日設定作成リクエストDTO
 */
data class BillingClosingDateCreateRequest(
    @field:NotNull(message = "締め日タイプは必須です")
    val type: ClosingDateType,

    val targetId: Long? = null,

    @field:NotNull(message = "締め日は必須です")
    @field:Min(value = 1, message = "締め日は1以上を入力してください")
    @field:Max(value = 31, message = "締め日は31以下を入力してください")
    val closingDay: Int,

    @field:Size(max = 255, message = "説明は255文字以内で入力してください")
    val description: String? = null
)

/**
 * 締め日設定更新リクエストDTO
 */
data class BillingClosingDateUpdateRequest(
    @field:NotNull(message = "締め日は必須です")
    @field:Min(value = 1, message = "締め日は1以上を入力してください")
    @field:Max(value = 31, message = "締め日は31以下を入力してください")
    val closingDay: Int,

    @field:Size(max = 255, message = "説明は255文字以内で入力してください")
    val description: String? = null
)
