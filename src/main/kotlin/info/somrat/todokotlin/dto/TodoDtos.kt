package info.somrat.todokotlin.dto

import info.somrat.todokotlin.model.Task
import info.somrat.todokotlin.model.Todo
import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class TodoRequest(
    @field:NotBlank(message = "name must not be blank")
    val name: String,
    val description: String? = null,
)

data class TodoResponse(
    val id: UUID,
    val name: String,
    val description: String?,
)

data class TodoDetailResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val tasks: List<TaskResponse>,
)

data class TaskRequest(
    @field:NotBlank(message = "name must not be blank")
    val name: String,
    val description: String? = null,
)

data class TaskResponse(
    val id: UUID,
    val name: String,
    val description: String?,
)

fun Todo.toResponse() = TodoResponse(requireNotNull(id), name, description)

fun Todo.toDetailResponse() = TodoDetailResponse(requireNotNull(id), name, description, tasks.map { it.toResponse() })

fun Task.toResponse() = TaskResponse(requireNotNull(id), name, description)
