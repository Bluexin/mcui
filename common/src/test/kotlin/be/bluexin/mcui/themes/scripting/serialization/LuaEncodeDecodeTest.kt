package be.bluexin.mcui.themes.scripting.serialization

import be.bluexin.mcui.themes.elements.Widget
import be.bluexin.mcui.themes.miniscript.LibHelper
import nl.adaptivity.xmlutil.XmlStreaming
import nl.adaptivity.xmlutil.serialization.XML
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import kotlin.test.Test
import kotlin.test.assertEquals

class LuaEncodeDecodeTest : KoinTest {

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { LibHelper() }
            }
        )
    }

    @Test
    fun `test widget with variables and one extra to lua`() {
        val widget = requireNotNull(
            javaClass.getResourceAsStream("/${LuaEncodeDecodeTest::class.simpleName}/widget1.xml")
        ).use {
            XML {
                autoPolymorphic = true
            }.decodeFromReader<Widget>(XmlStreaming.newReader(it, Charsets.UTF_8.name()))
        }

        val lua = AbstractLuaEncoder.LuaEncoder().apply {
            encodeSerializableValue(Widget.serializer(), widget)
        }.data

        val fromLua = AbstractLuaDecoder.LuaDecoder(lua).decodeSerializableValue(Widget.serializer())

        assertEquals(widget.expect, fromLua.expect)
        assertEquals(widget.extraScripts, fromLua.extraScripts)
    }
}