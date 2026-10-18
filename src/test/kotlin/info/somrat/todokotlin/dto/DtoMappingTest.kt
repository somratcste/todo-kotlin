package info.somrat.todokotlin.dto

import info.somrat.todokotlin.aTask
import info.somrat.todokotlin.aTodo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class DtoMappingTest {
    @Test
    fun `todo maps to response`() {
        val todo = aTodo(name = "Todo", description = "desc")

        assertThat(todo.toResponse()).isEqualTo(TodoResponse(todo.id!!, "Todo", "desc"))
    }

    @Test
    fun `todo maps to detail response including tasks`() {
        val todo = aTodo()
        val task = aTask(todo, name = "Task A")
        todo.tasks.add(task)

        val response = todo.toDetailResponse()

        assertThat(response.id).isEqualTo(todo.id)
        assertThat(response.tasks).containsExactly(TaskResponse(task.id!!, "Task A", "task description"))
    }

    @Test
    fun `task maps to response and keeps null description`() {
        assertThat(aTask(aTodo(), description = null).toResponse().description).isNull()
    }

    @Test
    fun `unsaved entity cannot be mapped`() {
        assertThatThrownBy { aTodo(id = null).toResponse() }.isInstanceOf(IllegalArgumentException::class.java)
    }
}
