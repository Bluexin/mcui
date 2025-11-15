package be.bluexin.mcui.themes.serde.legacyformat.dto

import be.bluexin.mcui.themes.elements.HudPartType
import be.bluexin.mcui.themes.miniscript.CacheType
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import kotlin.test.*

class HudXmlTest {

    private val xml = XML {
        autoPolymorphic = true
    }

    @Test
    fun `loads all attributes successfully`() {
        val hud = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/all_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<HudXml>(serialized)
        }

        assertEquals("test_hud", hud.name)
        assertEquals("1.0.0", hud.version)

        val entries = hud.parts.parts
        assertEquals(2, entries.size)

        // First entry: HEALTH_BOX
        val healthEntry = entries[0]
        assertEquals(HudPartType.HEALTH_BOX, healthEntry.key)
        val healthValue = healthEntry.value
        assertEquals("health box", healthValue.name)
        val children = assertNotNull(healthValue.children?.elements)
        assertEquals(1, children.size)
        assertTrue(children[0] is FragmentReferenceXml)
        assertEquals("mcui.test:health_box", (children[0] as FragmentReferenceXml).id)

        // Second entry: EXPERIENCE
        val expEntry = entries[1]
        assertEquals(HudPartType.EXPERIENCE, expEntry.key)
        val expValue = expEntry.value
        assertEquals("exp", expValue.name)
        assertEquals(AnonymousExpressionIntermediate("scaledwidth / 2.0", CacheType.SIZE_CHANGE), expValue.x)
        assertEquals(AnonymousExpressionIntermediate("scaledheight - 64", CacheType.SIZE_CHANGE), expValue.y)
        assertEquals(AnonymousExpressionIntermediate("!settings.boolean(\"hud:hide_exp\")"), expValue.enabled)
    }

    @Test
    fun `loads minimal hud successfully`() {
        val hud = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/minimal.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<HudXml>(serialized)
        }

        assertEquals("HUD", hud.name)
        assertEquals("unknown", hud.version)

        val entries = hud.parts.parts
        assertEquals(1, entries.size)

        val crosshairEntry = entries[0]
        assertEquals(HudPartType.CROSS_HAIR, crosshairEntry.key)
        val crosshairValue = crosshairEntry.value as ElementGroupXml
        assertEquals("crosshair", crosshairValue.name)
        assertNull(crosshairValue.children)
    }
}
