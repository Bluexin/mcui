package be.bluexin.mcui.themes.elements.visitor

import be.bluexin.mcui.themes.elements.*
import be.bluexin.mcui.themes.miniscript.*
import be.bluexin.mcui.themes.miniscript.api.GameContext
import be.bluexin.mcui.themes.miniscript.api.MiniscriptItemStack
import io.mockk.every
import io.mockk.mockk
import org.joml.Vector2d
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import org.koin.test.junit5.AutoCloseKoinTest
import kotlin.test.assertEquals

/**
 * Regression coverage for the PoseStack-leak bug where [Rectangle] and [ItemStackDisplay] (on its
 * empty-slot path) pushed a transform via [ElementVisitor.transform] without a matching
 * [ElementVisitor.popTransform], corrupting whatever shares the pose stack afterward (e.g. chat).
 */
class TransformBalanceTest : AutoCloseKoinTest() {

    @BeforeEach
    fun setup() {
        startKoin {
            modules(module { single<GameContext> { mockk(relaxed = true) } })
        }
    }

    /** Records push/pop depth without touching any real GL/Minecraft rendering machinery. */
    private class RecordingVisitor : ElementVisitor {
        var depth = 0
            private set

        override fun start(renderState: RenderState, context: ElementVisitor.Context) = true
        override fun transform(transform: Transform, context: ElementVisitor.Context) {
            depth++
        }

        override fun popTransform(context: ElementVisitor.Context) {
            depth--
        }

        override fun draw(context: ElementVisitor.Context, body: GLOperations.() -> Unit) {
            // never invoke body: element draw bodies call into real GL / item rendering
        }
    }

    private fun context() = ElementVisitor.Context(
        gameInfo = mockk(relaxed = true),
        mouse = Vector2d(0.0, 0.0),
    )

    private fun assertBalanced(element: Element) {
        val visitor = RecordingVisitor()
        element.visit(visitor, context())
        assertEquals(0, visitor.depth, "every transform() must be matched by a popTransform()")
    }

    private fun renderState(name: String) = RenderState(CBoolean.TRUE, name)

    private fun rectangle(name: String = "rect") = Rectangle(
        renderState = renderState(name),
        transform = Transform.ZERO,
        geometry = Rectangle.Geometry(
            rgba = null,
            width = CDouble.ZERO,
            height = CDouble.ZERO,
            texture = null,
            sourceX = CDouble.ZERO,
            sourceY = CDouble.ZERO,
            sourceWidth = CDouble.ZERO,
            sourceHeight = CDouble.ZERO,
            textureWidth = CInt.ZERO,
            textureHeight = CInt.ZERO,
        ),
    )

    private fun text(name: String = "text") = Text(
        renderState = renderState(name),
        transform = Transform.ZERO,
        textProperties = Text.TextProperties(
            text = CString.EMPTY,
            rgba = CInt.WHITE,
            shadow = CBoolean.FALSE,
            centered = CBoolean.FALSE,
        ),
    )

    private fun rawElement(name: String = "raw") = RawElement(
        renderState = renderState(name),
        transform = Transform.ZERO,
        expression = CUnit.UNIT,
        texture = null,
    )

    private fun emptyItemStackDisplay(name: String = "item_empty"): ItemStackDisplay {
        val emptyStack: MiniscriptItemStack = mockk {
            every { isEmpty() } returns true
        }
        return ItemStackDisplay(
            renderState = renderState(name),
            transform = Transform.ZERO,
            properties = ItemStackDisplay.ItemSlotProperties(
                slotIndex = CInt.ZERO,
                inventorySource = { _, _ -> emptyStack },
            ),
        )
    }

    private fun nonEmptyItemStackDisplay(name: String = "item_filled"): ItemStackDisplay {
        val filledStack: MiniscriptItemStack = mockk {
            every { isEmpty() } returns false
        }
        return ItemStackDisplay(
            renderState = renderState(name),
            transform = Transform.ZERO,
            properties = ItemStackDisplay.ItemSlotProperties(
                slotIndex = CInt.ZERO,
                inventorySource = { _, _ -> filledStack },
            ),
        )
    }

    @Test
    fun `Rectangle balances transform push and pop`() {
        assertBalanced(rectangle())
    }

    @Test
    fun `ItemStackDisplay balances transform push and pop when the referenced slot is empty`() {
        assertBalanced(emptyItemStackDisplay())
    }

    @Test
    fun `ItemStackDisplay balances transform push and pop when the referenced slot is filled`() {
        assertBalanced(nonEmptyItemStackDisplay())
    }

    @Test
    fun `Text balances transform push and pop`() {
        assertBalanced(text())
    }

    @Test
    fun `RawElement balances transform push and pop`() {
        assertBalanced(rawElement())
    }

    @Test
    fun `Group balances transform push and pop around mixed children`() {
        assertBalanced(
            Group(
                renderState = renderState("group"),
                transform = Transform.ZERO,
                children = listOf(rectangle(), text(), rawElement(), emptyItemStackDisplay()),
                texture = null,
            )
        )
    }

    @Test
    fun `Group balances transform push and pop with no children`() {
        assertBalanced(
            Group(
                renderState = renderState("empty_group"),
                transform = Transform.ZERO,
                children = emptyList(),
                texture = null,
            )
        )
    }

    @Test
    fun `RepetitionGroup balances transform push and pop across multiple iterations`() {
        assertBalanced(
            RepetitionGroup(
                renderState = renderState("repeat"),
                transform = Transform.ZERO,
                children = listOf(rectangle(), text()),
                amount = CInt { 3 },
                texture = null,
            )
        )
    }

    @Test
    fun `RepetitionGroup balances transform push and pop with zero iterations`() {
        assertBalanced(
            RepetitionGroup(
                renderState = renderState("repeat_zero"),
                transform = Transform.ZERO,
                children = listOf(rectangle()),
                amount = CInt.ZERO,
                texture = null,
            )
        )
    }

    @Test
    fun `FragmentReference balances transform push and pop`() {
        assertBalanced(
            FragmentReference(
                renderState = renderState("fragment"),
                transform = Transform.ZERO,
                group = Group(
                    renderState = renderState("fragment_group"),
                    transform = Transform.ZERO,
                    children = listOf(rectangle(), emptyItemStackDisplay()),
                    texture = null,
                ),
                variables = mutableMapOf(),
            )
        )
    }

    @Test
    fun `Hud balances transform push and pop across its parts`() {
        assertBalanced(
            Hud(
                name = "test_hud",
                parts = mapOf(
                    "HOTBAR" to Group(
                        renderState = renderState("hotbar"),
                        transform = Transform.ZERO,
                        children = listOf(rectangle(), text()),
                        texture = null,
                    ),
                    "STATUS" to Group(
                        renderState = renderState("status"),
                        transform = Transform.ZERO,
                        children = listOf(nonEmptyItemStackDisplay(), rawElement()),
                        texture = null,
                    ),
                ),
            )
        )
    }
}
