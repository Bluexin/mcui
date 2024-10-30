package be.bluexin.mcui.themes.scripting.lib

import org.luaj.vm2.LuaValue

interface LuaErrorHook {

    fun luaError(message: String): Nothing {
        LuaValue.error(message)
        error("Never hit as the above throws")
    }
}