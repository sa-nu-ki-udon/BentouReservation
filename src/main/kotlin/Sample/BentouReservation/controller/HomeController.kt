package Sample.BentouReservation.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/**
 * ホームコントローラー
 */
@Controller
class HomeController {

    /**
     * トップページ
     */
    @GetMapping("/")
    fun index(): String {
        return "index"
    }
}
