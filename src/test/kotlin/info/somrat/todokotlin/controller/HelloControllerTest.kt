package info.somrat.todokotlin.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(HelloController::class)
class HelloControllerTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
    ) {
        @Test
        fun `GET hello returns Hello`() {
            mockMvc.get("/hello").andExpect {
                status { isOk() }
                content { string("Hello") }
            }
        }
    }
