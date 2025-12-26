package Sample.BentouReservation.controller

import Sample.BentouReservation.service.DailyAggregationService
import Sample.BentouReservation.service.VendorService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

/**
 * 日次集計コントローラー
 */
@Controller
@RequestMapping("/daily-aggregation")
class DailyAggregationController(
    private val dailyAggregationService: DailyAggregationService,
    private val vendorService: VendorService
) {

    /**
     * 日次集計画面
     */
    @GetMapping
    fun index(
        @RequestParam(required = false) date: LocalDate?,
        model: Model
    ): String {
        val targetDate = date ?: LocalDate.now()
        val aggregation = dailyAggregationService.aggregateByDate(targetDate)

        model.addAttribute("aggregation", aggregation)
        model.addAttribute("targetDate", targetDate)
        model.addAttribute("previousDate", targetDate.minusDays(1))
        model.addAttribute("nextDate", targetDate.plusDays(1))

        return "daily-aggregation/index"
    }

    /**
     * 業者別日次集計印刷画面
     */
    @GetMapping("/print")
    fun print(
        @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false) vendorId: Long?,
        model: Model
    ): String {
        val targetDate = date ?: LocalDate.now()

        if (vendorId != null) {
            // 特定業者のみ
            val vendor = vendorService.findById(vendorId)
            val vendorAggregation = dailyAggregationService.getVendorAggregationForPrint(targetDate, vendorId)

            model.addAttribute("targetDate", targetDate)
            model.addAttribute("vendor", vendor)
            model.addAttribute("vendorAggregation", vendorAggregation)

            return "daily-aggregation/print-vendor"
        } else {
            // 全業者
            val aggregations = dailyAggregationService.getAllVendorAggregationsForPrint(targetDate)

            model.addAttribute("targetDate", targetDate)
            model.addAttribute("aggregations", aggregations)

            return "daily-aggregation/print-all"
        }
    }

    /**
     * 業者選択画面（印刷前）
     */
    @GetMapping("/select-vendor")
    fun selectVendor(
        @RequestParam(required = false) date: LocalDate?,
        model: Model
    ): String {
        val targetDate = date ?: LocalDate.now()
        val vendors = vendorService.findAll()

        model.addAttribute("targetDate", targetDate)
        model.addAttribute("vendors", vendors)

        return "daily-aggregation/select-vendor"
    }
}
