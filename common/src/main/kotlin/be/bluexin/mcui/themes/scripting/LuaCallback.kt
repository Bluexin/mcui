package be.bluexin.mcui.themes.scripting

import be.bluexin.mcui.themes.meta.ThemeManager
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.resources.ResourceLocation
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.jse.CoerceJavaToLua

@Serializable(LuaCallback.Serializer::class)
open class LuaCallback(
    private val luaClosure: LuaValue,
    private val themeId: ResourceLocation,
    private val script: String
) : KoinComponent {
    private val luaJManager: LuaJManager by inject()

    operator fun invoke(
        vararg args: Any?
    ) {
        if (this !is NoOpCallback) {
            luaJManager.runCallback(
                themeId, luaClosure,
                LuaValue.varargsOf(args.map { CoerceJavaToLua.coerce(it) }.toTypedArray())
            )
        }
    }

    class Serializer : KSerializer<LuaCallback>, KoinComponent {
        private val delegate = String.serializer()
        private val themeManager: ThemeManager by inject()
        private val luaJManager: LuaJManager by inject()

        override fun deserialize(decoder: Decoder): LuaCallback {
            val script = delegate.deserialize(decoder)
            val themeId = themeManager.currentTheme.id

            return if (script.isBlank()) NoOpCallback(themeId) else {
                val closure = luaJManager.compileSnippet("todo", script, themeId)
                LuaCallback(closure, themeId, script)
            }
        }

        override val descriptor: SerialDescriptor by lazy {
            PrimitiveSerialDescriptor(LuaCallback::class.java.canonicalName, PrimitiveKind.STRING)
        }

        override fun serialize(encoder: Encoder, value: LuaCallback) = delegate.serialize(encoder, value.script)
    }

    private class NoOpCallback(themeId: ResourceLocation) : LuaCallback(
        luaClosure = object : LuaFunction() {
            override fun invoke(args: Varargs) = LuaValue.NIL
        },
        themeId = themeId,
        script = "",
    )
}
