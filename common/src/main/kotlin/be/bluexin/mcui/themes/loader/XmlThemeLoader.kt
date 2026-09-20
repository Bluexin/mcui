package be.bluexin.mcui.themes.loader

import be.bluexin.mcui.themes.elements.legacy.Fragment
import be.bluexin.mcui.themes.elements.legacy.Hud
import be.bluexin.mcui.themes.elements.legacy.Widget
import be.bluexin.mcui.themes.meta.HudFormat
import be.bluexin.mcui.themes.serde.legacyformat.factory.ModernHudBuilder
import be.bluexin.mcui.themes.serde.legacyformat.xml.FragmentXml
import be.bluexin.mcui.themes.serde.legacyformat.xml.HudXml
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import nl.adaptivity.xmlutil.XmlStreaming
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlSerializationPolicy
import org.koin.core.annotation.Single
import java.io.InputStream

@Single
class XmlThemeLoader internal constructor(
    settingsLoader: SettingsLoader,
    modernHudBuilder: ModernHudBuilder,
) : AbstractThemeLoader(HudFormat.XML, settingsLoader, modernHudBuilder) {

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

    internal fun loadHudXml(resourceManager: ResourceManager, location: ResourceLocation): HudXml =
        resourceManager.getResourceOrThrow(location).open().load()

    internal fun loadFragmentXml(resourceManager: ResourceManager, location: ResourceLocation): FragmentXml? = try {
        resourceManager.getResource(location).map { resource ->
            resource.open().use { stream -> stream.load<FragmentXml>() }
        }.orElse(null)
    } catch (e: Throwable) {
        AbstractThemeLoader.Reporter += "Failed to load fragment $location : ${e.message}"
        null
    }
}