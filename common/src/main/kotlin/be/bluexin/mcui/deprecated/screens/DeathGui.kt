package be.bluexin.mcui.deprecated.screens

/*import be.bluexin.mcui.GLCore
import be.bluexin.mcui.SAOCore
import be.bluexin.mcui.util.ColorUtil
import be.bluexin.mcui.util.UIUtil
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.DestFactor
import net.minecraft.client.renderer.GlStateManager.SourceFactor
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.resources.ResourceLocation

class DeathGui : GuiScreen() {

    private var isHardCore = false
    var counter = 0

    override fun initGui() {
        isHardCore = mc.world.worldInfo.isHardcoreModeEnabled
    }

    override fun updateScreen() {
        if (counter < 40) counter++
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)
        // drawDefaultBackground()
        darkenScreen()
        GLCore.color(if (isHardCore) ColorUtil.HARDCORE_DEAD_COLOR else ColorUtil.DEAD_COLOR)
        // GLCore.glBlend(true)
        GLCore.glBindTexture(rl)
        GLCore.glTexturedRectV2(sr.scaledWidth / 2 - (w / 2), sr.scaledHeight / 2 - (h / 2), width = w, height = h, srcX = 0.0, srcY = 0.0, srcWidth = 256.0, srcHeight = 256.0)
        GLCore.glBlend(false)
    }

    fun darkenScreen() {
        GlStateManager.disableTexture2D()
        GLCore.glBlend(true)
        GlStateManager.disableAlpha()
        GLCore.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA.factor, DestFactor.ONE_MINUS_SRC_ALPHA.factor, SourceFactor.ONE.factor, DestFactor.ZERO.factor)
        GlStateManager.shadeModel(7425)
        val alpha = counter / 40f
        GLCore.begin(7, DefaultVertexFormats.POSITION_COLOR)
        GLCore.addVertex(width.toDouble(), 0.0, zLevel.toDouble(), 0f, 0f, 0f, alpha)
        GLCore.addVertex(0.0, 0.0, zLevel.toDouble(), 0f, 0f, 0f, alpha)
        GLCore.addVertex(0.0, height.toDouble(), zLevel.toDouble(), 0f, 0f, 0f, alpha)
        GLCore.addVertex(width.toDouble(), height.toDouble(), zLevel.toDouble(), 0f, 0f, 0f, alpha)
        GLCore.draw()
        GlStateManager.shadeModel(7424)
        // GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
    }

    override fun doesGuiPauseGame(): Boolean {
        return true
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        confirmedClick()
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == 1) {
            confirmedClick()
        }
    }

    fun confirmedClick() {
        if (mc.world.worldInfo.isHardcoreModeEnabled) {
            UIUtil.closeGame()
        } else {
            mc.player.preparePlayerToSpawn()
            mc.player.respawnPlayer()
            // ****** Prevents GUI attempting to open Death Screen twice ******
            // mc.player.setPlayerSPHealth(mc.player.maxHealth)
            mc.setScreen(null)
        }
    }

    companion object {
        val rl = ResourceLocation(Constants.MOD_ID, "textures/hud/buttons/death.png")
        const val w = 280.0
        const val h = 100.0
    }
}*/
