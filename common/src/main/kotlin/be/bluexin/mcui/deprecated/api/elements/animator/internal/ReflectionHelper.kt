package be.bluexin.mcui.deprecated.api.elements.animator.internal

/**
 * This is a copy from the library LibrarianLib
 * This code is covered under GNU Lesser General Public License v3.0
 */

import java.lang.reflect.Field

@Deprecated("")
object ReflectionHelper {

    @Deprecated("")
    fun findField(clazz: Class<*>, vararg fieldNames: String?): Field {
        var failed: Exception? = null
        val var3: Array<String> = fieldNames.asList().requireNoNulls().toTypedArray()
        val var4 = fieldNames.size
        var var5 = 0
        while (var5 < var4) {
            val fieldName = var3[var5]
            try {
                val f = clazz.getDeclaredField(fieldName)
                f.isAccessible = true
                return f
            } catch (var8: Exception) {
                failed = var8
                ++var5
            }
        }
        throw UnableToFindFieldException(var3, failed)
    }

    @Deprecated("")
    fun <T, E> getPrivateValue(classToAccess: Class<in E?>, instance: E?, fieldIndex: Int): T {
        return try {
            val f = classToAccess.declaredFields[fieldIndex]
            f.isAccessible = true
            f[instance] as T
        } catch (var4: Exception) {
            throw UnableToAccessFieldException(var4)
        }
    }

    class UnableToFindFieldException @Deprecated("") constructor(fieldNameList: Array<String>?, e: Exception?) :
        RuntimeException(e) {

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    class UnableToAccessFieldException : RuntimeException {

        @Deprecated("")
        constructor(fieldNames: Array<String?>?, e: Exception?) : super(e)

        constructor(e: Exception?) : super(e)

        companion object {
            private const val serialVersionUID = 1L
        }
    }
}
