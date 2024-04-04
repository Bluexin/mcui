package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.Constants
import be.bluexin.mcui.api.scripting.LuaJTest
import be.bluexin.mcui.api.themes.IHudDrawContext
import be.bluexin.mcui.themes.elements.access.toLua
import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.themes.meta.ThemeManager
import be.bluexin.mcui.themes.util.*
import be.bluexin.mcui.util.Client
import be.bluexin.mcui.util.DeserializationOrder
import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.client.gui.GuiComponent
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.resources.ResourceLocation
import nl.adaptivity.xmlutil.serialization.XmlBefore
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.luaj.vm2.LuaValue

/**
 * Widget
 */
@Serializable
@XmlSerialName(value = "widget")
@LuajExpose(LuajExpose.IncludeType.OPT_IN)
class Widget(
    @XmlSerialName("expect")
    @XmlBefore("x", "children", "texture")
    @DeserializationOrder(0)
    val expect: Expect? = null,
    @XmlSerialName("contentWidth")
    @LuajExpose
    var contentWidth: CInt,
    @XmlSerialName("contentHeight")
    @LuajExpose
    var contentHeight: CInt,
    @XmlSerialName("active")
    @LuajExpose
    var active: CBoolean = CBoolean.TRUE,
    @XmlElement
    @XmlSerialName("onClick")
    val onClickScript: String? = null,
    @XmlElement
    @XmlSerialName("onMouseOverEvent")
    val onMouseOverEventScript: String? = null,
) : ElementGroupParent(), GuiEventListener, Renderable, NarratableEntry {

    @Transient
    var TMP_CTX: IHudDrawContext? = null

    @Transient
    private var focused = false

    @Transient
    @LuajExpose
    var isMouseOver = false
        private set

    /**
     * Basic check against contentWidth/contentHeight already performed.
     * Returning false will let the parent handle the event.
     *
     * @param mouseX number the mouse's X position, relative to this widget
     * @param mouseY number the mouse's Y position, relative to this widget
     * @param mouseButton number which button was pressed
     * @return boolean whether the click was handled
     * @see mouse_buttons (lua)
     */
    @LuajExpose
    var onClick: Widget.(Double, Double, Int) -> Boolean = { _, _, _ -> false }

    /**
     * @param mouseX number the mouse's X position, relative to this widget
     * @param mouseY number the mouse's Y position, relative to this widget
     * @param isMouseOver boolean whether the mouse entered (true) or left (false)
     */
    @LuajExpose
    var onMouseOverEvent: Widget.(Double, Double, Boolean) -> Unit = { _, _, _ -> }

    @Transient
    private val variables: MutableMap<String, CValue<*>?> = mutableMapOf()

    init {
        if (expect != null) LibHelper.popContext()
    }

    private inline fun <T> withContext(body: (IHudDrawContext) -> T): T? = TMP_CTX?.let {
        it.pushContext(variables)
        val r = body(it)
        it.popContext()
        r
    }

    private fun checkMouseOver(mouseX: Int, mouseY: Int, ctx: IHudDrawContext) =
        checkMouseOver(mouseX.toDouble(), mouseY.toDouble(), ctx)

    private fun checkMouseOver(mouseX: Double, mouseY: Double, ctx: IHudDrawContext) {
        val x = x(ctx)
        val y = y(ctx)
        val wasMouseOver = isMouseOver
        val scale = scale?.let { it(ctx) } ?: 1.0
        isMouseOver = isActive(ctx) && mouseX >= x && mouseX < x + contentWidth(ctx) * scale
                && mouseY >= y && mouseY < y + contentHeight(ctx) * scale
        if (isMouseOver != wasMouseOver) {
            try {
                onMouseOverEvent(mouseX - x, mouseY - y, isMouseOver)
            } catch (e: Throwable) {
                Client.showError("Error while evaluating onMouseOverEvent handler for $name", e)
                onMouseOverEvent = { _, _, _ -> }
            }
        }
    }

    override fun draw(ctx: IHudDrawContext, poseStack: PoseStack) {
        if (!enabled(ctx)) return

        prepareDraw(ctx, poseStack)
        /*GuiComponent.fill(
            poseStack,
            0, 0,
            contentWidth(ctx),
            contentHeight(ctx),
            0,
            if (isMouseOver) 0xd632ef44.toInt() else 0x1082e544
        )*/

        drawChildren(ctx, poseStack)
        finishDraw(ctx, poseStack)
    }

    override fun setup(parent: ElementParent, fragments: Map<ResourceLocation, () -> Fragment>): Boolean {
        val anonymous = super.setup(parent, fragments)

        val missing = expect?.variables.orEmpty().filter { (key, it) ->
            val inContext = variables[key]
            inContext == null || inContext.type != it.type
        }
        val defaults = missing.onEach { (key, it) ->
            if (it.hasDefault()) variables[key] = it.type.expressionAdapter.compile(it)
        }.keys
        val realMissing = missing - defaults
        if (realMissing.isNotEmpty()) {
            val present = variables.mapValues { (_, value) -> value?.value?.expressionIntermediate }
            val message = "Missing variables $realMissing for (present : $present) in "
            Constants.LOG.warn(message + hierarchyName())
            AbstractThemeLoader.Reporter += message + nameOrParent()
        }

        val access = toLua()
        loadCallbackFromScript(access, onClickScript, ::onClick.name)
        loadCallbackFromScript(access, onMouseOverEventScript, ::onMouseOverEvent.name)

        return anonymous
    }

    private fun loadCallbackFromScript(access: LuaValue, script: String?, name: String) {
        if (!script.isNullOrBlank()) try {
            access.set(name, LuaJTest.compileSnippet("${this.name}/$name".lowercase(), script, ThemeManager.currentTheme.id))
        } catch (e: Throwable) {
            AbstractThemeLoader.Reporter += e.message ?: "Unknown error loading ${this.name}/$name"
            Constants.LOG.warn("Error loading ${this.name}/$name", e)
        }
    }

    @LuajExpose
    fun setVariable(key: String, variable: CValue<*>?) {
        variables[key] = variable
    }

    @LuajExpose
    fun getVariable(key: String): CValue<*>? = variables[key]

    override fun setFocused(focused: Boolean) {
        this.focused = focused
    }

    override fun isFocused(): Boolean = focused

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return withContext { ctx ->
            checkMouseOver(mouseX, mouseY, ctx)
            isMouseOver && (
                    children.any { it is Widget && it.mouseClicked(mouseX, mouseY, button) } ||
                            checkOnClickSafely(ctx, mouseX, mouseY, button)
                    )
        } ?: false
    }

    private fun checkOnClickSafely(ctx: IHudDrawContext, mouseX: Double, mouseY: Double, button: Int): Boolean = try {
        onClick(mouseX - x(ctx), mouseY - y(ctx), button)
    } catch (e: Throwable) {
        Client.showError("Error while evaluating onClick handler for $name", e)
        onClick = { _, _, _ -> false }
        false
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        withContext {
            checkMouseOver(mouseX, mouseY, it)
            this.draw(it, poseStack)
        }
    }

    override fun isActive(): Boolean = withContext(::isActive) ?: false

    private fun isActive(ctx: IHudDrawContext) = enabled(ctx) && active(ctx)

    override fun updateNarration(narrationElementOutput: NarrationElementOutput) {
//        TODO("Not yet implemented")
    }

    // FIXME
    override fun narrationPriority() = NarratableEntry.NarrationPriority.FOCUSED

    private val CValue<*>?.type get() = (this?.value?.expressionIntermediate as? NamedExpressionIntermediate)?.type
}
