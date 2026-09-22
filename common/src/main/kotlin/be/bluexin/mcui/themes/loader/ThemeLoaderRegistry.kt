package be.bluexin.mcui.themes.loader

import be.bluexin.mcui.themes.meta.ThemeMetadata
import org.koin.core.annotation.Single

/**
 * Dispatches a theme's HUD loading to the [AbstractThemeLoader] of its format
 * ([ThemeMetadata.modernSource]). One loader per serialized format : `legacy_xml` is handled by
 * [LegacyXmlThemeLoader] today ; future formats (xml_v1, json_v1, ...) add their own loader and
 * [ThemeMetadata.modernSource] value, without changing the load flow.
 */
@Single
class ThemeLoaderRegistry(
    private val loaders: List<AbstractThemeLoader>,
) {
    fun resolve(metadata: ThemeMetadata): AbstractThemeLoader? =
        loaders.firstOrNull { it.formatId == metadata.modernSource }
}