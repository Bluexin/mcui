package be.bluexin.mcui.themes.serde.legacyformat.dto

import be.bluexin.mcui.themes.miniscript.CacheType
import be.bluexin.mcui.themes.miniscript.serialization.JelType
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FragmentXmlTest {

    private val xml = XML {
        autoPolymorphic = true
    }

    @Test
    fun `loads all attributes successfully`() {
        val fragment = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/all_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<FragmentXml>(serialized)
        }

        assertEquals("test_fragment_all", fragment.name)

        assertEquals(
            mapOf(
                "width" to NamedExpressionIntermediate(JelType.DOUBLE),
                "height" to NamedExpressionIntermediate(
                    JelType.DOUBLE,
                    cacheType = CacheType.STATIC,
                    expression = "100"
                ),
            ), fragment.expect?.variables
        )

        assertEquals(AnonymousExpressionIntermediate("20 + scaledwidth / 3"), fragment.x)
        assertEquals(AnonymousExpressionIntermediate("30 + scaledheight / 3"), fragment.y)
        assertEquals(AnonymousExpressionIntermediate("4", CacheType.STATIC), fragment.z)
        assertEquals(AnonymousExpressionIntermediate("2.5", CacheType.STATIC), fragment.scale)
        assertEquals(AnonymousExpressionIntermediate("width > 0"), fragment.enabled)

        assertEquals(AnonymousExpressionIntermediate("my_fragment_texture"), fragment.texture)

        assertContentEquals(
            listOf(
                ElementGroupXml(
                    name = "content_group",
                    x = AnonymousExpressionIntermediate("5", CacheType.STATIC),
                    y = AnonymousExpressionIntermediate("10", CacheType.STATIC),
                ),
                StringXml(
                    name = "label",
                    x = AnonymousExpressionIntermediate("15", CacheType.STATIC),
                    y = AnonymousExpressionIntermediate("20", CacheType.STATIC),
                    text = AnonymousExpressionIntermediate("\"Fragment Label\"")
                )
            ),
            fragment.children?.elements
        )
    }

    @Test
    fun `loads missing attributes successfully`() {
        val fragment = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/no_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<FragmentXml>(serialized)
        }

        assertEquals("test_fragment_no", fragment.name)

        assertNull(fragment.expect)
        assertNull(fragment.x)
        assertNull(fragment.y)
        assertNull(fragment.z)
        assertNull(fragment.scale)
        assertNull(fragment.enabled)
        assertNull(fragment.texture)
        assertNull(fragment.children)
    }

    @Test
    fun `loads fragment with expect but no other attributes`() {
        val fragment = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/expect_only.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<FragmentXml>(serialized)
        }

        assertEquals("test_fragment_expect", fragment.name)

        assertEquals(
            mapOf(
                "text" to NamedExpressionIntermediate(JelType.STRING),
                "visible" to NamedExpressionIntermediate(
                    JelType.BOOLEAN,
                    cacheType = CacheType.STATIC,
                    expression = "true"
                ),
            ), fragment.expect?.variables
        )

        assertNull(fragment.x)
        assertNull(fragment.y)
        assertNull(fragment.z)
        assertNull(fragment.scale)
        assertNull(fragment.enabled)
        assertNull(fragment.texture)
        assertNull(fragment.children)
    }
}
