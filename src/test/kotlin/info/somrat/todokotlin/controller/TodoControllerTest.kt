package info.somrat.todokotlin.controller

import com.ninjasquad.springmockk.MockkBean
import info.somrat.todokotlin.dto.PageResponse
import info.somrat.todokotlin.dto.TaskResponse
import info.somrat.todokotlin.dto.TodoDetailResponse
import info.somrat.todokotlin.dto.TodoRequest
import info.somrat.todokotlin.dto.TodoResponse
import info.somrat.todokotlin.exception.NotFoundException
import info.somrat.todokotlin.service.TodoService
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.util.UUID

@WebMvcTest(TodoController::class)
class TodoControllerTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
    ) {
        @MockkBean
        private lateinit var todoService: TodoService

        private val id = UUID.randomUUID()

        private fun emptyPage() =
            PageResponse<TodoResponse>(totalItems = 0, totalPage = 0, currentPage = 0, data = emptyList())

        @Test
        fun `GET todos returns a page`() {
            every { todoService.list(0, 10) } returns
                PageResponse(
                    totalItems = 1,
                    totalPage = 1,
                    currentPage = 0,
                    data = listOf(TodoResponse(id, "Todo 3", "todo description")),
                )

            mockMvc.get("/todos?pageNo=0&pageSize=10").andExpect {
                status { isOk() }
                jsonPath("\$.success") { value(true) }
                jsonPath("\$.message") { value("") }
                jsonPath("\$.totalItems") { value(1) }
                jsonPath("\$.totalPage") { value(1) }
                jsonPath("\$.currentPage") { value(0) }
                jsonPath("\$.data[0].id") { value(id.toString()) }
                jsonPath("\$.data[0].name") { value("Todo 3") }
            }
        }

        @Test
        fun `GET todos uses defaults when no paging params are given`() {
            every { todoService.list(0, 10) } returns emptyPage()

            mockMvc.get("/todos").andExpect {
                status { isOk() }
                jsonPath("\$.data") { isEmpty() }
            }
        }

        @Test
        fun `GET todos clamps out-of-range paging params`() {
            every { todoService.list(0, 100) } returns emptyPage()

            mockMvc.get("/todos?pageNo=-5&pageSize=1000").andExpect { status { isOk() } }

            verify(exactly = 1) { todoService.list(0, 100) }
        }

        @Test
        fun `GET todos rejects a non numeric page`() {
            mockMvc.get("/todos?pageNo=abc").andExpect {
                status { isBadRequest() }
                jsonPath("\$.success") { value(false) }
            }
        }

        @Test
        fun `POST todos creates a todo`() {
            every { todoService.create(TodoRequest("Todo 1", "todo 1 description")) } returns
                TodoResponse(id, "Todo 1", "todo 1 description")

            mockMvc
                .post("/todos") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"name":"Todo 1","description":"todo 1 description"}"""
                }.andExpect {
                    status { isCreated() }
                    jsonPath("\$.success") { value(true) }
                    jsonPath("\$.message") { value("Todo created successfully!") }
                    jsonPath("\$.object.id") { value(id.toString()) }
                    jsonPath("\$.object.name") { value("Todo 1") }
                }
        }

        @Test
        fun `POST todos with blank name is rejected`() {
            mockMvc
                .post("/todos") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"name":"  ","description":"x"}"""
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("\$.success") { value(false) }
                    jsonPath("\$.message") { value("name must not be blank") }
                    jsonPath("\$.object") { doesNotExist() }
                }

            verify(exactly = 0) { todoService.create(any()) }
        }

        @Test
        fun `POST todos with malformed json is rejected`() {
            mockMvc
                .post("/todos") {
                    contentType = MediaType.APPLICATION_JSON
                    content = "{not json"
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("\$.success") { value(false) }
                }
        }

        @Test
        fun `GET todo by id returns details with tasks`() {
            val taskId = UUID.randomUUID()
            every { todoService.get(id) } returns
                TodoDetailResponse(
                    id,
                    "Todo 3",
                    "todo description",
                    listOf(TaskResponse(taskId, "Task 1", "Task 1 description")),
                )

            mockMvc.get("/todos/$id").andExpect {
                status { isOk() }
                jsonPath("\$.message") { value("Todo found successfully!") }
                jsonPath("\$.object.id") { value(id.toString()) }
                jsonPath("\$.object.tasks[0].id") { value(taskId.toString()) }
                jsonPath("\$.object.tasks[0].name") { value("Task 1") }
            }
        }

        @Test
        fun `GET todo by id returns 404 when missing`() {
            every { todoService.get(id) } throws NotFoundException("Todo not found with id $id")

            mockMvc.get("/todos/$id").andExpect {
                status { isNotFound() }
                jsonPath("\$.success") { value(false) }
                jsonPath("\$.message") { value("Todo not found with id $id") }
            }
        }

        @Test
        fun `GET todo with an invalid id is a bad request`() {
            mockMvc.get("/todos/not-a-uuid").andExpect {
                status { isBadRequest() }
                jsonPath("\$.success") { value(false) }
            }
        }

        @Test
        fun `PUT todo updates it`() {
            every { todoService.update(id, TodoRequest("Todo 4 update", "todo 4 description Update")) } returns
                TodoResponse(id, "Todo 4 update", "todo 4 description Update")

            mockMvc
                .put("/todos/$id") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"name":"Todo 4 update","description":"todo 4 description Update"}"""
                }.andExpect {
                    status { isOk() }
                    jsonPath("\$.message") { value("Todo updated successfully!") }
                    jsonPath("\$.object.name") { value("Todo 4 update") }
                }
        }

        @Test
        fun `PUT todo returns 404 when missing`() {
            every { todoService.update(id, TodoRequest("x")) } throws NotFoundException("Todo not found with id $id")

            mockMvc
                .put("/todos/$id") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"name":"x"}"""
                }.andExpect { status { isNotFound() } }
        }

        @Test
        fun `DELETE todo deletes it`() {
            every { todoService.delete(id) } just runs

            mockMvc.delete("/todos/$id").andExpect {
                status { isOk() }
                jsonPath("\$.success") { value(true) }
                jsonPath("\$.message") { value("Todo deleted successfully!") }
                jsonPath("\$.object") { doesNotExist() }
            }

            verify(exactly = 1) { todoService.delete(id) }
        }

        @Test
        fun `DELETE todo returns 404 when missing`() {
            every { todoService.delete(id) } throws NotFoundException("Todo not found with id $id")

            mockMvc.delete("/todos/$id").andExpect { status { isNotFound() } }
        }
    }
