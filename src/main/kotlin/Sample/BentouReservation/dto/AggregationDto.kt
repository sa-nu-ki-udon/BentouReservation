package Sample.BentouReservation.dto

import java.math.BigDecimal
import java.time.LocalDate

/**
 * 日次集計レスポンスDTO
 */
data class DailyAggregationResponse(
    val orderDate: LocalDate,
    val vendors: List<VendorAggregation>
)

/**
 * 業者別集計
 */
data class VendorAggregation(
    val vendorId: Long,
    val vendorName: String,
    val menus: List<MenuAggregation>,
    val totalCount: Int,
    val totalPrice: BigDecimal
)

/**
 * メニュー別集計
 */
data class MenuAggregation(
    val menuId: Long,
    val menuName: String,
    val orderCount: Int,
    val priceAtOrder: BigDecimal,
    val totalPrice: BigDecimal
)

/**
 * 月次集計（個人別）レスポンスDTO
 */
data class MonthlyEmployeeBillingResponse(
    val employeeId: Long,
    val employeeName: String,
    val employeeNumber: String,
    val department: String?,
    val year: Int,
    val month: Int,
    val orders: List<MonthlyOrderDetail>,
    val totalAmount: BigDecimal
)

/**
 * 月次注文詳細
 */
data class MonthlyOrderDetail(
    val orderDate: LocalDate,
    val menuName: String,
    val vendorName: String,
    val price: BigDecimal
)

/**
 * 月次集計（業者別）レスポンスDTO
 */
data class MonthlyVendorBillingResponse(
    val vendorId: Long,
    val vendorName: String,
    val year: Int,
    val month: Int,
    val menuOrders: List<VendorMenuOrder>,
    val totalAmount: BigDecimal
)

/**
 * 業者のメニュー別注文詳細
 */
data class VendorMenuOrder(
    val menuName: String,
    val orders: List<VendorOrderDetail>
)

/**
 * 業者の注文詳細
 */
data class VendorOrderDetail(
    val orderDate: LocalDate,
    val employeeName: String,
    val price: BigDecimal
)

/**
 * 月次集計サマリー（全体）
 */
data class MonthlySummaryResponse(
    val year: Int,
    val month: Int,
    val totalOrders: Int,
    val totalAmount: BigDecimal,
    val vendorSummaries: List<VendorMonthlySummary>,
    val employeeSummaries: List<EmployeeMonthlySummary>
)

/**
 * 業者別月次サマリー
 */
data class VendorMonthlySummary(
    val vendorId: Long,
    val vendorName: String,
    val orderCount: Int,
    val totalAmount: BigDecimal
)

/**
 * 社員別月次サマリー
 */
data class EmployeeMonthlySummary(
    val employeeId: Long,
    val employeeName: String,
    val orderCount: Int,
    val totalAmount: BigDecimal
)
