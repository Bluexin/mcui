package be.bluexin.mcui.themes.scripting.lib

import be.bluexin.mcui.themes.meta.ThemeDefinition
import net.minecraft.resources.ResourceLocation
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaValue

/**
 * The legacy `Widget`/`Fragment` interaction tree (and the `LuaScriptedScreen` built on it) has been
 * removed pending its replacement by a new widget system. These four functions used to build/read
 * that tree at a theme script's request ; they now fail loudly instead of silently doing nothing, so
 * any theme still calling them gets a clear, intentional error rather than a crash or a "nil value".
 */
private const val WIDGET_SYSTEM_UNAVAILABLE =
    "Widget/Fragment scripting is not available : the legacy widget system was removed, pending its replacement"

class ReadFragment(
    @Suppress("unused") private val themeDefinition: ThemeDefinition
) : LuaFunction(), LuaErrorHook {
    override fun call(arg: LuaValue): LuaValue = luaError(WIDGET_SYSTEM_UNAVAILABLE)
    override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue = luaError(WIDGET_SYSTEM_UNAVAILABLE)
}

class ReadWidget(
    @Suppress("unused") private val themeDefinition: ThemeDefinition
) : LuaFunction(), LuaErrorHook {
    override fun call(arg: LuaValue): LuaValue = luaError(WIDGET_SYSTEM_UNAVAILABLE)
    override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue = luaError(WIDGET_SYSTEM_UNAVAILABLE)
}

class LoadFragment(
    @Suppress("unused") private val theme: ThemeDefinition
) : LuaFunction(), LuaErrorHook {
    override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue = luaError(WIDGET_SYSTEM_UNAVAILABLE)
    override fun call(arg1: LuaValue, arg2: LuaValue, arg3: LuaValue): LuaValue = luaError(WIDGET_SYSTEM_UNAVAILABLE)
}

object LoadWidget : LuaFunction(), LuaErrorHook {
    override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue = luaError(WIDGET_SYSTEM_UNAVAILABLE)
    override fun call(arg1: LuaValue, arg2: LuaValue, arg3: LuaValue): LuaValue = luaError(WIDGET_SYSTEM_UNAVAILABLE)
}

object RegisterScreen : LuaFunction() {
    override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
        val id = arg1.checkjstring().let(::ResourceLocation)
        val callback = arg2.checkfunction()

//        if (id in registry) return LuaValue.error("$id already registered")
        registry[id] = { callback.invoke(LuaValue.varargsOf(arrayOf(LuaValue.valueOf(it.toString())))) }

        return LuaValue.TRUE
    }

    private val registry = mutableMapOf<ResourceLocation, (ResourceLocation) -> Unit>()

    operator fun get(id: ResourceLocation): ((ResourceLocation) -> Unit)? = registry[id]
    fun allIds(): Set<ResourceLocation> = registry.keys

    fun getAll(): Map<ResourceLocation, (ResourceLocation) -> Unit> = registry.toMap()

    fun clear() = registry.clear()
}
