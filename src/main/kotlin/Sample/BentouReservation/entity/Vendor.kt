package Sample.BentouReservation.entity

import jakarta.persistence.*

/**
 * 業者マスタ
 * 弁当業者の情報を管理するエンティティ
 */
@Entity
@Table(
    name = "vendors",
    indexes = [
        Index(name = "idx_vendors_deleted", columnList = "deleted"),
        Index(name = "idx_vendors_name", columnList = "name")
    ]
)
class Vendor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Column(name = "contact_info", length = 255)
    var contactInfo: String? = null,

    @Column(name = "deleted", nullable = false)
    var deleted: Boolean = false

) : BaseEntity() {

    @OneToMany(mappedBy = "vendor", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var menus: MutableList<Menu> = mutableListOf()

    override fun toString(): String {
        return "Vendor(id=$id, name='$name', contactInfo=$contactInfo, deleted=$deleted)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Vendor) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
