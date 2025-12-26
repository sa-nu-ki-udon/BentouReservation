package Sample.BentouReservation.dto

import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * メニューレスポンスDTO
 */
data class MenuResponse(
    val id: Long,
    val vendorId: Long,
    val vendorName: String,
    val name: String,
    val price: BigDecimal,
    val effectiveFrom: LocalDate,
    val description: String?,
    val deleted: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * メニュー作成リクエストDTO
 */
data class MenuCreateRequest(
    @field:NotNull(message = "業者IDは必須です")
    val vendorId: Long,

    @field:NotBlank(message = "メニュー名は必須です")
    @field:Size(max = 100, message = "メニュー名は100文字以内で入力してください")
    val name: String,

    @field:NotNull(message = "価格は必須です")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "価格は0より大きい値を入力してください")
    @field:Digits(integer = 10, fraction = 2, message = "価格は整数10桁、小数2桁以内で入力してください")
    val price: BigDecimal,

    @field:NotNull(message = "適用開始日は必須です")
    val effectiveFrom: LocalDate,

    @field:Size(max = 255, message = "説明は255文字以内で入力してください")
    val description: String? = null
)

/**
 * メニュー更新リクエストDTO（価格変更）
 */
data class MenuUpdateRequest(
    @field:NotBlank(message = "メニュー名は必須です")
    @field:Size(max = 100, message = "メニュー名は100文字以内で入力してください")
    val name: String,

    @field:Size(max = 255, message = "説明は255文字以内で入力してください")
    val description: String? = null
)

/**
 * メニュー価格変更リクエストDTO
 */
data class MenuPriceChangeRequest(
    @field:NotNull(message = "新価格は必須です")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "価格は0より大きい値を入力してください")
    @field:Digits(integer = 10, fraction = 2, message = "価格は整数10桁、小数2桁以内で入力してください")
    val newPrice: BigDecimal,

    @field:NotNull(message = "適用開始日は必須です")
    val effectiveFrom: LocalDate
)
