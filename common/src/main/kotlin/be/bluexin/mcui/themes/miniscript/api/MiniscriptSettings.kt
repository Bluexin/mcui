package be.bluexin.mcui.themes.miniscript.api

import be.bluexin.mcui.config.Settings
import be.bluexin.mcui.themes.meta.ThemeAnalyzer
import be.bluexin.mcui.themes.meta.ThemeManager
import net.minecraft.resources.ResourceLocation
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Suppress("unused", "MemberVisibilityCanBePrivate") // Exposed to JEL
interface MiniscriptSettings {
    /**
     * @return a string setting value
     */
    fun string(key: String): String

    /**
     * @return a boolean setting value
     */
    fun boolean(key: String): Boolean

    /**
     * @return an integer setting value
     */
    fun int(key: String): Int

    /**
     * @return a decimal number setting value
     */
    fun double(key: String): Double

    /**
     * @return a resource location setting value
     */
    fun resourceLocation(key: String): ResourceLocation
}

internal object MiniscriptSettingsImpl : MiniscriptSettings, KoinComponent {
    private val themeManager by inject<ThemeManager>()

    private fun setting(key: String): Any? =
        Settings[themeManager.getScreenConfiguration(ThemeAnalyzer.HUD)!!, ResourceLocation(key)]

    override fun string(key: String) = setting(key) as String
    override fun boolean(key: String) = setting(key) as Boolean
    override fun int(key: String) = setting(key) as Int
    override fun double(key: String) = setting(key) as Double
    override fun resourceLocation(key: String) = setting(key) as ResourceLocation
}