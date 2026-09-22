package be.bluexin.mcui.themes.elements.visitor

import be.bluexin.mcui.themes.elements.Element
import be.bluexin.mcui.themes.elements.RenderState
import be.bluexin.mcui.themes.elements.Transform
import be.bluexin.mcui.themes.miniscript.CValue
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
         * Run a profiled block, if profiling is available (on Minecraft, this uses the vanilla profiler).
         * String argument is the profiler key to be used.
         */
        val profile: (String, () -> Unit) -> Unit = { _, block -> block() },
        /**
         * Push/pop a named-variable scope made available to Miniscripts through JEL dynamic properties.
         */
        val pushVariables: (Map<String, CValue<*>>) -> Unit = {},
        val popVariables: () -> Unit = {},
    )
}