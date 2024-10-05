package be.bluexin.mcui.themes.scripting.lib

import be.bluexin.luajksp.annotations.LKExposed
import be.bluexin.mcui.config.Settings
import be.bluexin.mcui.themes.meta.ThemeManager
import be.bluexin.mcui.themes.miniscript.ResourceLocationMapper
import be.bluexin.mcui.util.Client
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.ZeroArgFunction

object SettingsLib : TwoArgFunction() {
    override fun call(modname: LuaValue, env: LuaValue): LuaValue {
        val settingsTable = LuaTable()
        settingsTable["listAll"] = ListAll
        settingsTable["themes"] = Themes
        settingsTable["currentTheme"] = CurrentTheme
        settingsTable["setTheme"] = SetTheme
        settingsTable["allScreenIds"] = AllScreenIds
        settingsTable["getThemesImplementingScreenId"] = GetThemesImplementingScreenId
        settingsTable["getScreenConfiguration"] = GetScreenConfiguration
        settingsTable["setScreenConfiguration"] = SetScreenConfiguration
        env["settings"] = settingsTable
        if (!env["package"].isnil()) {
            env["package"]["loaded"]["settings"] = settingsTable
        }
        // TODO : https://github.com/lunarmodules/Penlight ?

        return settingsTable
    }

    private object ListAll : ZeroArgFunction() {
        override fun call(): LuaValue = tableOf(
            emptyArray(),
            Settings.findAll().map(LKExposed::toLua).toTypedArray()
        )
    }

    private object Themes : ZeroArgFunction(), KoinComponent {
        private val themeManager by inject<ThemeManager>()

        override fun call(): LuaValue = tableOf(
            emptyArray(),
            themeManager.themeList.keys.map(ResourceLocationMapper::toLua).toTypedArray()
        )
    }

    private object CurrentTheme : ZeroArgFunction(), KoinComponent {
        private val themeManager by inject<ThemeManager>()

        override fun call(): LuaValue = ResourceLocationMapper.toLua(themeManager.currentTheme.id)
    }

    private object SetTheme : OneArgFunction(), KoinComponent {
        private val themeManager by inject<ThemeManager>()

        override fun call(p0: LuaValue): LuaValue {
            val themeId = ResourceLocationMapper.fromLua(p0)

            Client.mc.tell {
                themeManager.load(Client.mc.resourceManager, themeId)
            }

            return LuaValue.NONE
        }
    }

    private object AllScreenIds : ZeroArgFunction(), KoinComponent {
        private val themeManager by inject<ThemeManager>()
        override fun call(): LuaValue = LuaValue.tableOf(
            /* namedValues = */ emptyArray(),
            /* unnamedValues = */ themeManager.allScreenIds.map(ResourceLocationMapper::toLua).toTypedArray()
        )

    }

    private object GetThemesImplementingScreenId : OneArgFunction(), KoinComponent {
        private val themeManager by inject<ThemeManager>()

        override fun call(p0: LuaValue): LuaValue {
            val screenId = ResourceLocationMapper.fromLua(p0)

            return LuaValue.tableOf(
                /* namedValues = */ emptyArray(),
                /* unnamedValues = */ themeManager.getAllScreens(screenId).keys
                    .map(ResourceLocationMapper::toLua).toTypedArray()
            )
        }
    }

    private object GetScreenConfiguration : OneArgFunction(), KoinComponent {
        private val themeManager by inject<ThemeManager>()

        override fun call(p0: LuaValue): LuaValue {
            val screenId = ResourceLocationMapper.fromLua(p0)

            return themeManager.getScreenConfiguration(screenId)
                ?.let(ResourceLocationMapper::toLua)
                ?: LuaValue.NIL
        }
    }

    private object SetScreenConfiguration : TwoArgFunction(), KoinComponent {
        private val themeManager by inject<ThemeManager>()

        override fun call(p0: LuaValue, p1: LuaValue): LuaValue {
            val screenId = ResourceLocationMapper.fromLua(p0)
            val themeId = ResourceLocationMapper.fromLua(p1)
            themeManager.setScreenConfiguration(screenId, themeId)

            return LuaValue.NONE
        }
    }
}
