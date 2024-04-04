package be.bluexin.mcui.themes.settings

import be.bluexin.mcui.config.Setting
import be.bluexin.mcui.themes.meta.ThemeMetadata
import be.bluexin.mcui.themes.util.json.JsonResourceLocationTypeAdapter
import be.bluexin.mcui.themes.util.json.JsonSettingAdapterFactory
import be.bluexin.mcui.util.append
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import kotlin.jvm.optionals.getOrNull

object SettingsLoader {
    private val rlType = object : TypeToken<ResourceLocation>() {}.type
    private val listType = object : TypeToken<List<Setting<*>>>() {}.type

    fun loadSettings(resourceManager: ResourceManager, theme: ThemeMetadata): List<Setting<*>>? {
        return resourceManager.getResource(theme.themeRoot.append("/settings.json"))
            .map<List<Setting<*>>> { resource ->
                try {
                    JsonSettingAdapterFactory.currentNamespace.set(theme.id)
                    resource.open().use {
                        GsonBuilder()
                            .registerTypeAdapter(rlType, JsonResourceLocationTypeAdapter())
                            .create()
                            .fromJson(it.reader(), listType)
                    }
                } finally {
                    JsonSettingAdapterFactory.currentNamespace.remove()
                }
            }.getOrNull()
    }
}
