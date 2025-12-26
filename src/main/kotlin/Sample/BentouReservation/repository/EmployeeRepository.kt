package Sample.BentouReservation.repository

import Sample.BentouReservation.entity.Employee
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 社員マスタリポジトリ
 */
@Repository
interface EmployeeRepository : JpaRepository<Employee, Long> {

    /**
     * 削除されていない社員を全件取得
     */
    fun findByDeletedFalse(): List<Employee>

    /**
     * 社員番号で検索
     */
    fun findByEmployeeNumber(employeeNumber: String): Employee?

    /**
     * 社員番号で検索（削除されていないもののみ）
     */
    fun findByEmployeeNumberAndDeletedFalse(employeeNumber: String): Employee?

    /**
     * 社員名で検索（削除されていないもののみ）
     */
    fun findByNameContainingAndDeletedFalse(name: String): List<Employee>

    /**
     * 部署で検索（削除されていないもののみ）
     */
    fun findByDepartmentAndDeletedFalse(department: String): List<Employee>

    /**
     * IDで検索（削除されていないもののみ）
     */
    fun findByIdAndDeletedFalse(id: Long): Employee?
}
