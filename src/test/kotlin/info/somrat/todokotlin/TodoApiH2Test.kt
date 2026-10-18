package info.somrat.todokotlin

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc

/** Fast end-to-end run against the in-memory H2 database. */
@SpringBootTest
@AutoConfigureMockMvc
class TodoApiH2Test : AbstractTodoApiTest()
