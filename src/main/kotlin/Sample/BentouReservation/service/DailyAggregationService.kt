package Sample.BentouReservation.service

import Sample.BentouReservation.dto.*
import Sample.BentouReservation.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

/**
 * 日次集計サービス
 */
@Service
@Transactional(readOnly = true)
class DailyAggregationService(
    private val orderRepository: OrderRepository
) {

    /**
     * 指定日の日次集計を取得
     */
    fun aggregateByDate(orderDate: LocalDate): DailyAggregationResponse {
        val results = orderRepository.findDailyAggregationByDate(orderDate)

        // 業者ごとにグループ化
        val vendorMap = results.groupBy { it.vendorId }

        val vendorAggregations = vendorMap.map { (vendorId, vendorResults) ->
            val vendorName = vendorResults.first().vendorName

            val menuAggregations = vendorResults.map { result ->
                MenuAggregation(
                    menuId = result.menuId,
                    menuName = result.menuName,
                    orderCount = result.orderCount.toInt(),
                    priceAtOrder = result.totalPrice.divide(BigDecimal(result.orderCount)),
                    totalPrice = result.totalPrice
                )
            }

            val totalCount = menuAggregations.sumOf { it.orderCount }
            val totalPrice = menuAggregations.fold(BigDecimal.ZERO) { acc, menu -> acc + menu.totalPrice }

            VendorAggregation(
                vendorId = vendorId,
                vendorName = vendorName,
                menus = menuAggregations,
                totalCount = totalCount,
                totalPrice = totalPrice
            )
        }

        return DailyAggregationResponse(
            orderDate = orderDate,
            vendors = vendorAggregations
        )
    }

    /**
     * 期間の日次集計を取得
     */
    fun aggregateByDateRange(startDate: LocalDate, endDate: LocalDate): List<DailyAggregationResponse> {
        val results = mutableListOf<DailyAggregationResponse>()
        var currentDate = startDate

        while (!currentDate.isAfter(endDate)) {
            val aggregation = aggregateByDate(currentDate)
            if (aggregation.vendors.isNotEmpty()) {
                results.add(aggregation)
            }
            currentDate = currentDate.plusDays(1)
        }

        return results
    }

    /**
     * 指定日の業者別集計を取得（印刷用）
     */
    fun getVendorAggregationForPrint(orderDate: LocalDate, vendorId: Long): VendorAggregation? {
        val aggregation = aggregateByDate(orderDate)
        return aggregation.vendors.find { it.vendorId == vendorId }
    }

    /**
     * 指定日の全業者の集計を取得（印刷用）
     */
    fun getAllVendorAggregationsForPrint(orderDate: LocalDate): List<VendorAggregation> {
        val aggregation = aggregateByDate(orderDate)
        return aggregation.vendors
    }
}
