package be.bluexin.mcui.themes.scripting.lib

import be.bluexin.mcui.Constants
import be.bluexin.mcui.themes.elements.*
import be.bluexin.mcui.themes.elements.access.WidgetAccess
import be.bluexin.mcui.themes.loader.XmlThemeLoader
import be.bluexin.mcui.themes.meta.ThemeDefinition
import be.bluexin.mcui.themes.miniscript.LibHelper
import be.bluexin.mcui.themes.miniscript.Variables
import be.bluexin.mcui.themes.scripting.serialization.AbstractLuaDecoder
import be.bluexin.mcui.themes.scripting.serialization.AbstractLuaEncoder
import be.bluexin.mcui.util.debug
import be.bluexin.mcui.util.trace
import kotlinx.serialization.ExperimentalSerializationApi
import net.minecraft.resources.ResourceLocation
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaValue
import java.lang.ref.WeakReference
import java.util.*

class ReadFragment(
    private val themeDefinition: ThemeDefinition
) : LuaFunction(), KoinComponent, LuaErrorHook {
    private val xmlThemeLoader: XmlThemeLoader by inject()

    override fun call(arg: LuaValue) = call(arg.checkjstring())

    override fun call(arg: String): LuaValue {
        return wrap(loadFragment(arg))
    }

    override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
        return wrap(loadFragment(ResourceLocation(arg1.checkjstring(), arg2.checkjstring())))
    }

    private fun wrap(fragment: Fragment) = AbstractLuaEncoder.LuaEncoder().apply {
        encodeSerializableValue(Fragment.serializer(), fragment)
    }.data

    private fun loadFragment(
        rl: ResourceLocation
    ): Fragment = themeDefinition.fragments[rl]
        ?.let(xmlThemeLoader::loadFragment)
        ?: luaError("Unknown fragment : $rl")

    private fun loadFragment(
        rl: String
    ) = loadFragment(ResourceLocation(rl))
}

class ReadWidget(
    private val themeDefinition: ThemeDefinition
) : LuaFunction(), KoinComponent, LuaErrorHook {
    private val xmlThemeLoader: XmlThemeLoader by inject()

    override fun call(arg: LuaValue) = call(arg.checkjstring())

    override fun call(arg: String): LuaValue {
        return wrap(loadWidget(arg))
    }

    override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
        return wrap(loadWidget(ResourceLocation(arg1.checkjstring(), arg2.checkjstring())))
    }

    private fun wrap(widget: Widget) = AbstractLuaEncoder.LuaEncoder().apply {
        encodeSerializableValue(Widget.serializer(), widget)
    }.data

    private fun loadWidget(
        rl: ResourceLocation
    ): Widget = themeDefinition.widgets[rl]
        ?.let(xmlThemeLoader::loadWidget)
        ?: luaError("Unknown widget : $rl")

    private fun loadWidget(
        rl: String
    ) = loadWidget(ResourceLocation(rl))
}

class LoadFragment(private val theme: ThemeDefinition) : LuaFunction(), KoinComponent {
    private val libHelper: LibHelper by inject()

    private fun generateId() = "mcuigenerated:${UUID.randomUUID()}"

