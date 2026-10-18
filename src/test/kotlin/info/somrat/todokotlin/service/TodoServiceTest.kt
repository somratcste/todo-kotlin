package info.somrat.todokotlin.service

import info.somrat.todokotlin.aTask
import info.somrat.todokotlin.aTodo
import info.somrat.todokotlin.dto.TodoRequest
import info.somrat.todokotlin.exception.NotFoundException
import info.somrat.todokotlin.model.Todo
import info.somrat.todokotlin.repository.TodoRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.util.ReflectionTestUtils
import java.util.Optional
import java.util.UUID

class TodoServiceTest {
    private val todoRepository = mockk<TodoRepository>()
    private val service = TodoService(todoRepository)

    @Test
    fun `list returns a mapped page`() {
        val todo = aTodo()
        every { todoRepository.findAll(PageRequest.of(0, 10)) } returns PageImpl(listOf(todo), PageRequest.of(0, 10), 1)

        val result = service.list(0, 10)

        assertThat(result.success).isTrue()
        assertThat(result.totalItems).isEqualTo(1)
        assertThat(result.totalPage).isEqualTo(1)
        assertThat(result.currentPage).isZero()
        assertThat(result.data).extracting("id").containsExactly(todo.id)
    }

    @Test
    fun `get returns todo with tasks`() {
        val todo = aTodo()
        todo.tasks.add(aTask(todo))
        every { todoRepository.findById(todo.id!!) } returns Optional.of(todo)

        val result = service.get(todo.id!!)

        assertThat(result.id).isEqualTo(todo.id)
        assertThat(result.tasks).hasSize(1)
    }

    @Test
    fun `get throws when todo is missing`() {
        val id = UUID.randomUUID()
        every { todoRepository.findById(id) } returns Optional.empty()

        assertThatThrownBy { service.get(id) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("Todo not found with id $id")
    }

    @Test
    fun `create saves and returns the new todo`() {
        val newId = UUID.randomUUID()
        every { todoRepository.save(any<Todo>()) } answers {
            firstArg<Todo>().also { ReflectionTestUtils.setField(it, "id", newId) }
        }

        val result = service.create(TodoRequest("Todo 1", "todo 1 description"))

        assertThat(result.id).isEqualTo(newId)
        assertThat(result.name).isEqualTo("Todo 1")
        assertThat(result.description).isEqualTo("todo 1 description")
    }

    @Test
    fun `update changes name and description`() {
        val todo = aTodo(name = "old", description = "old")
        every { todoRepository.findById(todo.id!!) } returns Optional.of(todo)

        val result = service.update(todo.id!!, TodoRequest("new", "new desc"))

        assertThat(todo.name).isEqualTo("new")
        assertThat(todo.description).isEqualTo("new desc")
        assertThat(result.name).isEqualTo("new")
    }

    @Test
    fun `update throws when todo is missing`() {
        val id = UUID.randomUUID()
        every { todoRepository.findById(id) } returns Optional.empty()

        assertThatThrownBy { service.update(id, TodoRequest("x")) }.isInstanceOf(NotFoundException::class.java)
    }

    @Test
    fun `delete removes the todo`() {
        val todo = aTodo()
        every { todoRepository.findById(todo.id!!) } returns Optional.of(todo)
        every { todoRepository.delete(todo) } returns Unit

        service.delete(todo.id!!)

        verify(exactly = 1) { todoRepository.delete(todo) }
    }

    @Test
    fun `delete throws and deletes nothing when todo is missing`() {
        val id = UUID.randomUUID()
        every { todoRepository.findById(id) } returns Optional.empty()

        assertThatThrownBy { service.delete(id) }.isInstanceOf(NotFoundException::class.java)

        verify(exactly = 0) { todoRepository.delete(any<Todo>()) }
    }
}
