package Sample.BentouReservation.controller

import Sample.BentouReservation.dto.MenuSetCreateRequest
import Sample.BentouReservation.dto.MenuSetUpdateRequest
import Sample.BentouReservation.service.MenuService
import Sample.BentouReservation.service.MenuSetService
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

/**
 * メニューセット管理コントローラー
 */
@Controller
@RequestMapping("/menu-sets")
class MenuSetController(
    private val menuSetService: MenuSetService,
    private val menuService: MenuService
) {

    /**
     * メニューセット一覧画面
     */
    @GetMapping
    fun list(
        @RequestParam(required = false) name: String?,
        model: Model
    ): String {
        val menuSets = if (name.isNullOrBlank()) {
            menuSetService.findAll()
        } else {
            menuSetService.searchByName(name)
        }

        model.addAttribute("menuSets", menuSets)
        model.addAttribute("searchName", name ?: "")
        return "menu-sets/list"
    }

    /**
     * メニューセット詳細画面
     */
    @GetMapping("/{id}")
    fun detail(@PathVariable id: Long, model: Model): String {
        val menuSet = menuSetService.findById(id)
        model.addAttribute("menuSet", menuSet)
        return "menu-sets/detail"
    }

    /**
     * メニューセット新規登録画面
     */
    @GetMapping("/new")
    fun newForm(model: Model): String {
        val menus = menuService.findCurrentMenus()
        val menuSetCreateRequest = MenuSetCreateRequest(
            name = "",
            description = null,
            menuIds = emptyList()
        )

        model.addAttribute("menus", menus)
        model.addAttribute("menuSetCreateRequest", menuSetCreateRequest)
        return "menu-sets/new"
    }

    /**
     * メニューセット新規登録処理
     */
    @PostMapping
    fun create(
        @Valid @ModelAttribute menuSetCreateRequest: MenuSetCreateRequest,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        if (bindingResult.hasErrors()) {
            val menus = menuService.findCurrentMenus()
            model.addAttribute("menus", menus)
            return "menu-sets/new"
        }

        try {
            val created = menuSetService.create(menuSetCreateRequest)
            redirectAttributes.addFlashAttribute("successMessage", "メニューセットを登録しました")
            return "redirect:/menu-sets/${created.id}"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
            return "redirect:/menu-sets/new"
        }
    }

    /**
     * メニューセット編集画面
     */
    @GetMapping("/{id}/edit")
    fun editForm(@PathVariable id: Long, model: Model): String {
        val menuSet = menuSetService.findById(id)
        val menus = menuService.findCurrentMenus()
        val updateRequest = MenuSetUpdateRequest(
            name = menuSet.name,
            description = menuSet.description,
            menuIds = menuSet.menus.map { it.menuId }
        )

        model.addAttribute("menuSet", menuSet)
        model.addAttribute("menus", menus)
        model.addAttribute("menuSetUpdateRequest", updateRequest)
        return "menu-sets/edit"
    }

    /**
     * メニューセット更新処理
     */
    @PostMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute menuSetUpdateRequest: MenuSetUpdateRequest,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        if (bindingResult.hasErrors()) {
            val menuSet = menuSetService.findById(id)
            val menus = menuService.findCurrentMenus()
            model.addAttribute("menuSet", menuSet)
            model.addAttribute("menus", menus)
            return "menu-sets/edit"
        }

        try {
            menuSetService.update(id, menuSetUpdateRequest)
            redirectAttributes.addFlashAttribute("successMessage", "メニューセット情報を更新しました")
            return "redirect:/menu-sets/$id"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
            return "redirect:/menu-sets/$id/edit"
        }
    }

    /**
     * メニューセット削除処理
     */
    @PostMapping("/{id}/delete")
    fun delete(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            menuSetService.delete(id)
            redirectAttributes.addFlashAttribute("successMessage", "メニューセットを削除しました")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
        }
        return "redirect:/menu-sets"
    }
}
