package be.bluexin.mcui.themes.serde.legacyformat.dto

import be.bluexin.mcui.themes.miniscript.CacheType
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StringXmlTest {

    private val xml = XML {
        autoPolymorphic = true
    }

    @Test
    fun `loads all attributes successfully`() {
        val string = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/all_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<StringXml>(serialized)
        }

        assertEquals("test_string_all", string.name)

        assertEquals(AnonymousExpressionIntermediate("scaledwidth / 2"), string.x)
        assertEquals(AnonymousExpressionIntermediate("scaledheight / 2"), string.y)
        assertEquals(AnonymousExpressionIntermediate("1", CacheType.STATIC), string.z)
        assertEquals(AnonymousExpressionIntermediate("1.5", CacheType.STATIC), string.scale)
        assertEquals(AnonymousExpressionIntermediate("true"), string.enabled)

        assertEquals(AnonymousExpressionIntermediate("0xFFFFFFFF", CacheType.STATIC), string.rgba)
        assertEquals(AnonymousExpressionIntermediate("\"Hello World\""), string.text)
        assertEquals(AnonymousExpressionIntermediate("true", CacheType.STATIC), string.shadow)
        assertEquals(AnonymousExpressionIntermediate("false", CacheType.STATIC), string.centered)
    }

    @Test
    fun `loads missing attributes successfully`() {
        val string = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/no_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<StringXml>(serialized)
        }

        assertEquals("test_string_no", string.name)

        assertNull(string.x)
        assertNull(string.y)
        assertNull(string.z)
        assertNull(string.scale)
        assertNull(string.enabled)
        assertNull(string.rgba)
        assertNull(string.text)
        assertNull(string.shadow)
        assertNull(string.centered)
    }
}
