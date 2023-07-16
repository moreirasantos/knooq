package io.github.moreirasantos.knooq.testutils

import io.github.moreirasantos.knooq.Field
import io.github.moreirasantos.knooq.Record
import io.github.moreirasantos.knooq.SQLDataType
import io.github.moreirasantos.knooq.Table
import kotlinx.serialization.Serializable

private fun <T> identity(x: T): T = x

@Serializable
data class User(val id: Long, val name: String, val email: String)

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
