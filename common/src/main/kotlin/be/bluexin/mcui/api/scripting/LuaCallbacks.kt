package be.bluexin.mcui.api.scripting

import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.luajksp.annotations.LuajExposeExternal
import be.bluexin.mcui.Constants
import be.bluexin.mcui.themes.elements.*
import be.bluexin.mcui.themes.elements.access.toLua
import be.bluexin.mcui.themes.loader.JsonThemeLoader
import be.bluexin.mcui.themes.util.LibHelper
import be.bluexin.mcui.themes.util.Variables
import be.bluexin.mcui.util.AbstractLuaDecoder
import be.bluexin.mcui.util.AbstractLuaEncoder
import kotlinx.serialization.ExperimentalSerializationApi
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaValue
import java.lang.ref.WeakReference
import java.util.*

object ReadFragment : LuaFunction() {

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
    ): Fragment {
        return JsonThemeLoader.loadFragment(rl)
    }

    private fun loadFragment(
        rl: String
    ) = loadFragment(ResourceLocation(rl))
}

object ReadWidget : LuaFunction() {

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
    ): Widget {
        return JsonThemeLoader.loadWidget(rl)
    }

    private fun loadWidget(
        rl: String
    ) = loadWidget(ResourceLocation(rl))
}

object LoadFragment : LuaFunction() {

    private fun generateId() = "mcuigenerated:${UUID.randomUUID()}"

    override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
        val (target, fragment) = internalLoad(arg1, arg2)
        val id = generateId()
        val fragmentRef = FragmentReference(id = id).also {
            it.setup(target, mapOf(ResourceLocation(id) to { fragment }))
        }
        Minecraft.getInstance().tell {
            target.add(fragmentRef)
        }

        return LuaValue.TRUE
    }

    override fun call(arg1: LuaValue, arg2: LuaValue, arg3: LuaValue): LuaValue {
        val (target, fragment) = internalLoad(arg1, arg2)
        val fragmentReference = try {
            val id = generateId()
            val serializer = Variables.serializer()
            @OptIn(ExperimentalSerializationApi::class) // TODO : move this special handling to the decoder
            AbstractLuaDecoder.LuaMapDecoder(arg3.checktable(), null, serializer.descriptor.getElementDescriptor(0))
                .decodeSerializableValue(serializer).let { variables ->
                    FragmentReference(id = id, serializedVariables = variables).also {
                        it.setup(target, mapOf(ResourceLocation(id) to { fragment }))
                    }
                }
        } catch (e: Throwable) {
            Constants.LOG.error("Could not parse variables", e)
            FragmentReference()
        }

        Minecraft.getInstance().tell {
            target.add(fragmentReference)
        }

        return fragment.toLua()
    }

    private fun internalLoad(arg1: LuaValue, arg2: LuaValue): Pair<ElementGroupParent, Fragment> {
        val target = find(arg1.checkjstring().let(::ResourceLocation))
        requireNotNull(target) { "Couldn't find fragment $arg1" }
        return target to AbstractLuaDecoder.LuaDecoder(arg2.checktable())
            .decodeSerializableValue(Fragment.serializer())
    }

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

object LoadWidget : LuaFunction() {

    override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
        val (target, widget) = internalLoad(arg1, arg2)
        widget.setup(target, emptyMap())
        Minecraft.getInstance().tell {
            target += widget
        }

        return widget.toLua()
    }

    override fun call(arg1: LuaValue, arg2: LuaValue, arg3: LuaValue): LuaValue {
        val (target, widget) = internalLoad(arg1, arg2)
        try {
            val serializer = Variables.serializer()
            @OptIn(ExperimentalSerializationApi::class) // TODO : move this special handling to the decoder
            AbstractLuaDecoder.LuaMapDecoder(arg3.checktable(), null, serializer.descriptor.getElementDescriptor(0))
                .decodeSerializableValue(serializer).let { variables ->
                    variables.variable.forEach { (key, expressionIntermediate) ->
                        if (expressionIntermediate.expression.isNotEmpty()) {
                            val value = expressionIntermediate.type.expressionAdapter.compile(expressionIntermediate)
                            widget.setVariable(key, value)
                        }
                    }
                }
            LibHelper.popContext() // from Variables deser
            widget.setup(target, emptyMap())
        } catch (e: Throwable) {
            Constants.LOG.error("Could not parse variables", e)
            return LuaValue.FALSE
        }

        Minecraft.getInstance().tell {
            target += widget
        }

        return widget.toLua()
    }

    private fun internalLoad(arg1: LuaValue, arg2: LuaValue): Pair<WidgetParent, Widget> {
        val target = find(arg1.checkjstring().let(::ResourceLocation))
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

object RegisterScreen: LuaFunction() {
    override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
        val id = arg1.checkjstring().let(::ResourceLocation)
        val callback = arg2.checkfunction()

//        if (id in registry) return LuaValue.error("$id already registered")
        registry[id] = { callback.invoke(LuaValue.varargsOf(arrayOf(LuaValue.valueOf(it.toString())))) }

        return LuaValue.TRUE
    }

    private val registry = mutableMapOf<ResourceLocation, (ResourceLocation) -> Unit>()

    operator fun get(id: ResourceLocation): ((ResourceLocation) -> Unit)? = registry[id]

    fun clear() = registry.clear()
}
