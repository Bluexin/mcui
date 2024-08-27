package be.bluexin.mcui.themes.loader

import be.bluexin.mcui.themes.elements.Fragment
import be.bluexin.mcui.themes.elements.Hud
import be.bluexin.mcui.themes.elements.Widget
import be.bluexin.mcui.themes.meta.ThemeFormat
import nl.adaptivity.xmlutil.XmlStreaming
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlSerializationPolicy
import org.koin.core.annotation.Single
import java.io.InputStream

@Single
class XmlThemeLoader(settingsLoader: SettingsLoader) : AbstractThemeLoader(ThemeFormat.XML, settingsLoader) {

    internal val xml by lazy {
        XML {
            defaultPolicy {
                encodeDefault = XmlSerializationPolicy.XmlEncodeDefault.NEVER
            }
            indent = 4
            autoPolymorphic = true
        }
    }

    private inline fun <reified T> InputStream.load(): T = use {
        xml.decodeFromReader(XmlStreaming.newReader(it, Charsets.UTF_8.name()))
    }

    override fun InputStream.loadHud(): Hud = load()
    override fun InputStream.loadFragment(): Fragment = load()
    override fun InputStream.loadWidget(): Widget = load()
}
