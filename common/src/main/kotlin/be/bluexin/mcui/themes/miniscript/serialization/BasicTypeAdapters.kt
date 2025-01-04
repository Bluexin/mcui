package be.bluexin.mcui.themes.miniscript.serialization

import be.bluexin.mcui.logger
import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.themes.miniscript.*
import gnu.jel.CompilationException
import gnu.jel.CompiledExpression
import gnu.jel.Evaluator
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed class BasicExpressionAdapter<CValueType : CValue<T>, T : Any> : KoinComponent {
    private val logger = logger()
    private val libHelper: LibHelper by inject()

    fun compile(v: ExpressionIntermediate): CValueType = try {
//        Constants.LOG.debug("Compiling ${v.expression}")
        value(v.cacheType.cacheExpression(wrap(Evaluator.compile(v.expression, libHelper.jelLibrary, type)), v))
    } catch (ce: CompilationException) {
        val sb = StringBuilder("A compilation error occurred during theme loading. See more info below.\n")
            .append("–––COMPILATION ERROR :\n")
            .append(ce.message).append('\n')
            .append("  ")
            .append(v.expression).append('\n')
        val column = ce.column // Column, where error was found
        for (i in 0 until column + 1) sb.append(' ')
        sb.append('^')
        /*val w = StringWriter()
        ce.printStackTrace(PrintWriter(w))
        sb.append('\n').append(w)*/
        val message = sb.toString()
        logger.error(message)
        AbstractThemeLoader.Reporter += message.substringAfterLast("–––COMPILATION ERROR :\n")
        default
    } catch (e: Throwable) {
        val message = "An unknown error occurred while compiling '${v.expression}'"
        logger.error(message, e)
        AbstractThemeLoader.Reporter += (e.message ?: "unknown error") + " in ${v.expression}"
        default
    }

    /**
     * The targeted class for this adapter's expression.

     * @return target
     */
    abstract val type: Class<*>?

    abstract fun value(c: CachedExpression<T>): CValueType

    /**
     * Wrap the expression using the appropriate [CompiledExpressionWrapper].

     * @param ce expression to wrap
     * *
     * @return wrapped expression
     */
    abstract fun wrap(ce: CompiledExpression): CompiledExpressionWrapper<T>

    abstract val jelType: JelType

    abstract val default: CValueType
}

/**
 * Adapts an expression that should return a int.
 */
data object IntExpressionAdapter : BasicExpressionAdapter<CInt, Int>() {
    override fun value(c: CachedExpression<Int>) = CInt(c)

    override val type: Class<*> = Integer.TYPE

    override fun wrap(ce: CompiledExpression) = IntExpressionWrapper(ce)

    override val jelType: JelType by lazy { JelType.INT }

    override val default get() = CInt.ZERO
}

/**
 * Adapts an expression that should return a double.
 */
data object DoubleExpressionAdapter : BasicExpressionAdapter<CDouble, Double>() {
    override fun value(c: CachedExpression<Double>) = CDouble(c)

    override val type: Class<*> = java.lang.Double.TYPE

    override fun wrap(ce: CompiledExpression) = DoubleExpressionWrapper(ce)

    override val jelType: JelType by lazy { JelType.DOUBLE }

    override val default get() = CDouble.ZERO
}

/**
 * Adapts an expression that should return a String.
 */
data object StringExpressionAdapter : BasicExpressionAdapter<CString, String>() {
    override fun value(c: CachedExpression<String>) = CString(c)

    override val type: Class<*> = String::class.java

    override fun wrap(ce: CompiledExpression) = StringExpressionWrapper(ce)

    override val jelType: JelType by lazy { JelType.STRING }

    override val default get() = CString.EMPTY
}

/**
 * Adapts an expression that should return a boolean.
 */
data object BooleanExpressionAdapter : BasicExpressionAdapter<CBoolean, Boolean>() {
    override fun value(c: CachedExpression<Boolean>) = CBoolean(c)

    override val type: Class<*> = java.lang.Boolean.TYPE

    override fun wrap(ce: CompiledExpression) = BooleanExpressionWrapper(ce)

    override val jelType: JelType by lazy { JelType.BOOLEAN }

    override val default get() = CBoolean.FALSE
}

/**
 * Adapts an expression that should return [Unit] (aka void).
 */
data object UnitExpressionAdapter : BasicExpressionAdapter<CUnit, Unit>() {
    override val type: Class<*>? = null

    override fun value(c: CachedExpression<Unit>) = CUnit(c)

    override fun wrap(ce: CompiledExpression) = UnitExpressionWrapper(ce)

    override val jelType: JelType by lazy { JelType.UNIT }

    override val default get() = CUnit.UNIT
}

