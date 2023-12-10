package be.bluexin.mcui.config

import be.bluexin.mcui.Constants
import be.bluexin.mcui.platform.Services
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager

object Settings {
    val NS_BUILTIN = ResourceLocation(Constants.MOD_ID, "builtin")

    private val logger = LogManager.getLogger("${Constants.MOD_ID}.${javaClass.simpleName}")

    private val configurations: MutableMap<ResourceLocation, Configuration> = mutableMapOf()
    private val registry: MutableMap<Pair<ResourceLocation, ResourceLocation>, Setting<Any>> = mutableMapOf()

    private val updates: Channel<ResourceLocation> = Channel(capacity = Channel.BUFFERED) {
        logger.warn("Undelivered $it")
    }

    operator fun get(namespace: ResourceLocation, key: ResourceLocation): Any? =
        getSetting(namespace, key, "get")
            ?.let(this::get)

    operator fun <T : Any> get(setting: Setting<T>): T =
        setting.read(setting.property.string)
            ?.takeIf(setting::validate)
            ?: setting.defaultValue

    operator fun set(namespace: ResourceLocation, key: ResourceLocation, value: String) {
        val setting = getSetting(namespace, key, "set") ?: return

        setting.read(value)?.takeIf(setting::validate)?.let {
            set(setting, it)
        }
    }

    operator fun <T : Any> set(setting: Setting<T>, value: T) {
        value.takeIf(setting::validate)?.let {
            setting.property.set(setting.write(value))
            notifyUpdate(setting.namespace)
        }
    }

    fun isValid(namespace: ResourceLocation, key: ResourceLocation, value: String): Boolean {
        val (_, _, _, _, read, _, validate) = registry[namespace to key] ?: run {
            logger.warn("Trying to check validity for unregistered setting : $key in $namespace")
            return false
        }

        return read(value)?.takeIf(validate) != null
    }

    fun register(setting: Setting<*>): Property {
        val config = registerNamespace(setting.namespace)
        logger.info("Registering Setting ${setting.namespace}/${setting.key}")
        @Suppress("UNCHECKED_CAST")
        registry[setting.namespace to setting.key] = setting as Setting<Any>
        return config.register(setting.key, setting.write(setting.defaultValue), setting.comment, setting.type).also {
            notifyUpdate(setting.namespace)
        }
    }

    fun build(namespace: ResourceLocation) {
        configurations[namespace]?.build()
    }

    private fun notifyUpdate(namespace: ResourceLocation) {
        updates.trySend(namespace).onFailure {
            logger.warn("Failed to send config update for $namespace !")
        }
    }

    private fun registerNamespace(namespace: ResourceLocation): Configuration {
        return configurations.getOrPut(namespace) {
            logger.info("Registering namespace $namespace")
            Services.PLATFORM.config(namespace)
        }
    }

    /**
     * This sets up the listener for [updates], which will save the config file when an update is received for
     * a different config file (namespace) **or** a timeout has been reached
     */
    fun initialize() {
        CoroutineScope(Dispatchers.IO).launch {
            logger.info("[${currentCoroutineContext()}] Starting listener")
            flow<ResourceLocation> {
                logger.info("[${currentCoroutineContext()}] Starting updates filter")
                // emit a value when the new one is different from the previous one or a timeout has been reached
                var lastValue: ResourceLocation? = null
                while (true) select {
                    updates.onReceive { newValue -> // New value received
                        when {
                            lastValue == null -> lastValue = newValue
                            lastValue != newValue -> {
                                lastValue?.let { emit(it) }
                                lastValue = newValue
                            }
                        }
                    }
                    @OptIn(ExperimentalCoroutinesApi::class) // for onTimeout
                    onTimeout(5_000) { // Timeout reached
                        if (lastValue != null) {
                            lastValue?.let { emit(it) }
                            lastValue = null
                        }
                    }
                }
            }.flowOn(Dispatchers.Default).collect {
                configurations[it]?.save()
            }
            logger.warn("[${currentCoroutineContext()}] Stopping listener (this should never happen !)")
        }
    }

    private fun getConfig(namespace: ResourceLocation, operation: String): Configuration? =
        configurations[namespace] ?: run {
            logger.warn("Trying to $operation value from unregistered config : $namespace")
            null
        }

    private fun getConfigAndSetting(
        namespace: ResourceLocation, key: ResourceLocation, operation: String
    ): Pair<Configuration, Setting<Any>>? {
        val config = getConfig(namespace, operation) ?: return null

        val setting = registry[namespace to key] ?: run {
            logger.warn("Trying to $operation value from unregistered setting : $key in $namespace")
            return null
        }

        return config to setting
    }

    private fun getSetting(
        namespace: ResourceLocation, key: ResourceLocation, operation: String
    ): Setting<Any>? = registry[namespace to key] ?: run {
        logger.warn("Trying to $operation value from unregistered setting : $key in $namespace")
        null
    }

    fun unregister(oldTheme: ResourceLocation) {
        logger.info("Unregistering $oldTheme")
        configurations -= oldTheme
    }

    @Suppress("unused", "MemberVisibilityCanBePrivate") // Exposed to JEL
    object JelWrappers {
        /**
         * Easy getters to use with JEL.
         *
         * @param key the key of the setting to get
         * @return the current setting value
         */
        fun setting(key: String): Any? = Settings[ConfigHandler.currentTheme, ResourceLocation(key)]

        fun stringSetting(key: String): String = setting(key) as String

        fun booleanSetting(key: String): Boolean = setting(key) as Boolean

        fun intSetting(key: String): Int = setting(key) as Int

        fun doubleSetting(key: String): Double = setting(key) as Double

        fun resourceLocationSetting(key: String): ResourceLocation = setting(key) as ResourceLocation
    }
}
