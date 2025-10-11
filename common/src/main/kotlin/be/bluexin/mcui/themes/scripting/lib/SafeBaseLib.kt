package be.bluexin.mcui.themes.scripting.lib

import be.bluexin.mcui.themes.scripting.lib.SafeBaseLib.SAFE_FUNCTIONS
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.BaseLib
import org.luaj.vm2.lib.TwoArgFunction

/**
 * Provides a safe subset of Lua's base library functions.
 *
 * Allowed functions: see [SAFE_FUNCTIONS]
 *
 * Excluded for security:
 * - dofile, loadfile, load: Code execution/file access
 * - collectgarbage: DoS and timing attack vector
 */
object SafeBaseLib : TwoArgFunction() {

    override fun call(modname: LuaValue, env: LuaValue): LuaValue {
        val globals = env.checkglobals()

        // Load JseBaseLib into temporary environment
        val tempEnv = Globals()
        val baseLib = BaseLib()
        baseLib.call(modname, tempEnv)

        // Copy only whitelisted functions to target environment
        for (name in SAFE_FUNCTIONS) {
            val func = tempEnv[name]
            require(!func.isnil()) { "Expected function '$name' was not found after BaseLib load" }
            globals[name] = func
        }

        // Set global environment and version
        globals["_G"] = globals
        globals["_VERSION"] = tempEnv["_VERSION"]
        rebindGlobals(baseLib, globals)


        return globals
    }

    /**
     * Replace the globals field in the BaseLib instance to prevent leaking tempEnv.
     */
    private fun rebindGlobals(baseLib: BaseLib, realGlobals: Globals) {
        realGlobals.baselib = baseLib // this is usually set by BaseLib itself
        try {
            val globalsField = BaseLib::class.java.getDeclaredField("globals")
            globalsField.isAccessible = true
            globalsField[baseLib] = realGlobals
        } catch (e: Exception) {
            throw IllegalStateException("Failed to rebind BaseLib globals field via reflection", e)
        }
    }

    /**
     * Whitelist of safe base library functions.
     * Only these functions will be available in the sandbox.
     */
    private val SAFE_FUNCTIONS = arrayOf(
        // Type operations
        "type",
        "tonumber",
        "tostring",

        // Error handling
        "assert",
        "error",
        "pcall",
        "xpcall",

        // Table iteration
        "pairs",
        "ipairs",
        "next",

        // Raw table operations
        "rawget",
        "rawset",
        "rawequal",
        "rawlen",

        // Metatable operations
        "getmetatable",
        "setmetatable",

        // Utilities
        "select",
        "print",
    )
}
