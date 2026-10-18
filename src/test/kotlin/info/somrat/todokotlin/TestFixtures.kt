package info.somrat.todokotlin

import info.somrat.todokotlin.model.Task
import info.somrat.todokotlin.model.Todo
import org.springframework.test.util.ReflectionTestUtils
import java.util.UUID

fun aTodo(
    name: String = "Todo 1",
    description: String? = "todo description",
    id: UUID? = UUID.randomUUID(),
): Todo = Todo(name, description).also { ReflectionTestUtils.setField(it, "id", id) }

fun aTask(
    todo: Todo,
    name: String = "Task 1",
    description: String? = "task description",
    id: UUID? = UUID.randomUUID(),
): Task = Task(name, description, todo).also { ReflectionTestUtils.setField(it, "id", id) }
