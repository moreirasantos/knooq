package io.github.moreirasantos.knooq.context

import io.github.moreirasantos.knooq.RenderKeywordCase
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


@OptIn(ExperimentalContracts::class)
internal inline fun renderContext(builderAction: RenderContext.() -> Unit): RenderContext {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return RenderContext().apply(builderAction)
}

sealed interface Context<C : Context<C>> {
    fun sql(c: Char): C
    fun sql(string: String, literal: Boolean): C
    fun getRenderKeywordCase() = RenderKeywordCase.AS_IS

    val separatorRequired: Boolean
    fun separatorRequired(separatorRequired: Boolean): C
    fun declareFields(d: Boolean): C
    fun declareAliases(d: Boolean): C
}

@Suppress("UNCHECKED_CAST")
internal sealed class AbstractContext<C : Context<C>> : Context<C> {

    var declareFields: Boolean = false
    var declareAliases: Boolean = false
    override fun declareFields(d: Boolean): C {
        this.declareFields = d
        declareAliases(d)
        return this as C
    }

    override fun declareAliases(d: Boolean): C {
        this.declareAliases = d
        return this as C
    }
}


internal class RenderContext : AbstractContext<RenderContext>() {

    val sql = StringBuilder()

    override var separatorRequired: Boolean = false
    override fun separatorRequired(separatorRequired: Boolean): RenderContext {
        this.separatorRequired = separatorRequired
        return this
    }

    private var separator: Boolean = false

    private val indent = 0
    private val stringLiteral = 0
    private val stringLiteralEscapedApos = "'"
    private var newline = false
    private val cachedNewline = "\n"


    override fun sql(c: Char): RenderContext {
        applyNewLine();
        sql.append(c);

        if (c == '\'' && stringLiteral()) sql.append(c)

        resetSeparatorFlags();
        return this;

    }

    override fun sql(string: String, literal: Boolean): RenderContext {
        var s = string

        if (!literal) s = NEWLINE.replace(s, "$0" + indentation())

        if (stringLiteral()) s = s.replace("'", stringLiteralEscapedApos)

        applyNewLine();
        sql.append(s);
        resetSeparatorFlags();
        return this;
    }


    private fun indentation() = "".padStart(indent, indentation)
    private fun stringLiteral() = stringLiteral > 0
    private fun applyNewLine() {
        if (newline) {
            sql.append(cachedNewline);
            sql.append(indentation());
        }
    }

    private fun resetSeparatorFlags() {
        separatorRequired = false
        separator = false
        newline = false
    }

    companion object {
        private val NEWLINE = Regex("[\\n\\r]")
        private const val indentation: Char = ' '
    }
}
