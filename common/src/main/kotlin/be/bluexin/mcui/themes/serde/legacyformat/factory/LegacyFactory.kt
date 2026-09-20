package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.themes.elements.Element
import be.bluexin.mcui.themes.elements.RenderState
import be.bluexin.mcui.themes.elements.Transform
import be.bluexin.mcui.themes.miniscript.*
import be.bluexin.mcui.themes.miniscript.serialization.*
import be.bluexin.mcui.themes.serde.Factory
import be.bluexin.mcui.themes.serde.Factory.Context
import be.bluexin.mcui.themes.serde.legacyformat.xml.ChildrenXml
import be.bluexin.mcui.themes.serde.legacyformat.xml.ElementXml
import be.bluexin.mcui.themes.serde.legacyformat.xml.ExpressionIntermediate
import kotlin.reflect.KClass
import kotlin.reflect.KProperty0

internal sealed class LegacyFactory<IN : ElementXml, out OUT : Any>(
    val xmlType: KClass<out IN>
) : Factory<IN, OUT> {

    fun <T : ElementXml.WithRenderState> createRenderState(input: T, context: Context) = RenderState(
        enabled = input::enabled.compileBoolean(context) ?: CBoolean.TRUE,
        name = input.name,
    )

    fun <T : ElementXml.WithTransform> createTransform(input: T, context: Context) = Transform(
        x = input::x.compileDouble(context) ?: CDouble.ZERO,
        y = input::y.compileDouble(context) ?: CDouble.ZERO,
        z = input::z.compileDouble(context) ?: CDouble.ZERO,
        scale = input::scale.compileDouble(context)
    )

    private fun <T> KProperty0<ExpressionIntermediate?>.compile(
        context: Context,
        compile: (ExpressionIntermediate) -> Result<T>
    ): T? = this()?.let { value ->
        context.nested(this.name)
        val r = value.let(compile)
        if (r.isFailure) {
            context.error("Failed to compile ${value.expression} : ${r.exceptionOrNull()?.message}")
        }
        context.pop()
        return r.getOrNull()
    }

    protected fun KProperty0<ExpressionIntermediate?>.compileInt(context: Context): CInt? =
        compile(context, IntExpressionAdapter::tryCompile)

    protected fun KProperty0<ExpressionIntermediate?>.compileDouble(context: Context): CDouble? =
        compile(context, DoubleExpressionAdapter::tryCompile)

    protected fun KProperty0<ExpressionIntermediate?>.compileString(context: Context): CString? =
        compile(context, StringExpressionAdapter::tryCompile)

    protected fun KProperty0<ExpressionIntermediate?>.compileBoolean(context: Context): CBoolean? =
        compile(context, BooleanExpressionAdapter::tryCompile)

    protected fun KProperty0<ExpressionIntermediate?>.compileUnit(context: Context): CUnit? =
        compile(context, UnitExpressionAdapter::tryCompile)

    protected fun createChildren(
        children: ChildrenXml?,
        context: Context,
        registry: LegacyFactoryRegistry
    ): List<Element> = children?.elements?.mapNotNull { childXml ->
        val childName = (childXml as? ElementXml.WithRenderState)?.name ?: "unknown"
        context.nested(childName)
        val result = registry.createFromXml(childXml, context)
        context.pop()

        if (result.isFailure) {
            context.error("Failed to create child element: ${result.exceptionOrNull()?.message}")
        }
        result.getOrNull()
    }.orEmpty()
}