package be.bluexin.mcui.screens

import be.bluexin.mcui.Constants
import be.bluexin.mcui.themes.elements.Element
import be.bluexin.mcui.themes.elements.ElementGroup
import be.bluexin.mcui.themes.elements.Widget
import be.bluexin.mcui.themes.elements.WidgetParent
import be.bluexin.mcui.themes.meta.ThemeManager
import be.bluexin.mcui.themes.miniscript.HudDrawContext
import be.bluexin.mcui.themes.scripting.lib.LoadFragment
import be.bluexin.mcui.themes.scripting.lib.LoadWidget
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

class LuaScriptedScreen(
    private val screenId: ResourceLocation,
    private val themeId: ResourceLocation? = null
) : Screen(Component.literal("Lua Scripted Screen")), WidgetParent, KoinComponent {

    private val themeManager: ThemeManager by inject()

    override val name = screenId.toString()

    private val root = ElementGroup().apply {
        name = "${this@LuaScriptedScreen.name}.root"
        setup(this@LuaScriptedScreen, emptyMap())
    }

    private val rootId = ResourceLocation(root.name)

    private val context by lazy { HudDrawContext() }

    override val elements: Iterable<Element>
        get() = widgets

    private val widgets: MutableList<Widget> = LinkedList()

    init {
        LoadWidget[rootId] = this
        try {
            // TODO : caching
            if (themeId != null) {
                themeManager.getAllScreens(screenId)[themeId]?.invoke(rootId)
            } else {
                themeManager.getConfiguredScreen(screenId)?.invoke(rootId)
            }
        } catch (e: Throwable) {
            Minecraft.getInstance().player?.sendSystemMessage(Component.literal("Something went wrong : ${e.message}. See console for more info."))
            Constants.LOG.error("Couldn't evaluate initializer for $screenId", e)
        }
    }

    override fun setTooltipForNextRenderPass(tooltip: Component) {
        super.setTooltipForNextRenderPass(tooltip)
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (widgets.isEmpty()) return onClose()

        context.setTime(partialTick)
        widgets.forEach { it.draw(context, poseStack, mouseX.toDouble(), mouseY.toDouble()) }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        for (widget in widgets) {
            if (!widget.mouseClicked(mouseX, mouseY, button, context)) continue
//            this.focused = guiEventListener
            if (button == 0) {
                this.isDragging = true
            }
            return true
        }
        return false
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, delta: Double): Boolean {
        for (widget in widgets) {
            if (widget.mouseScrolled(mouseX, mouseY, delta, context)) return true
        }
        return false
    }

    override fun added() {
        super.added()
        LoadFragment[rootId] = root
        LoadWidget[rootId] = this
    }

    override fun removed() {
        super.removed()
        LoadFragment.clear(rootId)
        LoadWidget.clear(rootId)
    }

    override fun plusAssign(widget: Widget) {
        widget.setup(this, emptyMap())
        widgets += widget
    }
}