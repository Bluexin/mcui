package be.bluexin.mcui.screens

import be.bluexin.mcui.Constants
import be.bluexin.mcui.api.scripting.*
import be.bluexin.mcui.themes.elements.Element
import be.bluexin.mcui.themes.elements.ElementGroup
import be.bluexin.mcui.themes.elements.Widget
import be.bluexin.mcui.themes.elements.WidgetParent
import be.bluexin.mcui.themes.util.HudDrawContext
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import java.util.*

class LuaTestScreen : Screen(Component.literal("Lua Test Screen")), WidgetParent {

    override val name = this::class.simpleName?.lowercase() ?: Element.DEFAULT_NAME

    private val root = ElementGroup().apply {
        name = "${this@LuaTestScreen.name}.root"
        setup(this@LuaTestScreen, emptyMap())
    }

    private val rootId = ResourceLocation("mcui", root.name)

    private val context by lazy { HudDrawContext() }

    override val elements: Iterable<Element>
        get() = widgets

    private val widgets: MutableList<Widget> = LinkedList()

    override fun init() {
        super.init()
        addRenderableWidget(
            Button.builder(
                Component.literal("Load script")
            ) {
                try {
                    LuaJTest.runScript(ResourceLocation("mcui", "test.lua"))
                } catch (e: Throwable) {
                    Minecraft.getInstance().player?.sendSystemMessage(Component.literal("Something went wrong : ${e.message}. See console for more info."))
                    Constants.LOG.error("Couldn't evaluate test.lua", e)
                }
            }.pos(50, 100).build()
        )
        addRenderableWidget(
            Button.builder(
                Component.translatable("Load widget script")
            ) {
                try {
                    LuaJTest.runScript(ResourceLocation("mcui", "themes/hex2/screens.lua"))
                } catch (e: Throwable) {
                    Minecraft.getInstance().player?.sendSystemMessage(Component.literal("Something went wrong : ${e.message}. See console for more info."))
                    Constants.LOG.error("Couldn't evaluate screens.lua", e)
                }
            }.pos(50, 120).build()
        )
        addRenderableWidget(
            Button.builder(
                Component.translatable("Load mcui:testgui Screen")
            ) {
                try {
                    RegisterScreen[ResourceLocation("mcui", "testgui")]?.invoke(rootId)
                } catch (e: Throwable) {
                    Minecraft.getInstance().player?.sendSystemMessage(Component.literal("Something went wrong : ${e.message}. See console for more info."))
                    Constants.LOG.error("Couldn't evaluate initializer for mcui:testgui", e)
                }
            }.pos(50, 140).build()
        )

        addRenderableOnly(Renderable { poseStack: PoseStack, mx: Int, my: Int, _: Float ->
            root.draw(context, poseStack, mx.toDouble(), my.toDouble())
        })
        widgets.forEach(::addRenderableWidget)
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        context.setTime(partialTick)
        super.render(poseStack, mouseX, mouseY, partialTick)
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