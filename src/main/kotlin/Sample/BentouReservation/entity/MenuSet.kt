package Sample.BentouReservation.entity

import jakarta.persistence.*

/**
 * メニューセット
 * 複数の業者・メニューをまとめたセットを定義するエンティティ
 */
@Entity
@Table(
    name = "menu_sets",
    indexes = [
        Index(name = "idx_menu_sets_deleted", columnList = "deleted"),
        Index(name = "idx_menu_sets_name", columnList = "name")
    ]
)
class MenuSet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "deleted", nullable = false)
    var deleted: Boolean = false

) : BaseEntity() {

    @OneToMany(mappedBy = "menuSet", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var menuSetItems: MutableList<MenuSetItem> = mutableListOf()

    @OneToMany(mappedBy = "menuSet", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var calendarSettings: MutableList<CalendarSetting> = mutableListOf()

    /**
     * メニューセットにメニューを追加
     */
    fun addMenuItem(menu: Menu, displayOrder: Int = 0): MenuSetItem {
        val item = MenuSetItem(
            menuSet = this,
            menu = menu,
            displayOrder = displayOrder
        )
        menuSetItems.add(item)
        return item
    }

    /**
     * メニューセットからメニューを削除
     */
    fun removeMenuItem(item: MenuSetItem) {
        menuSetItems.remove(item)
    }

    override fun toString(): String {
        return "MenuSet(id=$id, name='$name', deleted=$deleted)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MenuSet) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
