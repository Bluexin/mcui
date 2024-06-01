package be.bluexin.mcui.deprecated.screens.util

import be.bluexin.mcui.GLCore
import be.bluexin.mcui.deprecated.api.screens.IIcon
import be.bluexin.mcui.util.Client
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.multiplayer.PlayerInfo
import net.minecraft.resources.ResourceLocation

class PlayerIcon(val player: PlayerInfo) : IIcon {

    private val texture = getSkin()

    private fun getSkin(): ResourceLocation {
        /*return if (player.isOnline) {
            Client.mc.skinManager.getInsecureSkinInformation(player.profile)[MinecraftProfileTexture.Type.SKIN]?.let {
                Client.mc.skinManager.loadSkin(it, MinecraftProfileTexture.Type.SKIN)
            } ?: DefaultPlayerSkin.getDefaultSkin(player.profile.id)
        } else DefaultPlayerSkin.getDefaultSkin(player.profile.id)*/
        return Client.mc.skinManager.getInsecureSkinLocation(player.profile)
    }

    override fun glDraw(x: Int, y: Int, z: Float, poseStack: PoseStack) {
        GLCore.color(1f, 1f, 1f, 1f)
        GLCore.glBlend(true)
        GLCore.depth(true)
//        GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN)
        GLCore.glBindTexture(texture)
        GLCore.glTexturedRectV2(
            x.toDouble(),
            y.toDouble(),
            width = 16.0,
            height = 16.0,
            srcX = 32.0,
            srcY = 31.0,
            srcWidth = 32.0,
            srcHeight = 32.0,
            poseStack = poseStack
        )
//        GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN)
        GLCore.glBlend(false)
    }
}
