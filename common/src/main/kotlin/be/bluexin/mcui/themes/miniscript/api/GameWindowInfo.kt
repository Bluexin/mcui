package be.bluexin.mcui.themes.miniscript.api

import be.bluexin.mcui.themes.miniscript.PartialTicksTracker
import be.bluexin.mcui.util.Client
import org.koin.core.annotation.Single

@Suppress("unused") // JEL access
interface GameWindowInfo {
    /**
     * @return screen width, scaled
     */
    val scaledWidth: Int

    /**
     * @return screen heigth, scaled
     */
    val scaledHeight: Int

    /**
     * @return screen width, scaled (JEL easy access)
     */
    fun scaledWidth(): Int = scaledWidth

    /**
     * @return screen heigth, scaled (JEL easy access)
     */
    fun scaledHeight(): Int = scaledHeight

    /**
     * the frame partial ticks
     */
    val partialTicks: Float

    /**
     * @return the frame partial ticks (JEL easy access)
     */
    fun partialTicks(): Float = partialTicks
}

@Single
internal class GameWindowInfoImpl(
    private val partialTicksTracker: PartialTicksTracker
) : GameWindowInfo {
    private val window by lazy { Client.mc.window }

    override val scaledWidth get() = window.guiScaledWidth
    override val scaledHeight get() = window.guiScaledHeight
    override val partialTicks get() = partialTicksTracker.partialTicks
}