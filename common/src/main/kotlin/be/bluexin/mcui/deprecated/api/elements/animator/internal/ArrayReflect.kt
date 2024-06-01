package be.bluexin.mcui.deprecated.api.elements.animator.internal

/**
 * This is a copy from the library LibrarianLib
 * This code is covered under GNU Lesser General Public License v3.0
 */

/**
 * Created by TheCodeWarrior
 */
object ArrayReflect {
    /*
     * Legal conversions:
     * From \ To | boolean | byte | char | short | int | long | float | double
     * ----------|-------------------------------------------------------------
     * boolean   |    #
     * byte      |    #       #      #       #      #     #
     * char      |    #       #      #       #      #
     * short     |    #       #      #       #      #
     * int       |    #       #      #       #
     * long      |    #       #      #
     * float     |    #       #
     * double    |    #
     */
    private fun badArray(array: Any?): RuntimeException {
        return if (array == null) NullPointerException("Array argument is null") else if (!array.javaClass.isArray) IllegalArgumentException(
            "Argument is not an array"
        ) else IllegalArgumentException("Array is of incompatible type")
    }

    operator fun get(array: Any?, index: Int): Any? {
        if (array is Array<*>) return (array as Array<Any?>)[index]
        if (array is BooleanArray) return array[index]
        if (array is ByteArray) return array[index]
        if (array is CharArray) return array[index]
        if (array is ShortArray) return array[index]
        if (array is IntArray) return array[index]
        if (array is LongArray) return array[index]
        if (array is FloatArray) return array[index]
        if (array is DoubleArray) return array[index]
        throw badArray(array)
    }

    fun setBoolean(array: Any?, index: Int, z: Boolean) {
        if (array is BooleanArray) array[index] = z else throw badArray(array)
    }

    fun setByte(array: Any?, index: Int, b: Byte) {
        when (array) {
            is ByteArray -> array[index] = b
            is ShortArray -> array[index] =
                b.toShort()
            is IntArray -> array[index] = b.toInt()
            is LongArray -> array[index] =
                b.toLong()
            is FloatArray -> array[index] =
                b.toFloat()
            is DoubleArray -> array[index] =
                b.toDouble()
            else -> throw badArray(array)
        }
    }

    fun setChar(array: Any?, index: Int, c: Char) {
        when (array) {
            is CharArray -> array[index] = c
            is IntArray -> array[index] =
                c.toInt()
            is LongArray -> array[index] =
                c.toLong()
            is FloatArray -> array[index] =
                c.toFloat()
            is DoubleArray -> array[index] =
                c.toDouble()
            else -> throw badArray(array)
        }
    }

    fun setShort(array: Any?, index: Int, s: Short) {
        when (array) {
            is ShortArray -> array[index] = s
            is IntArray -> array[index] =
                s.toInt()
            is LongArray -> array[index] =
                s.toLong()
            is FloatArray -> array[index] =
                s.toFloat()
            is DoubleArray -> array[index] =
                s.toDouble()
            else -> throw badArray(array)
        }
    }

    fun setInt(array: Any?, index: Int, i: Int) {
        when (array) {
            is IntArray -> array[index] = i
            is LongArray -> array[index] =
                i.toLong()
            is FloatArray -> array[index] =
                i.toFloat()
            is DoubleArray -> array[index] =
                i.toDouble()
            else -> throw badArray(array)
        }
    }

    fun setLong(array: Any?, index: Int, l: Long) {
        when (array) {
            is LongArray -> array[index] = l
            is FloatArray -> array[index] =
                l.toFloat()
            is DoubleArray -> array[index] =
                l.toDouble()
            else -> throw badArray(array)
        }
    }

    fun setFloat(array: Any?, index: Int, f: Float) {
        when (array) {
            is FloatArray -> array[index] = f
            is DoubleArray -> array[index] =
                f.toDouble()
            else -> throw badArray(array)
        }
    }

    fun setDouble(array: Any?, index: Int, d: Double) {
        if (array is DoubleArray) array[index] = d else throw badArray(array)
    }

    operator fun set(array: Any?, index: Int, value: Any?) {
        if (array is Array<*>) {
            try {
                (array as Array<Any?>)[index] = value
            } catch (e: ArrayStoreException) {
                throw badArray(array)
            }
        } else if (value is Boolean) setBoolean(array, index, value) else if (value is Byte) setByte(
            array,
            index,
            value
        ) else if (value is Short) setShort(array, index, value) else if (value is Char) setChar(
            array,
            index,
            value
        ) else if (value is Int) setInt(array, index, value) else if (value is Long) setLong(
            array,
            index,
            value
        ) else if (value is Float) setFloat(array, index, value) else if (value is Double) setDouble(
            array,
            index,
            value
        ) else throw badArray(array)
    }
}
