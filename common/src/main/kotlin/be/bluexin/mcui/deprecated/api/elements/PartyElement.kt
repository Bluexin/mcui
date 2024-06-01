package be.bluexin.mcui.deprecated.api.elements

import be.bluexin.mcui.util.Client
import be.bluexin.mcui.util.IconCore
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.resources.language.I18n

class PartyElement : IconLabelElement(IconCore.PARTY, I18n.get("sao.element.party")) {

    val player: LocalPlayer? get() = Client.mc.player

    // val partyElements = mutableListOf<PTMemberElement>()
//    val partyCap by lazy { player.getPartyCapability() }

    /**
     * Override the default element sequence to merge our party elements while keeping them separate.
     */
    // override val elementsSequence: Sequence<NeoElement> = elements.asSequence().filter(NeoElement::listed).plus(partyElements.asSequence().filter(NeoElement::listed))
    // override val otherElementsSequence: Sequence<NeoElement> = elements.asSequence().filter { !it.listed }.plus(partyElements.asSequence().filter { !it.listed })

    /*init {
        MinecraftForge.EVENT_BUS.register(this)
        resetPartyData()
    }

    @SubscribeEvent
    fun partyRefresh(e: PartyEvent.Refresh) {
        resetPartyData()
    }*/

    private fun resetPartyData() {
        // Only remove invalid entries, this will prevent menus
        // abruptly closing during refresh.
        if (this.selected) {
            (this.parent as CategoryButton).close(true)
            (this.parent as CategoryButton).open(true)
        }
        elements.clear()
        partyExtras()
        /*elements.removeIf {
            it is PTMemberElement && partyCap.partyData?.isMember(it.player) != true &&
                partyCap.partyData?.isInvited(it.player) != true
        }*/
        // elements[0].valid = !(partyCap.partyData?.isLeader(Minecraft().player) == false)
        // Don't bother with updating party functions if single player.
        /*if (1 < (Client.mc.integratedServer?.currentPlayerCount ?: 2)) {
            partyList()
        }*/
        if (!this.selected) elementsSequence.forEach { it.hide() }
    }

    fun partyList() {
        /*val party = partyCap.partyData

        if (party != null) {
            party.getMembers().filter { !it.equals(player) }.forEach { ptmember ->
                if (elements.none { it is PTMemberElement && !it.equals(ptmember) }) {
                    +partyMemberButton(party, ptmember, player)
                }
            }

            party.getInvited().forEach { ptmember ->
                if (elements.none { it is PTMemberElement && !it.equals(ptmember) }) {
                    +partyMemberButton(party, ptmember, player, true)
                }
            }
        }*/
    }

    /*fun partyMemberButton(party: IPartyData, player: PlayerInfo, ourPlayer: Player, invited: Boolean = false): CategoryButton =
        CategoryButton(PTMemberElement(player), this.tlParent) {
            +CategoryButton(IconLabelElement(IconCore.HELP, I18n.get("sao.element.inspect")), this.tlParent)
            if (party.leaderInfo.equals(ourPlayer)) {
                +CategoryButton(IconLabelElement(IconCore.CANCEL, I18n.get("sao.party.${if (invited) "cancel" else "kick"}")), this.tlParent).onClick { _, _ ->
                    if (invited) {
                        Type.CANCELINVITE.updateServer(player, PartyType.MAIN)
                        true
                    } else {
                        Type.KICK.updateServer(player, PartyType.MAIN)
                        true
                    }
                }
            }
        }*/

    fun partyExtras() {
        /*val party = partyCap.partyData

        if ((party != null && party.isLeader(player)) || party == null) +CategoryButton(IconLabelElement(IconCore.INVITE, I18n.get("sao.party.invite")), this.tlParent) {
            Client.mc.connection?.playerInfoMap?.filter { it.gameProfile.id != player.uniqueID }?.forEach {
                +CategoryButton(IconLabelElement(PlayerIcon(PlayerInfo(it.gameProfile.id, it.gameProfile.name)), it.gameProfile.name), this.tlParent).onClick { _, _ ->
                    Type.INVITE.updateServer(PlayerInfo(it.gameProfile.id, it.gameProfile.name), PartyType.MAIN)
                    true
                }
            }
        }
        if (party != null && party.size > 1) +CategoryButton(IconLabelElement(IconCore.CANCEL, I18n.get("sao.party.leave")), this.tlParent).onClick { _, _ ->
            Type.LEAVE.updateServer(player.playerInfo(), PartyType.MAIN)
            true
        }

        partyCap.inviteData.forEach { inviteParty ->
            +CategoryButton(IconLabelElement(PlayerIcon(inviteParty.leaderInfo), I18n.get("sao.party.invited", inviteParty.leaderInfo.username)), this.tlParent) {
                +CategoryButton(IconLabelElement(IconCore.CONFIRM, I18n.get("sao.misc.accept")), this.tlParent).onClick { _, _ ->
                    Type.ACCEPTINVITE.updateServer(player.playerInfo(), PartyType.INVITE)
                    true
                }
                +CategoryButton(IconLabelElement(IconCore.CANCEL, I18n.get("sao.misc.decline")), this.tlParent).onClick { _, _ ->
                    Type.CANCELINVITE.updateServer(player.playerInfo(), PartyType.INVITE)
                    true
                }
            }
        }*/
    }
}
