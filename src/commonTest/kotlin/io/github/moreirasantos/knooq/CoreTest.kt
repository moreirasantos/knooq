package io.github.moreirasantos.knooq

import io.github.moreirasantos.pgkn.PostgresDriver
import io.github.moreirasantos.knooq.testutils.User
import io.github.moreirasantos.knooq.testutils.UserTable
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContains

class CoreTest {

    private val driver = PostgresDriver(
        host = "localhost",
        port = 5678,
        database = "postgres",
        user = "postgres",
        password = "postgres",
    )

    private val database = Database(driver)

    // language=SQL
    private val createTable = """
        create table users
        (
            id integer not null constraint id primary key,
            name         text,
            email        text
        )
        """.trimIndent()

    // language=SQL
    private val insertUsers = """
        insert into users VALUES 
        (1, 'John', 'john@mail.com'),
        (2, 'Jane', 'jane@mail.com')
    """.trimIndent()

    @Test
    fun `should select from table`() {
        runBlocking {
            driver.execute(createTable)
            driver.execute(insertUsers)

            val users = database.select(UserTable.fields)
                .from(UserTable)
                .fetch()
                .intoClass(UserTable)

            assertContains(users, User(1, "John", "john@mail.com"))
            assertContains(users, User(2, "Jane", "jane@mail.com"))
        }
    }
}
