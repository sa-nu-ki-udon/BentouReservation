package Sample.BentouReservation.repository

import Sample.BentouReservation.entity.CalendarSetting
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * カレンダー設定リポジトリ
 */
@Repository
interface CalendarSettingRepository : JpaRepository<CalendarSetting, Long> {

    /**
     * 日付で検索
     */
    fun findByDate(date: LocalDate): CalendarSetting?

    /**
     * 期間で検索
     */
    fun findByDateBetweenOrderByDateAsc(startDate: LocalDate, endDate: LocalDate): List<CalendarSetting>

    /**
     * 期間で検索（メニューセットも一括取得）
     */
    @Query("""
        SELECT cs FROM CalendarSetting cs
        LEFT JOIN FETCH cs.menuSet ms
        LEFT JOIN FETCH ms.menuSetItems msi
        LEFT JOIN FETCH msi.menu
        WHERE cs.date BETWEEN :startDate AND :endDate
        ORDER BY cs.date ASC
    """)
    fun findByDateBetweenWithMenuSet(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<CalendarSetting>

    /**
     * 注文可能な日付を検索（休日でなく、メニューセットが設定されている）
     */
    @Query("""
        SELECT cs FROM CalendarSetting cs
        WHERE cs.date BETWEEN :startDate AND :endDate
        AND cs.isHoliday = false
        AND cs.menuSet IS NOT NULL
        ORDER BY cs.date ASC
    """)
    fun findOrderableDatesBetween(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<CalendarSetting>

    /**
     * 日付で検索（メニューセットも一括取得）
     */
    @Query("""
        SELECT cs FROM CalendarSetting cs
        LEFT JOIN FETCH cs.menuSet ms
        LEFT JOIN FETCH ms.menuSetItems msi
        LEFT JOIN FETCH msi.menu
        WHERE cs.date = :date
    """)
    fun findByDateWithMenuSet(@Param("date") date: LocalDate): CalendarSetting?

    /**
     * 指定年月のカレンダー設定を取得
     */
    @Query("""
        SELECT cs FROM CalendarSetting cs
        WHERE YEAR(cs.date) = :year AND MONTH(cs.date) = :month
        ORDER BY cs.date ASC
    """)
    fun findByYearAndMonth(
        @Param("year") year: Int,
        @Param("month") month: Int
    ): List<CalendarSetting>
}
