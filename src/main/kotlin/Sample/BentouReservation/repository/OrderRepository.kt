package Sample.BentouReservation.repository

import Sample.BentouReservation.entity.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * 注文リポジトリ
 */
@Repository
interface OrderRepository : JpaRepository<Order, Long> {

    /**
     * 社員IDと注文日で検索
     */
    fun findByEmployeeIdAndOrderDate(employeeId: Long, orderDate: LocalDate): Order?

    /**
     * 社員IDで検索
     */
    fun findByEmployeeId(employeeId: Long): List<Order>

    /**
     * 社員IDと期間で検索
     */
    fun findByEmployeeIdAndOrderDateBetweenOrderByOrderDateAsc(
        employeeId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Order>

    /**
     * 注文日で検索
     */
    fun findByOrderDate(orderDate: LocalDate): List<Order>

    /**
     * 期間で検索
     */
    fun findByOrderDateBetween(startDate: LocalDate, endDate: LocalDate): List<Order>

    /**
     * 日次集計：指定日の業者別メニュー別注文数を取得
     */
    @Query("""
        SELECT
            m.vendor.id as vendorId,
            m.vendor.name as vendorName,
            m.id as menuId,
            m.name as menuName,
            COUNT(o.id) as orderCount,
            SUM(o.priceAtOrder) as totalPrice
        FROM Order o
        JOIN o.menu m
        WHERE o.orderDate = :orderDate
        GROUP BY m.vendor.id, m.vendor.name, m.id, m.name
        ORDER BY m.vendor.name, m.name
    """)
    fun findDailyAggregationByDate(@Param("orderDate") orderDate: LocalDate): List<DailyAggregationResult>

    /**
     * 月次集計：社員IDと期間で注文を取得（社員別）
     */
    @Query("""
        SELECT o FROM Order o
        JOIN FETCH o.menu m
        JOIN FETCH m.vendor
        WHERE o.employee.id = :employeeId
        AND o.orderDate BETWEEN :startDate AND :endDate
        ORDER BY o.orderDate ASC
    """)
    fun findMonthlyOrdersByEmployee(
        @Param("employeeId") employeeId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<Order>

    /**
     * 月次集計：期間と業者IDで注文を取得（業者別）
     */
    @Query("""
        SELECT o FROM Order o
        JOIN FETCH o.menu m
        JOIN FETCH o.employee e
        WHERE m.vendor.id = :vendorId
        AND o.orderDate BETWEEN :startDate AND :endDate
        ORDER BY o.orderDate ASC, m.name, e.name
    """)
    fun findMonthlyOrdersByVendor(
        @Param("vendorId") vendorId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<Order>

    /**
     * 注文日で検索（関連エンティティも一括取得）
     */
    @Query("""
        SELECT o FROM Order o
        JOIN FETCH o.employee
        JOIN FETCH o.menu m
        JOIN FETCH m.vendor
        WHERE o.orderDate = :orderDate
        ORDER BY m.vendor.name, m.name, o.employee.name
    """)
    fun findByOrderDateWithDetails(@Param("orderDate") orderDate: LocalDate): List<Order>
}

/**
 * 日次集計結果
 */
interface DailyAggregationResult {
    val vendorId: Long
    val vendorName: String
    val menuId: Long
    val menuName: String
    val orderCount: Long
    val totalPrice: java.math.BigDecimal
}
