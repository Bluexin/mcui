package be.bluexin.mcui.themes.loader

import be.bluexin.mcui.config.Setting
import be.bluexin.mcui.config.Settings
import be.bluexin.mcui.themes.meta.ThemeDefinition
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.minecraft.server.packs.resources.ResourceManager
import org.koin.core.annotation.Single

@Single
class SettingsLoader {

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        useAlternativeNames = false
        allowTrailingComma = true
        allowComments = true
    }

    // If it is not lazy, Koin doesn't pick up modules anymore. Yeah idk why either.
    private val serializer by lazy {
        @Suppress("UNCHECKED_CAST") // generic type is actually not needed for serde
        ListSerializer(Setting.serializer(String.serializer())) as KSerializer<List<Setting<*>>>
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun loadSettings(resourceManager: ResourceManager, theme: ThemeDefinition) {
        val old = Settings.clear(theme.id)

        theme.settings
            ?.let(resourceManager::getResource)
            ?.map { resource ->
                resource.open()
                    .use { stream -> json.decodeFromStream(serializer, stream) }
                    .onEach {
                        it.namespace = theme.id
                        it.register()
                    }
            }

        Settings.build(theme.id, old)
    }
}
