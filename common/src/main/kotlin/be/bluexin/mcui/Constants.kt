package be.bluexin.mcui

import be.bluexin.mcui.Constants.MOD_ID
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Constants {
    const val MOD_ID = "mcui"
    const val MOD_VERSION = "indev" // TODO
    @JvmField
    val LOG: Logger = LoggerFactory.getLogger(MOD_ID)
}

fun Any.logger(): Logger = LoggerFactory.getLogger("$MOD_ID/${javaClass.simpleName}")
