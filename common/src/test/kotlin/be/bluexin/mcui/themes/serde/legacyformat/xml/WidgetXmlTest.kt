package be.bluexin.mcui.themes.serde.legacyformat.xml

import be.bluexin.mcui.themes.miniscript.CacheType
import be.bluexin.mcui.themes.miniscript.serialization.JelType
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import kotlin.test.*

class WidgetXmlTest {

    private val xml = XML {
        autoPolymorphic = true
    }

    @Test
    fun `loads all attributes successfully`() {
        val widget = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/all_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<WidgetXml>(serialized)
        }

        assertEquals("test_widget_all", widget.name)
        assertEquals(
            mapOf(
                "one" to NamedExpressionIntermediate(JelType.STRING),
                "two" to NamedExpressionIntermediate(JelType.STRING),
                "three" to NamedExpressionIntermediate(
                    JelType.BOOLEAN,
                    cacheType = CacheType.STATIC,
                    expression = "false"
                ),
            ), widget.expect?.variables
        )

        assertEquals(AnonymousExpressionIntermediate("12 + scaledwidth / 2"), widget.x)
        assertEquals(AnonymousExpressionIntermediate("15 + scaledheight / 2"), widget.y)
        assertEquals(AnonymousExpressionIntermediate("3", CacheType.STATIC), widget.z)
        assertEquals(AnonymousExpressionIntermediate("scale value", CacheType.STATIC), widget.scale)
        assertEquals(AnonymousExpressionIntermediate("foo() == bar()"), widget.enabled)

        assertContentEquals(
            listOf(
                WidgetXml(
                    name = "content",
                    x = AnonymousExpressionIntermediate("17", CacheType.STATIC),
                    y = AnonymousExpressionIntermediate("22", CacheType.STATIC),
                    enabled = AnonymousExpressionIntermediate("three"),
                    contentWidth = AnonymousExpressionIntermediate("100", CacheType.STATIC),
                    contentHeight = AnonymousExpressionIntermediate("120", CacheType.STATIC),
                )
            ),
            widget.children?.elements
        )

        assertEquals(AnonymousExpressionIntermediate("awesome texture !!"), widget.texture)

        assertEquals(AnonymousExpressionIntermediate("13 w"), widget.contentWidth)
        assertEquals(AnonymousExpressionIntermediate("15 h"), widget.contentHeight)

        assertEquals(AnonymousExpressionIntermediate("mia tooltip"), widget.tooltip)
        assertEquals(AnonymousExpressionIntermediate("maybeh"), widget.active)

        assertContains(assertNotNull(widget.onClickScript), "fanceh clicky")
        assertContains(assertNotNull(widget.onMouseOverEventScript), "fanceh hover")
        assertContains(assertNotNull(widget.onLoseFocusScript), "big sad")

        val extra = assertNotNull(widget.extraScripts?.values)
        val testExtra = assertNotNull(extra["testExtra"])
        assertContains(testExtra, "return function(self)")
        assertContains(testExtra, "return 42")
        assertContains(testExtra, "end")
    }

    @Test
    fun `loads missing attributes successfully`() {
        val widget = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/no_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<WidgetXml>(serialized)
        }

        assertEquals("test_widget_no", widget.name)

        assertNull(widget.expect)
        assertNull(widget.x)
        assertNull(widget.y)
        assertNull(widget.z)
        assertNull(widget.scale)
        assertNull(widget.enabled)
        assertNull(widget.children)
        assertNull(widget.texture)
        assertNull(widget.contentWidth)
        assertNull(widget.contentHeight)
        assertNull(widget.tooltip)
        assertNull(widget.active)
        assertNull(widget.onClickScript)
        assertNull(widget.onMouseOverEventScript)
        assertNull(widget.onLoseFocusScript)
        assertNull(widget.extraScripts)
    }
}