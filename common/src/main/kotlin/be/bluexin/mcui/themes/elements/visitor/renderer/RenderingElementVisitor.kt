package be.bluexin.mcui.themes.elements.visitor.renderer

import be.bluexin.mcui.themes.elements.RenderState
import be.bluexin.mcui.themes.elements.Transform
import be.bluexin.mcui.themes.elements.visitor.ElementVisitor
import be.bluexin.mcui.themes.elements.visitor.GLOperations
import com.mojang.blaze3d.vertex.PoseStack

internal class RenderingElementVisitor(
    private val poseStack: PoseStack
) : ElementVisitor {

    // profile ?
    override fun start(renderState: RenderState, context: ElementVisitor.Context) = renderState.enabled()

    override fun transform(transform: Transform, context: ElementVisitor.Context) {
        poseStack.pushPose()
        poseStack.translate(transform.x(), transform.y(), transform.z())

        transform.scale?.let { scale ->
            val s = scale().toFloat()
            poseStack.scale(s, s, s)
        }
    }

    override fun popTransform(context: ElementVisitor.Context) {
        poseStack.popPose()
    }

    override fun draw(context: ElementVisitor.Context, body: GLOperations.() -> Unit) {
        body(MinecraftGlOperations(poseStack))
    }
}
