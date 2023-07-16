package io.github.moreirasantos.knooq

import kotlin.reflect.KClass

enum class SQLDialect {
    POSTGRES
}

sealed interface DataType<T : Any> {
    val length: Int?
    fun length(length: Int): DataType<T>
    fun convert(value: Any): T
}

internal class DefaultDataType<T : Any>(
    private val dialect: SQLDialect?,
    private val type: KClass<T>,
    private val typeName: String,
    override val length: Int? = null
) : DataType<T> {
    override fun length(length: Int): DataType<T> = DefaultDataType(dialect, type, typeName, length)

    @Suppress("UNCHECKED_CAST")
    override fun convert(value: Any): T {
        // TODO("Not yet implemented")
        return value as T
    }
}

object SQLDataType {

    val VARCHAR: DataType<String> = DefaultDataType(null, String::class, "varchar(l)")

    val CHAR: DataType<String> = DefaultDataType(null, String::class, "char(l)")

    val LONGVARCHAR: DataType<String> = DefaultDataType(null, String::class, "longvarchar(l)")

    val CLOB: DataType<String> = DefaultDataType(null, String::class, "clob")

    val NVARCHAR: DataType<String> = DefaultDataType(null, String::class, "nvarchar(l)")

    val NCHAR: DataType<String> = DefaultDataType(null, String::class, "nchar(l)")

    val LONGNVARCHAR: DataType<String> = DefaultDataType(null, String::class, "longnvarchar(l)")

    val NCLOB: DataType<String> = DefaultDataType(null, String::class, "nclob")

    val BOOLEAN: DataType<Boolean> = DefaultDataType(null, Boolean::class, "boolean")

    val BIT: DataType<Boolean> = DefaultDataType(null, Boolean::class, "bit")

    val TINYINT: DataType<Byte> = DefaultDataType(null, Byte::class, "tinyint")

    val SMALLINT: DataType<Short> = DefaultDataType(null, Short::class, "smallint")

    val INTEGER: DataType<Int> = DefaultDataType(null, Int::class, "integer")

    val BIGINT: DataType<Long> = DefaultDataType(null, Long::class, "bigint")

    val TINYINTUNSIGNED: DataType<UByte> = DefaultDataType(null, UByte::class, "tinyint unsigned")

    val SMALLINTUNSIGNED: DataType<UShort> = DefaultDataType(null, UShort::class, "smallint unsigned")

    val BIGINTUNSIGNED: DataType<ULong> = DefaultDataType(null, ULong::class, "bigint unsigned")

    val DOUBLE: DataType<Double> = DefaultDataType(null, Double::class, "double")

    val FLOAT: DataType<Double> = DefaultDataType(null, Double::class, "float")

    val REAL: DataType<Float> = DefaultDataType(null, Float::class, "real")

    val BINARY: DataType<ByteArray> = DefaultDataType(null, ByteArray::class, "binary(l)")

    val VARBINARY: DataType<ByteArray> = DefaultDataType(null, ByteArray::class, "varbinary(l)")

    val LONGVARBINARY: DataType<ByteArray> = DefaultDataType(null, ByteArray::class, "longvarbinary(l)")

    val BLOB: DataType<ByteArray> = DefaultDataType(null, ByteArray::class, "blob")

    val OTHER: DataType<Any> = DefaultDataType(null, Any::class, "other")

    val RECORD: DataType<Record> = DefaultDataType(null, Record::class, "record")

    @Suppress("UNCHECKED_CAST")
    val RESULT: DataType<Result<Record>> = DefaultDataType(
        null,
        Result::class as KClass<Result<Record>>,
        "result"
    )
}
