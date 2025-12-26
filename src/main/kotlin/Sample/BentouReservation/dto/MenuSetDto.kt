package Sample.BentouReservation.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * メニューセットレスポンスDTO
 */
data class MenuSetResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val menus: List<MenuInSetResponse>,
    val deleted: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * メニューセット内のメニュー情報
 */
data class MenuInSetResponse(
    val menuId: Long,
    val menuName: String,
    val vendorName: String,
    val price: BigDecimal,
    val displayOrder: Int
)

/**
 * メニューセット作成リクエストDTO
 */
data class MenuSetCreateRequest(
    @field:NotBlank(message = "メニューセット名は必須です")
    @field:Size(max = 100, message = "メニューセット名は100文字以内で入力してください")
    val name: String,

    @field:Size(max = 255, message = "説明は255文字以内で入力してください")
    val description: String? = null,

    @field:NotEmpty(message = "メニューは1つ以上選択してください")
    val menuIds: List<Long>
)

/**
 * メニューセット更新リクエストDTO
 */
data class MenuSetUpdateRequest(
    @field:NotBlank(message = "メニューセット名は必須です")
    @field:Size(max = 100, message = "メニューセット名は100文字以内で入力してください")
    val name: String,

    @field:Size(max = 255, message = "説明は255文字以内で入力してください")
    val description: String? = null,

    @field:NotEmpty(message = "メニューは1つ以上選択してください")
    val menuIds: List<Long>
)

/**
 * メニューセット簡易レスポンスDTO（一覧表示用）
 */
data class MenuSetSummaryResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val menuCount: Int,
    val deleted: Boolean
)
