package Sample.BentouReservation.controller

import Sample.BentouReservation.dto.EmployeeCreateRequest
import Sample.BentouReservation.dto.EmployeeUpdateRequest
import Sample.BentouReservation.service.EmployeeService
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

/**
 * 社員管理コントローラー
 */
@Controller
@RequestMapping("/employees")
class EmployeeController(
    private val employeeService: EmployeeService
) {

    /**
     * 社員一覧画面
     */
    @GetMapping
    fun list(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) department: String?,
        model: Model
    ): String {
        val employees = when {
            !name.isNullOrBlank() -> employeeService.searchByName(name)
            !department.isNullOrBlank() -> employeeService.findByDepartment(department)
            else -> employeeService.findAll()
        }

        model.addAttribute("employees", employees)
        model.addAttribute("searchName", name ?: "")
        model.addAttribute("searchDepartment", department ?: "")
        return "employees/list"
    }

    /**
     * 社員詳細画面
     */
    @GetMapping("/{id}")
    fun detail(@PathVariable id: Long, model: Model): String {
        val employee = employeeService.findById(id)
        model.addAttribute("employee", employee)
        return "employees/detail"
    }

    /**
     * 社員新規登録画面
     */
    @GetMapping("/new")
    fun newForm(model: Model): String {
        model.addAttribute("employeeCreateRequest", EmployeeCreateRequest("", "", null))
        return "employees/new"
    }

    /**
     * 社員新規登録処理
     */
    @PostMapping
    fun create(
        @Valid @ModelAttribute employeeCreateRequest: EmployeeCreateRequest,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes
    ): String {
        if (bindingResult.hasErrors()) {
            return "employees/new"
        }

        try {
            val created = employeeService.create(employeeCreateRequest)
            redirectAttributes.addFlashAttribute("successMessage", "社員を登録しました")
            return "redirect:/employees/${created.id}"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
            return "redirect:/employees/new"
        }
    }

    /**
     * 社員編集画面
     */
    @GetMapping("/{id}/edit")
    fun editForm(@PathVariable id: Long, model: Model): String {
        val employee = employeeService.findById(id)
        val updateRequest = EmployeeUpdateRequest(
            name = employee.name,
            department = employee.department
        )

        model.addAttribute("employee", employee)
        model.addAttribute("employeeUpdateRequest", updateRequest)
        return "employees/edit"
    }

    /**
     * 社員更新処理
     */
    @PostMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute employeeUpdateRequest: EmployeeUpdateRequest,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        if (bindingResult.hasErrors()) {
            val employee = employeeService.findById(id)
            model.addAttribute("employee", employee)
            return "employees/edit"
        }

        try {
            employeeService.update(id, employeeUpdateRequest)
            redirectAttributes.addFlashAttribute("successMessage", "社員情報を更新しました")
            return "redirect:/employees/$id"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
            return "redirect:/employees/$id/edit"
        }
    }

    /**
     * 社員削除処理
     */
    @PostMapping("/{id}/delete")
    fun delete(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            employeeService.delete(id)
            redirectAttributes.addFlashAttribute("successMessage", "社員を削除しました")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
        }
        return "redirect:/employees"
    }
}
