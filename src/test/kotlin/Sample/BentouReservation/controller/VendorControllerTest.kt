package Sample.BentouReservation.controller

import Sample.BentouReservation.dto.VendorCreateRequest
import Sample.BentouReservation.dto.VendorResponse
import Sample.BentouReservation.dto.VendorUpdateRequest
import Sample.BentouReservation.service.VendorService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("VendorController テスト")
class VendorControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var vendorService: VendorService

    private lateinit var testVendorResponse: VendorResponse

    @BeforeEach
    fun setUp() {
        val now = LocalDateTime.now()
        testVendorResponse = VendorResponse(
            id = 1L,
            name = "テスト業者",
            contactInfo = "123-4567",
            deleted = false,
            createdAt = now,
            updatedAt = now
        )
    }

    @Test
    @DisplayName("GET /vendors - 業者一覧画面を表示できる")
    fun `should display vendor list successfully`() {
        // Given
        val vendors = listOf(testVendorResponse)
        whenever(vendorService.findAll()).thenReturn(vendors)

        // When & Then
        mockMvc.get("/vendors")
            .andExpect { status { isOk() } }
    }

    @Test
    @DisplayName("GET /vendors/{id} - 業者詳細画面を表示できる")
    fun `should display vendor detail successfully`() {
        // Given
        whenever(vendorService.findById(1L)).thenReturn(testVendorResponse)

        // When & Then
        mockMvc.get("/vendors/1")
            .andExpect { status { isOk() } }
    }

    @Test
    @DisplayName("GET /vendors/new - 新規登録フォーム画面を表示できる")
    fun `should display vendor creation form`() {
        // When & Then
        mockMvc.get("/vendors/new")
            .andExpect { status { isOk() } }
    }

    @Test
    @DisplayName("POST /vendors - 業者を作成できる")
    fun `should create vendor successfully`() {
        // Given
        val request = VendorCreateRequest(name = "新規業者", contactInfo = "090-1234-5678")
        val createdVendor = testVendorResponse.copy(id = 2L, name = "新規業者")
        whenever(vendorService.create(request)).thenReturn(createdVendor)

        // When & Then
        mockMvc.post("/vendors") {
            param("name", "新規業者")
            param("contactInfo", "090-1234-5678")
        }.andExpect {
            status { isFound() }
        }
    }

    @Test
    @DisplayName("GET /vendors/{id}/edit - 業者編集フォームを表示できる")
    fun `should display vendor edit form`() {
        // Given
        whenever(vendorService.findById(1L)).thenReturn(testVendorResponse)

        // When & Then
        mockMvc.get("/vendors/1/edit")
            .andExpect { status { isOk() } }
    }

    @Test
    @DisplayName("POST /vendors/{id} - 業者を更新できる")
    fun `should update vendor successfully`() {
        // Given
        val request = VendorUpdateRequest(name = "更新業者", contactInfo = "098-7654-3210")
        val updatedVendor = testVendorResponse.copy(name = "更新業者")
        whenever(vendorService.update(1L, request)).thenReturn(updatedVendor)

        // When & Then
        mockMvc.post("/vendors/1") {
            param("name", "更新業者")
            param("contactInfo", "098-7654-3210")
        }.andExpect {
            status { isFound() }
        }
    }

    @Test
    @DisplayName("POST /vendors/{id}/delete - 業者を削除できる")
    fun `should delete vendor successfully`() {
        // Given
        whenever(vendorService.delete(1L)).then { }

        // When & Then
        mockMvc.post("/vendors/1/delete")
            .andExpect { status { isFound() } }
    }
}
