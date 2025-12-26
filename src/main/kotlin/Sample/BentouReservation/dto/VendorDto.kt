package Sample.BentouReservation.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * 業者レスポンスDTO
 */
data class VendorResponse(
    val id: Long,
    val name: String,
    val contactInfo: String?,
    val deleted: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * 業者作成リクエストDTO
 */
data class VendorCreateRequest(
    @field:NotBlank(message = "業者名は必須です")
    @field:Size(max = 100, message = "業者名は100文字以内で入力してください")
    val name: String,

    @field:Size(max = 255, message = "連絡先は255文字以内で入力してください")
    val contactInfo: String? = null
)

/**
 * 業者更新リクエストDTO
 */
data class VendorUpdateRequest(
    @field:NotBlank(message = "業者名は必須です")
    @field:Size(max = 100, message = "業者名は100文字以内で入力してください")
    val name: String,

    @field:Size(max = 255, message = "連絡先は255文字以内で入力してください")
    val contactInfo: String? = null
)
