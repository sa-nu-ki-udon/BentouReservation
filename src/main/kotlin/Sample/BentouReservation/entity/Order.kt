package Sample.BentouReservation.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

/**
 * 注文
 * 社員の弁当注文情報を管理するエンティティ
 */
@Entity
@Table(
    name = "orders",
    indexes = [
        Index(name = "idx_orders_employee_id", columnList = "employee_id"),
        Index(name = "idx_orders_order_date", columnList = "order_date"),
        Index(name = "idx_orders_menu_id", columnList = "menu_id"),
        Index(name = "idx_orders_created_at", columnList = "created_at")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_orders_employee_date",
            columnNames = ["employee_id", "order_date"]
        )
    ]
)
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = ForeignKey(name = "fk_orders_employee"))
    var employee: Employee,

    @Column(name = "order_date", nullable = false)
    var orderDate: LocalDate,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false, foreignKey = ForeignKey(name = "fk_orders_menu"))
    var menu: Menu,

    @Column(name = "price_at_order", nullable = false, precision = 10, scale = 2)
    var priceAtOrder: BigDecimal

) : BaseEntity() {

    override fun toString(): String {
        return "Order(id=$id, orderDate=$orderDate, priceAtOrder=$priceAtOrder)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Order) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
