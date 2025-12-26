package Sample.BentouReservation.entity

import jakarta.persistence.*

/**
 * 社員マスタ
 * 社員の情報を管理するエンティティ
 * MVP版ではログイン機能なしのため、簡易的な設計
 */
@Entity
@Table(
    name = "employees",
    indexes = [
        Index(name = "idx_employees_number", columnList = "employee_number"),
        Index(name = "idx_employees_deleted", columnList = "deleted"),
        Index(name = "idx_employees_name", columnList = "name")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_employees_number", columnNames = ["employee_number"])
    ]
)
class Employee(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "employee_number", nullable = false, unique = true, length = 50)
    var employeeNumber: String,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Column(name = "department", length = 100)
    var department: String? = null,

    @Column(name = "deleted", nullable = false)
    var deleted: Boolean = false

) : BaseEntity() {

    @OneToMany(mappedBy = "employee", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var orders: MutableList<Order> = mutableListOf()

    override fun toString(): String {
        return "Employee(id=$id, employeeNumber='$employeeNumber', name='$name', department=$department, deleted=$deleted)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Employee) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
