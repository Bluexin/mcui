package be.bluexin.mcui.themes.meta

import be.bluexin.mcui.Constants
import be.bluexin.mcui.util.append
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.Resource
import net.minecraft.server.packs.resources.ResourceManager
import org.koin.core.annotation.Single
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipFile
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.streams.asSequence

interface ThemeDetector {
    /**
     * Returns a map of path to display name for themes
     */
    fun listThemes(resourceManager: ResourceManager): Map<ResourceLocation, ThemeMetadata>
}

@Single
internal class ThemeDetectorImpl : ThemeDetector {

    /**
     * Returns a map of path to display name for themes
     */
    override fun listThemes(resourceManager: ResourceManager): Map<ResourceLocation, ThemeMetadata> {
        // TODO : variant returning the whole stack to check for overriding of critical scripts ?
        // TODO : legacy themes
        return resourceManager.listResources("themes") { rl ->
            ThemeFormat.entries.any { rl.path.endsWith(it.hudFileSuffix) }
        }.map { (key, value) ->
            (key to value).extractThemesMetadataV2(resourceManager)
        }.filter { (_, it) ->
            try {
                it.type.loader
                    .loadHud(resourceManager, it.themeRoot.append("/${it.type.hudFileSuffix}"))
                    .setup(emptyMap())
                true
            } catch (e: Throwable) {
                Constants.LOG.warn("Could not load HUD for $it !", e)
                false
            }
        }.toMap()
    }

    /**
     * Find `hud.xml`/`hud.json` files defined in given [File], which can be a zip/jar or folder.
     *
     * This returns a List and not a lazy Sequence to be able to close opened resources (see usages of [use]) within
     * the scope of this method.
     */
    @Deprecated("Should not need this anymore")
    private fun File.findThemePaths(): Map<Path, List<Path>> {
        Constants.LOG.debug("Analyzing {}", this)
        return when {
            // jar
            this.isFile -> ZipFile(this).use { zip ->
                zip.entries().asSequence()
                    .filter { !it.isDirectory && (it.name.endsWith(".xml") || it.name.endsWith(".json")) }
                    .map { Paths.get(it.name) }
                    .groupBy(Path::getParent)
            }
            // folder
            this.isDirectory -> {
                val thisAsPath = this.toPath().let {
                    if (it.name != "main") it
                    else it.parent.parent.parent.resolve("resources").resolve("main") // in-dev
                }
                Files.find(thisAsPath, 6, { _, fileAttributes ->
                    fileAttributes.isRegularFile
                }).map { thisAsPath.relativize(it) }.use { s ->
                    s.asSequence()
                        .filter { it.name.endsWith(".xml") || it.name.endsWith(".json") }
                        .groupBy(Path::getParent)
                }
            }
            // unknown
            else -> {
                Constants.LOG.warn("Unknown file type : {}", this)
                emptyMap()
            }
        }
    }

    /**
     * Transform a list of paths into their respective metadata, indexed by id
     * @param themeDomain Domain name for the theme
     */
    private fun Map<Path, List<Path>>.extractThemesMetadata(themeDomain: String): Map<ResourceLocation, ThemeMetadata> =
        this.mapNotNull { (parent, paths) ->
            paths.mapNotNull { path ->
                ThemeFormat.fromFile(path.toString())?.let {
                    Triple(path, it, this[parent.resolve("fragments")].orEmpty())
                }
            }.singleOrNull()
        }.associate { (path, format, fragments) ->
            val domain = path.subpath(1, 2).toString()
            val name = path.parent.fileName.toString().let {
                if (it != "themes") it else {
                    Constants.LOG.warn("Theme pack {} is using the old theme structure !", themeDomain)
                    themeDomain.removeSuffix(".zip")
                }
            }
            val id = ResourceLocation(themeDomain, name)
            Constants.LOG.debug("Found candidate theme {} from {} at {}", id, themeDomain, path)
            val domainPath = path.parent.toString().replace(File.separator, "/").removePrefix("assets/$domain/")
            val themeRoot = ResourceLocation(domain, domainPath)
            id to ThemeMetadata(
                id = id,
                themeRoot = themeRoot,
                name = name,
                type = format,
                fragments = fragments.associate {
                    ResourceLocation(name, it.nameWithoutExtension) to themeRoot.append("/fragments/${it.fileName}")
                }
            )
        }

    /**
     *
     */
    private fun Pair<ResourceLocation, Resource>.extractThemesMetadataV2(
        resourceManager: ResourceManager
    ): Pair<ResourceLocation, ThemeMetadata> {
        val (hudRl, hud) = this
        val themeRoot = hudRl.parent
        val themeName = themeRoot.path.substringAfterLast('/').let {
            if (it != "themes") it else {
                val packName = hud.sourcePackId()
                Constants.LOG.warn("Theme pack {} is using the old theme structure !", packName)
                packName.removeSuffix(".zip")
            }
        }
        val themeId = ResourceLocation(themeRoot.namespace, themeName)
        Constants.LOG.debug("Found candidate theme {} from {}", themeId, themeRoot)
        val fragments = resourceManager.listResources(themeRoot.path + "/fragments") {
            it.namespace == themeRoot.namespace && ThemeFormat.fromFileExtension(it.path) != null
        }.map { (rl, _) ->
            ResourceLocation(
                themeId.toString().replace(':', '.'),
                rl.path.substringAfter("/fragments/").substringBeforeLast('.')
            ) to rl
        }.toMap()
        return themeId to ThemeMetadata(
            id = themeId,
            themeRoot = themeRoot,
            name = themeName,
            type = ThemeFormat.fromFileExtension(hudRl.path)!!,
            fragments = fragments
        )
    }

    private val String.parent get() = substringBeforeLast('/')
    private val ResourceLocation.parent get() = ResourceLocation(namespace, path.parent)
}
