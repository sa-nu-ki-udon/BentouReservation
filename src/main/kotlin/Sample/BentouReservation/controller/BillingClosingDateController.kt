package Sample.BentouReservation.controller

import Sample.BentouReservation.dto.BillingClosingDateCreateRequest
import Sample.BentouReservation.dto.BillingClosingDateUpdateRequest
import Sample.BentouReservation.entity.ClosingDateType
import Sample.BentouReservation.service.BillingClosingDateService
import Sample.BentouReservation.service.EmployeeService
import Sample.BentouReservation.service.VendorService
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

/**
 * 締め日設定コントローラー
 */
@Controller
@RequestMapping("/billing-closing-dates")
class BillingClosingDateController(
    private val billingClosingDateService: BillingClosingDateService,
    private val vendorService: VendorService,
    private val employeeService: EmployeeService
) {

    /**
     * 締め日設定一覧画面
     */
    @GetMapping
    fun list(model: Model): String {
        val vendorClosingDates = billingClosingDateService.findVendorClosingDates()
        val employeeClosingDates = billingClosingDateService.findEmployeeClosingDates()

        model.addAttribute("vendorClosingDates", vendorClosingDates)
        model.addAttribute("employeeClosingDates", employeeClosingDates)

        return "billing-closing-dates/list"
    }

    /**
     * 締め日設定詳細画面
     */
    @GetMapping("/{id}")
    fun detail(@PathVariable id: Long, model: Model): String {
        val closingDate = billingClosingDateService.findById(id)
        model.addAttribute("closingDate", closingDate)
        return "billing-closing-dates/detail"
    }

    /**
     * 締め日設定新規登録画面
     */
    @GetMapping("/new")
    fun newForm(
        @RequestParam(required = false) type: ClosingDateType?,
        model: Model
    ): String {
        val vendors = vendorService.findAll()
        val closingDateCreateRequest = BillingClosingDateCreateRequest(
            type = type ?: ClosingDateType.VENDOR,
            targetId = null,
            closingDay = 20,
            description = null
        )

        model.addAttribute("vendors", vendors)
        model.addAttribute("billingClosingDateCreateRequest", closingDateCreateRequest)
        model.addAttribute("closingDateTypes", ClosingDateType.values())

        return "billing-closing-dates/new"
    }

    /**
     * 締め日設定新規登録処理
     */
    @PostMapping
    fun create(
        @Valid @ModelAttribute billingClosingDateCreateRequest: BillingClosingDateCreateRequest,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        if (bindingResult.hasErrors()) {
            val vendors = vendorService.findAll()
            model.addAttribute("vendors", vendors)
            model.addAttribute("closingDateTypes", ClosingDateType.values())
            return "billing-closing-dates/new"
        }

        try {
            val created = billingClosingDateService.create(billingClosingDateCreateRequest)
            redirectAttributes.addFlashAttribute("successMessage", "締め日設定を登録しました")
            return "redirect:/billing-closing-dates/${created.id}"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
            return "redirect:/billing-closing-dates/new"
        }
    }

    /**
     * 締め日設定編集画面
     */
    @GetMapping("/{id}/edit")
    fun editForm(@PathVariable id: Long, model: Model): String {
        val closingDate = billingClosingDateService.findById(id)
        val updateRequest = BillingClosingDateUpdateRequest(
            closingDay = closingDate.closingDay,
            description = closingDate.description
        )

        model.addAttribute("closingDate", closingDate)
        model.addAttribute("billingClosingDateUpdateRequest", updateRequest)

        return "billing-closing-dates/edit"
    }

    /**
     * 締め日設定更新処理
     */
    @PostMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute billingClosingDateUpdateRequest: BillingClosingDateUpdateRequest,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        if (bindingResult.hasErrors()) {
            val closingDate = billingClosingDateService.findById(id)
            model.addAttribute("closingDate", closingDate)
            return "billing-closing-dates/edit"
        }

        try {
            billingClosingDateService.update(id, billingClosingDateUpdateRequest)
            redirectAttributes.addFlashAttribute("successMessage", "締め日設定を更新しました")
            return "redirect:/billing-closing-dates/$id"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
            return "redirect:/billing-closing-dates/$id/edit"
        }
    }

    /**
     * 締め日設定削除処理
     */
    @PostMapping("/{id}/delete")
    fun delete(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            billingClosingDateService.delete(id)
            redirectAttributes.addFlashAttribute("successMessage", "締め日設定を削除しました")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
        }
        return "redirect:/billing-closing-dates"
    }
}
