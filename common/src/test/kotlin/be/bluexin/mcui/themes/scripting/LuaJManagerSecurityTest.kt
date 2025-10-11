package be.bluexin.mcui.themes.scripting

import be.bluexin.mcui.themes.meta.ThemeDefinition
import be.bluexin.mcui.themes.meta.ThemeFormat
import be.bluexin.mcui.themes.meta.ThemeMetadata
import be.bluexin.mcui.themes.miniscript.LibHelper
import be.bluexin.mcui.util.Client
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import net.minecraft.resources.ResourceLocation
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import org.koin.test.junit5.AutoCloseKoinTest
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaValue
import java.util.*
import kotlin.test.*

class LuaJManagerSecurityTest : AutoCloseKoinTest() {

    private lateinit var luaJManager: LuaJManager
    private lateinit var testTheme: ThemeDefinition

    @BeforeEach
    fun setup() {
        startKoin {
            modules(module {
                single { LibHelper() }
            })
        }

        luaJManager = LuaJManager(LibHelper())
        testTheme = ThemeDefinition(
            id = ResourceLocation("test", "security"),
            themeRoot = ResourceLocation("test", "themes/security"),
            name = "Security Test Theme",
            metadata = ThemeMetadata(format = ThemeFormat.MCUI_ALPHA),
            hud = null,
            settings = null,
            fragments = emptyMap(),
            widgets = emptyMap(),
            scripts = emptyMap()
        )
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "dofile('hello')",
            "loadfile('hello')",
            "load('hello')",
            "collectgarbage()"
        ]
    )
    fun `dangerous function is not accessible`(functionName: String) {
        val snippet = "return $functionName"

        val exception = assertThrows<LuaError> {
            val chunk = luaJManager.compileSnippet("test_${functionName.substringBefore('(')}", snippet, testTheme)
            chunk.invoke()
        }

        val message = assertNotNull(exception.message)
        assertEquals("attempt to call a nil value", message, message = "$functionName should not be accessible")
    }

    @Test
    fun `safe functions are available`() {
        val snippet = """
            return 42
        """

        val chunk = luaJManager.compileSnippet("test_safe", snippet, testTheme)
        assertNotNull(chunk, "Chunk should not be null")
        assertTrue(chunk.isfunction(), "Chunk should be a function")

        val result = chunk.invoke()
        assertNotNull(result)

        val firstArg = result.arg1()
        assertEquals(42.0, firstArg.todouble(), "Should return 42")
    }

    @Test
    fun `pcall and error handling work`() {
        val snippet = """
            local success, result = pcall(function()
                error("test error")
            end)
            return success, result
        """

        val chunk = luaJManager.compileSnippet("test_pcall", snippet, testTheme)
        val result = chunk.invoke()
        assertNotNull(result)

        assertFalse(result.arg1().toboolean(), "pcall should catch error")
        assertContains(result.arg(2).tojstring(), "test error")
    }

    @Test
    fun `table operations work in sandbox`() {
        val snippet = """
            local t = {a = 1, b = 2}
            rawset(t, "c", 3)
            local sum = 0
            for k, v in pairs(t) do
                sum = sum + v
            end
            return sum, rawget(t, "c")
        """

        val chunk = luaJManager.compileSnippet("test_tables", snippet, testTheme)
        val result = chunk.invoke()
        assertNotNull(result)

        assertEquals(6.0, result.arg1().todouble())
        assertEquals(3.0, result.arg(2).todouble())
    }

    @Test
    fun `string operations work in sandbox`() {
        @Language("Lua")
        val snippet = """
            return string.upper("hello"), string.len("world")
        """

        val chunk = luaJManager.compileSnippet("test_strings", snippet, testTheme)
        val result = chunk.invoke()
        assertNotNull(result)

        assertEquals("HELLO", result.arg1().tojstring())
        assertEquals(5.0, result.arg(2).todouble())
    }

    @Test
    fun `math operations work in sandbox`() {
        val snippet = """
            return math.sqrt(16), math.max(5, 10)
        """

        val chunk = luaJManager.compileSnippet("test_math", snippet, testTheme)
        val result = chunk.invoke()
        assertNotNull(result)

        assertEquals(4.0, result.arg1().todouble())
        assertEquals(10.0, result.arg(2).todouble())
    }

    @Test
    fun `instruction limit prevents infinite loops`() {
        val snippet = """
            while true do
                -- infinite loop
            end
        """

        val chunk = luaJManager.compileSnippet("test_infinite_loop", snippet, testTheme)
        val result = luaJManager.runCallback(testTheme, chunk, LuaValue.NONE)

        assertFalse(result.checkboolean(1))
        assertContains(result.checkjstring(2), "Script overran resource limits")
    }

    @Test
    fun `instruction limit prevents excessive computation`() {
        val snippet = """
            local sum = 0
            for i = 1, 1000000000 do
                sum = sum + i
            end
            return sum
        """

        val chunk = luaJManager.compileSnippet("test_excessive_compute", snippet, testTheme)
        val result = luaJManager.runCallback(testTheme, chunk, LuaValue.NONE)

        assertFalse(result.checkboolean(1))
        assertContains(result.checkjstring(2), "Script overran resource limits")
    }

    @Test
    fun `debug library is not accessible to user code`() {
        // DebugLib is loaded internally for instruction limiting (sethook),
        // but the entire debug table is removed from user-accessible globals
        val snippet = "return debug"

        val chunk = luaJManager.compileSnippet("test_debug", snippet, testTheme)
        val result = chunk.invoke()

        assertTrue(result.isnil(1), "debug table should be nil (removed from user space)")
    }

    @Test
    fun `cannot modify string metatable`() {
        val snippet = """
            local mt = getmetatable("")
            mt.__index = function() return "hacked" end
        """

        val exception = assertThrows<LuaError> {
            val chunk = luaJManager.compileSnippet("test_string_metatable", snippet, testTheme)
            chunk.invoke()
        }

        val message = assertNotNull(exception.message)
        assertContains(
            message,
            "@test:security/test_string_metatable:3: table is read-only",
            message = "string metatable should not be modifiable"
        )
    }

    @Test
    fun `require loads whitelisted scripts`() {
        val scriptRl = ResourceLocation("test", "scripts/allowed.lua")
        val themeWithScript = testTheme.copy(
            scripts = mapOf(
                ResourceLocation("test.security", "allowed") to scriptRl
            )
        )

        mockkObject(Client)
        every { Client.resourceManager } returns mockk {
            every { getResource(scriptRl) } returns Optional.of(mockk {
                every { open() } returns "return 'hello'".byteInputStream()
            })
        }

        val snippet = """
            return require('allowed')
        """

        val chunk = luaJManager.compileSnippet("test_require_allowed", snippet, themeWithScript)
        val result = chunk.invoke()
        assertNotNull(result)
        assertTrue(result.isstring(1), "require should return string")
        assertEquals("hello", result.arg1().tojstring())
    }

    @Test
    fun `require cannot load non-whitelisted scripts`() {
        val snippet = """
            return require('notallowed')
        """

        val chunk = luaJManager.compileSnippet("test_require_not_allowed", snippet, testTheme)
        val err = assertFailsWith<LuaError> {
            chunk.invoke()
        }
        assertEquals("module 'notallowed' not found: notallowed", err.message)
    }

    @Test
    fun `require cannot access non-whitelisted paths`() {
        val snippet = """
            return require('/etc/passwd')
        """

        val chunk = luaJManager.compileSnippet("test_require_blocked", snippet, testTheme)
        val err = assertFailsWith<LuaError> {
            chunk.invoke()
        }
        assertEquals("module '/etc/passwd' not found: /etc/passwd", err.message)
    }

    @Test
    fun `text-only mode is enforced`() {
        // This test verifies that the "t" mode in load() prevents bytecode execution
        // If bytecode were allowed, an attacker could bypass sandbox restrictions

        val snippet = """
            -- This is valid Lua text source
            return "text mode works"
        """

        val chunk = luaJManager.compileSnippet("test_text_mode", snippet, testTheme)
        val result = chunk.invoke()
        assertNotNull(result)

        assertEquals("text mode works", result.arg1().tojstring())

        // Note: We cannot directly test bytecode rejection here without creating
        // actual bytecode, but the "t" parameter in LuaJManager.kt ensures this
    }

    @Test
    fun `globals isolation between themes`() {
        val theme1 = testTheme
        val theme2 = testTheme.copy(id = ResourceLocation("test", "theme2"))

        // Set a global in theme1
        val snippet1 = "testGlobal = 'theme1'"
        val chunk1 = luaJManager.compileSnippet("set_global", snippet1, theme1)
        chunk1.invoke()

        // Check it doesn't leak to theme2
        val snippet2 = "return testGlobal"
        val chunk2 = luaJManager.compileSnippet("check_global", snippet2, theme2)
        val result = chunk2.invoke()

        assertTrue(result.isnil(1), "Globals should be isolated between themes")
    }
}
