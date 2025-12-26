package Sample.BentouReservation.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

/**
 * 締め日設定
 * 業者別・個人別の請求締め日を管理するエンティティ
 */
@Entity
@Table(
    name = "billing_closing_dates",
    indexes = [
        Index(name = "idx_billing_type", columnList = "type"),
        Index(name = "idx_billing_target_id", columnList = "target_id")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_billing_type_target",
            columnNames = ["type", "target_id"]
        )
    ]
)
class BillingClosingDate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var type: ClosingDateType,

    @Column(name = "target_id")
    var targetId: Long? = null,

    @Column(name = "closing_day", nullable = false)
    @Min(1)
    @Max(31)
    var closingDay: Int,

    @Column(name = "description", length = 255)
    var description: String? = null

) : BaseEntity() {

    override fun toString(): String {
        return "BillingClosingDate(id=$id, type=$type, targetId=$targetId, closingDay=$closingDay)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BillingClosingDate) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

/**
 * 締め日タイプ
 */
enum class ClosingDateType {
    /**
     * 業者別締め日
     */
    VENDOR,

    /**
     * 個人請求締め日
     */
    EMPLOYEE
}
