package info.somrat.todokotlin.controller

import com.ninjasquad.springmockk.MockkBean
import info.somrat.todokotlin.dto.TaskRequest
import info.somrat.todokotlin.dto.TaskResponse
import info.somrat.todokotlin.exception.NotFoundException
import info.somrat.todokotlin.service.TaskService
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

@WebMvcTest(TaskController::class)
class TaskControllerTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
    ) {
        @MockkBean
        private lateinit var taskService: TaskService

        private val todoId = UUID.randomUUID()
        private val taskId = UUID.randomUUID()
        private val base = "/todos/$todoId/tasks"

        @Test
        fun `POST task creates a task`() {
            every { taskService.create(todoId, TaskRequest("Task 2", "Task 2 description")) } returns
                TaskResponse(taskId, "Task 2", "Task 2 description")

            mockMvc
                .post(base) {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"name":"Task 2","description":"Task 2 description"}"""
                }.andExpect {
                    status { isCreated() }
                    jsonPath("\$.success") { value(true) }
                    jsonPath("\$.message") { value("Task created successfully!") }
                    jsonPath("\$.object.id") { value(taskId.toString()) }
                    jsonPath("\$.object.name") { value("Task 2") }
                }
        }

        @Test
        fun `POST task with blank name is rejected`() {
            mockMvc
                .post(base) {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"name":"","description":"x"}"""
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("\$.message") { value("name must not be blank") }
                }

            verify(exactly = 0) { taskService.create(any(), any()) }
        }

        @Test
        fun `POST task returns 404 when the todo is missing`() {
            every { taskService.create(todoId, TaskRequest("Task")) } throws
                NotFoundException("Todo not found with id $todoId")

            mockMvc
                .post(base) {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"name":"Task"}"""
                }.andExpect {
                    status { isNotFound() }
                    jsonPath("\$.success") { value(false) }
                }
        }

        @Test
        fun `GET task returns it`() {
            every { taskService.get(todoId, taskId) } returns TaskResponse(taskId, "Task 2", "Task 2 description")

            mockMvc.get("$base/$taskId").andExpect {
                status { isOk() }
                jsonPath("\$.message") { value("Task found successfully!") }
                jsonPath("\$.object.id") { value(taskId.toString()) }
                jsonPath("\$.object.description") { value("Task 2 description") }
            }
        }

        @Test
        fun `GET task returns 404 when missing`() {
            every { taskService.get(todoId, taskId) } throws
                NotFoundException("Task not found with id $taskId for todo $todoId")

            mockMvc.get("$base/$taskId").andExpect {
                status { isNotFound() }
                jsonPath("\$.message") { value("Task not found with id $taskId for todo $todoId") }
            }
        }

        @Test
        fun `GET task with an invalid id is a bad request`() {
            mockMvc.get("$base/nope").andExpect { status { isBadRequest() } }
        }

        @Test
        fun `PUT task updates it`() {
            val request = TaskRequest("Task 2 update", "Task 2 description update")
            every { taskService.update(todoId, taskId, request) } returns
                TaskResponse(taskId, "Task 2 update", "Task 2 description update")

            mockMvc
                .put("$base/$taskId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"name":"Task 2 update","description":"Task 2 description update"}"""
                }.andExpect {
                    status { isOk() }
                    jsonPath("\$.message") { value("Task updated successfully!") }
                    jsonPath("\$.object.name") { value("Task 2 update") }
                }
        }

        @Test
        fun `PUT task with blank name is rejected`() {
            mockMvc
                .put("$base/$taskId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"name":" "}"""
                }.andExpect { status { isBadRequest() } }
        }

        @Test
        fun `DELETE task deletes it`() {
            every { taskService.delete(todoId, taskId) } just runs

            mockMvc.delete("$base/$taskId").andExpect {
                status { isOk() }
                jsonPath("\$.message") { value("Task deleted successfully!") }
            }

            verify(exactly = 1) { taskService.delete(todoId, taskId) }
        }

        @Test
        fun `DELETE task returns 404 when missing`() {
            every { taskService.delete(todoId, taskId) } throws NotFoundException("Task not found")

            mockMvc.delete("$base/$taskId").andExpect { status { isNotFound() } }
        }
    }
