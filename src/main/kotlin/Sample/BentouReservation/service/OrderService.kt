package Sample.BentouReservation.service

import Sample.BentouReservation.dto.*
import Sample.BentouReservation.entity.Order
import Sample.BentouReservation.repository.CalendarSettingRepository
import Sample.BentouReservation.repository.EmployeeRepository
import Sample.BentouReservation.repository.MenuRepository
import Sample.BentouReservation.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime

/**
 * 注文サービス
 */
@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val employeeRepository: EmployeeRepository,
    private val menuRepository: MenuRepository,
    private val calendarSettingRepository: CalendarSettingRepository
) {

    companion object {
        // 注文締切時刻（8:30）
        private val ORDER_DEADLINE = LocalTime.of(8, 30)
    }

    /**
     * 社員IDと注文日で注文を取得
     */
    @Transactional(readOnly = true)
    fun findByEmployeeAndDate(employeeId: Long, orderDate: LocalDate): OrderResponse? {
        val order = orderRepository.findByEmployeeIdAndOrderDate(employeeId, orderDate)
        return order?.toResponse()
    }

    /**
     * 社員IDで注文履歴を取得
     */
    @Transactional(readOnly = true)
    fun findByEmployee(employeeId: Long): List<OrderHistoryResponse> {
        return orderRepository.findByEmployeeId(employeeId)
            .sortedByDescending { it.orderDate }
            .map { it.toHistoryResponse() }
    }

    /**
     * 社員IDと期間で注文を取得
     */
    @Transactional(readOnly = true)
    fun findByEmployeeAndDateRange(
        employeeId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<OrderSummaryResponse> {
        return orderRepository.findByEmployeeIdAndOrderDateBetweenOrderByOrderDateAsc(
            employeeId, startDate, endDate
        ).map { it.toSummaryResponse() }
    }

    /**
     * 指定日の注文を全件取得
     */
    @Transactional(readOnly = true)
    fun findByDate(orderDate: LocalDate): List<OrderResponse> {
        return orderRepository.findByOrderDateWithDetails(orderDate)
            .map { it.toResponse() }
    }

    /**
     * 注文を作成
     */
    fun create(request: OrderCreateRequest): OrderResponse {
        // 注文日が注文可能かチェック
        validateOrderable(request.orderDate)

        // 社員の存在確認
        val employee = employeeRepository.findByIdAndDeletedFalse(request.employeeId)
            ?: throw IllegalArgumentException("社員が見つかりません。ID: ${request.employeeId}")

        // メニューの存在確認
        val menu = menuRepository.findByIdAndDeletedFalse(request.menuId)
            ?: throw IllegalArgumentException("メニューが見つかりません。ID: ${request.menuId}")

        // 既に同じ日に注文があるかチェック
        val existing = orderRepository.findByEmployeeIdAndOrderDate(request.employeeId, request.orderDate)
        if (existing != null) {
            throw IllegalArgumentException("既に${request.orderDate}の注文が存在します")
        }

        val order = Order(
            employee = employee,
            orderDate = request.orderDate,
            menu = menu,
            priceAtOrder = menu.price  // 注文時の価格を保存
        )

        val saved = orderRepository.save(order)
        return saved.toResponse()
    }

    /**
     * 注文を更新（メニュー変更）
     */
    fun update(employeeId: Long, orderDate: LocalDate, request: OrderUpdateRequest): OrderResponse {
        // 注文日が変更可能かチェック
        validateOrderable(orderDate)

        // 既存の注文を取得
        val order = orderRepository.findByEmployeeIdAndOrderDate(employeeId, orderDate)
            ?: throw IllegalArgumentException("注文が見つかりません")

        // 新しいメニューの存在確認
        val newMenu = menuRepository.findByIdAndDeletedFalse(request.menuId)
            ?: throw IllegalArgumentException("メニューが見つかりません。ID: ${request.menuId}")

        order.menu = newMenu
        order.priceAtOrder = newMenu.price  // 変更時の価格を保存

        val updated = orderRepository.save(order)
        return updated.toResponse()
    }

    /**
     * 注文を削除
     */
    fun delete(employeeId: Long, orderDate: LocalDate) {
        // 注文日が変更可能かチェック
        validateOrderable(orderDate)

        val order = orderRepository.findByEmployeeIdAndOrderDate(employeeId, orderDate)
            ?: throw IllegalArgumentException("注文が見つかりません")

        orderRepository.delete(order)
    }

    /**
     * 注文が可能かどうかをチェック
     */
    private fun validateOrderable(orderDate: LocalDate) {
        val now = LocalDate.now()
        val currentTime = LocalTime.now()

        // 過去の日付は変更不可
        if (orderDate.isBefore(now)) {
            throw IllegalArgumentException("過去の日付の注文はできません")
        }

        // 当日の場合、締切時刻をチェック
        if (orderDate.isEqual(now) && currentTime.isAfter(ORDER_DEADLINE)) {
            throw IllegalArgumentException("本日の注文は締め切られました（締切時刻: ${ORDER_DEADLINE}）")
        }

        // カレンダー設定で注文可能かチェック
        val calendarSetting = calendarSettingRepository.findByDate(orderDate)
        if (calendarSetting == null || !calendarSetting.isOrderable()) {
            throw IllegalArgumentException("${orderDate}は注文できません（休日またはメニュー未設定）")
        }
    }

    /**
     * OrderエンティティをOrderResponseに変換
     */
    private fun Order.toResponse(): OrderResponse {
        return OrderResponse(
            id = this.id!!,
            employeeId = this.employee.id!!,
            employeeName = this.employee.name,
            orderDate = this.orderDate,
            menuId = this.menu.id!!,
            menuName = this.menu.name,
            vendorName = this.menu.vendor.name,
            priceAtOrder = this.priceAtOrder,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    /**
     * OrderエンティティをOrderSummaryResponseに変換
     */
    private fun Order.toSummaryResponse(): OrderSummaryResponse {
        return OrderSummaryResponse(
            orderDate = this.orderDate,
            menuId = this.menu.id!!,
            menuName = this.menu.name,
            vendorName = this.menu.vendor.name,
            price = this.priceAtOrder
        )
    }

    /**
     * OrderエンティティをOrderHistoryResponseに変換
     */
    private fun Order.toHistoryResponse(): OrderHistoryResponse {
        return OrderHistoryResponse(
            id = this.id!!,
            orderDate = this.orderDate,
            menuName = this.menu.name,
            vendorName = this.menu.vendor.name,
            priceAtOrder = this.priceAtOrder,
            createdAt = this.createdAt
        )
    }
}
