package Sample.BentouReservation.service

import Sample.BentouReservation.dto.*
import Sample.BentouReservation.entity.Order
import Sample.BentouReservation.repository.EmployeeRepository
import Sample.BentouReservation.repository.OrderRepository
import Sample.BentouReservation.repository.VendorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

/**
 * 月次集計サービス
 */
@Service
@Transactional(readOnly = true)
class MonthlyBillingService(
    private val orderRepository: OrderRepository,
    private val employeeRepository: EmployeeRepository,
    private val vendorRepository: VendorRepository
) {

    /**
     * 社員別月次集計を取得
     */
    fun getEmployeeBilling(employeeId: Long, yearMonth: YearMonth): MonthlyEmployeeBillingResponse {
        val employee = employeeRepository.findByIdAndDeletedFalse(employeeId)
            ?: throw IllegalArgumentException("社員が見つかりません。ID: $employeeId")

        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()

        val orders = orderRepository.findMonthlyOrdersByEmployee(employeeId, startDate, endDate)

        val orderDetails = orders.map { order ->
            MonthlyOrderDetail(
                orderDate = order.orderDate,
                menuName = order.menu.name,
                vendorName = order.menu.vendor.name,
                price = order.priceAtOrder
            )
        }

        val totalAmount = orders.fold(BigDecimal.ZERO) { acc, order -> acc + order.priceAtOrder }

        return MonthlyEmployeeBillingResponse(
            employeeId = employee.id!!,
            employeeName = employee.name,
            employeeNumber = employee.employeeNumber,
            department = employee.department,
            year = yearMonth.year,
            month = yearMonth.monthValue,
            orders = orderDetails,
            totalAmount = totalAmount
        )
    }

    /**
     * 全社員の月次集計を取得
     */
    fun getAllEmployeeBilling(yearMonth: YearMonth): List<MonthlyEmployeeBillingResponse> {
        val employees = employeeRepository.findByDeletedFalse()
        return employees.map { employee ->
            getEmployeeBilling(employee.id!!, yearMonth)
        }.filter { it.orders.isNotEmpty() }
    }

    /**
     * 業者別月次集計を取得
     */
    fun getVendorBilling(vendorId: Long, yearMonth: YearMonth): MonthlyVendorBillingResponse {
        val vendor = vendorRepository.findByIdAndDeletedFalse(vendorId)
            ?: throw IllegalArgumentException("業者が見つかりません。ID: $vendorId")

        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()

        val orders = orderRepository.findMonthlyOrdersByVendor(vendorId, startDate, endDate)

        // メニューごとにグループ化
        val menuGrouped = orders.groupBy { it.menu.name }

        val menuOrders = menuGrouped.map { (menuName, menuOrders) ->
            val orderDetails = menuOrders.map { order ->
                VendorOrderDetail(
                    orderDate = order.orderDate,
                    employeeName = order.employee.name,
                    price = order.priceAtOrder
                )
            }

            VendorMenuOrder(
                menuName = menuName,
                orders = orderDetails
            )
        }

        val totalAmount = orders.fold(BigDecimal.ZERO) { acc, order -> acc + order.priceAtOrder }

        return MonthlyVendorBillingResponse(
            vendorId = vendor.id!!,
            vendorName = vendor.name,
            year = yearMonth.year,
            month = yearMonth.monthValue,
            menuOrders = menuOrders,
            totalAmount = totalAmount
        )
    }

    /**
     * 全業者の月次集計を取得
     */
    fun getAllVendorBilling(yearMonth: YearMonth): List<MonthlyVendorBillingResponse> {
        val vendors = vendorRepository.findByDeletedFalse()
        return vendors.map { vendor ->
            getVendorBilling(vendor.id!!, yearMonth)
        }.filter { it.menuOrders.isNotEmpty() }
    }

    /**
     * 月次集計サマリーを取得
     */
    fun getMonthlySummary(yearMonth: YearMonth): MonthlySummaryResponse {
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()

        val allOrders = orderRepository.findByOrderDateBetween(startDate, endDate)

        val totalOrders = allOrders.size
        val totalAmount = allOrders.fold(BigDecimal.ZERO) { acc, order -> acc + order.priceAtOrder }

        // 業者別サマリー
        val vendorMap = allOrders.groupBy { it.menu.vendor.id!! }
        val vendorSummaries = vendorMap.map { (vendorId, orders) ->
            VendorMonthlySummary(
                vendorId = vendorId,
                vendorName = orders.first().menu.vendor.name,
                orderCount = orders.size,
                totalAmount = orders.fold(BigDecimal.ZERO) { acc, order -> acc + order.priceAtOrder }
            )
        }

        // 社員別サマリー
        val employeeMap = allOrders.groupBy { it.employee.id!! }
        val employeeSummaries = employeeMap.map { (employeeId, orders) ->
            EmployeeMonthlySummary(
                employeeId = employeeId,
                employeeName = orders.first().employee.name,
                orderCount = orders.size,
                totalAmount = orders.fold(BigDecimal.ZERO) { acc, order -> acc + order.priceAtOrder }
            )
        }

        return MonthlySummaryResponse(
            year = yearMonth.year,
            month = yearMonth.monthValue,
            totalOrders = totalOrders,
            totalAmount = totalAmount,
            vendorSummaries = vendorSummaries,
            employeeSummaries = employeeSummaries
        )
    }
}
