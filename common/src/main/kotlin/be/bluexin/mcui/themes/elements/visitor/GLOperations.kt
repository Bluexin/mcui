package be.bluexin.mcui.themes.elements.visitor

import be.bluexin.mcui.themes.miniscript.api.MiniscriptItemStack
import net.minecraft.resources.ResourceLocation

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
        stack: MiniscriptItemStack
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