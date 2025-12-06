package be.bluexin.mcui.themes.miniscript

import be.bluexin.mcui.themes.miniscript.api.*
import be.bluexin.mcui.util.Client
import org.koin.core.annotation.Single

// TODO : migrate more hooks
@Single
internal class GameContextImpl(
    private val gameWindowInfo: GameWindowInfo,
    private val miniscriptSettings: MiniscriptSettings
) : GameContext {
    override fun party() = emptyList<MiniscriptPlayer>()

    private var playerCache: MiniscriptPlayerImpl? = null
    override fun player(): MiniscriptPlayer {
        val thePlayer = Client.mc.player
        if (thePlayer === null && playerCache != null) {
            playerCache = null
        } else if (playerCache?.player !== thePlayer) {
            playerCache = thePlayer?.let(::MiniscriptPlayerImpl)
        }
        return requireNotNull(playerCache) { "Player not yet initialized" }
    }

    override fun settings() = miniscriptSettings
    override fun gameWindowInfo() = gameWindowInfo

    override fun currentlyOpenContainer(): MiniscriptContainer? = null // TODO
}
