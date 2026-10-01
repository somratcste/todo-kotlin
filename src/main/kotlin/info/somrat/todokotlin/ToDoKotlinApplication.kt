package info.somrat.todokotlin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ToDoKotlinApplication

fun main(args: Array<String>) {
    runApplication<ToDoKotlinApplication>(*args)
}
