package be.bluexin.mcui.themes.meta

import be.bluexin.mcui.Constants
import be.bluexin.mcui.themes.scripting.LuaJManager
import be.bluexin.mcui.themes.scripting.lib.RegisterScreen
import net.minecraft.resources.ResourceLocation
import org.koin.core.annotation.Single

@Single
class ThemeAnalyzer(
    private val luaJManager: LuaJManager
) {

    /**
     * Runs a theme's (optional) main Lua script and collects the screens it registered.
     * The HUD itself is not a screen : it is loaded separately by [ThemeManager].
     */
    fun analyzeThemeScreens(
        theme: ThemeDefinition,
        successReport: (() -> String) -> Unit,
        failureReport: (() -> String) -> Unit,
    ): Map<ResourceLocation, (ResourceLocation) -> Unit> = buildMap {
        // TODO : replace with using exported globals ?
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