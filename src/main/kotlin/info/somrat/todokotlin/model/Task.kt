package info.somrat.todokotlin.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.util.UUID

@Entity
class Task(
    @Column(nullable = false)
    var name: String,
    @Column(length = 1000)
    var description: String? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "todo_id")
    val todo: Todo,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null
}
