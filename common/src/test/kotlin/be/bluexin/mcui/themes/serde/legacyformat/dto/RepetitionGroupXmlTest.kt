package be.bluexin.mcui.themes.serde.legacyformat.dto

import be.bluexin.mcui.themes.miniscript.CacheType
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RepetitionGroupXmlTest {

    private val xml = XML {
        autoPolymorphic = true
    }

    @Test
    fun `loads all attributes successfully`() {
        val repetitionGroup = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/all_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<RepetitionGroupXml>(serialized)
        }

        assertEquals("test_repetition_group_all", repetitionGroup.name)

        assertEquals(AnonymousExpressionIntermediate("player.inventorySize", CacheType.NONE), repetitionGroup.amount)

        assertEquals(AnonymousExpressionIntermediate("15 + scaledwidth / 5"), repetitionGroup.x)
        assertEquals(AnonymousExpressionIntermediate("25 + scaledheight / 5"), repetitionGroup.y)
        assertEquals(AnonymousExpressionIntermediate("3", CacheType.STATIC), repetitionGroup.z)
        assertEquals(AnonymousExpressionIntermediate("1.8", CacheType.STATIC), repetitionGroup.scale)
        assertEquals(AnonymousExpressionIntermediate("player.inventorySize > 0"), repetitionGroup.enabled)

        assertEquals(AnonymousExpressionIntermediate("inventory_texture"), repetitionGroup.texture)

        assertContentEquals(
            listOf(
                RectangleXml(
                    name = "slot_bg",
                    x = AnonymousExpressionIntermediate("i * 20", CacheType.NONE),
                    y = AnonymousExpressionIntermediate("0", CacheType.STATIC),
                    width = AnonymousExpressionIntermediate("18", CacheType.STATIC),
                    height = AnonymousExpressionIntermediate("18", CacheType.STATIC),
                ),
                StringXml(
                    name = "slot_number",
                    x = AnonymousExpressionIntermediate("i * 20 + 9", CacheType.NONE),
                    y = AnonymousExpressionIntermediate("20", CacheType.STATIC),
                    text = AnonymousExpressionIntermediate("toString(i)")
                )
            ),
            repetitionGroup.children?.elements
        )
    }

    @Test
    fun `loads missing optional attributes successfully`() {
        val repetitionGroup = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/minimal_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<RepetitionGroupXml>(serialized)
        }

        assertEquals("test_repetition_group_minimal", repetitionGroup.name)

        assertEquals(AnonymousExpressionIntermediate("9", CacheType.STATIC), repetitionGroup.amount)

        assertNull(repetitionGroup.x)
        assertNull(repetitionGroup.y)
        assertNull(repetitionGroup.z)
        assertNull(repetitionGroup.scale)
        assertNull(repetitionGroup.enabled)
        assertNull(repetitionGroup.texture)
        assertNull(repetitionGroup.children)
    }
}
