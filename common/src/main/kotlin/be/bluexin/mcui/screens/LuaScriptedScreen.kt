package be.bluexin.mcui.screens

import be.bluexin.mcui.Constants
import be.bluexin.mcui.api.scripting.JNLua
import be.bluexin.mcui.api.scripting.LoadFragment
import be.bluexin.mcui.api.scripting.LoadWidget
import be.bluexin.mcui.api.scripting.RegisterScreen
import be.bluexin.mcui.themes.elements.Element
import be.bluexin.mcui.themes.elements.ElementGroup
import be.bluexin.mcui.themes.elements.Widget
import be.bluexin.mcui.themes.elements.WidgetParent
import be.bluexin.mcui.themes.util.HudDrawContext
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import java.util.*

class LuaScriptedScreen(
    private val screenId: ResourceLocation
) : Screen(Component.literal("Lua Scripted Screen")), WidgetParent {

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
            RegisterScreen[screenId]?.invoke(rootId)
        } catch (e: Throwable) {
            Minecraft.getInstance().player?.sendSystemMessage(Component.literal("Something went wrong : ${e.message}. See console for more info."))
            Constants.LOG.error("Couldn't evaluate initializer for mcui:testgui", e)
        }
    }

    override fun init() {
        super.init()

        addRenderableOnly(Renderable { poseStack: PoseStack, mx: Int, my: Int, _: Float ->
            root.draw(context, poseStack, mx.toDouble(), my.toDouble())
        })
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
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
        JNLua.close()
    }

    override fun plusAssign(widget: Widget) {
        widget.setup(this, emptyMap())
        widgets += widget
        addRenderableWidget(widget)
    }
}