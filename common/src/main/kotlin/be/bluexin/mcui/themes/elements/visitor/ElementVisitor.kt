package be.bluexin.mcui.themes.elements.visitor

import be.bluexin.mcui.themes.elements.Element
import be.bluexin.mcui.themes.elements.RenderState
import be.bluexin.mcui.themes.elements.Transform
import be.bluexin.mcui.themes.miniscript.api.GameContext
import org.joml.Vector2dc

interface ElementVisitor {
    fun visit(element: Element, context: Context): Unit = element.visit(this, context)

    /**
     * @return whether the element should continue the visit
     */
    fun start(renderState: RenderState, context: Context): Boolean

    fun transform(transform: Transform, context: Context)
    fun popTransform(context: Context)

    fun draw(context: Context, body: GLOperations.() -> Unit)

    data class Context(
        val gameInfo: GameContext,
        val mouse: Vector2dc,
        /**
         * Set the current repetition-group loop index while visiting a repeated group.
         * Wired to the legacy expression draw context so `i()`-style JEL expressions keep working.
         * No-op when no [be.bluexin.mcui.themes.elements.RepetitionGroup] is active.
         */
        val loopIndex: (Int) -> Unit = {},
        /**
         * Profiling hook, keyed by name. Defaults to identity so the element model stays Minecraft-free ;
         * wired to the legacy debug profiler during rendering (A/B parity).
         */
        val profile: (String, () -> Unit) -> Unit = { _, block -> block() },
    )
}