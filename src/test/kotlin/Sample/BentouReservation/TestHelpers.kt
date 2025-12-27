package Sample.BentouReservation

import Sample.BentouReservation.entity.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * テスト用ヘルパー関数
 * BaseEntity のprotected フィールドをリフレクションで初期化
 */

fun initializeBaseEntity(entity: BaseEntity, createdAt: LocalDateTime = LocalDateTime.now(), updatedAt: LocalDateTime = LocalDateTime.now()) {
    try {
        val createdAtField = BaseEntity::class.java.getDeclaredField("createdAt")
        val updatedAtField = BaseEntity::class.java.getDeclaredField("updatedAt")
        createdAtField.apply { isAccessible = true; set(entity, createdAt) }
        updatedAtField.apply { isAccessible = true; set(entity, updatedAt) }
    } catch (e: Exception) {
        // デフォルト値を使用するため、フィールドが既に初期化されている
    }
}

fun createTestVendor(
    id: Long? = null,
    name: String = "テスト業者",
    contactInfo: String? = null,
    deleted: Boolean = false
): Vendor {
    val now = LocalDateTime.now()
    return Vendor(id = id, name = name, contactInfo = contactInfo).apply {
        this.deleted = deleted
        initializeBaseEntity(this, now, now)
    }
}

fun createTestMenu(
    id: Long? = null,
    vendor: Vendor = createTestVendor(),
    name: String = "テストメニュー",
    price: BigDecimal = BigDecimal("500"),
    effectiveFrom: LocalDate = LocalDate.now(),
    description: String? = null,
    deleted: Boolean = false
): Menu {
    val now = LocalDateTime.now()
    return Menu(id = id, vendor = vendor, name = name, price = price, effectiveFrom = effectiveFrom, description = description).apply {
        this.deleted = deleted
        initializeBaseEntity(this, now, now)
    }
}

fun createTestEmployee(
    id: Long? = null,
    employeeNumber: String = "E001",
    name: String = "テスト社員",
    department: String? = null,
    deleted: Boolean = false
): Employee {
    val now = LocalDateTime.now()
    return Employee(id = id, employeeNumber = employeeNumber, name = name, department = department).apply {
        this.deleted = deleted
        initializeBaseEntity(this, now, now)
    }
}

fun createTestOrder(
    id: Long? = null,
    employee: Employee = createTestEmployee(),
    orderDate: LocalDate = LocalDate.now(),
    menu: Menu = createTestMenu(),
    priceAtOrder: BigDecimal = BigDecimal("500")
): Order {
    val now = LocalDateTime.now()
    return Order(id = id, employee = employee, orderDate = orderDate, menu = menu, priceAtOrder = priceAtOrder).apply {
        initializeBaseEntity(this, now, now)
    }
}
