package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.AfterSet
import be.bluexin.luajksp.annotations.BeforeSet
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.Constants
import be.bluexin.mcui.deprecated.api.themes.IHudDrawContext
import be.bluexin.mcui.themes.elements.access.FragmentReferenceAccess
import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.themes.meta.ThemeDefinition
import be.bluexin.mcui.themes.miniscript.*
import be.bluexin.mcui.themes.miniscript.serialization.JelType
import be.bluexin.mcui.themes.scripting.serialization.DeserializationOrder
import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.resources.ResourceLocation
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.luaj.vm2.LuaValue

@Serializable
@SerialName("fragmentReference")
@LuajExpose(LuajExpose.IncludeType.OPT_IN)
class FragmentReference(
    @DeserializationOrder(0)
    private val serializedVariables: Variables = Variables.EMPTY,
    private var id: String = MISSING_ID
) : Element(), ElementParent, KoinComponent, BeforeSet, AfterSet {
    private val libHelper: LibHelper by inject()

    init {
        if (serializedVariables !== Variables.EMPTY) get<LibHelper>().popContext()
    }

    override val rootElement: ElementParent
        get() = root.get() ?: this

    @Transient
    private val variables: MutableMap<String, CValue<*>?> =
        serializedVariables.variable.mapValuesTo(mutableMapOf()) { (_, it) ->
            if (it.expression.isEmpty()) null else it.type.expressionAdapter.compile(it)
    }

    @Transient
    private var fragment: Fragment? = null

    override fun setup(
        parent: ElementParent,
        fragments: Map<ResourceLocation, () -> Fragment>,
        theme: ThemeDefinition
    ): Boolean {
        val anonymous = super.setup(parent, fragments, theme)
        if (id == MISSING_ID) {
            val message = "Missing id in fragment reference "
            Constants.LOG.warn(message + hierarchyName)
            AbstractThemeLoader.Reporter += message + nameOrParent()
        } else {
            fragment = fragments[ResourceLocation(id)]?.invoke()
                ?.also { it.setup(this, fragments, theme) }
            if (fragment == null) {
                val message = "Missing fragment with id $id referenced in "
                Constants.LOG.warn(message + hierarchyName)
                AbstractThemeLoader.Reporter += message + nameOrParent()
            } else {
                val expect = fragment?.expect
                if (expect != null) {
                    val missing = expect.variables.filter { (key, it) ->
                        val inContext = variables[key]
                        inContext == null || inContext.type != it.type
                    }
                    expect.pushContext()
                    val defaults = missing.onEach { (key, it) ->
                        if (it.hasDefault()) variables[key] = it.type.expressionAdapter.compile(it)
                    }.keys
                    libHelper.popContext()
                    val realMissing = missing - defaults
                    if (realMissing.isNotEmpty()) {
                        val present = variables.mapValues { (_, value) -> value?.value?.expressionIntermediate }
                        val message = "Missing variables $realMissing for $id (present : $present) in "
                        Constants.LOG.warn(message + hierarchyName)
                        AbstractThemeLoader.Reporter += message + nameOrParent()
                    }
                }
            }
        }

        return anonymous
    }

    override fun draw(ctx: IHudDrawContext, poseStack: PoseStack, mouseX: Double, mouseY: Double) {
        if (!enabled(ctx)) return
        fragment?.let {
            poseStack.pushPose()
            val x = x(ctx)
            val y = y(ctx)
            poseStack.translate(x, y, z(ctx))

            scale?.let {
                val scale = it(ctx).toFloat()
                poseStack.scale(scale, scale, scale)
            }
            ctx.pushContext(variables)
            it.draw(ctx, poseStack, mouseX - x, mouseY - y)
            ctx.popContext()
            poseStack.popPose()
        }
    }

    override val elements: Iterable<Element>
        get() = fragment?.let(::listOf) ?: emptyList()

    private val CValue<*>?.type get() = (this?.value?.expressionIntermediate as? NamedExpressionIntermediate)?.type

    override fun toLua(): LuaValue = FragmentReferenceAccess(this)

    override fun getZ(ctx: IHudDrawContext): Double {
        ctx.pushContext(variables)
        val r = z(ctx)
        ctx.popContext()
        return r
    }

    override fun beforeSet() {
        libHelper.pushContext(
            variables.mapValues { (_, value) -> value.type ?: JelType.ERROR }
        )
    }

    override fun afterSet() {
        libHelper.popContext()
    }

    private companion object {
        private const val MISSING_ID = "@@MISSING_ID@@"
    }
}
