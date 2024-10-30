package be.bluexin.mcui.themes.scripting.lib

import be.bluexin.mcui.themes.meta.ThemeDefinition
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction

class ThemeLib(private val theme: ThemeDefinition) : TwoArgFunction() {
    override fun call(modname: LuaValue, env: LuaValue): LuaValue {
        val themeTable = LuaTable()
        themeTable["readFragment"] = ReadFragment(theme)
        themeTable["loadFragment"] = LoadFragment(theme)
        themeTable["readWidget"] = ReadWidget(theme)
        themeTable["loadWidget"] = LoadWidget
        themeTable["registerScreen"] = RegisterScreen
        env["theme"] = themeTable
        if (!env["package"].isnil()) {
            env["package"]["loaded"]["theme"] = themeTable
        }
        // TODO : https://github.com/lunarmodules/Penlight ?

        return themeTable
    }
}