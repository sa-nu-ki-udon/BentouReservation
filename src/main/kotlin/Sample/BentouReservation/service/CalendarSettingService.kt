package Sample.BentouReservation.service

import Sample.BentouReservation.dto.*
import Sample.BentouReservation.entity.CalendarSetting
import Sample.BentouReservation.repository.CalendarSettingRepository
import Sample.BentouReservation.repository.MenuSetRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.YearMonth

/**
 * カレンダー設定サービス
 */
@Service
@Transactional
class CalendarSettingService(
    private val calendarSettingRepository: CalendarSettingRepository,
    private val menuSetRepository: MenuSetRepository
) {

    /**
     * 日付でカレンダー設定を取得
     */
    @Transactional(readOnly = true)
    fun findByDate(date: LocalDate): CalendarSettingResponse? {
        val setting = calendarSettingRepository.findByDate(date)
        return setting?.toResponse()
    }

    /**
     * 期間でカレンダー設定を取得
     */
    @Transactional(readOnly = true)
    fun findByDateRange(startDate: LocalDate, endDate: LocalDate): List<CalendarSettingResponse> {
        return calendarSettingRepository.findByDateBetweenOrderByDateAsc(startDate, endDate)
            .map { it.toResponse() }
    }

    /**
     * 月のカレンダー設定を取得
     */
    @Transactional(readOnly = true)
    fun findByYearMonth(yearMonth: YearMonth): CalendarMonthResponse {
        val year = yearMonth.year
        val month = yearMonth.monthValue

        val settings = calendarSettingRepository.findByYearAndMonth(year, month)

        val days = settings.map { setting ->
            CalendarDayResponse(
                date = setting.date,
                menuSetId = setting.menuSet?.id,
                menuSetName = setting.menuSet?.name,
                isHoliday = setting.isHoliday,
                isOrderable = setting.isOrderable(),
                memo = setting.memo
            )
        }

        return CalendarMonthResponse(
            year = year,
            month = month,
            days = days
        )
    }

    /**
     * 注文可能な日付を取得
     */
    @Transactional(readOnly = true)
    fun findOrderableDates(startDate: LocalDate, endDate: LocalDate): List<CalendarSettingResponse> {
        return calendarSettingRepository.findOrderableDatesBetween(startDate, endDate)
            .map { it.toResponse() }
    }

    /**
     * カレンダー設定を作成または更新
     */
    fun createOrUpdate(request: CalendarSettingRequest): CalendarSettingResponse {
        val existing = calendarSettingRepository.findByDate(request.date)

        val menuSet = request.menuSetId?.let { menuSetId ->
            menuSetRepository.findByIdAndDeletedFalse(menuSetId)
                ?: throw IllegalArgumentException("メニューセットが見つかりません。ID: $menuSetId")
        }

        val setting = existing?.apply {
            this.menuSet = menuSet
            this.isHoliday = request.isHoliday
            this.memo = request.memo
        } ?: CalendarSetting(
            date = request.date,
            menuSet = menuSet,
            isHoliday = request.isHoliday,
            memo = request.memo
        )

        val saved = calendarSettingRepository.save(setting)
        return saved.toResponse()
    }

    /**
     * カレンダー一括設定
     */
    fun bulkCreateOrUpdate(request: CalendarBulkSettingRequest): List<CalendarSettingResponse> {
        val menuSet = request.menuSetId?.let { menuSetId ->
            menuSetRepository.findByIdAndDeletedFalse(menuSetId)
                ?: throw IllegalArgumentException("メニューセットが見つかりません。ID: $menuSetId")
        }

        val results = mutableListOf<CalendarSettingResponse>()
        var currentDate = request.startDate

        while (!currentDate.isAfter(request.endDate)) {
            val existing = calendarSettingRepository.findByDate(currentDate)

            val setting = existing?.apply {
                this.menuSet = menuSet
                this.isHoliday = request.isHoliday
                this.memo = request.memo
            } ?: CalendarSetting(
                date = currentDate,
                menuSet = menuSet,
                isHoliday = request.isHoliday,
                memo = request.memo
            )

            val saved = calendarSettingRepository.save(setting)
            results.add(saved.toResponse())

            currentDate = currentDate.plusDays(1)
        }

        return results
    }

    /**
     * カレンダー設定を削除
     */
    fun delete(date: LocalDate) {
        val setting = calendarSettingRepository.findByDate(date)
            ?: throw IllegalArgumentException("カレンダー設定が見つかりません。日付: $date")

        calendarSettingRepository.delete(setting)
    }

    /**
     * CalendarSettingエンティティをCalendarSettingResponseに変換
     */
    private fun CalendarSetting.toResponse(): CalendarSettingResponse {
        return CalendarSettingResponse(
            id = this.id!!,
            date = this.date,
            menuSetId = this.menuSet?.id,
            menuSetName = this.menuSet?.name,
            isHoliday = this.isHoliday,
            memo = this.memo,
            isOrderable = this.isOrderable(),
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}
