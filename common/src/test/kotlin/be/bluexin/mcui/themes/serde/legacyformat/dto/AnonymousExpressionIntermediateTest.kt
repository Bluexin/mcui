package be.bluexin.mcui.themes.serde.legacyformat.dto

import be.bluexin.mcui.themes.miniscript.CacheType
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AnonymousExpressionIntermediateTest {

    private val xml = XML {
        autoPolymorphic = true
    }

    @Test
    fun `deserializes expression with default cache type`() {
        val result = xml.decodeFromString<TestWrapper>(
            """<test><value>player.health</value></test>"""
        )

        assertEquals("player.health", result.value.expression)
        assertEquals(CacheType.PER_FRAME, result.value.cacheType)
    }

    @Test
    fun `deserializes expression with STATIC cache type`() {
        val result = xml.decodeFromString<TestWrapper>(
            """<test><value cache="STATIC">100</value></test>"""
        )

        assertEquals("100", result.value.expression)
        assertEquals(CacheType.STATIC, result.value.cacheType)
    }

    @Test
    fun `deserializes expression with PER_FRAME cache type`() {
        val result = xml.decodeFromString<TestWrapper>(
            """<test><value cache="PER_FRAME">player.level</value></test>"""
        )

        assertEquals("player.level", result.value.expression)
        assertEquals(CacheType.PER_FRAME, result.value.cacheType)
    }

    @Test
    fun `deserializes expression with SIZE_CHANGE cache type`() {
        val result = xml.decodeFromString<TestWrapper>(
            """<test><value cache="SIZE_CHANGE">scaledwidth / 2</value></test>"""
        )

        assertEquals("scaledwidth / 2", result.value.expression)
        assertEquals(CacheType.SIZE_CHANGE, result.value.cacheType)
    }

    @Test
    fun `deserializes expression with NONE cache type`() {
        val result = xml.decodeFromString<TestWrapper>(
            """<test><value cache="NONE">i * 22</value></test>"""
        )

        assertEquals("i * 22", result.value.expression)
        assertEquals(CacheType.NONE, result.value.cacheType)
    }

    @Test
    fun `deserializes empty expression`() {
        val result = xml.decodeFromString<TestWrapper>(
            """<test><value></value></test>"""
        )

        assertEquals("", result.value.expression)
        assertEquals(CacheType.PER_FRAME, result.value.cacheType)
    }

    @Test
    fun `deserializes multiline expression`() {
        val result = xml.decodeFromString<TestWrapper>(
            """<test><value cache="STATIC">
                player.health > 0 &amp;&amp;
                player.level > 5
            </value></test>"""
        )

        assertTrue(result.value.expression.contains("player.health > 0 &&"))
        assertTrue(result.value.expression.contains("player.level > 5"))
        assertEquals(CacheType.STATIC, result.value.cacheType)
    }

    @Test
    fun `deserializes expression with special characters`() {
        val result = xml.decodeFromString<TestWrapper>(
            """<test><value cache="STATIC">"Hello &lt;World&gt;"</value></test>"""
        )

        assertEquals("\"Hello <World>\"", result.value.expression)
        assertEquals(CacheType.STATIC, result.value.cacheType)
    }

    @Test
    fun `deserializes expression with numeric literal`() {
        val result = xml.decodeFromString<TestWrapper>(
            """<test><value cache="STATIC">42.5</value></test>"""
        )

        assertEquals("42.5", result.value.expression)
        assertEquals(CacheType.STATIC, result.value.cacheType)
    }

    @Test
    fun `deserializes expression with hex color`() {
        val result = xml.decodeFromString<TestWrapper>(
            """<test><value cache="STATIC">0xFFBA66FF</value></test>"""
        )

        assertEquals("0xFFBA66FF", result.value.expression)
        assertEquals(CacheType.STATIC, result.value.cacheType)
    }

    @Test
    fun `deserializes complex expression with function calls`() {
        val result = xml.decodeFromString<TestWrapper>(
            """<test><value cache="NONE">format("%d", player.level)</value></test>"""
        )

        assertEquals("format(\"%d\", player.level)", result.value.expression)
        assertEquals(CacheType.NONE, result.value.cacheType)
    }

    @Test
    fun `deserializes ternary expression`() {
        val result = xml.decodeFromString<TestWrapper>(
            """<test><value cache="NONE">i == player.selectedSlot ? 0xFFBA66FF : 0xCDCDCDAA</value></test>"""
        )

        assertEquals("i == player.selectedSlot ? 0xFFBA66FF : 0xCDCDCDAA", result.value.expression)
        assertEquals(CacheType.NONE, result.value.cacheType)
    }

    @Test
    fun `asAnonymous property returns equivalent instance`() {
        val original = AnonymousExpressionIntermediate("test", CacheType.STATIC)
        val asAnonymous = original.asAnonymous

        assertEquals(original, asAnonymous)
    }

    @Test
    fun `EMPTY constant has empty expression and default cache`() {
        assertEquals("", AnonymousExpressionIntermediate.EMPTY.expression)
        assertEquals(CacheType.PER_FRAME, AnonymousExpressionIntermediate.EMPTY.cacheType)
    }

    @Test
    fun `equality works correctly`() {
        val expr1 = AnonymousExpressionIntermediate("test", CacheType.STATIC)
        val expr2 = AnonymousExpressionIntermediate("test", CacheType.STATIC)
        val expr3 = AnonymousExpressionIntermediate("test", CacheType.PER_FRAME)
        val expr4 = AnonymousExpressionIntermediate("other", CacheType.STATIC)

        assertEquals(expr1, expr2)
        assertNotEquals(expr1, expr3)
        assertNotEquals(expr1, expr4)
    }

    @Serializable
    private data class TestWrapper(
        @XmlSerialName("value")
        val value: AnonymousExpressionIntermediate
    )
}
