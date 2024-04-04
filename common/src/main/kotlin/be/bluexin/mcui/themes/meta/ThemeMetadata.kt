package be.bluexin.mcui.themes.meta

import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.themes.loader.JsonThemeLoader
import be.bluexin.mcui.themes.loader.XmlThemeLoader
import net.minecraft.resources.ResourceLocation

data class ThemeMetadata(
    val id: ResourceLocation,
    /**
     * Root for the theme's configuration files (hud.json, settings.json, ...)
     */
    val themeRoot: ResourceLocation,
    val name: String,
    val type: ThemeFormat,
    val fragments: Map<ResourceLocation, ResourceLocation>
) {
    /**
     * Root for the theme's textures
     */
    val texturesRoot = ResourceLocation(themeRoot.namespace, "textures/${id.path}/")
    val nameTranslationKey = "theme.${id.toString().replace(':', '.')}.name"
    val descTranslationKey = "theme.${id.toString().replace(':', '.')}.description"
}

enum class ThemeFormat(val hudFileSuffix: String, val loader: () -> AbstractThemeLoader) {
    XML("hud.xml", { XmlThemeLoader }),
    JSON("hud.json", { JsonThemeLoader });

    companion object {
        private val fromFileExtension = entries.associateBy { it.hudFileSuffix.substringAfterLast('.') }

        fun fromFile(fileName: String): ThemeFormat? =
            fromFileExtension.values.firstOrNull { fileName.endsWith(it.hudFileSuffix) }

        fun fromFileExtension(fileName: String): ThemeFormat? = fromFileExtension[fileName.substringAfterLast('.')]
    }
}
