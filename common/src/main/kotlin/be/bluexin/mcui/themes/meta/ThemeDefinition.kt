package be.bluexin.mcui.themes.meta

import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.themes.loader.JsonThemeLoader
import be.bluexin.mcui.themes.loader.XmlThemeLoader
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.resources.ResourceLocation
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

data class ThemeDefinition(
    val id: ResourceLocation,
    /**
     * Root for the theme's configuration files (hud.json, settings.json, ...)
     */
    val themeRoot: ResourceLocation,
    val name: String,
    val metadata: ThemeMetadata,
    val type: HudFormat,
    val fragments: Map<ResourceLocation, ResourceLocation>
) {
    /**
     * Root for the theme's textures
     */
    val texturesRoot = ResourceLocation(themeRoot.namespace, "textures/${id.path}/")
    val nameTranslationKey = "theme.${id.toString().replace(':', '.')}.name"
    val descTranslationKey = "theme.${id.toString().replace(':', '.')}.description"
}

// this is actually hud format and should not matter much
enum class HudFormat(val hudFileSuffix: String, private val loaderP: KoinComponent.() -> AbstractThemeLoader) :
    KoinComponent {
    XML("hud.xml", { get<XmlThemeLoader>() }),
    JSON("hud.json", { get<JsonThemeLoader>() });

    val loader: AbstractThemeLoader get() = loaderP()

    companion object {
        private val fromFileExtension = entries.associateBy { it.hudFileSuffix.substringAfterLast('.') }

        fun fromFile(fileName: String): HudFormat? =
            fromFileExtension.values.firstOrNull { fileName.endsWith(it.hudFileSuffix) }

        fun fromFileExtension(fileName: String): HudFormat? = fromFileExtension[fileName.substringAfterLast('.')]
    }
}

@Serializable
data class ThemeMetadata(
    val version: String = UNKNOWN_VERSION,
    val format: ThemeFormat,
    val fragments: String = "fragments"
) {
    companion object {
        const val UNKNOWN_VERSION = "unknown"
    }
}

@Serializable(ThemeFormat.Serializer::class)
enum class ThemeFormat(val identifier: String) {
    ERROR("mcui:error"),
    LEGACY_SAOUI("mcui:legacy"),
    MCUI_ALPHA("mcui:alpha");

    companion object {
        private val lookup = entries.associateBy(ThemeFormat::identifier)
        operator fun get(identifier: String): ThemeFormat = lookup[identifier] ?: LEGACY_SAOUI
    }

    object Serializer : KSerializer<ThemeFormat> {
        override fun deserialize(decoder: Decoder) = ThemeFormat[decoder.decodeString()]
        override val descriptor = PrimitiveSerialDescriptor(ThemeFormat::class.qualifiedName!!, PrimitiveKind.STRING)
        override fun serialize(encoder: Encoder, value: ThemeFormat) = encoder.encodeString(value.identifier)
    }
}
