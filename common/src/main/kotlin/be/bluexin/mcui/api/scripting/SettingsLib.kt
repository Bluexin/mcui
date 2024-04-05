package be.bluexin.mcui.api.scripting

import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction

object SettingsLib : TwoArgFunction() {
    override fun call(modname: LuaValue, env: LuaValue): LuaValue {
        val settingsTable = LuaTable()
        settingsTable["readFragment"] = ReadFragment
        env["settings"] = settingsTable
        if (!env["package"].isnil()) {
            env["package"]["loaded"]["theme"] = settingsTable
        }
        // TODO : https://github.com/lunarmodules/Penlight ?

        return settingsTable
    }
}