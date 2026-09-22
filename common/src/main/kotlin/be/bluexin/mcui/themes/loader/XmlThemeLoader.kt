package be.bluexin.mcui.themes.loader

import be.bluexin.mcui.Constants
import be.bluexin.mcui.themes.elements.legacy.Fragment
import be.bluexin.mcui.themes.elements.legacy.Hud
import be.bluexin.mcui.themes.elements.legacy.Widget
import be.bluexin.mcui.themes.meta.ThemeDefinition
import be.bluexin.mcui.themes.meta.ThemeMetadata
import be.bluexin.mcui.themes.serde.legacyformat.factory.FragmentFactory
import be.bluexin.mcui.themes.serde.legacyformat.factory.HudFactory
import be.bluexin.mcui.themes.serde.legacyformat.factory.LegacyFormatContext
import be.bluexin.mcui.themes.serde.legacyformat.xml.FragmentXml
import be.bluexin.mcui.themes.serde.legacyformat.xml.HudXml
import be.bluexin.mcui.util.append
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import nl.adaptivity.xmlutil.XmlStreaming
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlSerializationPolicy
import org.koin.core.annotation.Single
import java.io.InputStream

/**
 * The "legacy_xml" format loader (one [AbstractThemeLoader] per format).
 *
 * Loads the legacy tree via [AbstractThemeLoader], and the modern `elements.*` tree straight from
 * the same XML DTOs through the legacy factory pipeline ([HudFactory]/[FragmentFactory]) for the
 * A/B migration.
 */
@Single
class XmlThemeLoader internal constructor(
    private val hudFactory: HudFactory,
    private val fragmentFactory: FragmentFactory,
) : AbstractThemeLoader() {

    override val formatId: String = ThemeMetadata.LEGACY_XML

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

    override fun buildModernHud(
        resourceManager: ResourceManager,
        theme: ThemeDefinition,
    ): be.bluexin.mcui.themes.elements.Hud? = runCatching {
        val location = theme.themeRoot.append("/${ThemeDefinition.HUD_FILE}")
        val hudXml = loadHudXml(resourceManager, location)

        val fragmentPaths = theme.fragments
        val context = LegacyFormatContext(fragmentFactory) { id ->
            fragmentPaths[id]?.let { path -> loadFragmentXml(resourceManager, path) }
        }

        hudFactory.create(hudXml, context).getOrThrow()
    }.onFailure {
        Constants.LOG.warn("Failed to load modern element tree for $theme", it)
        AbstractThemeLoader.Reporter += it.message ?: "unknown error"
    }.getOrNull()

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