package be.bluexin.mcui.themes.scripting.lib

import be.bluexin.mcui.themes.meta.ThemeDefinition
import be.bluexin.mcui.themes.scripting.LuaJManager
import org.koin.core.component.KoinComponent
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction

class SafetyLib(
    private val theme: ThemeDefinition,
    private val luajManager: LuaJManager,
) : TwoArgFunction(), KoinComponent {

    private lateinit var globals: Globals

    override fun call(modname: LuaValue, env: LuaValue): LuaValue {
        globals = env.checkglobals()

        val packageLib = env["package"].checktable()
        packageLib["searchers"] = LuaValue.listOf(arrayOf(ScriptSearcher()))

        return NIL
    }

    inner class ScriptSearcher : OneArgFunction() {
        override fun call(arg: LuaValue): LuaValue = theme
            .scripts[theme.themeResource(arg.checkjstring())]
            ?.let { luajManager.load(it, theme) }
            ?: NIL
    }

}