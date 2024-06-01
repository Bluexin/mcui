package be.bluexin.mcui.themes.meta

import be.bluexin.mcui.themes.elements.Widget
import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.themes.loader.JsonThemeLoader
import be.bluexin.mcui.themes.loader.SettingsLoader
import be.bluexin.mcui.themes.loader.XmlThemeLoader
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import java.io.File
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.system.measureTimeMillis

object ThemeConverter {

    private val logger by lazy { LogManager.getLogger(javaClass) }

    @JvmStatic
    fun main(args: Array<String>) {
//        Launch.blackboard = mapOf("fml.deobfuscatedEnvironment" to true)
        if (args.size != 2) {
            logger.error(
                "Wrong arguments provided : {}, expected [xml theme path] [output file path]",
                args.joinToString(prefix = "[", postfix = "]")
            )
            return
        }
        val toRead = File(args[0])
        val toWrite = File(args[1])
        val settingsLoader = SettingsLoader()
        val xmlThemeLoader = XmlThemeLoader(settingsLoader)
        val (hud, fragments) = try {
            logger.info("Loading $toRead")
            clearAndLogErrors {
                val hud = xmlThemeLoader.loadHud(toRead)
                val fragmentRoot = toRead.toPath().resolveSibling("fragments")
                val ns = fragmentRoot.parent.name
                val fragments = buildMap {
                    if (fragmentRoot.isDirectory()) {
                        fragmentRoot.listDirectoryEntries().forEach {
                            put(
                                ResourceLocation(
                                    ns,
                                    it.nameWithoutExtension
                                )
                            ) { xmlThemeLoader.loadFragment(it.toFile()) }
                        }
                    }
                }
                hud.setup(fragments)

                hud to fragments
            }
        } catch (e: Exception) {
            val message = "Something went wrong reading $toRead"
            logger.error(message, e)
            return
        }
        val jsonThemeLoader = JsonThemeLoader(settingsLoader)
        try {
            logger.info("Exporting to $toWrite")
            clearAndLogErrors {
                toWrite.parentFile.mkdirs()
                jsonThemeLoader.export(hud, toWrite)
                if (fragments.isNotEmpty()) {
                    val fragmentExportRoot = toWrite.toPath().resolveSibling("fragments")
                    fragmentExportRoot.toFile().mkdirs()
                    fragments.forEach { (key, fragment) ->
                        jsonThemeLoader.export(fragment(), fragmentExportRoot.resolve("${key.path}.json").toFile())
                    }
                }
            }
        } catch (e: Exception) {
            val message = "Something went wrong writing $toWrite"
            logger.error(message, e)
            return
        }
        logger.info("Converted $toRead to $toWrite. Checking loading")

        val time = measureTimeMillis {
            clearAndLogErrors {
                val read = jsonThemeLoader.loadHud(toWrite)
                val fragmentRoot = toWrite.toPath().resolveSibling("fragments")
                val ns = fragments.keys.first().namespace
                val readFragments = buildMap {
                    if (fragmentRoot.isDirectory()) {
                        fragmentRoot.listDirectoryEntries().forEach {
                            put(
                                ResourceLocation(
                                    ns,
                                    it.nameWithoutExtension
                                )
                            ) { jsonThemeLoader.loadFragment(it.toFile()) }
                        }
                    }
                }

                read.setup(readFragments)
            }
        }
        logger.info("Read in $time ms.")
    }

    private inline fun <T> clearAndLogErrors(body: () -> T): T {
        AbstractThemeLoader.Reporter.errors.clear()

        val r = body()

        if (AbstractThemeLoader.Reporter.errors.isNotEmpty()) {
            logger.warn("${AbstractThemeLoader.Reporter.errors.size} errors detected :")
            AbstractThemeLoader.Reporter.errors.forEach(logger::warn)
        }

        return r
    }
}

object XmlTests {
    @JvmStatic
    fun main(args: Array<String>) {
        val xml = XmlThemeLoader(SettingsLoader()).xml

        val iss = javaClass.classLoader.getResourceAsStream("assets/mcui/themes/hex2/widgets/label_button.xml")
            ?: error("Couldn't load iss")
        val widget = xml.decodeFromString<Widget>(
            iss.reader().readText()
        )

        println(xml.encodeToString(widget))
    }
}
