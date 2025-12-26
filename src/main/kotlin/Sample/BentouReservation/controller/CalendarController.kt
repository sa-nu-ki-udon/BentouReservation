package Sample.BentouReservation.controller

import Sample.BentouReservation.dto.CalendarBulkSettingRequest
import Sample.BentouReservation.dto.CalendarSettingRequest
import Sample.BentouReservation.service.CalendarSettingService
import Sample.BentouReservation.service.MenuSetService
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.time.LocalDate
import java.time.YearMonth

/**
 * カレンダー設定コントローラー
 */
@Controller
@RequestMapping("/calendar")
class CalendarController(
    private val calendarSettingService: CalendarSettingService,
    private val menuSetService: MenuSetService
) {

    /**
     * カレンダー表示画面
     */
    @GetMapping
    fun calendar(
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) month: Int?,
        model: Model
    ): String {
        val yearMonth = if (year != null && month != null) {
            YearMonth.of(year, month)
        } else {
            YearMonth.now()
        }

        val calendarMonth = calendarSettingService.findByYearMonth(yearMonth)
        val menuSets = menuSetService.findAll()

        model.addAttribute("calendarMonth", calendarMonth)
        model.addAttribute("menuSets", menuSets)
        model.addAttribute("currentYear", yearMonth.year)
        model.addAttribute("currentMonth", yearMonth.monthValue)
        model.addAttribute("previousMonth", yearMonth.minusMonths(1))
        model.addAttribute("nextMonth", yearMonth.plusMonths(1))

        return "calendar/index"
    }

    /**
     * カレンダー設定画面（単日設定）
     */
    @GetMapping("/settings/new")
    fun newSettingForm(
        @RequestParam date: LocalDate,
        model: Model
    ): String {
        val menuSets = menuSetService.findAll()
        val existing = calendarSettingService.findByDate(date)

        val settingRequest = existing?.let {
            CalendarSettingRequest(
                date = it.date,
                menuSetId = it.menuSetId,
                isHoliday = it.isHoliday,
                memo = it.memo
            )
        } ?: CalendarSettingRequest(
            date = date,
            menuSetId = null,
            isHoliday = false,
            memo = null
        )

        model.addAttribute("menuSets", menuSets)
        model.addAttribute("calendarSettingRequest", settingRequest)
        model.addAttribute("isEdit", existing != null)
        return "calendar/setting-form"
    }

    /**
     * カレンダー設定保存処理
     */
    @PostMapping("/settings")
    fun saveSetting(
        @Valid @ModelAttribute calendarSettingRequest: CalendarSettingRequest,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        if (bindingResult.hasErrors()) {
            val menuSets = menuSetService.findAll()
            model.addAttribute("menuSets", menuSets)
            return "calendar/setting-form"
        }

        try {
            calendarSettingService.createOrUpdate(calendarSettingRequest)
            redirectAttributes.addFlashAttribute("successMessage", "カレンダー設定を保存しました")

            val yearMonth = YearMonth.from(calendarSettingRequest.date)
            return "redirect:/calendar?year=${yearMonth.year}&month=${yearMonth.monthValue}"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
            return "redirect:/calendar/settings/new?date=${calendarSettingRequest.date}"
        }
    }

    /**
     * カレンダー一括設定画面
     */
    @GetMapping("/settings/bulk")
    fun bulkSettingForm(
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) month: Int?,
        model: Model
    ): String {
        val menuSets = menuSetService.findAll()
        val yearMonth = if (year != null && month != null) {
            YearMonth.of(year, month)
        } else {
            YearMonth.now()
        }

        val bulkSettingRequest = CalendarBulkSettingRequest(
            startDate = yearMonth.atDay(1),
            endDate = yearMonth.atEndOfMonth(),
            menuSetId = null,
            isHoliday = false,
            memo = null
        )

        model.addAttribute("menuSets", menuSets)
        model.addAttribute("calendarBulkSettingRequest", bulkSettingRequest)
        return "calendar/bulk-setting-form"
    }

    /**
     * カレンダー一括設定保存処理
     */
    @PostMapping("/settings/bulk")
    fun saveBulkSetting(
        @Valid @ModelAttribute calendarBulkSettingRequest: CalendarBulkSettingRequest,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        if (bindingResult.hasErrors()) {
            val menuSets = menuSetService.findAll()
            model.addAttribute("menuSets", menuSets)
            return "calendar/bulk-setting-form"
        }

        try {
            calendarSettingService.bulkCreateOrUpdate(calendarBulkSettingRequest)
            redirectAttributes.addFlashAttribute("successMessage", "カレンダー設定を一括保存しました")

            val yearMonth = YearMonth.from(calendarBulkSettingRequest.startDate)
            return "redirect:/calendar?year=${yearMonth.year}&month=${yearMonth.monthValue}"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
            return "redirect:/calendar/settings/bulk"
        }
    }

    /**
     * カレンダー設定削除処理
     */
    @PostMapping("/settings/delete")
    fun deleteSetting(
        @RequestParam date: LocalDate,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            calendarSettingService.delete(date)
            redirectAttributes.addFlashAttribute("successMessage", "カレンダー設定を削除しました")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
        }

        val yearMonth = YearMonth.from(date)
        return "redirect:/calendar?year=${yearMonth.year}&month=${yearMonth.monthValue}"
    }
}
