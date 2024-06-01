package be.bluexin.mcui.deprecated.api.elements

import be.bluexin.mcui.deprecated.screens.util.PlayerIcon
import net.minecraft.client.multiplayer.PlayerInfo
import net.minecraft.client.resources.language.I18n

class PTMemberElement(val player: PlayerInfo) : IconLabelElement(
    PlayerIcon(player),
    if (/*party?.isInvited(player) == true*/ false) I18n.get(
        "sao.party.player_invited",
        player.displayNameString
    ) else player.displayNameString
) {

    val invited = false //party?.isInvited(player) ?: false

    override fun update() {
        super.update()
        /*if ((invited && party?.isInvited(player) != true) || party?.isMember(player) != true) {
            (parent as? CategoryButton)?.elements?.remove(this)
        }*/
    }

    companion object {
//        val party = Client.mc.player.getPartyCapability().partyData
//        val invitedParty = Client.mc.player.getPartyCapability().inviteData
    }
}

val PlayerInfo.displayNameString: String get() = tabListDisplayName?.string ?: profile.name
