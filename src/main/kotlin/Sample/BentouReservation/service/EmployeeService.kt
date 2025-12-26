package Sample.BentouReservation.service

import Sample.BentouReservation.dto.*
import Sample.BentouReservation.entity.Employee
import Sample.BentouReservation.repository.EmployeeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 社員サービス
 */
@Service
@Transactional
class EmployeeService(
    private val employeeRepository: EmployeeRepository
) {

    /**
     * 全社員を取得（削除されていないもののみ）
     */
    @Transactional(readOnly = true)
    fun findAll(): List<EmployeeResponse> {
        return employeeRepository.findByDeletedFalse()
            .map { it.toResponse() }
    }

    /**
     * IDで社員を取得
     */
    @Transactional(readOnly = true)
    fun findById(id: Long): EmployeeResponse {
        val employee = employeeRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("社員が見つかりません。ID: $id")
        return employee.toResponse()
    }

    /**
     * 社員番号で社員を取得
     */
    @Transactional(readOnly = true)
    fun findByEmployeeNumber(employeeNumber: String): EmployeeResponse? {
        val employee = employeeRepository.findByEmployeeNumberAndDeletedFalse(employeeNumber)
        return employee?.toResponse()
    }

    /**
     * 社員名で検索
     */
    @Transactional(readOnly = true)
    fun searchByName(name: String): List<EmployeeResponse> {
        return employeeRepository.findByNameContainingAndDeletedFalse(name)
            .map { it.toResponse() }
    }

    /**
     * 部署で検索
     */
    @Transactional(readOnly = true)
    fun findByDepartment(department: String): List<EmployeeResponse> {
        return employeeRepository.findByDepartmentAndDeletedFalse(department)
            .map { it.toResponse() }
    }

    /**
     * 全社員の簡易情報を取得
     */
    @Transactional(readOnly = true)
    fun findAllSummary(): List<EmployeeSummaryResponse> {
        return employeeRepository.findByDeletedFalse()
            .map { it.toSummaryResponse() }
    }

    /**
     * 社員を作成
     */
    fun create(request: EmployeeCreateRequest): EmployeeResponse {
        // 社員番号の重複チェック
        val existing = employeeRepository.findByEmployeeNumber(request.employeeNumber)
        if (existing != null) {
            throw IllegalArgumentException("社員番号が既に使用されています: ${request.employeeNumber}")
        }

        val employee = Employee(
            employeeNumber = request.employeeNumber,
            name = request.name,
            department = request.department
        )

        val saved = employeeRepository.save(employee)
        return saved.toResponse()
    }

    /**
     * 社員を更新
     */
    fun update(id: Long, request: EmployeeUpdateRequest): EmployeeResponse {
        val employee = employeeRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("社員が見つかりません。ID: $id")

        employee.name = request.name
        employee.department = request.department

        val updated = employeeRepository.save(employee)
        return updated.toResponse()
    }

    /**
     * 社員を論理削除
     */
    fun delete(id: Long) {
        val employee = employeeRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("社員が見つかりません。ID: $id")

        employee.deleted = true
        employeeRepository.save(employee)
    }

    /**
     * EmployeeエンティティをEmployeeResponseに変換
     */
    private fun Employee.toResponse(): EmployeeResponse {
        return EmployeeResponse(
            id = this.id!!,
            employeeNumber = this.employeeNumber,
            name = this.name,
            department = this.department,
            deleted = this.deleted,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    /**
     * EmployeeエンティティをEmployeeSummaryResponseに変換
     */
    private fun Employee.toSummaryResponse(): EmployeeSummaryResponse {
        return EmployeeSummaryResponse(
            id = this.id!!,
            employeeNumber = this.employeeNumber,
            name = this.name,
            department = this.department
        )
    }
}
