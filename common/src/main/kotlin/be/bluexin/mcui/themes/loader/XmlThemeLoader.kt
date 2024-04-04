package be.bluexin.mcui.themes.loader

import be.bluexin.mcui.themes.meta.ThemeFormat
import be.bluexin.mcui.themes.elements.Fragment
import be.bluexin.mcui.themes.elements.Hud
import be.bluexin.mcui.themes.elements.Widget
import nl.adaptivity.xmlutil.StAXReader
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlSerializationPolicy
import java.io.InputStream

object XmlThemeLoader : AbstractThemeLoader(ThemeFormat.XML) {

    internal val xml by lazy {
        XML {
            defaultPolicy {
                encodeDefault = XmlSerializationPolicy.XmlEncodeDefault.NEVER
            }
            indentString = "    "
            autoPolymorphic = true
        }
    }

    override fun InputStream.loadHud(): Hud = use {
        xml.decodeFromReader(StAXReader(it, "UTF-8"))
    }

    override fun InputStream.loadFragment(): Fragment = use {
        xml.decodeFromReader(StAXReader(it, "UTF-8"))
    }

    override fun InputStream.loadWidget(): Widget = use {
        xml.decodeFromReader(StAXReader(it, "UTF-8"))
    }
}
