package Sample.BentouReservation.repository

import Sample.BentouReservation.entity.BillingClosingDate
import Sample.BentouReservation.entity.ClosingDateType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 締め日設定リポジトリ
 */
@Repository
interface BillingClosingDateRepository : JpaRepository<BillingClosingDate, Long> {

    /**
     * タイプで検索
     */
    fun findByType(type: ClosingDateType): List<BillingClosingDate>

    /**
     * タイプとターゲットIDで検索
     */
    fun findByTypeAndTargetId(type: ClosingDateType, targetId: Long): BillingClosingDate?

    /**
     * 業者別締め日を全件取得
     */
    fun findByTypeOrderByClosingDayAsc(type: ClosingDateType): List<BillingClosingDate>
}
