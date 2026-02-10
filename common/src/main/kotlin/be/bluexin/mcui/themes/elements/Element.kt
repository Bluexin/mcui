package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.LKExposed
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.themes.elements.visitor.ElementVisitor
import be.bluexin.mcui.themes.miniscript.CBoolean
import be.bluexin.mcui.themes.miniscript.CDouble

interface Element : LKExposed {
    val transform: Transform

    fun visit(visitor: ElementVisitor, context: ElementVisitor.Context)
}

@LuajExpose
data class RenderState(
    var enabled: CBoolean,
    var name: String
)

@LuajExpose
data class Transform(
    var x: CDouble,
    var y: CDouble,
    var z: CDouble,
    var scale: CDouble?
) {
    companion object {
        val ZERO = Transform(CDouble.ZERO, CDouble.ZERO, CDouble.ZERO, null)
    }
}

