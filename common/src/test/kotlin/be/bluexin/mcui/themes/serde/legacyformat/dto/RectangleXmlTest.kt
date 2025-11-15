package be.bluexin.mcui.themes.serde.legacyformat.dto

import be.bluexin.mcui.themes.miniscript.CacheType
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RectangleXmlTest {

    private val xml = XML {
        autoPolymorphic = true
    }

    @Test
    fun `loads all attributes successfully`() {
        val rectangle = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/all_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<RectangleXml>(serialized)
        }

        assertEquals("test_rectangle_all", rectangle.name)

        assertEquals(AnonymousExpressionIntermediate("-6 - strWidth(text)/2.0"), rectangle.x)
        assertEquals(AnonymousExpressionIntermediate("50"), rectangle.y)
        assertEquals(AnonymousExpressionIntermediate("1", CacheType.STATIC), rectangle.z)
        assertEquals(AnonymousExpressionIntermediate("1.2", CacheType.STATIC), rectangle.scale)
        assertEquals(AnonymousExpressionIntermediate("true"), rectangle.enabled)

        assertEquals(AnonymousExpressionIntermediate("0x424242FF", CacheType.STATIC), rectangle.rgba)
        assertEquals(AnonymousExpressionIntermediate("0.0", CacheType.STATIC), rectangle.srcX)
        assertEquals(AnonymousExpressionIntermediate("0.0", CacheType.STATIC), rectangle.srcY)
        assertEquals(AnonymousExpressionIntermediate("6", CacheType.STATIC), rectangle.w)
        assertEquals(AnonymousExpressionIntermediate("16", CacheType.STATIC), rectangle.h)
        assertEquals(AnonymousExpressionIntermediate("24.0", CacheType.STATIC), rectangle.srcW)
        assertEquals(AnonymousExpressionIntermediate("64.0", CacheType.STATIC), rectangle.srcH)

        assertEquals(AnonymousExpressionIntermediate("mcui:themes/hex2/textures/hex_labels.png"), rectangle.texture)
    }

    @Test
    fun `loads missing attributes successfully`() {
        val rectangle = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/no_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<RectangleXml>(serialized)
        }

        assertEquals("test_rectangle_no", rectangle.name)

        assertNull(rectangle.x)
        assertNull(rectangle.y)
        assertNull(rectangle.z)
        assertNull(rectangle.scale)
        assertNull(rectangle.enabled)
        assertNull(rectangle.rgba)
        assertNull(rectangle.srcX)
        assertNull(rectangle.srcY)
        assertNull(rectangle.w)
        assertNull(rectangle.h)
        assertNull(rectangle.srcW)
        assertNull(rectangle.srcH)
        assertNull(rectangle.texture)
    }

    @Test
    fun `loads rectangle with minimal geometry`() {
        val rectangle = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/minimal_geometry.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<RectangleXml>(serialized)
        }

        assertEquals("simple_rect", rectangle.name)

        assertEquals(AnonymousExpressionIntermediate("10", CacheType.STATIC), rectangle.x)
        assertEquals(AnonymousExpressionIntermediate("20", CacheType.STATIC), rectangle.y)
        assertEquals(AnonymousExpressionIntermediate("100", CacheType.STATIC), rectangle.w)
        assertEquals(AnonymousExpressionIntermediate("50", CacheType.STATIC), rectangle.h)

        assertNull(rectangle.z)
        assertNull(rectangle.scale)
        assertNull(rectangle.enabled)
        assertNull(rectangle.rgba)
        assertNull(rectangle.srcX)
        assertNull(rectangle.srcY)
        assertNull(rectangle.srcW)
        assertNull(rectangle.srcH)
        assertNull(rectangle.texture)
    }
}
