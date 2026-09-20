package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.Constants
import be.bluexin.mcui.themes.elements.Hud
import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.themes.loader.XmlThemeLoader
import be.bluexin.mcui.themes.meta.HudFormat
import be.bluexin.mcui.themes.meta.ThemeDefinition
import be.bluexin.mcui.util.append
import net.minecraft.server.packs.resources.ResourceManager
import org.koin.core.annotation.Single

/**
 * Builds the modern [elements.Hud][Hud] tree from a theme's legacy XML, going through the
 * DTO layer (`HudXml`) and the factory pipeline (`Factory`).
 *
 * This is the "new loader" side of the migration: the same XML files keep loading, but they get
 * converted into the visitor-renderable `elements.*` tree instead of (or alongside) the legacy one.
 */
@Single
internal class ModernHudBuilder(
    private val xmlThemeLoader: XmlThemeLoader,
    private val hudFactory: HudFactory,
    private val fragmentFactory: FragmentFactory,
) {

    fun buildHud(resourceManager: ResourceManager, theme: ThemeDefinition): Result<Hud> = runCatching {
        val location = theme.themeRoot.append("/${HudFormat.XML.hudFileSuffix}")
        val hudXml = xmlThemeLoader.loadHudXml(resourceManager, location)

        val fragmentPaths = theme.fragments
        val context = LegacyFormatContext(fragmentFactory) { id ->
            fragmentPaths[id]?.let { path -> xmlThemeLoader.loadFragmentXml(resourceManager, path) }
        }

        hudFactory.create(hudXml, context).getOrThrow()
    }.onFailure {
        Constants.LOG.warn("Failed to load modern element tree for $theme", it)
        AbstractThemeLoader.Reporter += it.message ?: "unknown error"
    }
}