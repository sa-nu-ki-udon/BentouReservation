package Sample.BentouReservation.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * 社員レスポンスDTO
 */
data class EmployeeResponse(
    val id: Long,
    val employeeNumber: String,
    val name: String,
    val department: String?,
    val deleted: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * 社員作成リクエストDTO
 */
data class EmployeeCreateRequest(
    @field:NotBlank(message = "社員番号は必須です")
    @field:Size(max = 50, message = "社員番号は50文字以内で入力してください")
    val employeeNumber: String,

    @field:NotBlank(message = "氏名は必須です")
    @field:Size(max = 100, message = "氏名は100文字以内で入力してください")
    val name: String,

    @field:Size(max = 100, message = "部署は100文字以内で入力してください")
    val department: String? = null
)

/**
 * 社員更新リクエストDTO
 */
data class EmployeeUpdateRequest(
    @field:NotBlank(message = "氏名は必須です")
    @field:Size(max = 100, message = "氏名は100文字以内で入力してください")
    val name: String,

    @field:Size(max = 100, message = "部署は100文字以内で入力してください")
    val department: String? = null
)

/**
 * 社員簡易レスポンスDTO（一覧表示用）
 */
data class EmployeeSummaryResponse(
    val id: Long,
    val employeeNumber: String,
    val name: String,
    val department: String?
)
