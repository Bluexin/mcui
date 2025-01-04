package be.bluexin.mcui.themes.miniscript

import be.bluexin.mcui.themes.miniscript.api.*
import be.bluexin.mcui.util.Client
import org.koin.core.annotation.Single

// TODO : migrate more hooks
@Single
internal class DrawContextImpl(
    private val gameWindowInfo: GameWindowInfo,
    private val miniscriptSettings: MiniscriptSettings
) : DrawContext {

    private var playerCache: MiniscriptPlayerImpl? = null
    override fun player(): MiniscriptPlayer {
        val thePlayer = Client.mc.player
        if (thePlayer == null && playerCache != null) {
            playerCache = null
        } else if (playerCache?.player !== thePlayer) {
            playerCache = thePlayer?.let(::MiniscriptPlayerImpl)
        }
        return requireNotNull(playerCache) { "Player not yet initialized" }
    }

    override fun settings() = miniscriptSettings
    override fun gameWindowInfo() = gameWindowInfo
}
