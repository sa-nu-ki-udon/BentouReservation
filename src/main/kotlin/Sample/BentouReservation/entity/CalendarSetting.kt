package Sample.BentouReservation.entity

import jakarta.persistence.*
import java.time.LocalDate

/**
 * カレンダー設定
 * 日付ごとのメニューセットと休日情報を管理するエンティティ
 */
@Entity
@Table(
    name = "calendar_settings",
    indexes = [
        Index(name = "idx_calendar_date", columnList = "date"),
        Index(name = "idx_calendar_menu_set_id", columnList = "menu_set_id"),
        Index(name = "idx_calendar_is_holiday", columnList = "is_holiday")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_calendar_date", columnNames = ["date"])
    ]
)
class CalendarSetting(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "date", nullable = false, unique = true)
    var date: LocalDate,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_set_id", foreignKey = ForeignKey(name = "fk_calendar_settings_menu_set"))
    var menuSet: MenuSet? = null,

    @Column(name = "is_holiday", nullable = false)
    var isHoliday: Boolean = false,

    @Column(name = "memo", length = 255)
    var memo: String? = null

) : BaseEntity() {

    /**
     * 注文可能かどうか
     * 休日でなく、かつメニューセットが設定されている場合に注文可能
     */
    fun isOrderable(): Boolean {
        return !isHoliday && menuSet != null
    }

    override fun toString(): String {
        return "CalendarSetting(id=$id, date=$date, isHoliday=$isHoliday, menuSetId=${menuSet?.id})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CalendarSetting) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
