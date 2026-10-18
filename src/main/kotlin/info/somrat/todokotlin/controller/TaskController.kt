package info.somrat.todokotlin.controller

import info.somrat.todokotlin.dto.ApiResponse
import info.somrat.todokotlin.dto.TaskRequest
import info.somrat.todokotlin.dto.TaskResponse
import info.somrat.todokotlin.service.TaskService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/todos/{todoId}/tasks")
class TaskController(
    private val taskService: TaskService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable todoId: UUID,
        @Valid @RequestBody request: TaskRequest,
    ): ApiResponse<TaskResponse> = ApiResponse.ok("Task created successfully!", taskService.create(todoId, request))

    @GetMapping("/{taskId}")
    fun get(
        @PathVariable todoId: UUID,
        @PathVariable taskId: UUID,
    ): ApiResponse<TaskResponse> = ApiResponse.ok("Task found successfully!", taskService.get(todoId, taskId))

    @PutMapping("/{taskId}")
    fun update(
        @PathVariable todoId: UUID,
        @PathVariable taskId: UUID,
        @Valid @RequestBody request: TaskRequest,
    ): ApiResponse<TaskResponse> =
        ApiResponse.ok("Task updated successfully!", taskService.update(todoId, taskId, request))

    @DeleteMapping("/{taskId}")
    fun delete(
        @PathVariable todoId: UUID,
        @PathVariable taskId: UUID,
    ): ApiResponse<Nothing> {
        taskService.delete(todoId, taskId)
        return ApiResponse.ok("Task deleted successfully!")
    }
}
