package Sample.BentouReservation.controller

import Sample.BentouReservation.dto.VendorCreateRequest
import Sample.BentouReservation.dto.VendorUpdateRequest
import Sample.BentouReservation.service.VendorService
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

/**
 * 業者管理コントローラー
 */
@Controller
@RequestMapping("/vendors")
class VendorController(
    private val vendorService: VendorService
) {

    /**
     * 業者一覧画面
     */
    @GetMapping
    fun list(
        @RequestParam(required = false) name: String?,
        model: Model
    ): String {
        val vendors = if (name.isNullOrBlank()) {
            vendorService.findAll()
        } else {
            vendorService.searchByName(name)
        }

        model.addAttribute("vendors", vendors)
        model.addAttribute("searchName", name ?: "")
        return "vendors/list"
    }

    /**
     * 業者詳細画面
     */
    @GetMapping("/{id}")
    fun detail(@PathVariable id: Long, model: Model): String {
        val vendor = vendorService.findById(id)
        model.addAttribute("vendor", vendor)
        return "vendors/detail"
    }

    /**
     * 業者新規登録画面
     */
    @GetMapping("/new")
    fun newForm(model: Model): String {
        model.addAttribute("vendorCreateRequest", VendorCreateRequest("", null))
        return "vendors/new"
    }

    /**
     * 業者新規登録処理
     */
    @PostMapping
    fun create(
        @Valid @ModelAttribute vendorCreateRequest: VendorCreateRequest,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes
    ): String {
        if (bindingResult.hasErrors()) {
            return "vendors/new"
        }

        try {
            val created = vendorService.create(vendorCreateRequest)
            redirectAttributes.addFlashAttribute("successMessage", "業者を登録しました")
            return "redirect:/vendors/${created.id}"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
            return "redirect:/vendors/new"
        }
    }

    /**
     * 業者編集画面
     */
    @GetMapping("/{id}/edit")
    fun editForm(@PathVariable id: Long, model: Model): String {
        val vendor = vendorService.findById(id)
        val updateRequest = VendorUpdateRequest(
            name = vendor.name,
            contactInfo = vendor.contactInfo
        )
        model.addAttribute("vendor", vendor)
        model.addAttribute("vendorUpdateRequest", updateRequest)
        return "vendors/edit"
    }

    /**
     * 業者更新処理
     */
    @PostMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute vendorUpdateRequest: VendorUpdateRequest,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        if (bindingResult.hasErrors()) {
            val vendor = vendorService.findById(id)
            model.addAttribute("vendor", vendor)
            return "vendors/edit"
        }

        try {
            vendorService.update(id, vendorUpdateRequest)
            redirectAttributes.addFlashAttribute("successMessage", "業者情報を更新しました")
            return "redirect:/vendors/$id"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
            return "redirect:/vendors/$id/edit"
        }
    }

    /**
     * 業者削除処理
     */
    @PostMapping("/{id}/delete")
    fun delete(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            vendorService.delete(id)
            redirectAttributes.addFlashAttribute("successMessage", "業者を削除しました")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
        }
        return "redirect:/vendors"
    }
}
