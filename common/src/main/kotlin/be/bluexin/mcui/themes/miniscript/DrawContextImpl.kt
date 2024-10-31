package be.bluexin.mcui.themes.miniscript

import be.bluexin.mcui.themes.miniscript.api.*
import be.bluexin.mcui.util.Client
import org.koin.core.annotation.Single

// TODO : migrate more hooks
@Single
internal class DrawContextImpl : DrawContext {
    val player: MiniscriptPlayer by lazy {
        // TODO : this needs to be set on world join !
        MiniscriptPlayerImplementation(requireNotNull(Client.mc.player) { "Player not yet initialized" })
    }

    override fun player(): MiniscriptPlayer = player

    override fun settings(): MiniscriptSettings = MiniscriptSettingsImpl
}
