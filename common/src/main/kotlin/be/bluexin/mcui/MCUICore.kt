package be.bluexin.mcui

import be.bluexin.mcui.config.ConfigHandler
import be.bluexin.mcui.config.OptionCore
import be.bluexin.mcui.config.Settings
import be.bluexin.mcui.platform.Services
import be.bluexin.mcui.themes.loader.ThemeLoaderModule
import be.bluexin.mcui.themes.meta.ThemeMetaModule
import be.bluexin.mcui.themes.miniscript.MiniscriptModule
import be.bluexin.mcui.themes.scripting.ScriptingModule
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import org.koin.logger.slf4jLogger

object MCUICore {
    private val logger = logger()

    fun init() {
        logger.info(
            "Hello from Kotlin Common init on {}! we are currently in a {} environment!",
            Services.PLATFORM.platformName,
            Services.PLATFORM.environmentName
        )

        val koin = startKoin {
            slf4jLogger()
            modules(
                ThemeMetaModule().module,
                ScriptingModule().module,
                ThemeLoaderModule().module,
                MiniscriptModule().module,
            )
        }

        Settings.initialize()
        OptionCore.Initializer.registerSettings()
        ConfigHandler.registerSettings()
        Settings.build(Settings.NS_BUILTIN, null)
    }
}