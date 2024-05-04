package be.bluexin.mcui.themes.elements

import net.minecraft.network.chat.Component

interface WidgetParent: ElementParent {
    operator fun plusAssign(widget: Widget)
    fun setTooltipForNextRenderPass(tooltip: Component)
}