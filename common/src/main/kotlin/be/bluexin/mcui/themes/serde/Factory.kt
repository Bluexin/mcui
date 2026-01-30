package be.bluexin.mcui.themes.serde

import be.bluexin.mcui.themes.elements.Fragment
import net.minecraft.resources.ResourceLocation

internal interface Factory<in IN : Any, out OUT : Any> {
    fun create(input: IN, context: Context): Result<OUT>

    interface Context {
        fun nested(path: String)
        fun pop()
        fun error(message: String)
        fun loadFragment(id: ResourceLocation): Fragment?

        companion object {
            inline fun <T> Context.tryRun(path: String, block: () -> T): Result<T> = runCatching {
                nested(path)
                try {
                    block()
                } finally {
                    pop()
                }
            }

            inline fun <T> Context.run(path: String, block: () -> T): T = tryRun(path, block).getOrThrow()
        }
    }
}
