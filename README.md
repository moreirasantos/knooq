[![Kotlin Experimental](https://kotl.in/badges/experimental.svg)](https://kotlinlang.org/docs/components-stability.html)
[![CI](https://github.com/miguel-moreira/pgkn/actions/workflows/blank.yml/badge.svg?branch=main)](https://github.com/miguel-moreira/knooq/actions/workflows/blank.yml)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.moreirasantos/knooq)](https://central.sonatype.com/artifact/io.github.moreirasantos/knooq/)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.0-blue.svg?logo=kotlin)](http://kotlinlang.org)

# knOOQ
knOOQ, that stands for **Kotlin/Native Object Oriented Querying**, is a DSL that models the SQL language as a type safe Kotlin Native API.

Inspired by [jOOQ](https://github.com/jOOQ/jOOQ)

## Usage
```
val db: Database = Database(PostgresDriver(
        host = "localhost",
        port = 5678,
        database = "postgres",
        user = "postgres",
        password = "postgres"))

db.select(UserTable.fields)
        .from(UserTable)
        .fetch()
        .intoClass(UserTable)
```
Currently there is no code generation from Database, but you can declare your own classes:
```
class UserRecord : Record.DataRecord<User> {
    override val fields: List<Field<*>> get() = listOf(UserTable.ID, UserTable.NAME, UserTable.EMAIL)
    override var values: List<Any?> = emptyList()
    override fun into(): User = User(UserTable.ID.get(this)!!, UserTable.NAME.get(this)!!, UserTable.EMAIL.get(this)!!)
}
object UserTable : Table.DataTable<UserRecord, User> {
    val ID: Field<Long> = Field("id", SQLDataType.BIGINT, String::toLong)
    val NAME: Field<String> = Field("name", SQLDataType.VARCHAR, ::identity)
    val EMAIL: Field<String> = Field("email", SQLDataType.VARCHAR, ::identity)

    override val name: String = "users"
    override val fields: List<Field<*>> get() = listOf(ID, NAME, EMAIL)
    override val newRecord: () -> UserRecord = ::UserRecord
}

private fun <T> identity(x: T): T = x
```
