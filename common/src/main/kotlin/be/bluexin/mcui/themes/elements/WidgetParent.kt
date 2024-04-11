package be.bluexin.mcui.themes.elements

interface WidgetParent: ElementParent {
    operator fun plusAssign(widget: Widget)
}