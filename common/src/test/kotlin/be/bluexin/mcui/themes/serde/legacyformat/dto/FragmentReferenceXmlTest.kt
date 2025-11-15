package be.bluexin.mcui.themes.serde.legacyformat.dto

import be.bluexin.mcui.themes.miniscript.CacheType
import be.bluexin.mcui.themes.miniscript.serialization.JelType
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FragmentReferenceXmlTest {

    private val xml = XML {
        autoPolymorphic = true
    }

    @Test
    fun `loads all attributes successfully`() {
        val fragmentRef = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/all_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<FragmentReferenceXml>(serialized)
        }

        assertEquals("test_fragment_ref_all", fragmentRef.name)
        assertEquals("mcui.test:my_fragment", fragmentRef.id)

        assertEquals(AnonymousExpressionIntermediate("50"), fragmentRef.x)
        assertEquals(AnonymousExpressionIntermediate("100"), fragmentRef.y)
        assertEquals(AnonymousExpressionIntermediate("2", CacheType.STATIC), fragmentRef.z)
        assertEquals(AnonymousExpressionIntermediate("1.2", CacheType.STATIC), fragmentRef.scale)
        assertEquals(AnonymousExpressionIntermediate("player.health > 0"), fragmentRef.enabled)

        val variables = assertNotNull(fragmentRef.variables?.values)
        assertEquals(2, variables.size)
        assertEquals(
            NamedExpressionIntermediate(
                JelType.STRING,
                expression = "\"Hello Fragment\"",
                cacheType = CacheType.STATIC
            ),
            variables["text"]
        )
        assertEquals(
            NamedExpressionIntermediate(
                JelType.BOOLEAN,
                expression = "true"
            ),
            variables["enabled"]
        )
    }

    @Test
    fun `loads missing attributes successfully`() {
        val fragmentRef = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/no_attributes.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<FragmentReferenceXml>(serialized)
        }

        assertEquals("test_fragment_ref_no", fragmentRef.name)

        assertNull(fragmentRef.id)
        assertNull(fragmentRef.x)
        assertNull(fragmentRef.y)
        assertNull(fragmentRef.z)
        assertNull(fragmentRef.scale)
        assertNull(fragmentRef.enabled)
        assertNull(fragmentRef.variables)
    }

    @Test
    fun `loads fragment reference without variables`() {
        val fragmentRef = requireNotNull(
            javaClass.getResourceAsStream("/${this::class.simpleName}/minimal.xml")
        ).use {
            val serialized = it.bufferedReader().readText()
            xml.decodeFromString<FragmentReferenceXml>(serialized)
        }

        assertEquals("test_fragment_ref_minimal", fragmentRef.name)
        assertEquals("mcui.test:simple_fragment", fragmentRef.id)

        assertNull(fragmentRef.x)
        assertNull(fragmentRef.y)
        assertNull(fragmentRef.z)
        assertNull(fragmentRef.scale)
        assertNull(fragmentRef.enabled)
        assertNull(fragmentRef.variables)
    }
}
