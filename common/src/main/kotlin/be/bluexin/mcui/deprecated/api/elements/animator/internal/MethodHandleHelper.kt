package be.bluexin.mcui.deprecated.api.elements.animator.internal

/**
 * This is a copy from the library LibrarianLib
 * This code is covered under GNU Lesser General Public License v3.0
 */

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * @author WireSegal
 * Created at 6:49 PM on 8/14/16.
 */
object MethodHandleHelper {

    //region base

    //endregion

    //region getter

    /**
     * Provides a wrapper for an existing MethodHandle getter.
     * No casts are required to use this, although they are recommended.
     */
    @JvmStatic
    fun <T : Any> wrapperForGetter(handle: MethodHandle): (T) -> Any? {
        val wrapper = InvocationWrapper(
            handle.asType(
                MethodType.genericMethodType(1)
            )
        )
        return { wrapper(it) }
    }

    @JvmStatic
    fun <T : Any> wrapperForGetter(field: Field): (T) -> Any? = wrapperForGetter(MethodHandles.publicLookup().unreflectGetter(field))

    /**
     * Provides a wrapper for an existing static MethodHandle getter.
     * No casts are required to use this, although they are recommended.
     */
    @JvmStatic
    fun wrapperForStaticGetter(handle: MethodHandle): () -> Any? {
        val wrapper = InvocationWrapper(handle.asType(MethodType.genericMethodType(0)))
        return { wrapper() }
    }

    @JvmStatic
    fun wrapperForStaticGetter(field: Field): () -> Any? = wrapperForStaticGetter(MethodHandles.publicLookup().unreflectGetter(field))

    //endregion

    //region setter

    /**
     * Provides a wrapper for an existing MethodHandle setter.
     */
    @JvmStatic
    fun <T : Any> wrapperForSetter(handle: MethodHandle): (T, Any?) -> Unit {
        val wrapper = InvocationWrapper(handle.asType(MethodType.genericMethodType(2)))
        return { obj, value -> wrapper(obj, value) }
    }

    @JvmStatic
    fun <T : Any> wrapperForSetter(field: Field): (T, Any?) -> Unit = wrapperForSetter(MethodHandles.publicLookup().unreflectSetter(field))

    /**
     * Provides a wrapper for an existing static MethodHandle setter.
     */
    @JvmStatic
    fun wrapperForStaticSetter(handle: MethodHandle): (Any?) -> Unit {
        val wrapper = InvocationWrapper(handle.asType(MethodType.genericMethodType(1)))
        return { wrapper(it) }
    }

    @JvmStatic
    fun wrapperForStaticSetter(field: Field): (Any?) -> Unit = wrapperForStaticSetter(MethodHandles.publicLookup().unreflectSetter(field))

    //endregion

    //region methods

    /**
     * Provides a wrapper for an existing MethodHandle method wrapper.
     */
    @JvmStatic
    fun <T : Any> wrapperForMethod(handle: MethodHandle): (T, Array<Any?>) -> Any? {
        val type = handle.type()
        val count = type.parameterCount()
        var remapped = handle.asType(MethodType.genericMethodType(count))

        if (count > 1) {
            remapped = remapped.asSpreader(Array<Any>::class.java, count)
        }

        val wrapper = InvocationWrapper(remapped)
        if (count == 1) {
            return { obj, _ -> wrapper(obj) }
        }

        return { obj, args -> wrapper.invokeArity(arrayOf(obj, *args)) }
    }

    @JvmStatic
    fun <T : Any> wrapperForMethod(method: Method): (T, Array<Any?>) -> Any? = wrapperForMethod(
        MethodHandles.publicLookup().unreflect(method)
    )

    /**
     * Provides a wrapper for an existing MethodHandle method wrapper.
     */
    @JvmStatic
    fun wrapperForStaticMethod(handle: MethodHandle): (Array<Any?>) -> Any? {
        val type = handle.type()
        val count = type.parameterCount()
        val wrapper = InvocationWrapper(handle.asType(MethodType.genericMethodType(count)).asSpreader(Array<Any>::class.java, count))
        return { wrapper.invokeArity(it) }
    }

    @JvmStatic
    fun wrapperForStaticMethod(method: Method): (Array<Any?>) -> Any? = wrapperForStaticMethod(
        MethodHandles.publicLookup().unreflect(method)
    )

    //endregion

    //region constructors

    //endregion

    //region delegates

    //endregion
}

//region extensions

//endregion
