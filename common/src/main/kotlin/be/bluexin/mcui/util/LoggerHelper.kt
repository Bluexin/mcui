package be.bluexin.mcui.util

import org.slf4j.Logger

inline fun Logger.trace(message: () -> String) {
    if (isTraceEnabled) trace(message())
}

inline fun Logger.debug(message: () -> String) {
    if (isDebugEnabled) debug(message())
}
