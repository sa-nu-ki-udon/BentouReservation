package Sample.BentouReservation.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

/**
 * メニューマスタ
 * メニューの情報と価格履歴を管理するエンティティ
 * 価格変更時は新しいレコードとして登録する
 */
@Entity
@Table(
    name = "menus",
    indexes = [
        Index(name = "idx_menus_vendor_id", columnList = "vendor_id"),
        Index(name = "idx_menus_deleted", columnList = "deleted"),
        Index(name = "idx_menus_effective_from", columnList = "effective_from")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_menus_vendor_name_effective",
            columnNames = ["vendor_id", "name", "effective_from"]
        )
    ]
)
class Menu(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", nullable = false, foreignKey = ForeignKey(name = "fk_menus_vendor"))
    var vendor: Vendor,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,

    @Column(name = "effective_from", nullable = false)
    var effectiveFrom: LocalDate,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "deleted", nullable = false)
    var deleted: Boolean = false

) : BaseEntity() {

    @OneToMany(mappedBy = "menu", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var menuSetItems: MutableList<MenuSetItem> = mutableListOf()

    @OneToMany(mappedBy = "menu", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var orders: MutableList<Order> = mutableListOf()

    override fun toString(): String {
        return "Menu(id=$id, name='$name', price=$price, effectiveFrom=$effectiveFrom, deleted=$deleted)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Menu) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
