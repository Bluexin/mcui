package be.bluexin.mcui.deprecated.screens.util

import be.bluexin.mcui.util.Client
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.GuiComponent
import net.minecraft.client.gui.components.toasts.Toast
import net.minecraft.client.gui.components.toasts.ToastComponent
import net.minecraft.world.item.ItemStack

class CraftingAlert(private val itemStack: ItemStack) : Toast {
    private var init = false
    private val itemIcon = ItemIcon { itemStack }
    private var lastDelta: Long = 0

    override fun render(poseStack: PoseStack, toastGui: ToastComponent, delta: Long): Toast.Visibility {
        if (!init) {
            init = true
//            SoundCore.MESSAGE.play()
        }
        RenderSystem.setShaderTexture(0, Toast.TEXTURE)
//        GlStateManager.color(1.0f, 1.0f, 1.0f)

        GuiComponent.blit(poseStack, 0, 0, 0, 96, 160, 32)
        itemIcon.glDraw(6, 8, poseStack)

        /*toastGui.minecraft.fontRenderer.drawString(I18n.get("notificationCraftingTitle", CraftingUtil.craftLimit * itemStack.count, itemStack.displayName), 25, 12, -11534256)
        Gui.drawRect(3, 28, 157, 29, -1)
        val f = CraftingUtil.currentCount.toFloat() / CraftingUtil.craftLimit.toFloat()

        val i: Int = if (CraftingUtil.currentCount >= CraftingUtil.craftLimit) {
            -16755456
        } else {
            -11206656
        }

        Gui.drawRect(3, 28, (3.0f + 154.0f * f).toInt(), 29, i)
        this.lastDelta = delta*/

        return /*if (!CraftingUtil.craftReady || CraftingUtil.currentCount >= CraftingUtil.craftLimit) {
            Toast.Visibility.HIDE } else*/ Toast.Visibility.SHOW
    }

    companion object {
        fun new(stack: ItemStack) {
            Client.mc.toasts.addToast(CraftingAlert(stack))
        }
    }
}
