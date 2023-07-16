package io.github.moreirasantos.knooq

import io.github.moreirasantos.pgkn.PostgresDriver

private val logger = KLogger("io.github.moreirasantos.knooq.CoreKt")

class Config(val driver: PostgresDriver)
class Database(driver: PostgresDriver) {
    private val config: Config = Config(driver)

    fun select(fields: List<Field<*>>) = Builder<Record>(config).select(fields)
}

private class Builder<R : Record>(
    override val config: Config,
    override val query: Query = InternalQuery(config)
) : Step.Select<R>

sealed interface Step {

    sealed interface Select<R : Record> : From<R> {
        fun select(fields: List<Field<*>>) = apply { query.addSelect(fields) }
    }

    sealed interface From<R : Record> : Join<R> {
        override val query: Query
        fun from(table: Table<R>) = apply { query.addFrom(table) }
    }

    sealed interface Join<R : Record> : Where<R> {
        override val query: Query
        fun join(table: Table<R>) = apply { }
    }

    sealed interface Where<R : Record> : Limit<R> {
        override val query: Query
        fun where(condition: Condition) = apply { query.addConditions(condition) }

    }

    sealed interface Limit<R : Record> : Fetch<R> {
        override val query: Query
        fun limit(limit: Long) = apply { query.addLimit(limit) }

    }

    sealed interface Fetch<R : Record> {
        val config: Config
        val query: Query

        suspend fun fetch(): Result<Record> = query.executeQuery()
    }

    sealed class Result<R : Record>(private val records: List<R>) : List<R> by records {
        fun <Z : Record> into(table: Table<Z>): Result<Z> = InternalResult(this.map { it.into(table) })

        fun <T, Z : Record.DataRecord<T>> intoClass(table: Table<Z>) = this.asSequence()
            .map { it.into(table) }
            .map(Record.DataRecord<T>::into)
            .toList()
    }

}

private class InternalResult<R : Record>(records: List<R>) : Step.Result<R>(records)

interface Condition

object NoCondition : Condition

private class CombinedCondition(val operator: Operator, val conditions: List<Condition>) : Condition {
    constructor(operator: Operator, left: Condition, right: Condition) : this(operator, listOf(left, right))
}

sealed class Query(private val config: Config) {
    private var result: Step.Result<Record>? = null
    private val fields: MutableList<Field<*>> = mutableListOf()
    private val from: MutableList<Table<*>> = mutableListOf()
    private var condition: Condition = DSL.NO_CONDITION
    private var limit: QueryLimit = QueryLimit()
    fun addSelect(fields: List<Field<*>>) = this.fields.addAll(fields)

    fun <R : Record> addFrom(table: Table<R>) = from.add(table)

    fun addConditions(condition: Condition) {
        this.condition = DSL.condition(Operator.AND, this.condition, condition);
    }

    fun addLimit(numberOfRows: Long) {
        limit.setNumberOfRows(numberOfRows)
    }

    private suspend fun execute(): Int {
        // A super simple render for now

        val sql = buildString {
            append("select ")
            // TODO Default value is bad, let's make name non-nullable in some way
            append(fields.joinToString(separator = ", ", transform = { it.name ?: "" }))
            // TODO
            append(" from ${from.first().name}")
        }
        logger.debug { "Executing: $sql" }

        result = config.driver.execute(sql) {
            Record.DbRecord(
                fields = fields,
                values = List(fields.size) { index -> it.getString(index) })
        }.let(::InternalResult)

        return 1
    }

    suspend fun executeQuery(): Step.Result<Record> {
        execute()
        return result!! //TODO will this ever need null check
    }
}

class QueryLimit {
    private var numberOfRows: Param<Long>? = null
    fun setNumberOfRows(numberOfRows: Long) {
        this.numberOfRows = DSL.`val`(numberOfRows, SQLDataType.BIGINT)
    }
}

enum class Operator(val sql: String) { AND("and"), OR("or") }

private class InternalQuery(config: Config) : Query(config)

sealed interface Table<out R : Record> {
    val name: String

    val fields: List<Field<*>>

    val newRecord: () -> R

    interface DataTable<out R : Record, T> : Table<R>
}

open class Field<T : Any>(
    val name: String?,
    val dataType: DataType<T>,
    private val converter: (String) -> T
) {
    // TODO : move converter to internal class? idk
    fun get(record: Record): T? = record.get(this, converter)
}

open class TableField<R : Record, T : Any>(
    name: String,
    dataType: DataType<T>,
    converter: (String) -> T,
    val table: Table<R>
) : Field<T>(name, dataType, converter)

open class Param<T : Any>(val value: T, name: String?, dataType: DataType<T>) : Field<T>(name, dataType, { it as T }) {
    var inline: Boolean = false
}

class Val<T : Any>(value: T, dataType: DataType<T>, name: String?) : Param<T>(value, name, dataType)

sealed interface Record {
    val fields: List<Field<*>>
    var values: List<Any?>

    fun <T : Any> get(field: Field<T>, converter: (String) -> T): T? = get(fields.indexOf(field), converter)

    fun <T> get(index: Int, converter: (String) -> T): T = converter(this.values[index] as String)

    fun <R : Record> into(table: Table<R>): R = table.newRecord().apply { this.values = this@Record.values }

    class DbRecord(
        override val fields: List<Field<*>>,
        override var values: List<Any?>
    ) : Record

    interface DataRecord<T> : Record {
        fun into(): T
    }
}

object DSL {
    fun condition(operator: Operator, left: Condition, right: Condition): Condition = when {
        left is NoCondition -> right
        right is NoCondition -> left
        else -> CombinedCondition(operator, left, right)
    }

    val NO_CONDITION: Condition = NoCondition

    @Suppress("FunctionNaming")
    fun <T : Any> `val`(value: Any, field: Field<T>) = `val`(value, field.dataType)
    @Suppress("FunctionNaming")
    fun <T : Any> `val`(value: Any, type: DataType<T>) = Val(type.convert(value), type, null)

    fun <T : Any> inline(value: Any, type: DataType<T>) = Val(type.convert(value), type, null).apply {
        this.inline = true
    }
}
