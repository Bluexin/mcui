package be.bluexin.mcui.themes.scripting

import be.bluexin.mcui.Constants
import be.bluexin.mcui.logger
import be.bluexin.mcui.themes.meta.ThemeDefinition
import be.bluexin.mcui.themes.miniscript.LibHelper
import be.bluexin.mcui.themes.scripting.lib.SafetyLib
import be.bluexin.mcui.themes.scripting.lib.SettingsLib
import be.bluexin.mcui.themes.scripting.lib.ThemeLib
import be.bluexin.mcui.util.Client
import be.bluexin.mcui.util.debug
import be.bluexin.mcui.util.info
import be.bluexin.mcui.util.warn
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
// State of LuaJC :
// - UpValue in nested prototype does not get properly captured (see themeId in addThemeSettings onClick)
// - JavaLoader needs to use the current class loader as parent to load closures in modded context
@Single // TODO : scoped ? Or it injects the theme's globals into the scope ?
class LuaJManager(
    private val libHelper: LibHelper
) {
    private val scriptInstructionsLimit = LuaValue.valueOf(50_000) // TODO: evaluate proper limit

    private val logger = logger()

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

    private fun getEnvFor(theme: ThemeDefinition) = scriptGlobals.getOrPut(theme.id) {
        val globals = Globals().apply {
            // TODO : verify & lock down file access
            // dofile, load, loadfile,
            load(JseBaseLib())
            // TODO : verify this can't be used to access bad files
            load(PackageLib())
            load(Bit32Lib())
            load(TableLib())
            load(JseStringLib())
            load(JseMathLib())
            load(ThemeLib(theme))
            load(SettingsLib)
            load(SafetyLib(theme, this@LuaJManager))
        }
        val setHook = globals.enableDebugSafely()

        ScriptEnvironment(globals, setHook)
    }

    private val hookFunc = object : ZeroArgFunction() {
        override fun call(): LuaValue {
            throw IllegalStateException("Script overran resource limits")
        }
    }

    fun runMainScript(rl: ResourceLocation, theme: ThemeDefinition): Varargs? {
        return Minecraft.getInstance().resourceManager.getResource(rl)
            .map(Resource::open)
            .map { scriptStream ->
                val (userGlobals, setHook) = getEnvFor(theme)
                val chunk = serverGlobals.load(scriptStream, "@$rl", "t", userGlobals)
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

                if (result.arg1().isboolean() && result.arg1().checkboolean()) {
                    logger.debug { "Read $rl : $result" }
                } else {
                    logger.warn { "Read $rl : $result" }
                }

                // TODO: currently this doesn't throw, but returns a Lua Vararg with (false, <errorMessage>) in case of failure
                result
            }.getOrNull()
    }

    fun load(rl: ResourceLocation, theme: ThemeDefinition): LuaValue? = Client.resourceManager
        .getResource(rl)
        .map(Resource::open)
        .map { scriptStream ->
            val (userGlobals) = getEnvFor(theme)
            serverGlobals.load(scriptStream, "@$rl", "t", userGlobals)
        }
        .filter(LuaValue::isfunction)
        .getOrNull()

    fun compileSnippet(key: String, snippet: String, theme: ThemeDefinition): LuaValue {
        val (userGlobals, _) = getEnvFor(theme)
        val chunk = serverGlobals.load(snippet, "@${theme.id.withPath { "$it/$key" }}", userGlobals)

        return chunk
    }

    fun runCallback(theme: ThemeDefinition, closure: LuaValue, args: Varargs) {
        require(closure is LuaClosure)
        val (userGlobals, setHook) = getEnvFor(theme)
        val userThread = LuaThread(userGlobals, closure)
        setHook(
            LuaValue.varargsOf(
                arrayOf(userThread, hookFunc, LuaValue.EMPTYSTRING, scriptInstructionsLimit)
            )
        )
        userThread.resume(args)
    }

    fun clearGlobals(theme: ThemeDefinition) {
        logger.info { "Clearing globals for ${theme.id}" }
        scriptGlobals.remove(theme.id)
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
