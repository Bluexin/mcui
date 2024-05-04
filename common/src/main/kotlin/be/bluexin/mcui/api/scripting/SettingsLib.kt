package be.bluexin.mcui.api.scripting

import be.bluexin.luajksp.annotations.LKExposed
import be.bluexin.mcui.config.Settings
import be.bluexin.mcui.themes.meta.ThemeManager
import be.bluexin.mcui.themes.util.LKResourceLocation
import be.bluexin.mcui.themes.util.ResourceLocationMapper
import be.bluexin.mcui.util.Client
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
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

    private object Themes : ZeroArgFunction() {
        override fun call(): LuaValue = tableOf(
            emptyArray(),
            ThemeManager.themeList.keys.map(ResourceLocationMapper::toLua).toTypedArray()
        )
    }

    private object CurrentTheme : ZeroArgFunction() {
        override fun call(): LuaValue = ResourceLocationMapper.toLua(ThemeManager.currentTheme.id)
    }

    private object SetTheme : OneArgFunction() {
        override fun call(p0: LuaValue): LuaValue {
            val themeId = ResourceLocationMapper.fromLua(p0)

            Client.mc.tell {
                ThemeManager.load(Client.mc.resourceManager, themeId)
            }

            return LuaValue.NONE
        }
    }
}