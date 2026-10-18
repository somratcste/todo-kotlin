package info.somrat.todokotlin.repository

import info.somrat.todokotlin.model.Task
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TaskRepository : JpaRepository<Task, UUID> {
    fun findByIdAndTodoId(
        id: UUID,
        todoId: UUID,
    ): Task?
}