    override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
        try {
            val (target, fragment) = internalLoad(arg1, arg2)
            val id = generateId()
            val fragmentRef = FragmentReference(id = id).also {
                it.setup(target, mapOf(ResourceLocation(id) to { fragment }), theme)
            }
            target.add(fragmentRef)

            return fragment.toLua()
        } catch (e: Throwable) {
            try {
                libHelper.popContext()
            } catch (_: Throwable) {
            }
            Constants.LOG.error("Could not parse variables", e)
            return FALSE
        }
    }

    override fun call(arg1: LuaValue, arg2: LuaValue, arg3: LuaValue): LuaValue {
        try {
            val (target, fragment) = internalLoad(arg1, arg2)
            val id = generateId()
            val serializer = Variables.serializer()

            val luaVars = arg3.checktable()
            val variables = if (luaVars.length() > 0) {
                @OptIn(ExperimentalSerializationApi::class) // TODO : move this special handling to the decoder
                AbstractLuaDecoder
                    .LuaMapDecoder(luaVars, null, serializer.descriptor.getElementDescriptor(0))
                    .decodeSerializableValue(serializer)
            } else Variables.EMPTY

            // This pops context
            val fragmentReference = FragmentReference(id = id, serializedVariables = variables).also {
                it.setup(target, mapOf(ResourceLocation(id) to { fragment }), theme)
            }

            target.add(fragmentReference)

            return fragment.toLua()
        } catch (e: Throwable) {
            try {
                libHelper.popContext()
            } catch (_: Throwable) {
            }
            Constants.LOG.error("Could not parse variables", e)
            return FALSE
        }
    }

    private fun internalLoad(arg1: LuaValue, arg2: LuaValue): Pair<ElementGroupParent, Fragment> {
        val target = find(arg1.checkjstring().let(::ResourceLocation))
        requireNotNull(target) { "Couldn't find fragment $arg1" }
        return target to AbstractLuaDecoder.LuaDecoder(arg2.checktable())
            .decodeSerializableValue(Fragment.serializer())
    }

    companion object {
        private val roots = mutableMapOf<ResourceLocation, WeakReference<ElementGroupParent>>()

        operator fun set(id: ResourceLocation, root: ElementGroupParent) {
            roots.remove(id)?.clear()
            roots[id] = WeakReference(root)
        }

        private fun find(id: ResourceLocation) = roots[id]?.let {
            val root = it.get()
            if (root == null) roots.remove(id)
            root
        }

        fun clear(id: ResourceLocation) {
            roots.remove(id)?.clear()
        }
    }
}

object LoadWidget : LuaFunction(), KoinComponent {
    private val libHelper: LibHelper by inject()

    override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
        try {
            val (target, widget) = internalLoad(arg1, arg2)
            target += widget

            return widget.toLua()
        } catch (e: Throwable) {
            try {
                libHelper.popContext()
            } catch (_: Throwable) {
            }
            Constants.LOG.error("Could not parse variables", e)
            return LuaValue.FALSE
        }
    }

    override fun call(arg1: LuaValue, arg2: LuaValue, arg3: LuaValue): LuaValue {
        try {
            val (target, widget) = internalLoad(arg1, arg2)
            val serializer = Variables.serializer()
            val luaVars = arg3.checktable()
            if (luaVars.keyCount() > 0) {
                @OptIn(ExperimentalSerializationApi::class) // TODO : move this special handling to the decoder
                AbstractLuaDecoder.LuaMapDecoder(luaVars, null, serializer.descriptor.getElementDescriptor(0))
                    .decodeSerializableValue(serializer).let { variables ->
                        variables.variable.forEach { (key, expressionIntermediate) ->
                            if (expressionIntermediate.expression.isNotEmpty()) {
                                val value =
                                    expressionIntermediate.type.expressionAdapter.compile(expressionIntermediate)
                                Constants.LOG.trace { "Deserialized $key -> `${expressionIntermediate.expression}`" }
                                widget.setVariable(key, value)
                            }
                        }
                    }
                libHelper.popContext() // from Variables deser
            }

            target += widget
            Constants.LOG.debug { "Adding ${widget.name} to ${(target as? Widget)?.hierarchyName ?: target.name}" }

            return widget.toLua()
        } catch (e: Throwable) {
            try {
                libHelper.popContext()
            } catch (_: Throwable) {
            }
            Constants.LOG.error("Could not parse variables", e)
            return LuaValue.FALSE
        }
    }

    private fun internalLoad(arg1: LuaValue, arg2: LuaValue): Pair<WidgetParent, Widget> {
        val target = when {
            arg1.isuserdata(Widget::class.java) -> (arg1 as WidgetAccess).wrapped
            arg1.isstring() -> find(arg1.checkjstring().let(::ResourceLocation))
            else -> {
                argerror(1, "Expected Widget reference or resource location, found $arg1")
                kotlin.error("Unreachable")
            }
        }
        requireNotNull(target) { "Couldn't find fragment $arg1" }

        return target to AbstractLuaDecoder.LuaDecoder(arg2.checktable())
            .decodeSerializableValue(Widget.serializer())
    }

    private val roots = mutableMapOf<ResourceLocation, WeakReference<WidgetParent>>()

    // TODO : id should be a resource location
    operator fun set(id: ResourceLocation, root: WidgetParent) {
        roots.remove(id)?.clear()
        roots[id] = WeakReference(root)
    }

    private fun find(id: ResourceLocation) = roots[id]?.let {
        val root = it.get()
        if (root == null) roots.remove(id)
        root
    }

    fun clear(id: ResourceLocation) {
        roots.remove(id)?.clear()
    }
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
