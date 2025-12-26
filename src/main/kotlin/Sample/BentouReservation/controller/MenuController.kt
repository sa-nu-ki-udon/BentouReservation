package Sample.BentouReservation.controller

import Sample.BentouReservation.dto.MenuCreateRequest
import Sample.BentouReservation.dto.MenuPriceChangeRequest
import Sample.BentouReservation.dto.MenuUpdateRequest
import Sample.BentouReservation.service.MenuService
import Sample.BentouReservation.service.VendorService
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.time.LocalDate

/**
 * メニュー管理コントローラー
 */
@Controller
@RequestMapping("/menus")
class MenuController(
    private val menuService: MenuService,
    private val vendorService: VendorService
) {

    /**
     * メニュー一覧画面
     */
    @GetMapping
    fun list(
        @RequestParam(required = false) vendorId: Long?,
        @RequestParam(required = false) name: String?,
        model: Model
    ): String {
        val menus = when {
            vendorId != null -> menuService.findByVendorId(vendorId)
            !name.isNullOrBlank() -> menuService.searchByName(name)
            else -> menuService.findAll()
        }

        val vendors = vendorService.findAll()

        model.addAttribute("menus", menus)
        model.addAttribute("vendors", vendors)
        model.addAttribute("selectedVendorId", vendorId)
        model.addAttribute("searchName", name ?: "")
        return "menus/list"
    }

    /**
     * メニュー詳細画面
     */
    @GetMapping("/{id}")
    fun detail(@PathVariable id: Long, model: Model): String {
        val menu = menuService.findById(id)
        model.addAttribute("menu", menu)
        return "menus/detail"
    }

    /**
     * メニュー新規登録画面
     */
    @GetMapping("/new")
    fun newForm(
        @RequestParam(required = false) vendorId: Long?,
        model: Model
    ): String {
        val vendors = vendorService.findAll()
        val menuCreateRequest = MenuCreateRequest(
            vendorId = vendorId ?: vendors.firstOrNull()?.id ?: 0L,
            name = "",
            price = java.math.BigDecimal.ZERO,
            effectiveFrom = LocalDate.now(),
            description = null
        )

        model.addAttribute("vendors", vendors)
        model.addAttribute("menuCreateRequest", menuCreateRequest)
        return "menus/new"
    }

    /**
     * メニュー新規登録処理
     */
    @PostMapping
    fun create(
        @Valid @ModelAttribute menuCreateRequest: MenuCreateRequest,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        if (bindingResult.hasErrors()) {
            val vendors = vendorService.findAll()
            model.addAttribute("vendors", vendors)
            return "menus/new"
        }

        try {
            val created = menuService.create(menuCreateRequest)
            redirectAttributes.addFlashAttribute("successMessage", "メニューを登録しました")
            return "redirect:/menus/${created.id}"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
            return "redirect:/menus/new"
        }
    }

    /**
     * メニュー編集画面
     */
    @GetMapping("/{id}/edit")
    fun editForm(@PathVariable id: Long, model: Model): String {
        val menu = menuService.findById(id)
        val updateRequest = MenuUpdateRequest(
            name = menu.name,
            description = menu.description
        )

        model.addAttribute("menu", menu)
        model.addAttribute("menuUpdateRequest", updateRequest)
        return "menus/edit"
    }

    /**
     * メニュー更新処理
     */
    @PostMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute menuUpdateRequest: MenuUpdateRequest,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        if (bindingResult.hasErrors()) {
            val menu = menuService.findById(id)
            model.addAttribute("menu", menu)
            return "menus/edit"
        }

        try {
            menuService.update(id, menuUpdateRequest)
            redirectAttributes.addFlashAttribute("successMessage", "メニュー情報を更新しました")
            return "redirect:/menus/$id"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
            return "redirect:/menus/$id/edit"
        }
    }

    /**
     * メニュー価格変更画面
     */
    @GetMapping("/{id}/change-price")
    fun changePriceForm(@PathVariable id: Long, model: Model): String {
        val menu = menuService.findById(id)
        val priceChangeRequest = MenuPriceChangeRequest(
            newPrice = menu.price,
            effectiveFrom = LocalDate.now()
        )

        model.addAttribute("menu", menu)
        model.addAttribute("menuPriceChangeRequest", priceChangeRequest)
        return "menus/change-price"
    }

    /**
     * メニュー価格変更処理
     */
    @PostMapping("/{id}/change-price")
    fun changePrice(
        @PathVariable id: Long,
        @Valid @ModelAttribute menuPriceChangeRequest: MenuPriceChangeRequest,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        if (bindingResult.hasErrors()) {
            val menu = menuService.findById(id)
            model.addAttribute("menu", menu)
            return "menus/change-price"
        }

        try {
            val newMenu = menuService.changePrice(id, menuPriceChangeRequest)
            redirectAttributes.addFlashAttribute("successMessage", "価格を変更しました")
            return "redirect:/menus/${newMenu.id}"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
            return "redirect:/menus/$id/change-price"
        }
    }

    /**
     * メニュー削除処理
     */
    @PostMapping("/{id}/delete")
    fun delete(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            menuService.delete(id)
            redirectAttributes.addFlashAttribute("successMessage", "メニューを削除しました")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
        }
        return "redirect:/menus"
    }
}
