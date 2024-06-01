package be.bluexin.mcui.deprecated.api.elements.animator.internal

import be.bluexin.mcui.deprecated.api.elements.animator.LerperHandler
import be.bluexin.mcui.deprecated.api.elements.animator.registerLerper

/**
 * This is a copy from the library LibrarianLib
 * This code is covered under GNU Lesser General Public License v3.0
 */

/**
 * TODO: Document file PrimitiveLerpers
 *
 * Created by TheCodeWarrior
 */
object PrimitiveLerpers {
    init {
        LerperHandler.registerLerper(Double::class.javaObjectType) { from, to, frac ->
            from + (to - from) * frac
        }
        LerperHandler.registerLerper(Double::class.javaPrimitiveType!!) { from, to, frac ->
            from + (to - from) * frac
        }

        LerperHandler.registerLerper(Float::class.javaObjectType) { from, to, frac ->
            from + (to - from) * frac
        }
        LerperHandler.registerLerper(Float::class.javaPrimitiveType!!) { from, to, frac ->
            from + (to - from) * frac
        }

        LerperHandler.registerLerper(Long::class.javaObjectType) { from, to, frac ->
            (from + (to - from) * frac).toLong()
        }
        LerperHandler.registerLerper(Long::class.javaPrimitiveType!!) { from, to, frac ->
            (from + (to - from) * frac).toLong()
        }

        LerperHandler.registerLerper(Int::class.javaObjectType) { from, to, frac ->
            (from + (to - from) * frac).toInt()
        }
        LerperHandler.registerLerper(Int::class.javaPrimitiveType!!) { from, to, frac ->
            (from + (to - from) * frac).toInt()
        }

        LerperHandler.registerLerper(Short::class.javaObjectType) { from, to, frac ->
            (from + (to - from) * frac).toInt().toShort()
        }
        LerperHandler.registerLerper(Short::class.javaPrimitiveType!!) { from, to, frac ->
            (from + (to - from) * frac).toInt().toShort()
        }

        LerperHandler.registerLerper(Byte::class.javaObjectType) { from, to, frac ->
            (from + (to - from) * frac).toInt().toByte()
        }
        LerperHandler.registerLerper(Byte::class.javaPrimitiveType!!) { from, to, frac ->
            (from + (to - from) * frac).toInt().toByte()
        }

        LerperHandler.registerLerper(Char::class.javaObjectType) { from, to, frac ->
            from + ((to - from) * frac).toInt()
        }
        LerperHandler.registerLerper(Char::class.javaPrimitiveType!!) { from, to, frac ->
            from + ((to - from) * frac).toInt()
        }

        LerperHandler.registerLerper(Boolean::class.javaObjectType) { from, to, frac ->
            if (frac >= 0.5f) to else from
        }
        LerperHandler.registerLerper(Boolean::class.javaPrimitiveType!!) { from, to, frac ->
            if (frac >= 0.5f) to else from
        }
    }
}
