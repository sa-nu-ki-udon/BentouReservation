package Sample.BentouReservation.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * カレンダー設定レスポンスDTO
 */
data class CalendarSettingResponse(
    val id: Long,
    val date: LocalDate,
    val menuSetId: Long?,
    val menuSetName: String?,
    val isHoliday: Boolean,
    val memo: String?,
    val isOrderable: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * カレンダー設定作成・更新リクエストDTO
 */
data class CalendarSettingRequest(
    @field:NotNull(message = "日付は必須です")
    val date: LocalDate,

    val menuSetId: Long? = null,

    @field:NotNull(message = "休日フラグは必須です")
    val isHoliday: Boolean = false,

    @field:Size(max = 255, message = "メモは255文字以内で入力してください")
    val memo: String? = null
)

/**
 * カレンダー月次レスポンスDTO（月表示用）
 */
data class CalendarMonthResponse(
    val year: Int,
    val month: Int,
    val days: List<CalendarDayResponse>
)

/**
 * カレンダー日次レスポンスDTO
 */
data class CalendarDayResponse(
    val date: LocalDate,
    val menuSetId: Long?,
    val menuSetName: String?,
    val isHoliday: Boolean,
    val isOrderable: Boolean,
    val hasOrder: Boolean = false,  // ログインユーザーが注文済みかどうか
    val memo: String?
)

/**
 * カレンダー一括設定リクエストDTO
 */
data class CalendarBulkSettingRequest(
    @field:NotNull(message = "開始日は必須です")
    val startDate: LocalDate,

    @field:NotNull(message = "終了日は必須です")
    val endDate: LocalDate,

    val menuSetId: Long? = null,

    @field:NotNull(message = "休日フラグは必須です")
    val isHoliday: Boolean = false,

    @field:Size(max = 255, message = "メモは255文字以内で入力してください")
    val memo: String? = null
)
