package be.bluexin.mcui.api.scripting

import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction

object ThemeLib : TwoArgFunction() {
    override fun call(modname: LuaValue, env: LuaValue): LuaValue {
        val themeTable = LuaTable()
        themeTable["readFragment"] = ReadFragment
        themeTable["loadFragment"] = LoadFragment
        themeTable["readWidget"] = ReadWidget
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