package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.AfterSet
import be.bluexin.luajksp.annotations.BeforeSet
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.Constants
import be.bluexin.mcui.deprecated.api.themes.IHudDrawContext
import be.bluexin.mcui.logger
import be.bluexin.mcui.themes.elements.access.WidgetAccess
import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.themes.meta.ThemeDefinition
import be.bluexin.mcui.themes.meta.ThemeManager
import be.bluexin.mcui.themes.miniscript.*
import be.bluexin.mcui.themes.miniscript.serialization.JelType
import be.bluexin.mcui.themes.scripting.LuaJManager
import be.bluexin.mcui.themes.scripting.serialization.DeserializationOrder
import be.bluexin.mcui.util.Client
import be.bluexin.mcui.util.info
import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import nl.adaptivity.xmlutil.serialization.XmlBefore
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * Widget
 */
@Serializable
@SerialName("widget")
@XmlSerialName(value = "widget")
@LuajExpose(LuajExpose.IncludeType.OPT_IN)
class Widget(
    @XmlSerialName("expect")
    @XmlBefore("x", "children", "texture")
    @DeserializationOrder(0)
    val expect: Expect? = null,
    /**
     * Width used for hover events
     */
    @XmlSerialName("contentWidth")
    @LuajExpose
    var contentWidth: CInt,
    /**
     * Height used for hover events
     */
    @XmlSerialName("contentHeight")
    @LuajExpose
    var contentHeight: CInt,
    /**
     * Whether this widget is interactive
     */
    @XmlSerialName("active")
    @LuajExpose
    var active: CBoolean = CBoolean.TRUE,
    @XmlElement
    @XmlSerialName("onClick")
    val onClickScript: String? = null,
    @XmlElement
    @XmlSerialName("onMouseOverEvent")
    val onMouseOverEventScript: String? = null,
    @XmlElement
    @XmlSerialName
    @LuajExpose
    var tooltip: CString? = null,
    @XmlElement
    @XmlSerialName("onLoseFocus")
    val onLoseFocusScript: String? = null,
) : ElementGroupParent(), GuiEventListener, Renderable, NarratableEntry, WidgetParent, KoinComponent, BeforeSet,
    AfterSet {

    private val themeManager: ThemeManager by inject()
    private val luaJManager: LuaJManager by inject()
    private val libHelper: LibHelper by inject()

    @Transient
    private var focused = false

    /**
     * Whether the mouse is currently over this widget
     */
    @Transient
    @LuajExpose
    var isMouseOver = false
        private set

    /**
     * Called when the mouse is clicked inside this widget.
     * Basic check against contentWidth/contentHeight already performed.
     * Returning false will let the parent handle the event.
     *
     * @param self Widget the receiver widget
     * @param mouseX number the mouse's X position, relative to this widget
     * @param mouseY number the mouse's Y position, relative to this widget
     * @param mouseButton mouse_buttons which button was pressed
     * @return boolean whether the click was handled
     */
    @LuajExpose
    var onClick: Widget.(Double, Double, Int) -> Boolean = { _, _, _ -> false }

    /**
     * Called when the mouse wheel is scrolled over this widget.
     * Basic check against contentWidth/contentHeight already performed.
     * Returning false will let the parent handle the event.
     *
     * @param self Widget the receiver widget
     * @param mouseX number the mouse's X position, relative to this widget
     * @param mouseY number the mouse's Y position, relative to this widget
     * @param delta number the scroll delta
     * @return boolean whether the click was handled
     */
    @LuajExpose
    var onScroll: Widget.(Double, Double, Double) -> Boolean = { _, _, _ -> false }

    /**
     * Called when the mouse enters or leaves the widget
     *
     * @param self Widget the receiver widget
     * @param mouseX number the mouse's X position, relative to this widget
     * @param mouseY number the mouse's Y position, relative to this widget
     * @param isMouseOver boolean whether the mouse entered (true) or left (false)
     */
    @LuajExpose
    var onMouseOverEvent: Widget.(Double, Double, Boolean) -> Unit = { _, _, _ -> }

    /**
     * Called when the widget isn't being focused anymore
     *
     * @param self Widget the receiver widget
     */
    @LuajExpose
    var onLoseFocus: Widget.() -> Unit = { }

    @Transient
    private val variables: MutableMap<String, CValue<*>?> = mutableMapOf()

    /**
     * Table to store arbitrary Lua data.
     */
    @LuajExpose
    @Transient // TODO : be able to include these in deserialization ?
    var extra: LuaTable = LuaTable()

    @Transient
    private lateinit var theme: ThemeDefinition

    init {
        if (expect != null) libHelper.popContext()
    }

    private inline fun <T> IHudDrawContext.withContext(crossinline body: (IHudDrawContext) -> T): T {
        pushContext(variables)
        val r = body(this)
        popContext()
        return r
    }

    /**
     * Mouse coords relative to this widget's (x,y)
     */
    private fun checkMouseOver(
        mouseX: Double,
        mouseY: Double,
        ctx: IHudDrawContext,
    ): Boolean {
        val wasMouseOver = isMouseOver
        val scale = scale?.let { it(ctx) } ?: 1.0
        isMouseOver = isActive(ctx) && mouseX >= 0 && mouseX < contentWidth(ctx) * scale
                && mouseY >= 0 && mouseY < contentHeight(ctx) * scale
        if (isMouseOver != wasMouseOver) {
            try {
                onMouseOverEvent(mouseX, mouseY, isMouseOver)
            } catch (e: Throwable) {
                Client.showError("Error while evaluating onMouseOverEvent handler for $name", e)
                onMouseOverEvent = { _, _, _ -> }
            }
        }

        return isMouseOver
    }

    override fun draw(ctx: IHudDrawContext, poseStack: PoseStack, mouseX: Double, mouseY: Double) {
        ctx.withContext {
            if (!enabled(it)) return@withContext
            checkMouseOver(mouseX - x(it), mouseY - y(it), it)

            prepareDraw(it, poseStack)
            drawChildren(it, poseStack, mouseX, mouseY)
            if (isMouseOver) tooltip?.let { tt -> setTooltipForNextRenderPass(Component.literal(tt(it))) }
            finishDraw(it, poseStack)
        }
    }

    override fun setTooltipForNextRenderPass(tooltip: Component) {
        when (val parent = parent.get()) {
            is WidgetParent -> parent.setTooltipForNextRenderPass(tooltip)
        }
    }

    override fun setup(
        parent: ElementParent,
        fragments: Map<ResourceLocation, () -> Fragment>,
        theme: ThemeDefinition
    ): Boolean {
        this.theme = theme
        val anonymous = super.setup(parent, fragments, theme)

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
                val message = "Missing variables $realMissing for (present : $present) in "
                Constants.LOG.warn(message + hierarchyName)
                AbstractThemeLoader.Reporter += message + nameOrParent()
            }
        }

        val access = toLua()
        loadCallbackFromScript(access, onClickScript, ::onClick.name, theme)
        loadCallbackFromScript(access, onMouseOverEventScript, ::onMouseOverEvent.name, theme)
        loadCallbackFromScript(access, onLoseFocusScript, ::onLoseFocus.name, theme)

        return anonymous
    }

    private fun loadCallbackFromScript(access: LuaValue, script: String?, name: String, theme: ThemeDefinition) {
        if (!script.isNullOrBlank()) try {
            access.set(
                name,
                luaJManager.compileSnippet("${this.name}/$name".lowercase(), script, theme)
            )
        } catch (e: Throwable) {
            AbstractThemeLoader.Reporter += e.message ?: "Unknown error loading ${this.name}/$name"
            Constants.LOG.warn("Error loading ${this.name}/$name", e)
        }
    }

    /**
     * Add a variable to this Widget's context
     */
    @LuajExpose
    fun setVariable(key: String, variable: CValue<*>?) {
        variables[key] = variable
    }

    /**
     * Get a variable from this Widget's context
     */
    @LuajExpose
    fun getVariable(key: String): CValue<*>? = variables[key]

    override fun setFocused(focused: Boolean) {
        this.focused = focused
    }

    override fun isFocused(): Boolean = focused

    /*override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return withContext { ctx ->
            checkMouseOver(mouseX, mouseY, ctx)
            isMouseOver && (
                    children.any { it is Widget && it.mouseClicked(mouseX, mouseY, button) } ||
                            checkOnClickSafely(ctx, mouseX, mouseY, button)
                    )
        } ?: false
    }*/

    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int, ctx: IHudDrawContext): Boolean =
        ctx.withContext { context ->
            if (!enabled(ctx)) return@withContext false
            val relMouseX = mouseX - x(ctx)
            val relMouseY = mouseY - y(ctx)
            children.any { it is Widget && it.mouseClicked(relMouseX, relMouseY, button, ctx) }
                    || (checkMouseOver(relMouseX, relMouseY, context)
                    && checkOnClickSafely(relMouseX, relMouseY, button))
        }

    fun mouseScrolled(mouseX: Double, mouseY: Double, delta: Double, ctx: IHudDrawContext): Boolean =
        ctx.withContext { context ->
            if (!enabled(ctx)) return@withContext false
            val relMouseX = mouseX - x(ctx)
            val relMouseY = mouseY - y(ctx)
            children.any { it is Widget && it.mouseScrolled(mouseX, mouseY, delta, ctx) }
                    || (checkMouseOver(relMouseX, relMouseY, context)
                    && checkOnScrollSafely(relMouseX, relMouseY, delta))
        }

    /**
     * Mouse coords relative to this widget's (x,y)
     */
    private fun checkOnClickSafely(mouseX: Double, mouseY: Double, button: Int): Boolean = try {
        onClick(mouseX, mouseY, button)
    } catch (e: Throwable) {
        Client.showError("Error while evaluating onClick handler for $name", e)
        onClick = { _, _, _ -> false }
        false
    }

    /**
     * Mouse coords relative to this widget's (x,y)
     */
    private fun checkOnScrollSafely(mouseX: Double, mouseY: Double, delta: Double): Boolean = try {
        onScroll(mouseX, mouseY, delta)
    } catch (e: Throwable) {
        Client.showError("Error while evaluating onScroll handler for $name", e)
        onScroll = { _, _, _ -> false }
        false
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) = Unit

