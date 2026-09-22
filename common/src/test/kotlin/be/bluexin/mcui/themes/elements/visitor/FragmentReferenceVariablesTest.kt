package be.bluexin.mcui.themes.elements.visitor

import be.bluexin.mcui.themes.elements.FragmentReference
import be.bluexin.mcui.themes.elements.Group
import be.bluexin.mcui.themes.elements.RenderState
import be.bluexin.mcui.themes.elements.Transform
import be.bluexin.mcui.themes.miniscript.CBoolean
import be.bluexin.mcui.themes.miniscript.CInt
import be.bluexin.mcui.themes.miniscript.CValue
import be.bluexin.mcui.themes.miniscript.api.GameContext
import io.mockk.mockk
import org.joml.Vector2d
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import org.koin.test.junit5.AutoCloseKoinTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Regression coverage for the bug where [FragmentReference.visit] never pushed its `variables` onto
 * the [ElementVisitor.Context] variable-scope hooks, so an expression inside a fragment's body could
 * never resolve the reference's variable values at render time (compile-time type resolution worked,
 * but the runtime value lookup was never scoped to the fragment reference being visited).
 */
class FragmentReferenceVariablesTest : AutoCloseKoinTest() {

    @BeforeEach
    fun setup() {
        startKoin {
            modules(module { single<GameContext> { mockk(relaxed = true) } })
        }
    }

    /** Records, for every [start] call, whether a variable scope was active at that point. */
    private class ScopeCheckingVisitor(private val isScopeActive: () -> Boolean) : ElementVisitor {
        val activeDuringVisit = mutableListOf<Pair<String, Boolean>>()

        override fun start(renderState: RenderState, context: ElementVisitor.Context): Boolean {
            activeDuringVisit += renderState.name to isScopeActive()
            return true
        }

        override fun transform(transform: Transform, context: ElementVisitor.Context) {}
        override fun popTransform(context: ElementVisitor.Context) {}
        override fun draw(context: ElementVisitor.Context, body: GLOperations.() -> Unit) {}
    }

    @Test
    fun `FragmentReference pushes its variables while visiting the group and pops them after`() {
        var scopeActive = false
        val pushedVariables = mutableListOf<Map<String, CValue<*>>>()
        var popCount = 0

        val visitor = ScopeCheckingVisitor(isScopeActive = { scopeActive })

        val variables: MutableMap<String, CValue<*>> = mutableMapOf("foo" to CInt.ZERO)

        val fragmentReference = FragmentReference(
            renderState = RenderState(CBoolean.TRUE, "fragment"),
            transform = Transform.ZERO,
            group = Group(
                renderState = RenderState(CBoolean.TRUE, "fragment_group"),
                transform = Transform.ZERO,
                children = emptyList(),
                texture = null,
            ),
            variables = variables,
        )

        val context = ElementVisitor.Context(
            gameInfo = mockk(relaxed = true),
            mouse = Vector2d(0.0, 0.0),
            pushVariables = { vars ->
                pushedVariables += vars
                scopeActive = true
            },
            popVariables = {
                popCount++
                scopeActive = false
            },
        )

        fragmentReference.visit(visitor, context)

        assertEquals<List<Map<String, CValue<*>>>>(
            listOf(variables), pushedVariables,
            "the fragment reference's own variables must be pushed"
        )
        assertEquals(1, popCount, "the variable scope must be popped exactly once")
        assertEquals(
            listOf("fragment" to false, "fragment_group" to true), visitor.activeDuringVisit,
            "the fragment's group must be visited while (and only while) the variable scope is active"
        )
        assertFalse(scopeActive, "the variable scope must not still be active after visit() returns")
    }
}
