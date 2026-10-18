package info.somrat.todokotlin.service

import info.somrat.todokotlin.dto.TaskRequest
import info.somrat.todokotlin.dto.TaskResponse
import info.somrat.todokotlin.dto.toResponse
import info.somrat.todokotlin.exception.NotFoundException
import info.somrat.todokotlin.model.Task
import info.somrat.todokotlin.repository.TaskRepository
import info.somrat.todokotlin.repository.TodoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val todoRepository: TodoRepository,
) {
    @Transactional
    fun create(
        todoId: UUID,
        request: TaskRequest,
    ): TaskResponse {
        val todo =
            todoRepository
                .findById(todoId)
                .orElseThrow { NotFoundException("Todo not found with id $todoId") }
        val task = Task(request.name, request.description, todo)
        todo.tasks.add(task)
        return taskRepository.save(task).toResponse()
    }

    fun get(
        todoId: UUID,
        taskId: UUID,
    ): TaskResponse = find(todoId, taskId).toResponse()

    fun update(
        todoId: UUID,
        taskId: UUID,
        request: TaskRequest,
    ): TaskResponse {
        val task =
            find(todoId, taskId).apply {
                name = request.name
                description = request.description
            }
        return task.toResponse()
    }

    fun delete(
        todoId: UUID,
        taskId: UUID,
    ) {
        val task = find(todoId, taskId)
        task.todo.tasks.remove(task)
    }

    private fun find(
        todoId: UUID,
        taskId: UUID,
    ): Task =
        taskRepository.findByIdAndTodoId(taskId, todoId)
            ?: throw NotFoundException("Task not found with id $taskId for todo $todoId")
}
