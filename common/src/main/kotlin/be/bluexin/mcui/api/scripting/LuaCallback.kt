package be.bluexin.mcui.api.scripting

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
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.jse.CoerceJavaToLua

@Serializable(LuaCallback.Serializer::class)
class LuaCallback(
    private val luaClosure: LuaValue,
    private val themeId: ResourceLocation,
    private val script: String
) {

    operator fun invoke(
        vararg args: Any?
    ) {
        if (this !== NO_OP) {
            LuaJTest.runCallback(
                themeId, luaClosure,
                LuaValue.varargsOf(args.map { CoerceJavaToLua.coerce(it) }.toTypedArray())
            )
        }
    }

    class Serializer : KSerializer<LuaCallback> {
        private val delegate = String.serializer()

        override fun deserialize(decoder: Decoder): LuaCallback {
            val script = delegate.deserialize(decoder)

            return if (script.isBlank()) NO_OP else {
                val closure = LuaJTest.compileSnippet("todo", script, ThemeManager.currentTheme.id)
                LuaCallback(
                    closure, ThemeManager.currentTheme.id, script
                )
            }
        }

        override val descriptor: SerialDescriptor by lazy {
            PrimitiveSerialDescriptor(LuaCallback::class.java.canonicalName, PrimitiveKind.STRING)
        }

        override fun serialize(encoder: Encoder, value: LuaCallback) = delegate.serialize(encoder, value.script)
    }

    companion object {
        val NO_OP = LuaCallback(
            luaClosure = object : LuaFunction() {
                override fun invoke(args: Varargs) = LuaValue.NIL
            },
            themeId = ThemeManager.currentTheme.id,
            script = "",
        )
    }
}
