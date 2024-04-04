package be.bluexin.mcui.themes.elements

import net.minecraft.resources.ResourceLocation

interface WidgetParent: ElementParent {

    val rootId: ResourceLocation
    operator fun plusAssign(widget: Widget)
}