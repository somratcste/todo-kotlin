package info.somrat.todokotlin.repository

import info.somrat.todokotlin.model.Task
import info.somrat.todokotlin.model.Todo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.data.domain.PageRequest
import java.util.UUID

@DataJpaTest
class RepositoryTest
    @Autowired
    constructor(
        private val todoRepository: TodoRepository,
        private val taskRepository: TaskRepository,
        private val em: TestEntityManager,
    ) {
        @Test
        fun `saving a todo generates an id`() {
            assertThat(todoRepository.save(Todo("Todo", "desc")).id).isNotNull()
        }

        @Test
        fun `findAll is paged`() {
            repeat(5) { todoRepository.save(Todo("Todo $it")) }

            val page = todoRepository.findAll(PageRequest.of(1, 2))

            assertThat(page.totalElements).isEqualTo(5)
            assertThat(page.totalPages).isEqualTo(3)
            assertThat(page.content).hasSize(2)
            assertThat(page.number).isEqualTo(1)
        }

        @Test
        fun `findByIdAndTodoId finds a task only under its own todo`() {
            val todo = em.persist(Todo("Todo"))
            val other = em.persist(Todo("Other"))
            val task = em.persist(Task("Task", null, todo))
            em.flush()

            assertThat(taskRepository.findByIdAndTodoId(task.id!!, todo.id!!)?.id).isEqualTo(task.id)
            assertThat(taskRepository.findByIdAndTodoId(task.id!!, other.id!!)).isNull()
            assertThat(taskRepository.findByIdAndTodoId(UUID.randomUUID(), todo.id!!)).isNull()
        }

        @Test
        fun `deleting a todo cascades to its tasks`() {
            val todo = Todo("Todo")
            todo.tasks.add(Task("Task", null, todo))
            val saved = todoRepository.save(todo)
            em.flush()
            em.clear()

            todoRepository.delete(todoRepository.findById(saved.id!!).orElseThrow())
            em.flush()

            assertThat(todoRepository.existsById(saved.id!!)).isFalse()
            assertThat(taskRepository.count()).isZero()
        }

        @Test
        fun `removing a task from its todo deletes it (orphan removal)`() {
            val todo = Todo("Todo")
            val task = Task("Task", null, todo)
            todo.tasks.add(task)
            todoRepository.save(todo)
            em.flush()

            todo.tasks.remove(task)
            em.flush()

            assertThat(taskRepository.count()).isZero()
            assertThat(todoRepository.count()).isEqualTo(1)
        }
    }
