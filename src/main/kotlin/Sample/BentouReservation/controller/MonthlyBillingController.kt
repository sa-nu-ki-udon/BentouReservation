package Sample.BentouReservation.controller

import Sample.BentouReservation.service.EmployeeService
import Sample.BentouReservation.service.MonthlyBillingService
import Sample.BentouReservation.service.VendorService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.YearMonth

/**
 * 月次集計コントローラー
 */
@Controller
@RequestMapping("/monthly-billing")
class MonthlyBillingController(
    private val monthlyBillingService: MonthlyBillingService,
    private val employeeService: EmployeeService,
    private val vendorService: VendorService
) {

    /**
     * 月次集計トップ画面
     */
    @GetMapping
    fun index(
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) month: Int?,
        model: Model
    ): String {
        val yearMonth = if (year != null && month != null) {
            YearMonth.of(year, month)
        } else {
            YearMonth.now()
        }

        val summary = monthlyBillingService.getMonthlySummary(yearMonth)

        model.addAttribute("summary", summary)
        model.addAttribute("yearMonth", yearMonth)
        model.addAttribute("previousMonth", yearMonth.minusMonths(1))
        model.addAttribute("nextMonth", yearMonth.plusMonths(1))

        return "monthly-billing/index"
    }

    /**
     * 社員別月次集計画面
     */
    @GetMapping("/employee")
    fun employeeBilling(
        @RequestParam(required = false) employeeId: Long?,
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) month: Int?,
        model: Model
    ): String {
        val yearMonth = if (year != null && month != null) {
            YearMonth.of(year, month)
        } else {
            YearMonth.now()
        }

        if (employeeId == null) {
            // 社員選択画面
            val employees = employeeService.findAllSummary()
            model.addAttribute("employees", employees)
            model.addAttribute("yearMonth", yearMonth)
            return "monthly-billing/select-employee"
        }

        // 社員別集計表示
        val billing = monthlyBillingService.getEmployeeBilling(employeeId, yearMonth)
        val employees = employeeService.findAllSummary()

        model.addAttribute("billing", billing)
        model.addAttribute("employees", employees)
        model.addAttribute("yearMonth", yearMonth)
        model.addAttribute("previousMonth", yearMonth.minusMonths(1))
        model.addAttribute("nextMonth", yearMonth.plusMonths(1))

        return "monthly-billing/employee"
    }

    /**
     * 全社員月次集計画面
     */
    @GetMapping("/all-employees")
    fun allEmployeesBilling(
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) month: Int?,
        model: Model
    ): String {
        val yearMonth = if (year != null && month != null) {
            YearMonth.of(year, month)
        } else {
            YearMonth.now()
        }

        val billings = monthlyBillingService.getAllEmployeeBilling(yearMonth)

        model.addAttribute("billings", billings)
        model.addAttribute("yearMonth", yearMonth)
        model.addAttribute("previousMonth", yearMonth.minusMonths(1))
        model.addAttribute("nextMonth", yearMonth.plusMonths(1))

        return "monthly-billing/all-employees"
    }

    /**
     * 業者別月次集計画面
     */
    @GetMapping("/vendor")
    fun vendorBilling(
        @RequestParam(required = false) vendorId: Long?,
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) month: Int?,
        model: Model
    ): String {
        val yearMonth = if (year != null && month != null) {
            YearMonth.of(year, month)
        } else {
            YearMonth.now()
        }

        if (vendorId == null) {
            // 業者選択画面
            val vendors = vendorService.findAll()
            model.addAttribute("vendors", vendors)
            model.addAttribute("yearMonth", yearMonth)
            return "monthly-billing/select-vendor"
        }

        // 業者別集計表示
        val billing = monthlyBillingService.getVendorBilling(vendorId, yearMonth)
        val vendors = vendorService.findAll()

        model.addAttribute("billing", billing)
        model.addAttribute("vendors", vendors)
        model.addAttribute("yearMonth", yearMonth)
        model.addAttribute("previousMonth", yearMonth.minusMonths(1))
        model.addAttribute("nextMonth", yearMonth.plusMonths(1))

        return "monthly-billing/vendor"
    }

    /**
     * 全業者月次集計画面
     */
    @GetMapping("/all-vendors")
    fun allVendorsBilling(
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) month: Int?,
        model: Model
    ): String {
        val yearMonth = if (year != null && month != null) {
            YearMonth.of(year, month)
        } else {
            YearMonth.now()
        }

        val billings = monthlyBillingService.getAllVendorBilling(yearMonth)

        model.addAttribute("billings", billings)
        model.addAttribute("yearMonth", yearMonth)
        model.addAttribute("previousMonth", yearMonth.minusMonths(1))
        model.addAttribute("nextMonth", yearMonth.plusMonths(1))

        return "monthly-billing/all-vendors"
    }

    /**
     * 社員別月次集計印刷画面
     */
    @GetMapping("/employee/print")
    fun printEmployeeBilling(
        @RequestParam employeeId: Long,
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) month: Int?,
        model: Model
    ): String {
        val yearMonth = if (year != null && month != null) {
            YearMonth.of(year, month)
        } else {
            YearMonth.now()
        }

        val billing = monthlyBillingService.getEmployeeBilling(employeeId, yearMonth)

        model.addAttribute("billing", billing)
        model.addAttribute("yearMonth", yearMonth)

        return "monthly-billing/print-employee"
    }

    /**
     * 業者別月次集計印刷画面
     */
    @GetMapping("/vendor/print")
    fun printVendorBilling(
        @RequestParam vendorId: Long,
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) month: Int?,
        model: Model
    ): String {
        val yearMonth = if (year != null && month != null) {
            YearMonth.of(year, month)
        } else {
            YearMonth.now()
        }

        val billing = monthlyBillingService.getVendorBilling(vendorId, yearMonth)

        model.addAttribute("billing", billing)
        model.addAttribute("yearMonth", yearMonth)

        return "monthly-billing/print-vendor"
    }
}
