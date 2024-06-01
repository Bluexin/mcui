package be.bluexin.mcui.deprecated.api.elements.animator.internal;

/**
 * This is a copy from the library LibrarianLib
 * This code is covered under GNU Lesser General Public License v3.0
 */

import java.lang.invoke.MethodHandle;

/**
 * @author WireSegal
 * Created at 7:38 PM on 10/22/16.
 */
/*package-private*/ class InvocationWrapper {
    private final MethodHandle handle;

    /*package-private*/ InvocationWrapper(MethodHandle handle) {
        this.handle = handle;
    }

    /*package-private*/ Object invoke() throws Throwable {
        return handle.invokeExact();
    }

    /*package-private*/ Object invoke(Object obj) throws Throwable {
        return handle.invokeExact(obj);
    }

    /*package-private*/ Object invoke(Object obj, Object second) throws Throwable {
        return handle.invokeExact(obj, second);
    }

    /*package-private*/ Object invokeArity(Object[] args) throws Throwable {
        return handle.invokeExact(args);
    }
}
