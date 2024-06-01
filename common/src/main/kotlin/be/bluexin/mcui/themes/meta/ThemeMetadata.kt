package be.bluexin.mcui.themes.meta

import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.themes.loader.JsonThemeLoader
import be.bluexin.mcui.themes.loader.XmlThemeLoader
import net.minecraft.resources.ResourceLocation
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

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

enum class ThemeFormat(val hudFileSuffix: String, private val loaderP: KoinComponent.() -> AbstractThemeLoader) :
    KoinComponent {
    XML("hud.xml", { get<XmlThemeLoader>() }),
    JSON("hud.json", { get<JsonThemeLoader>() });

    val loader: AbstractThemeLoader get() = loaderP()

    companion object {
        private val fromFileExtension = entries.associateBy { it.hudFileSuffix.substringAfterLast('.') }

        fun fromFile(fileName: String): ThemeFormat? =
            fromFileExtension.values.firstOrNull { fileName.endsWith(it.hudFileSuffix) }

        fun fromFileExtension(fileName: String): ThemeFormat? = fromFileExtension[fileName.substringAfterLast('.')]
    }
}
