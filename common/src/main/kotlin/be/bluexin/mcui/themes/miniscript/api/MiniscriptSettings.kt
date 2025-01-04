package be.bluexin.mcui.themes.miniscript.api

import be.bluexin.mcui.Constants
import be.bluexin.mcui.config.Settings
import be.bluexin.mcui.themes.meta.ThemeAnalyzer
import be.bluexin.mcui.themes.meta.ThemeManager
import net.minecraft.resources.ResourceLocation
import org.koin.core.annotation.Single

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

@Single
internal class MiniscriptSettingsImpl(
    private val themeManager: ThemeManager
) : MiniscriptSettings {

    private fun setting(key: String): Any? =
        Settings[themeManager.getScreenConfiguration(ThemeAnalyzer.HUD)!!, ResourceLocation(key)]

    override fun string(key: String) = (setting(key) as? String).orEmpty()
    override fun boolean(key: String) = setting(key) as? Boolean ?: false
    override fun int(key: String) = setting(key) as? Int ?: 0
    override fun double(key: String) = setting(key) as? Double ?: .0
    override fun resourceLocation(key: String) =
        setting(key) as? ResourceLocation ?: ResourceLocation(Constants.MOD_ID, "missing")
}