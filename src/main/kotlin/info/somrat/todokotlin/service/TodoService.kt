package info.somrat.todokotlin.service

import info.somrat.todokotlin.dto.PageResponse
import info.somrat.todokotlin.dto.TodoDetailResponse
import info.somrat.todokotlin.dto.TodoRequest
import info.somrat.todokotlin.dto.TodoResponse
import info.somrat.todokotlin.dto.toDetailResponse
import info.somrat.todokotlin.dto.toResponse
import info.somrat.todokotlin.exception.NotFoundException
import info.somrat.todokotlin.model.Todo
import info.somrat.todokotlin.repository.TodoRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TodoService(
    private val todoRepository: TodoRepository,
) {
    fun list(
        pageNo: Int,
        pageSize: Int,
    ): PageResponse<TodoResponse> {
        val page = todoRepository.findAll(PageRequest.of(pageNo, pageSize))
        return PageResponse(
            totalItems = page.totalElements,
            totalPage = page.totalPages,
            currentPage = page.number,
            data = page.content.map { it.toResponse() },
        )
    }

    fun get(id: UUID): TodoDetailResponse = find(id).toDetailResponse()

    fun create(request: TodoRequest): TodoResponse =
        todoRepository.save(Todo(request.name, request.description)).toResponse()

    fun update(
        id: UUID,
        request: TodoRequest,
    ): TodoResponse {
        val todo =
            find(id).apply {
                name = request.name
                description = request.description
            }
        return todo.toResponse()
    }

    fun delete(id: UUID) = todoRepository.delete(find(id))

    private fun find(id: UUID): Todo =
        todoRepository.findById(id).orElseThrow { NotFoundException("Todo not found with id $id") }
}
