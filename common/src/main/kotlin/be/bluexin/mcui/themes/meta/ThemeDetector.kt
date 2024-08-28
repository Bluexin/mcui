package be.bluexin.mcui.themes.meta

import be.bluexin.mcui.logger
import be.bluexin.mcui.util.append
import be.bluexin.mcui.util.debug
import be.bluexin.mcui.util.error
import be.bluexin.mcui.util.warn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.Resource
import net.minecraft.server.packs.resources.ResourceManager
import org.koin.core.annotation.Single

interface ThemeDetector {
    /**
     * Returns a map of path to display name for themes
     */
    fun listThemes(resourceManager: ResourceManager): Map<ResourceLocation, ThemeDefinition>
}

@Single
internal class ThemeDetectorImpl : ThemeDetector {

    private val logger = logger()

    /**
     * Returns a map of path to display name for themes
     */
    override fun listThemes(resourceManager: ResourceManager): Map<ResourceLocation, ThemeDefinition> {
        // TODO : use variant returning the whole stack to check for overriding of critical scripts ?
        val modernThemes = resourceManager.listResources("themes") { rl ->
            rl.path.endsWith("/theme.mcui.json")
        }.map { it.extractMcuiThemeDefinition(resourceManager) }
        val modernThemesSet = modernThemes.map { it.second.themeRoot }.toSet()
        return (modernThemes + resourceManager.listResources("themes") { rl ->
            rl.parent !in modernThemesSet && HudFormat.entries.any {
                rl.path.endsWith("/${it.hudFileSuffix}")
            }
        }.map { (key, value) ->
            (key to value).extractThemeDefinition(resourceManager)
        }).filter { (_, it) ->
            try {
                it.type.loader
                    .loadHud(resourceManager, it.themeRoot.append("/${it.type.hudFileSuffix}"))
                    .setup(emptyMap())
                true
            } catch (e: Throwable) {
                logger.warn(e) { "Could not load HUD for $it !" }
                false
            }
        }.toMap()
    }

    /**
     *
     */
    private fun Pair<ResourceLocation, Resource>.extractThemeDefinition(
        resourceManager: ResourceManager
    ): Pair<ResourceLocation, ThemeDefinition> {
        val (hudRl, hud) = this
        val themeRoot = hudRl.parent
        val themeName = themeRoot.path.substringAfterLast('/').let {
            if (it != "themes") it else {
                val packName = hud.sourcePackId()
                logger.warn { "Theme pack $packName is using the old theme structure !" }
                packName.removeSuffix(".zip")
            }
        }
        val themeId = ResourceLocation(themeRoot.namespace, themeName)
        logger.debug { "Found candidate theme $themeId from $themeRoot" }
        val fragments = resourceManager.listResources(themeRoot.path + "/fragments") {
            it.namespace == themeRoot.namespace && HudFormat.fromFileExtension(it.path) != null
        }.map { (rl, _) ->
            ResourceLocation(
                themeId.toString().replace(':', '.'),
                rl.path.substringAfter("/fragments/").substringBeforeLast('.')
            ) to rl
        }.toMap()
        return themeId to ThemeDefinition(
            id = themeId,
            themeRoot = themeRoot,
            name = themeName,
            metadata = ThemeMetadata(format = ThemeFormat.LEGACY_SAOUI /*TODO*/),
            type = HudFormat.fromFileExtension(hudRl.path)!!,
            fragments = fragments
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        useAlternativeNames = false
        allowTrailingComma = true
        allowComments = true
    }

    /**
     * For modern format
     */
    @OptIn(ExperimentalSerializationApi::class)
    private fun Map.Entry<ResourceLocation, Resource>.extractMcuiThemeDefinition(
        resourceManager: ResourceManager
    ): Pair<ResourceLocation, ThemeDefinition> {
        val (metadataRl, metadata) = this
        val themeRoot = metadataRl.parent
        val themeName = themeRoot.path.substringAfterLast('/')
        val themeId = ResourceLocation(themeRoot.namespace, themeName)
        val themeMetadata: ThemeMetadata = try {
            metadata.open().use(json::decodeFromStream)
        } catch (e: Throwable) {
            logger.error(e) { "Could not decode metadata from $metadataRl" }
            ThemeMetadata(format = ThemeFormat.ERROR)
        }
        logger.debug { "Found candidate theme $themeId from $themeRoot and metadata $themeMetadata" }
        val fragments = resourceManager.listResources("${themeRoot.path}/${themeMetadata.fragments}") {
            it.namespace == themeRoot.namespace && HudFormat.fromFileExtension(it.path) != null
        }.map { (rl, _) ->
            ResourceLocation(
                themeId.toString().replace(':', '.'),
                rl.path.substringAfter("/${themeMetadata.fragments}/").substringBeforeLast('.')
            ) to rl
        }.toMap()
        return themeId to ThemeDefinition(
            id = themeId,
            themeRoot = themeRoot,
            name = themeName,
            metadata = themeMetadata,
            type = HudFormat.XML /* TODO */,
            fragments = fragments
        )
    }

    private val String.parent get() = substringBeforeLast('/')
    private val ResourceLocation.parent get() = ResourceLocation(namespace, path.parent)
}
