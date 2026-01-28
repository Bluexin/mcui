package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.LKExposed
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.themes.miniscript.CBoolean
import be.bluexin.mcui.themes.miniscript.CDouble
import be.bluexin.mcui.themes.miniscript.api.GameContext
import be.bluexin.mcui.themes.miniscript.api.MiniscriptItemStack
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.resources.ResourceLocation
import org.joml.Vector2dc

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

interface ElementVisitor {
    fun visit(element: Element, context: Context): Unit = element.visit(this, context)

    /**
     * @return whether the element should continue the visit
     */
    fun start(renderState: RenderState, context: Context): Boolean

    fun transform(transform: Transform, context: Context)

    fun draw(context: Context, body: GLOperations.() -> Unit)

    data class Context(
        val poseStack: PoseStack,
        val mouse: Vector2dc,
        val gameInfo: GameContext,
    )
}

interface GLOperations {
    fun configureBlend(enabled: Boolean)
    fun setColor(red: Float, green: Float, blue: Float, alpha: Float)
    fun setColor(rgba: Int)
    fun getShaderColor(): FloatArray
    fun bindTexture(texture: () -> ResourceLocation)
    fun drawString(
        string: String,
        x: Float,
        y: Float,
        rgba: Int,
        shadow: Boolean,
        centered: Boolean
    )

    fun drawRectangle(
        x: Double,
        y: Double,
        z: Double,
        width: Double,
        height: Double,
        sourceX: Double,
        sourceY: Double,
        sourceWidth: Double,
        sourceHeight: Double,
        textureWidth: Int,
        textureHeight: Int,
    )

    fun renderItemStack(
        x: Int,
        y: Int,
        partialTicks: Float,
        stack: MiniscriptItemStack,
        poseStack: PoseStack
    )

    companion object {
        inline fun GLOperations.withColor(rgba: Int?, block: () -> Unit) {
            if (rgba == null) block()
            else {
                val (r, g, b, a) = getShaderColor()
                setColor(rgba)
                try {
                    block()
                } finally {
                    setColor(r, g, b, a)
                }
            }
        }
    }
}
