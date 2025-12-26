package Sample.BentouReservation.dto

import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 注文レスポンスDTO
 */
data class OrderResponse(
    val id: Long,
    val employeeId: Long,
    val employeeName: String,
    val orderDate: LocalDate,
    val menuId: Long,
    val menuName: String,
    val vendorName: String,
    val priceAtOrder: BigDecimal,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * 注文作成リクエストDTO
 */
data class OrderCreateRequest(
    @field:NotNull(message = "社員IDは必須です")
    val employeeId: Long,

    @field:NotNull(message = "注文日は必須です")
    val orderDate: LocalDate,

    @field:NotNull(message = "メニューIDは必須です")
    val menuId: Long
)

/**
 * 注文更新リクエストDTO（メニュー変更）
 */
data class OrderUpdateRequest(
    @field:NotNull(message = "メニューIDは必須です")
    val menuId: Long
)

/**
 * 注文簡易レスポンスDTO（カレンダー表示用）
 */
data class OrderSummaryResponse(
    val orderDate: LocalDate,
    val menuId: Long,
    val menuName: String,
    val vendorName: String,
    val price: BigDecimal
)

/**
 * 注文履歴レスポンスDTO（個人の注文履歴表示用）
 */
data class OrderHistoryResponse(
    val id: Long,
    val orderDate: LocalDate,
    val menuName: String,
    val vendorName: String,
    val priceAtOrder: BigDecimal,
    val createdAt: LocalDateTime
)
