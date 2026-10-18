package info.somrat.todokotlin.controller

import info.somrat.todokotlin.dto.ApiResponse
import info.somrat.todokotlin.dto.PageResponse
import info.somrat.todokotlin.dto.TodoDetailResponse
import info.somrat.todokotlin.dto.TodoRequest
import info.somrat.todokotlin.dto.TodoResponse
import info.somrat.todokotlin.service.TodoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/todos")
class TodoController(
    private val todoService: TodoService,
) {
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") pageNo: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
    ): PageResponse<TodoResponse> = todoService.list(pageNo.coerceAtLeast(0), pageSize.coerceIn(1, 100))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: TodoRequest,
    ): ApiResponse<TodoResponse> = ApiResponse.ok("Todo created successfully!", todoService.create(request))

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: UUID,
    ): ApiResponse<TodoDetailResponse> = ApiResponse.ok("Todo found successfully!", todoService.get(id))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: TodoRequest,
    ): ApiResponse<TodoResponse> = ApiResponse.ok("Todo updated successfully!", todoService.update(id, request))

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID,
    ): ApiResponse<Nothing> {
        todoService.delete(id)
        return ApiResponse.ok("Todo deleted successfully!")
    }
}
