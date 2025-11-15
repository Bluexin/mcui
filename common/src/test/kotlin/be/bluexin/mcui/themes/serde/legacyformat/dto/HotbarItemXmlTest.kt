package be.bluexin.mcui.themes.serde.legacyformat.dto

import be.bluexin.mcui.themes.miniscript.CacheType
import kotlinx.serialization.decodeFromString
import net.minecraft.world.entity.HumanoidArm
import nl.adaptivity.xmlutil.serialization.XML
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HotbarItemXmlTest {

    private val xml = XML {
        autoPolymorphic = true
    }

    @Test
    fun `loads all attributes successfully`() {
        val hotbarItem = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/all_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<HotbarItemXml>(serialized)
        }

        assertEquals("test_hotbar_item_all", hotbarItem.name)

        assertEquals(AnonymousExpressionIntermediate("10"), hotbarItem.x)
        assertEquals(AnonymousExpressionIntermediate("i * 22", CacheType.NONE), hotbarItem.y)
        assertEquals(AnonymousExpressionIntermediate("1", CacheType.STATIC), hotbarItem.z)
        assertEquals(AnonymousExpressionIntermediate("1.0", CacheType.STATIC), hotbarItem.scale)
        assertEquals(AnonymousExpressionIntermediate("i == player.selectedSlot"), hotbarItem.enabled)

        assertEquals(
            AnonymousExpressionIntermediate(
                "i == player.selectedSlot ? 0xFFBA66FF : 0xCDCDCDAA",
                CacheType.NONE
            ), hotbarItem.rgba
        )
        assertEquals(AnonymousExpressionIntermediate("0", CacheType.STATIC), hotbarItem.srcX)
        assertEquals(AnonymousExpressionIntermediate("25.0", CacheType.STATIC), hotbarItem.srcY)
        assertEquals(AnonymousExpressionIntermediate("20.0", CacheType.STATIC), hotbarItem.w)
        assertEquals(AnonymousExpressionIntermediate("20.0", CacheType.STATIC), hotbarItem.h)
        assertEquals(AnonymousExpressionIntermediate("20.0", CacheType.STATIC), hotbarItem.srcW)
        assertEquals(AnonymousExpressionIntermediate("20.0", CacheType.STATIC), hotbarItem.srcH)

        assertEquals(AnonymousExpressionIntermediate("saoui:textures/sao/gui.png"), hotbarItem.texture)

        assertEquals(AnonymousExpressionIntermediate("i", CacheType.NONE), hotbarItem.slot)
        assertEquals(AnonymousExpressionIntermediate("2", CacheType.STATIC), hotbarItem.itemXoffset)
        assertEquals(AnonymousExpressionIntermediate("2", CacheType.STATIC), hotbarItem.itemYoffset)
        assertEquals(HumanoidArm.LEFT, hotbarItem.hand)
    }

    @Test
    fun `loads missing attributes successfully`() {
        val hotbarItem = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/no_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<HotbarItemXml>(serialized)
        }

        assertEquals("test_hotbar_item_no", hotbarItem.name)

        assertNull(hotbarItem.x)
        assertNull(hotbarItem.y)
        assertNull(hotbarItem.z)
        assertNull(hotbarItem.scale)
        assertNull(hotbarItem.enabled)
        assertNull(hotbarItem.rgba)
        assertNull(hotbarItem.srcX)
        assertNull(hotbarItem.srcY)
        assertNull(hotbarItem.w)
        assertNull(hotbarItem.h)
        assertNull(hotbarItem.srcW)
        assertNull(hotbarItem.srcH)
        assertNull(hotbarItem.texture)
        assertNull(hotbarItem.slot)
        assertNull(hotbarItem.itemXoffset)
        assertNull(hotbarItem.itemYoffset)
        assertNull(hotbarItem.hand)
    }

    @Test
    fun `loads hotbar item with right hand`() {
        val hotbarItem = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/right_hand.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<HotbarItemXml>(serialized)
        }

        assertEquals("offhand_item", hotbarItem.name)
        assertEquals(AnonymousExpressionIntermediate("0", CacheType.STATIC), hotbarItem.slot)
        assertEquals(HumanoidArm.RIGHT, hotbarItem.hand)
    }
}
