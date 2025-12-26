package Sample.BentouReservation.controller

import Sample.BentouReservation.dto.OrderCreateRequest
import Sample.BentouReservation.dto.OrderUpdateRequest
import Sample.BentouReservation.service.CalendarSettingService
import Sample.BentouReservation.service.EmployeeService
import Sample.BentouReservation.service.OrderService
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.time.LocalDate
import java.time.YearMonth

/**
 * 注文管理コントローラー
 */
@Controller
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService,
    private val employeeService: EmployeeService,
    private val calendarSettingService: CalendarSettingService
) {

    /**
     * 注文カレンダー画面（社員選択）
     */
    @GetMapping
    fun index(
        @RequestParam(required = false) employeeId: Long?,
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) month: Int?,
        model: Model
    ): String {
        val employees = employeeService.findAllSummary()

        // 社員が選択されていない場合は選択画面
        if (employeeId == null) {
            model.addAttribute("employees", employees)
            return "orders/select-employee"
        }

        val yearMonth = if (year != null && month != null) {
            YearMonth.of(year, month)
        } else {
            YearMonth.now()
        }

        val employee = employeeService.findById(employeeId)
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()

        // カレンダー設定を取得
        val calendarSettings = calendarSettingService.findByDateRange(startDate, endDate)

        // 注文履歴を取得
        val orders = orderService.findByEmployeeAndDateRange(employeeId, startDate, endDate)

        model.addAttribute("employee", employee)
        model.addAttribute("employees", employees)
        model.addAttribute("calendarSettings", calendarSettings)
        model.addAttribute("orders", orders)
        model.addAttribute("currentYear", yearMonth.year)
        model.addAttribute("currentMonth", yearMonth.monthValue)
        model.addAttribute("previousMonth", yearMonth.minusMonths(1))
        model.addAttribute("nextMonth", yearMonth.plusMonths(1))

        return "orders/calendar"
    }

    /**
     * 注文入力画面
     */
    @GetMapping("/new")
    fun newOrderForm(
        @RequestParam employeeId: Long,
        @RequestParam date: LocalDate,
        model: Model
    ): String {
        val employee = employeeService.findById(employeeId)
        val calendarSetting = calendarSettingService.findByDate(date)
            ?: throw IllegalArgumentException("カレンダー設定が見つかりません")

        if (!calendarSetting.isOrderable) {
            throw IllegalArgumentException("この日は注文できません")
        }

        // 既存の注文があるかチェック
        val existingOrder = orderService.findByEmployeeAndDate(employeeId, date)
        if (existingOrder != null) {
            // 既存の注文がある場合は編集画面にリダイレクト
            return "redirect:/orders/edit?employeeId=$employeeId&date=$date"
        }

        val orderCreateRequest = OrderCreateRequest(
            employeeId = employeeId,
            orderDate = date,
            menuId = 0L
        )

        model.addAttribute("employee", employee)
        model.addAttribute("calendarSetting", calendarSetting)
        model.addAttribute("orderCreateRequest", orderCreateRequest)
        return "orders/new"
    }

    /**
     * 注文作成処理
     */
    @PostMapping
    fun createOrder(
        @Valid @ModelAttribute orderCreateRequest: OrderCreateRequest,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        if (bindingResult.hasErrors()) {
            val employee = employeeService.findById(orderCreateRequest.employeeId)
            val calendarSetting = calendarSettingService.findByDate(orderCreateRequest.orderDate)
            model.addAttribute("employee", employee)
            model.addAttribute("calendarSetting", calendarSetting)
            return "orders/new"
        }

        try {
            orderService.create(orderCreateRequest)
            redirectAttributes.addFlashAttribute("successMessage", "注文を登録しました")

            val yearMonth = YearMonth.from(orderCreateRequest.orderDate)
            return "redirect:/orders?employeeId=${orderCreateRequest.employeeId}&year=${yearMonth.year}&month=${yearMonth.monthValue}"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
            return "redirect:/orders/new?employeeId=${orderCreateRequest.employeeId}&date=${orderCreateRequest.orderDate}"
        }
    }

    /**
     * 注文編集画面
     */
    @GetMapping("/edit")
    fun editOrderForm(
        @RequestParam employeeId: Long,
        @RequestParam date: LocalDate,
        model: Model
    ): String {
        val employee = employeeService.findById(employeeId)
        val order = orderService.findByEmployeeAndDate(employeeId, date)
            ?: throw IllegalArgumentException("注文が見つかりません")

        val calendarSetting = calendarSettingService.findByDate(date)
            ?: throw IllegalArgumentException("カレンダー設定が見つかりません")

        val orderUpdateRequest = OrderUpdateRequest(
            menuId = order.menuId
        )

        model.addAttribute("employee", employee)
        model.addAttribute("order", order)
        model.addAttribute("calendarSetting", calendarSetting)
        model.addAttribute("orderUpdateRequest", orderUpdateRequest)
        return "orders/edit"
    }

    /**
     * 注文更新処理
     */
    @PostMapping("/update")
    fun updateOrder(
        @RequestParam employeeId: Long,
        @RequestParam date: LocalDate,
        @Valid @ModelAttribute orderUpdateRequest: OrderUpdateRequest,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        if (bindingResult.hasErrors()) {
            val employee = employeeService.findById(employeeId)
            val order = orderService.findByEmployeeAndDate(employeeId, date)
            val calendarSetting = calendarSettingService.findByDate(date)
            model.addAttribute("employee", employee)
            model.addAttribute("order", order)
            model.addAttribute("calendarSetting", calendarSetting)
            return "orders/edit"
        }

        try {
            orderService.update(employeeId, date, orderUpdateRequest)
            redirectAttributes.addFlashAttribute("successMessage", "注文を変更しました")

            val yearMonth = YearMonth.from(date)
            return "redirect:/orders?employeeId=$employeeId&year=${yearMonth.year}&month=${yearMonth.monthValue}"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
            return "redirect:/orders/edit?employeeId=$employeeId&date=$date"
        }
    }

    /**
     * 注文削除処理
     */
    @PostMapping("/delete")
    fun deleteOrder(
        @RequestParam employeeId: Long,
        @RequestParam date: LocalDate,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            orderService.delete(employeeId, date)
            redirectAttributes.addFlashAttribute("successMessage", "注文を削除しました")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
        }

        val yearMonth = YearMonth.from(date)
        return "redirect:/orders?employeeId=$employeeId&year=${yearMonth.year}&month=${yearMonth.monthValue}"
    }

    /**
     * 注文履歴画面
     */
    @GetMapping("/history")
    fun history(
        @RequestParam employeeId: Long,
        model: Model
    ): String {
        val employee = employeeService.findById(employeeId)
        val orders = orderService.findByEmployee(employeeId)

        model.addAttribute("employee", employee)
        model.addAttribute("orders", orders)
        return "orders/history"
    }
}
