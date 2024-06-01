package be.bluexin.mcui.deprecated.screens.util

import be.bluexin.mcui.deprecated.api.screens.IIcon
import be.bluexin.mcui.util.Client
import be.bluexin.mcui.util.IconCore
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.GuiComponent
import net.minecraft.client.gui.components.toasts.Toast
import net.minecraft.client.gui.components.toasts.ToastComponent

class NotificationAlert(val icon: IIcon, val title: String, val subtitle: String) : Toast {

    private var firstDrawTime: Long = 0
    private var newDisplay = true

    override fun render(poseStack: PoseStack, toastGui: ToastComponent, delta: Long): Toast.Visibility {
        if (newDisplay) {
            firstDrawTime = delta
            newDisplay = false
//            SoundCore.MESSAGE.play()
        }

        RenderSystem.setShaderTexture(0, Toast.TEXTURE)
//        GlStateManager.color(1.0f, 1.0f, 1.0f)
        GuiComponent.blit(poseStack, 0, 0, 0, 96, 160, 32)
        icon.glDraw(6, 8, poseStack)

        if (subtitle.isEmpty()) {
            toastGui.minecraft.font.draw(poseStack, title, 25f, 12f, -11534256)
        } else {
            toastGui.minecraft.font.draw(poseStack, title, 25f, 7f, -11534256)
            toastGui.minecraft.font.draw(poseStack, subtitle, 25f, 18f, -16777216)
        }
        return if (delta - firstDrawTime < 5000L) Toast.Visibility.SHOW else Toast.Visibility.HIDE
    }

    companion object {
        fun new(icon: IconCore, title: String, subtitle: String) {
            Client.mc.toasts.addToast(NotificationAlert(icon, title, subtitle))
        }
    }
}
