package be.bluexin.mcui.themes.serde.legacyformat.dto

import be.bluexin.mcui.themes.miniscript.CacheType
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GroupXmlTest {

    private val xml = XML {
        autoPolymorphic = true
    }

    @Test
    fun `loads all attributes successfully`() {
        val elementGroup = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/all_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<GroupXml>(serialized)
        }

        assertEquals("test_element_group_all", elementGroup.name)

        assertEquals(AnonymousExpressionIntermediate("10 + scaledwidth / 4"), elementGroup.x)
        assertEquals(AnonymousExpressionIntermediate("20 + scaledheight / 4"), elementGroup.y)
        assertEquals(AnonymousExpressionIntermediate("5", CacheType.STATIC), elementGroup.z)
        assertEquals(AnonymousExpressionIntermediate("2.0", CacheType.STATIC), elementGroup.scale)
        assertEquals(AnonymousExpressionIntermediate("player.health > 0"), elementGroup.enabled)

        assertEquals(AnonymousExpressionIntermediate("my_texture"), elementGroup.texture)

        assertContentEquals(
            listOf(
                GroupXml(
                    name = "nested_group",
                    x = AnonymousExpressionIntermediate("5", CacheType.STATIC),
                    y = AnonymousExpressionIntermediate("10", CacheType.STATIC),
                )
            ),
            elementGroup.children?.elements
        )
    }

    @Test
    fun `loads missing attributes successfully`() {
        val elementGroup = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/no_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<GroupXml>(serialized)
        }

        assertEquals("test_element_group_no", elementGroup.name)

        assertNull(elementGroup.x)
        assertNull(elementGroup.y)
        assertNull(elementGroup.z)
        assertNull(elementGroup.scale)
        assertNull(elementGroup.enabled)
        assertNull(elementGroup.texture)
        assertNull(elementGroup.children)
    }
}
