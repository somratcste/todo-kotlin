package info.somrat.todokotlin.repository

import info.somrat.todokotlin.model.Todo
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TodoRepository : JpaRepository<Todo, UUID>
