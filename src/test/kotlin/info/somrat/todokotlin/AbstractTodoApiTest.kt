package info.somrat.todokotlin

import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.util.UUID

/**
 * End-to-end API scenarios: real controllers, services and JPA.
 * Subclasses decide which database the application runs against.
 */
abstract class AbstractTodoApiTest {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    private fun createTodo(
        name: String = "Todo 1",
        description: String = "todo 1 description",
    ): String {
        val body =
            mockMvc
                .post("/todos") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"name":"$name","description":"$description"}"""
                }.andExpect { status { isCreated() } }
                .andReturn()
                .response.contentAsString
        return JsonPath.read(body, "$.object.id")
    }

    private fun createTask(
        todoId: String,
        name: String = "Task 1",
    ): String {
        val body =
            mockMvc
                .post("/todos/$todoId/tasks") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"name":"$name","description":"$name description"}"""
                }.andExpect { status { isCreated() } }
                .andReturn()
                .response.contentAsString
        return JsonPath.read(body, "$.object.id")
    }

    @Test
    fun `todo crud lifecycle`() {
        val id = createTodo()

        mockMvc.get("/todos/$id").andExpect {
            status { isOk() }
            jsonPath("\$.object.name") { value("Todo 1") }
            jsonPath("\$.object.tasks") { isEmpty() }
        }

        mockMvc
            .put("/todos/$id") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"Todo 1 update","description":"updated"}"""
            }.andExpect {
                status { isOk() }
                jsonPath("\$.object.name") { value("Todo 1 update") }
            }

        mockMvc.get("/todos/$id").andExpect {
            jsonPath("\$.object.name") { value("Todo 1 update") }
            jsonPath("\$.object.description") { value("updated") }
        }

        mockMvc.delete("/todos/$id").andExpect {
            status { isOk() }
            jsonPath("\$.message") { value("Todo deleted successfully!") }
        }

        mockMvc.get("/todos/$id").andExpect { status { isNotFound() } }
    }

    @Test
    fun `listing todos is paged`() {
        repeat(3) { createTodo("Paged $it") }

        val body =
            mockMvc
                .get("/todos?pageNo=0&pageSize=2")
                .andExpect {
                    status { isOk() }
                    jsonPath("\$.success") { value(true) }
                    jsonPath("\$.currentPage") { value(0) }
                    jsonPath("\$.data.length()") { value(2) }
                }.andReturn()
                .response.contentAsString

        assertThat(JsonPath.read<Int>(body, "$.totalItems")).isGreaterThanOrEqualTo(3)
        assertThat(JsonPath.read<Int>(body, "$.totalPage")).isGreaterThanOrEqualTo(2)
    }

    @Test
    fun `task crud lifecycle`() {
        val todoId = createTodo()
        val taskId = createTask(todoId, "Task 2")

        mockMvc.get("/todos/$todoId/tasks/$taskId").andExpect {
            status { isOk() }
            jsonPath("\$.object.name") { value("Task 2") }
            jsonPath("\$.object.description") { value("Task 2 description") }
        }

        mockMvc.get("/todos/$todoId").andExpect {
            jsonPath("\$.object.tasks.length()") { value(1) }
            jsonPath("\$.object.tasks[0].id") { value(taskId) }
        }

        mockMvc
            .put("/todos/$todoId/tasks/$taskId") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"Task 2 update","description":"Task 2 description update"}"""
            }.andExpect {
                status { isOk() }
                jsonPath("\$.object.name") { value("Task 2 update") }
            }

        mockMvc.get("/todos/$todoId/tasks/$taskId").andExpect {
            jsonPath("\$.object.description") { value("Task 2 description update") }
        }

        mockMvc.delete("/todos/$todoId/tasks/$taskId").andExpect {
            status { isOk() }
            jsonPath("\$.message") { value("Task deleted successfully!") }
        }

        mockMvc.get("/todos/$todoId/tasks/$taskId").andExpect { status { isNotFound() } }
        mockMvc.get("/todos/$todoId").andExpect { jsonPath("\$.object.tasks") { isEmpty() } }
    }

    @Test
    fun `deleting a todo also deletes its tasks`() {
        val todoId = createTodo()
        val taskId = createTask(todoId)

        mockMvc.delete("/todos/$todoId").andExpect { status { isOk() } }

        mockMvc.get("/todos/$todoId/tasks/$taskId").andExpect { status { isNotFound() } }
    }

    @Test
    fun `a task is not reachable through another todo`() {
        val todoA = createTodo("A")
        val todoB = createTodo("B")
        val taskId = createTask(todoA)

        mockMvc.get("/todos/$todoB/tasks/$taskId").andExpect { status { isNotFound() } }
        mockMvc.delete("/todos/$todoB/tasks/$taskId").andExpect { status { isNotFound() } }
        mockMvc.get("/todos/$todoA/tasks/$taskId").andExpect { status { isOk() } }
    }

    @Test
    fun `creating a task for an unknown todo returns 404`() {
        mockMvc
            .post("/todos/${UUID.randomUUID()}/tasks") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"Task"}"""
            }.andExpect {
                status { isNotFound() }
                jsonPath("\$.success") { value(false) }
            }
    }

    @Test
    fun `validation errors return 400 with the failure envelope`() {
        mockMvc
            .post("/todos") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"description":"no name"}"""
            }.andExpect { status { isBadRequest() } }

        mockMvc
            .post("/todos") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":""}"""
            }.andExpect {
                status { isBadRequest() }
                jsonPath("\$.success") { value(false) }
                jsonPath("\$.message") { value("name must not be blank") }
            }
    }

    @Test
    fun `unknown todo returns 404 for get update and delete`() {
        val id = UUID.randomUUID()

        mockMvc.get("/todos/$id").andExpect { status { isNotFound() } }
        mockMvc
            .put("/todos/$id") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"x"}"""
            }.andExpect { status { isNotFound() } }
        mockMvc.delete("/todos/$id").andExpect { status { isNotFound() } }
    }
}