//    override fun isActive(): Boolean = withContext(::isActive) ?: false

    private fun isActive(ctx: IHudDrawContext) = enabled(ctx) && active(ctx)

    override fun updateNarration(narrationElementOutput: NarrationElementOutput) {
//        TODO("Not yet implemented")
    }

    // FIXME
    override fun narrationPriority() = NarratableEntry.NarrationPriority.FOCUSED

    private val CValue<*>?.type get() = (this?.value?.expressionIntermediate as? NamedExpressionIntermediate)?.type

    override fun toLua(): LuaValue = WidgetAccess(this)

    override fun plusAssign(widget: Widget) {
        widget.setup(this, emptyMap(), theme)
        add(widget)
    }

    @LuajExpose
    fun setFocus() {
        logger.info { "Setting focus to self ($hierarchyName)" }
        (rootElement as? WidgetParent)?.setFocus(this)
    }

    private val logger = logger()

    fun loseFocus() {
        logger.info { "$hierarchyName loses focus" }
        onLoseFocus()
    }

    override fun setFocus(target: Widget) {
        logger.info { "Setting focus on self ($hierarchyName) to ${target.hierarchyName}" }
        val root = rootElement
        if (root != this && root is WidgetParent) root.setFocus(target)
    }

    override fun beforeSet() {
        libHelper.pushContext(
            variables.mapValues { (_, value) -> value.type ?: JelType.ERROR }
        )
    }

    override fun afterSet() {
        libHelper.popContext()
    }
}
