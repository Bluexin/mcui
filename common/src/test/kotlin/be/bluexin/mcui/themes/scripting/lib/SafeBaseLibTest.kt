package be.bluexin.mcui.themes.scripting.lib

import be.bluexin.mcui.themes.scripting.LuaJManagerSecurityTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.luaj.vm2.Globals
import kotlin.test.*

/**
 * Tests for SafeBaseLib that verify the whitelist approach.
 * These tests only check function availability and basic setup without executing Lua code.
 * For tests that execute Lua code and verify runtime behavior, see [LuaJManagerSecurityTest].
 */
class SafeBaseLibTest {

    private fun createSafeGlobals() = Globals().apply {
        load(SafeBaseLib)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "type", "tonumber", "tostring",
            "assert", "error", "pcall", "xpcall",
            "pairs", "ipairs", "next",
            "rawget", "rawset", "rawequal", "rawlen",
            "getmetatable", "setmetatable",
            "select", "print",
            "_G", "_VERSION"
        ]
    )
    fun `safe function is available`(functionName: String) {
        val globals = createSafeGlobals()
        assertFalse(globals[functionName].isnil(), "$functionName should be available")
    }

    @ParameterizedTest
    @ValueSource(strings = ["dofile", "loadfile", "load", "collectgarbage"])
    fun `dangerous function is not available`(functionName: String) {
        val globals = createSafeGlobals()
        assertTrue(globals[functionName].isnil(), "$functionName should NOT be available")
    }

    @Test
    fun `_G points to correct globals`() {
        val globals = createSafeGlobals()
        assertSame(globals, globals["_G"].checkglobals(), "_G should reference the globals itself")
    }

    @Test
    fun `_VERSION is set`() {
        val globals = createSafeGlobals()
        val version = globals["_VERSION"].tojstring()
        assertTrue(version.contains("Lua"), "_VERSION should contain 'Lua'")
    }

    @Test
    fun `baselib is set correctly`() {
        val globals = createSafeGlobals()
        assertNotNull(globals.baselib, "globals.baselib should be set by SafeBaseLib")
    }

    @Test
    fun `functions are callable`() {
        val globals = createSafeGlobals()

        // Verify functions are actually function types, not nil
        assertTrue(globals["type"].isfunction(), "type should be a function")
        assertTrue(globals["print"].isfunction(), "print should be a function")
        assertTrue(globals["pairs"].isfunction(), "pairs should be a function")
    }

    @Test
    fun `whitelisted functions count matches expected`() {
        val globals = createSafeGlobals()

        val expectedSafeFunctions = setOf(
            "type", "tonumber", "tostring",
            "assert", "error", "pcall", "xpcall",
            "pairs", "ipairs", "next",
            "rawget", "rawset", "rawequal", "rawlen",
            "getmetatable", "setmetatable",
            "select", "print",
            "_G", "_VERSION"
        )

        // Count how many expected functions are present
        val presentCount = expectedSafeFunctions.count { !globals[it].isnil() }

        assertEquals(
            expectedSafeFunctions.size, presentCount,
            "All ${expectedSafeFunctions.size} safe functions should be available"
        )
    }
}
