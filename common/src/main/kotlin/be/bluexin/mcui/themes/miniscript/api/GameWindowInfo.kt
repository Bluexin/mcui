package be.bluexin.mcui.themes.miniscript.api

import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.themes.miniscript.PartialTicksTracker
import be.bluexin.mcui.util.Client
import org.koin.core.annotation.Single

@Suppress("unused") // JEL access
@LuajExpose
interface GameWindowInfo {

    /**
     * @return screen width, scaled (JEL easy access)
     */
    fun scaledWidth(): Int

    /**
     * @return screen heigth, scaled (JEL easy access)
     */
    fun scaledHeight(): Int

    /**
     * @return the frame partial ticks (JEL easy access)
     */
    fun partialTicks(): Float
}

@Single
internal class GameWindowInfoImpl(
    private val partialTicksTracker: PartialTicksTracker
) : GameWindowInfo {
    private val window by lazy { Client.mc.window }

    override fun scaledWidth() = window.guiScaledWidth
    override fun scaledHeight() = window.guiScaledHeight
    override fun partialTicks() = partialTicksTracker.partialTicks
}