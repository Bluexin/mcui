package be.bluexin.mcui.themes.elements.visitor.renderer

import be.bluexin.mcui.themes.elements.Element
import be.bluexin.mcui.themes.elements.visitor.ElementVisitor
import be.bluexin.mcui.themes.miniscript.HudDrawContext
import be.bluexin.mcui.themes.miniscript.api.GameContext
import be.bluexin.mcui.themes.miniscript.profile
import com.mojang.blaze3d.vertex.PoseStack
import org.joml.Vector2d
import org.joml.Vector2dc
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Render entrypoint for the modern element tree : maps Minecraft state to a [RenderingElementVisitor]
 * and an initial [ElementVisitor.Context], then visits the given [element].
 *
 * Traversal, z-ordering and per-part profiling live in the tree itself ([Element.visit]) ; this class
 * only owns the adaptation seam. Mirrors the legacy `Hud.drawAll` behavior for A/B parity :
 *
 * - mouse starts at (-1, -1) by default (widgets don't hover during HUD rendering) and is made
 *   group-relative as the tree descends,
 * - repetition-group loop indices are forwarded to the legacy expression context (`i()`),
 * - the [ElementVisitor.Context.profile] hook is wired to the legacy debug profiler.
 */
@Single
internal class ModernElementRenderer : KoinComponent {
    private val hudDrawContext: HudDrawContext by inject()
    private val gameContext: GameContext by inject()

    fun render(element: Element, poseStack: PoseStack, mouse: Vector2dc = Vector2d(-1.0, -1.0)) {
        val visitor = RenderingElementVisitor(poseStack)
        val context = ElementVisitor.Context(
            gameInfo = gameContext,
            mouse = mouse,
            loopIndex = hudDrawContext::setI,
            profile = { key, block -> hudDrawContext.profile(key, block) },
        )
        visitor.visit(element, context)
    }
}