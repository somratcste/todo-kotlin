package info.somrat.todokotlin.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import java.util.UUID

@Entity
class Todo(
    @Column(nullable = false)
    var name: String,
    @Column(length = 1000)
    var description: String? = null,
    @OneToMany(mappedBy = "todo", cascade = [CascadeType.ALL], orphanRemoval = true)
    val tasks: MutableList<Task> = mutableListOf(),
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null
}
