package be.bluexin.mcui.api.scripting

import be.bluexin.luajksp.annotations.LKExposed
import be.bluexin.mcui.config.Settings
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.ZeroArgFunction

object SettingsLib : TwoArgFunction() {
    override fun call(modname: LuaValue, env: LuaValue): LuaValue {
        val settingsTable = LuaTable()
        settingsTable["listAll"] = ListAll
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
}