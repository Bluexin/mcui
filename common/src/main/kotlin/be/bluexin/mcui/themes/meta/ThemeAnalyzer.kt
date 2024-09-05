package be.bluexin.mcui.themes.meta

import be.bluexin.mcui.Constants
import be.bluexin.mcui.themes.elements.Hud
import be.bluexin.mcui.themes.scripting.LuaJManager
import be.bluexin.mcui.themes.scripting.lib.RegisterScreen
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import org.koin.core.annotation.Single

@Single
class ThemeAnalyzer(
    private val luaJManager: LuaJManager
) {

    fun analyzeThemeScreens(
        resourceManager: ResourceManager,
        theme: ThemeDefinition,
        /**
         * Temporary (tm)
         */
        setHud: (Hud) -> Unit,
        successReport: (() -> String) -> Unit,
        failureReport: (() -> String) -> Unit,
    ): Map<ResourceLocation, (ResourceLocation) -> Unit> = buildMap {
        if (theme.hud != null) this[HUD] = {
            theme.hud
                .let(HudFormat::fromFile)?.loader
                ?.load(resourceManager, theme, setHud)
        }

        RegisterScreen.clear()
        luaJManager.clearGlobals(theme)
        theme.scripts[theme.themeResource("theme")]
            ?.let { luaJManager.runMainScript(it, theme) }
            ?.let { retVal ->
                val result = retVal.arg1().checkboolean()
                val resultValue = retVal.arg(2)

                if (!result) {
                    failureReport { "Failed to run scripts for theme ${theme.id} : $resultValue" }
                } else {
                    successReport { "Successfully run scripts for ${theme.id}, result: $resultValue" }
                }
            }

        putAll(RegisterScreen.getAll())
        RegisterScreen.clear()
    }

    companion object {
        val HUD = ResourceLocation(Constants.MOD_ID, "hud")
        val MCUI_SETTINGS = ResourceLocation(Constants.MOD_ID, "settings")
    }
}