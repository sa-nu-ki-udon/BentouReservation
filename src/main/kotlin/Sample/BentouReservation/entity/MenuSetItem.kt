package Sample.BentouReservation.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * メニューセット詳細
 * メニューセットとメニューの紐付けを管理する中間テーブル
 */
@Entity
@Table(
    name = "menu_set_items",
    indexes = [
        Index(name = "idx_menu_set_items_set_id", columnList = "menu_set_id"),
        Index(name = "idx_menu_set_items_menu_id", columnList = "menu_id")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_menu_set_items_set_menu",
            columnNames = ["menu_set_id", "menu_id"]
        )
    ]
)
class MenuSetItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_set_id", nullable = false, foreignKey = ForeignKey(name = "fk_menu_set_items_set"))
    var menuSet: MenuSet,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false, foreignKey = ForeignKey(name = "fk_menu_set_items_menu"))
    var menu: Menu,

    @Column(name = "display_order")
    var displayOrder: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

) {

    @PrePersist
    protected fun onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now()
        }
    }

    override fun toString(): String {
        return "MenuSetItem(id=$id, displayOrder=$displayOrder)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MenuSetItem) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
