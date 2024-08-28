package be.bluexin.mcui.util

import org.slf4j.Logger

inline fun Logger.trace(message: () -> String) {
    if (isTraceEnabled) trace(message())
}

inline fun Logger.trace(error: Throwable, message: () -> String) {
    if (isTraceEnabled) trace(message(), error)
}

inline fun Logger.debug(message: () -> String) {
    if (isDebugEnabled) debug(message())
}

inline fun Logger.debug(error: Throwable, message: () -> String) {
    if (isDebugEnabled) debug(message(), error)
}

inline fun Logger.warn(message: () -> String) {
    if (isWarnEnabled) warn(message())
}

inline fun Logger.warn(error: Throwable, message: () -> String) {
    if (isWarnEnabled) warn(message(), error)
}

inline fun Logger.error(message: () -> String) {
    if (isErrorEnabled) error(message())
}

inline fun Logger.error(error: Throwable, message: () -> String) {
    if (isErrorEnabled) error(message(), error)
}
