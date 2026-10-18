package info.somrat.todokotlin.service

import info.somrat.todokotlin.aTask
import info.somrat.todokotlin.aTodo
import info.somrat.todokotlin.dto.TaskRequest
import info.somrat.todokotlin.exception.NotFoundException
import info.somrat.todokotlin.model.Task
import info.somrat.todokotlin.repository.TaskRepository
import info.somrat.todokotlin.repository.TodoRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.util.Optional
import java.util.UUID

class TaskServiceTest {
    private val taskRepository = mockk<TaskRepository>()
    private val todoRepository = mockk<TodoRepository>()
    private val service = TaskService(taskRepository, todoRepository)

    @Test
    fun `create attaches the task to its todo`() {
        val todo = aTodo()
        val newId = UUID.randomUUID()
        every { todoRepository.findById(todo.id!!) } returns Optional.of(todo)
        every { taskRepository.save(any<Task>()) } answers {
            firstArg<Task>().also { ReflectionTestUtils.setField(it, "id", newId) }
        }

        val result = service.create(todo.id!!, TaskRequest("Task 2", "Task 2 description"))

        assertThat(result.id).isEqualTo(newId)
        assertThat(result.name).isEqualTo("Task 2")
        assertThat(todo.tasks).extracting("name").containsExactly("Task 2")
    }

    @Test
    fun `create throws when todo is missing`() {
        val todoId = UUID.randomUUID()
        every { todoRepository.findById(todoId) } returns Optional.empty()

        assertThatThrownBy { service.create(todoId, TaskRequest("x")) }.isInstanceOf(NotFoundException::class.java)

        verify(exactly = 0) { taskRepository.save(any<Task>()) }
    }

    @Test
    fun `get returns the task`() {
        val todo = aTodo()
        val task = aTask(todo)
        every { taskRepository.findByIdAndTodoId(task.id!!, todo.id!!) } returns task

        val result = service.get(todo.id!!, task.id!!)

        assertThat(result.id).isEqualTo(task.id)
        assertThat(result.name).isEqualTo(task.name)
    }

    @Test
    fun `get throws when task is not under the todo`() {
        val todoId = UUID.randomUUID()
        val taskId = UUID.randomUUID()
        every { taskRepository.findByIdAndTodoId(taskId, todoId) } returns null

        assertThatThrownBy { service.get(todoId, taskId) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("Task not found with id $taskId for todo $todoId")
    }

    @Test
    fun `update changes name and description`() {
        val todo = aTodo()
        val task = aTask(todo, name = "old", description = "old")
        every { taskRepository.findByIdAndTodoId(task.id!!, todo.id!!) } returns task

        val result = service.update(todo.id!!, task.id!!, TaskRequest("Task 2 update", "Task 2 description update"))

        assertThat(task.name).isEqualTo("Task 2 update")
        assertThat(task.description).isEqualTo("Task 2 description update")
        assertThat(result.name).isEqualTo("Task 2 update")
    }

    @Test
    fun `update throws when task is missing`() {
        val todoId = UUID.randomUUID()
        val taskId = UUID.randomUUID()
        every { taskRepository.findByIdAndTodoId(taskId, todoId) } returns null

        assertThatThrownBy { service.update(todoId, taskId, TaskRequest("x")) }
            .isInstanceOf(NotFoundException::class.java)
    }

    @Test
    fun `delete detaches the task from its todo`() {
        val todo = aTodo()
        val task = aTask(todo)
        todo.tasks.add(task)
        every { taskRepository.findByIdAndTodoId(task.id!!, todo.id!!) } returns task

        service.delete(todo.id!!, task.id!!)

        assertThat(todo.tasks).isEmpty()
    }

    @Test
    fun `delete throws when task is missing`() {
        val todoId = UUID.randomUUID()
        val taskId = UUID.randomUUID()
        every { taskRepository.findByIdAndTodoId(taskId, todoId) } returns null

        assertThatThrownBy { service.delete(todoId, taskId) }.isInstanceOf(NotFoundException::class.java)
    }
}
