package be.bluexin.mcui.themes.miniscript

import com.mojang.blaze3d.vertex.PoseStack
import org.koin.core.annotation.Single

@Single
class PoseStackTracker {
    /**
     * Consider using [withStack] instead of directly updating this variable
     */
    var poseStack: PoseStack? = null

    inline fun <T> withStack(poseStack: PoseStack, block: () -> T): T = try {
        this.poseStack = poseStack
        block()
    } finally {
        this.poseStack = null
    }
}