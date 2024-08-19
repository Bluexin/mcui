package be.bluexin.mcui.themes.scripting

import be.bluexin.mcui.Constants
import be.bluexin.mcui.themes.meta.ThemeManager
import be.bluexin.mcui.themes.miniscript.LibHelper
import be.bluexin.mcui.themes.scripting.lib.SettingsLib
import be.bluexin.mcui.themes.scripting.lib.ThemeLib
import be.bluexin.mcui.util.debug
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.Resource
import org.koin.core.annotation.Single
import org.luaj.vm2.*
import org.luaj.vm2.compiler.LuaC
import org.luaj.vm2.lib.*
import org.luaj.vm2.lib.jse.JseBaseLib
import org.luaj.vm2.lib.jse.JseMathLib
import org.luaj.vm2.lib.jse.JseStringLib
import kotlin.jvm.optionals.getOrNull

// TODO : check BCEL for lua-to-(jvm bytecode) compilation -- see LuaJC::install
@Single // TODO : scoped ? Or it injects the theme's globals into the scope ?
class LuaJManager(
    private val themeManager: ThemeManager,
    private val libHelper: LibHelper
) {
    private val scriptInstructionsLimit = LuaValue.valueOf(50_000) // TODO: evaluate proper limit

    private val serverGlobals = Globals().apply {
        load(JseBaseLib())
        load(PackageLib())
        load(JseStringLib())
        load(JseMathLib())
        LoadState.install(this)
        LuaC.install(this)
//        LuaJC.install(this)
        LuaString.s_metatable = ReadOnlyLuaTable(LuaString.s_metatable)
    }

    private val scriptGlobals = mutableMapOf<ResourceLocation, ScriptEnvironment>()

    /**
     * Enables debug for hooks to work, but remove it from user space.
     * Returns a reference to sethook
     */
    private fun Globals.enableDebugSafely(): LuaValue {
        load(DebugLib())
        val setHook = get("debug").get("sethook")
        set("debug", LuaValue.NIL)
        return setHook
    }

    private fun getEnvFor(theme: ResourceLocation) = scriptGlobals.getOrPut(theme) {
        val globals = Globals().apply {
            load(JseBaseLib())
            load(PackageLib())
            load(Bit32Lib())
            load(TableLib())
            load(JseStringLib())
            load(JseMathLib())
            load(ThemeLib)
            load(SettingsLib)
        }
        val setHook = globals.enableDebugSafely()

        ScriptEnvironment(globals, setHook)
    }

    private val hookFunc = object : ZeroArgFunction() {
        override fun call(): LuaValue {
            throw IllegalStateException("Script overran resource limits")
        }
    }

    fun runScript(rl: ResourceLocation): Varargs? {
        return Minecraft.getInstance().resourceManager.getResource(rl)
            .map(Resource::open)
            .map { scriptStream ->
                val (userGlobals, setHook) = getEnvFor(themeManager.currentTheme.id)
                val chunk = serverGlobals.load(scriptStream, "=$rl", "t", userGlobals)
                val userThread = LuaThread(userGlobals, chunk)
                setHook(
                    LuaValue.varargsOf(
                        arrayOf(userThread, hookFunc, LuaValue.EMPTYSTRING, scriptInstructionsLimit)
                    )
                )

                val result = userThread.resume(
                    LuaValue.varargsOf(
                        arrayOf(
                            LuaValue.valueOf("${Constants.MOD_ID}-0.0.1.INDEV"),
                            LuaValue.valueOf(rl.toString()),
                        )
                    )
                )

                try {
                    // ew
                    libHelper.popContext()
                } catch (_: Throwable) {
                }

                // TODO: currently this doesn't throw, but returns a Lua Vararg with (false, <errorMessage>) in case of failure
                Constants.LOG.debug { "Read $rl : $result" }
                result
            }.getOrNull()
    }

    fun compileSnippet(key: String, snippet: String, themeId: ResourceLocation): LuaValue {
        val (userGlobals, _) = getEnvFor(themeId)
        val chunk = serverGlobals.load(snippet, "=${themeId.withPath { "$it/$key" }}", userGlobals)

        return chunk
    }

    fun runCallback(themeId: ResourceLocation, closure: LuaValue, args: Varargs) {
        require(closure is LuaClosure)
        val (userGlobals, setHook) = getEnvFor(themeId)
        val userThread = LuaThread(userGlobals, closure)
        setHook(
            LuaValue.varargsOf(
                arrayOf(userThread, hookFunc, LuaValue.EMPTYSTRING, scriptInstructionsLimit)
            )
        )
        userThread.resume(args)
    }

    private data class ScriptEnvironment(
        val globals: Globals,
        val setHook: LuaValue
    )
}

private class ReadOnlyLuaTable(table: LuaValue) : LuaTable() {
    init {
        presize(table.length(), 0)
        var n = table.next(NIL)
        while (!n.arg1().isnil()) {
            val key = n.arg1()
            val value = n.arg(2)
            super.rawset(key, if (value.istable()) ReadOnlyLuaTable(value) else value)
            n = table
                .next(n.arg1())
        }
    }

    override fun setmetatable(metatable: LuaValue): LuaValue {
        return error("table is read-only")
    }

    override fun set(key: Int, value: LuaValue) {
        error("table is read-only")
    }

    override fun rawset(key: Int, value: LuaValue) {
        error("table is read-only")
    }

    override fun rawset(key: LuaValue, value: LuaValue) {
        error("table is read-only")
    }

    override fun remove(pos: Int): LuaValue {
        return error("table is read-only")
    }
}
