package be.bluexin.mcui.themes.meta

import be.bluexin.mcui.logger
import be.bluexin.mcui.themes.meta.ThemeDefinition.Companion.themeResource
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
            (key to value).extractLegacyThemeDefinition(resourceManager)
        })/*.filter { (_, it) ->
            it.hud != null && try {
                it.hud
                    .let(HudFormat::fromFile)
                    ?.loader
                    ?.loadHud(resourceManager, it.hud)
                    ?.setup(emptyMap()) != null
            } catch (e: Throwable) {
                logger.warn(e) { "Could not load HUD for $it !" }
                false
            }
        }*/.toMap()
    }

    /**
     * For legacy SAOUI formats
     */
    private fun Pair<ResourceLocation, Resource>.extractLegacyThemeDefinition(
        resourceManager: ResourceManager
    ): Pair<ResourceLocation, ThemeDefinition> {
        val (hudRl, hud) = this
        val themeRoot = hudRl.parent
        var format: ThemeFormat
        val themeName = themeRoot.path.substringAfterLast('/').let {
            if (it != "themes") {
                format = ThemeFormat.MODERN_LEGACY_SAOUI
                it
            } else {
                format = ThemeFormat.LEGACY_SAOUI
                val packName = hud.sourcePackId()
                logger.warn { "Theme pack $packName is using a very old theme structure, and will probably not work well" }
                packName.removeSuffix(".zip")
            }
        }.lowercase()
        val themeId = ResourceLocation(themeRoot.namespace, themeName)
        logger.debug { "Found candidate theme $themeId from $themeRoot" }
        return themeId to ThemeDefinition(
            id = themeId,
            themeRoot = themeRoot,
            name = themeName,
            metadata = ThemeMetadata(format = format),
            hud = hudRl,
            settings = if (format == ThemeFormat.MODERN_LEGACY_SAOUI) resourceManager.findThemeSettings(themeRoot) else null,
            fragments = resourceManager.findThemeResources(themeId, themeRoot, "fragments"),
            widgets = emptyMap(),
            scripts = emptyMap(),
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
     * For modern MCUI format
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
        val hudCandidates = HudFormat.entries.asSequence()
            .map { themeRoot.append("/${it.hudFileSuffix}") }
            .filter { resourceManager.getResource(it).isPresent }
            .toList()
        val hud = when (hudCandidates.size) {
            0 -> {
                logger.debug { "No HUD definition found in $themeId" }
                null
            }

            1 -> hudCandidates.single()
            else -> {
                logger.warn { "Multiple HUD candidates found for $themeId : ${hudCandidates}, none will be loaded." }
                null
            }
        }
        return themeId to ThemeDefinition(
            id = themeId,
            themeRoot = themeRoot,
            name = themeName,
            metadata = themeMetadata,
            hud = hud,
            settings = resourceManager.findThemeSettings(themeRoot),
            fragments = resourceManager.findThemeResources(themeId, themeRoot, themeMetadata.fragments),
            widgets = resourceManager.findThemeResources(themeId, themeRoot, themeMetadata.widgets),
            scripts = resourceManager.findThemeResources(themeId, themeRoot, themeMetadata.scripts, setOf("lua")),
        )
    }

    private fun ResourceManager.findThemeResources(
        themeId: ResourceLocation,
        themeRoot: ResourceLocation,
        path: String,
        allowedExtensions: Set<String> = setOf("json", "xml"),
    ): Map<ResourceLocation, ResourceLocation> =
        listResources("${themeRoot.path}/$path") {
            it.namespace == themeRoot.namespace && it.path.substringAfterLast('.') in allowedExtensions
        }.map { (rl, _) ->
            themeId.themeResource(
                rl.path.substringAfter("/$path/").substringBeforeLast('.')
            ) to rl
        }.toMap()

    private fun ResourceManager.findThemeSettings(themeRoot: ResourceLocation): ResourceLocation? {
        val settingsLocation = themeRoot.append("/settings.json")
        return if (getResource(settingsLocation).isPresent) settingsLocation else null
    }

    private val String.parent get() = substringBeforeLast('/')
    private val ResourceLocation.parent get() = ResourceLocation(namespace, path.parent)
}
