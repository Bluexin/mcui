package be.bluexin.mcui.themes.serde.legacyformat.xml

import be.bluexin.mcui.themes.miniscript.CacheType
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RawElementXmlTest {

    private val xml = XML {
        autoPolymorphic = true
    }

    @Test
    fun `loads all attributes successfully`() {
        val rawElement = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/all_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<RawElementXml>(serialized)
        }

        assertEquals("test_raw_element_all", rawElement.name)

        assertEquals(AnonymousExpressionIntermediate("i * 11"), rawElement.x)
        assertEquals(AnonymousExpressionIntermediate("25"), rawElement.y)
        assertEquals(AnonymousExpressionIntermediate("2", CacheType.STATIC), rawElement.z)
        assertEquals(AnonymousExpressionIntermediate("1.5", CacheType.STATIC), rawElement.scale)
        assertEquals(AnonymousExpressionIntermediate("i < 5"), rawElement.enabled)

        assertEquals(
            AnonymousExpressionIntermediate("player.statusEffect(i).glDraw(i * 11, 0, getZ)", CacheType.NONE),
            rawElement.expression
        )
        assertEquals(AnonymousExpressionIntermediate("my_custom_texture"), rawElement.texture)
    }

    @Test
    fun `loads missing attributes successfully`() {
        val rawElement = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/no_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<RawElementXml>(serialized)
        }

        assertEquals("test_raw_element_no", rawElement.name)

        assertNull(rawElement.x)
        assertNull(rawElement.y)
        assertNull(rawElement.z)
        assertNull(rawElement.scale)
        assertNull(rawElement.enabled)
        assertNull(rawElement.expression)
        assertNull(rawElement.texture)
    }

    @Test
    fun `loads raw element with expression only`() {
        val rawElement = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/expression_only.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<RawElementXml>(serialized)
        }

        assertEquals("custom_draw", rawElement.name)

        assertNull(rawElement.x)
        assertNull(rawElement.y)
        assertNull(rawElement.z)
        assertNull(rawElement.scale)
        assertNull(rawElement.enabled)
        assertNull(rawElement.texture)

        assertEquals(
            AnonymousExpressionIntermediate("drawCustomEffect()", CacheType.NONE),
            rawElement.expression
        )
    }
}
