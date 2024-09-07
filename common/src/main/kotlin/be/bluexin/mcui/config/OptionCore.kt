/*
 * Copyright (C) 2016-2024 Arnaud 'Bluexin' Solé
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package be.bluexin.mcui.config

import be.bluexin.mcui.deprecated.api.info.IOption
import net.minecraft.client.resources.language.I18n
import net.minecraft.resources.ResourceLocation

enum class OptionCore(
    /**
     * @return Returns the Option name in String format
     */
    private val unformattedName: String,
    private val defaultValue: Boolean,
    override val category: OptionCore?,
    /**
     * @return Returns true if this is a Category or not
     */
    val isCategory: Boolean = category == null,
    override val isRestricted: Boolean = false
) : IOption {

    // Main Categories
    UI("optCatUI", defaultValue = false, category = null),
    THEME("optTheme", defaultValue = false, category = null),
    ENTITIES("optEntities", defaultValue = false, category = null),
    HEALTH_OPTIONS("optCatHealth", defaultValue = false, category = null),
    HOTBAR_OPTIONS("optCatHotBar", defaultValue = false, category = null),
    EFFECTS("optCatEffects", defaultValue = false, category = null),
    MISC("optCatMisc", defaultValue = false, category = null),
    DEBUG("optCatDebug", defaultValue = false, category = null),

    // General UI Settings
    UI_ONLY("optionUIOnly", defaultValue = false, category = UI),
    DEFAULT_INVENTORY("optionDefaultInv", defaultValue = true, category = UI),
    DEFAULT_DEATH_SCREEN("optionDefaultDeath", defaultValue = false, category = UI),
    DEFAULT_DEBUG("optionDefaultDebug", defaultValue = false, category = UI),
    ALWAYS_SHOW("optionAlwaysShow", defaultValue = true, category = UI),
    LOGOUT("optionLogout", defaultValue = true, category = UI),
    GUI_PAUSE("optionGuiPause", defaultValue = false, category = UI),
    UI_MOVEMENT("optionUIMovement", defaultValue = true, category = UI),

    // Themes
    VANILLA_UI("optionDefaultUI", defaultValue = false, category = THEME),

    // Entity Options
    ENTITY_HEALTH_BARS("optionEntityHealthBars", defaultValue = false, isCategory = true, category = ENTITIES),
    ENTITY_CRYSTALS("optionEntityCrystals", defaultValue = false, isCategory = true, category = ENTITIES),

    // Health Options
    SMOOTH_HEALTH("optionSmoothHealth", defaultValue = true, category = HEALTH_OPTIONS),
    REMOVE_HPXP("optionLightHud", defaultValue = false, category = HEALTH_OPTIONS),
    ALT_ABSORB_POS("optionAltAbsorbPos", defaultValue = false, category = HEALTH_OPTIONS),
    ENEMY_ONSCREEN_HEALTH("optionEnemyOnscreenHealth", defaultValue = true, category = HEALTH_OPTIONS),
    HIDE_OFFLINE_PARTY("optionHideOfflineParty", defaultValue = false, category = HEALTH_OPTIONS),

    // Health Bars
    INNOCENT_HEALTH("optionInnocentHealthBars", defaultValue = true, category = ENTITY_HEALTH_BARS),
    VIOLENT_HEALTH("optionViolentHealthBars", defaultValue = true, category = ENTITY_HEALTH_BARS),
    KILLER_HEALTH("optionKillerHealthBars", defaultValue = true, category = ENTITY_HEALTH_BARS),
    BOSS_HEALTH("optionBossHealthBars", defaultValue = true, category = ENTITY_HEALTH_BARS),
    CREATIVE_HEALTH("optionCreativeHealthBars", defaultValue = true, category = ENTITY_HEALTH_BARS),
    OP_HEALTH("optionOPHealthBars", defaultValue = true, category = ENTITY_HEALTH_BARS),
    INVALID_HEALTH("optionInvalidHealthBars", defaultValue = true, category = ENTITY_HEALTH_BARS),
    DEV_HEALTH("optionDevHealthBars", defaultValue = true, category = ENTITY_HEALTH_BARS),

    // CRYSTALS
    INNOCENT_CRYSTAL("optionInnocentCrystal", defaultValue = true, category = ENTITY_CRYSTALS),
    VIOLENT_CRYSTAL("optionViolentCrystal", defaultValue = true, category = ENTITY_CRYSTALS),
    KILLER_CRYSTAL("optionKillerCrystal", defaultValue = true, category = ENTITY_CRYSTALS),
    BOSS_CRYSTAL("optionBossCrystal", defaultValue = true, category = ENTITY_CRYSTALS),
    CREATIVE_CRYSTAL("optionCreativeCrystal", defaultValue = true, category = ENTITY_CRYSTALS),
    OP_CRYSTAL("optionOPCrystal", defaultValue = true, category = ENTITY_CRYSTALS),
    INVALID_CRYSTAL("optionInvalidCrystal", defaultValue = true, category = ENTITY_CRYSTALS),
    DEV_CRYSTAL("optionDevCrystal", defaultValue = true, category = ENTITY_CRYSTALS),

    // Hotbar
    DEFAULT_HOTBAR("optionDefaultHotbar", defaultValue = false, category = HOTBAR_OPTIONS, isRestricted = true),
    HOR_HOTBAR("optionHorHotbar", defaultValue = false, category = HOTBAR_OPTIONS, isRestricted = true),
    VER_HOTBAR("optionVerHotbar", defaultValue = true, category = HOTBAR_OPTIONS, isRestricted = true),

    // Effects
    SPINNING_CRYSTALS("optionSpinning", defaultValue = true, category = EFFECTS),
    PARTICLES("optionParticles", defaultValue = true, category = EFFECTS),
    SOUND_EFFECTS("optionSounds", defaultValue = true, category = EFFECTS),
    MOUSE_OVER_EFFECT("optionMouseOver", defaultValue = true, category = EFFECTS),

    // Misc
    AGGRO_SYSTEM("optionAggro", defaultValue = true, category = MISC),
    MOUNT_STAT_VIEW("optionMountStatView", defaultValue = true, category = MISC),
    CUSTOM_FONT("optionCustomFont", defaultValue = false, category = MISC),
    TEXT_SHADOW("optionTextShadow", defaultValue = true, category = MISC),

    // Debug
    RENDER_SYSTEM("optionRenderSystem", defaultValue = false, isCategory = true, category = DEBUG),

    // Render System
    RENDER_CROSSHAIRS("optionRenderCrosshairs", defaultValue = true, category = RENDER_SYSTEM),
    RENDER_ARMOR("optionRenderArmor", defaultValue = true, category = RENDER_SYSTEM),
    RENDER_HOTBAR("optionRenderHotbar", defaultValue = true, category = RENDER_SYSTEM),
    RENDER_AIR("optionRenderAir", defaultValue = true, category = RENDER_SYSTEM),
    RENDER_POTION_ICONS("optionRenderPotionIcons", defaultValue = true, category = RENDER_SYSTEM),
    RENDER_HEALTH("optionRenderHealth", defaultValue = true, category = RENDER_SYSTEM),
    RENDER_FOOD("optionRenderFood", defaultValue = true, category = RENDER_SYSTEM),
    RENDER_EXPERIENCE("optionRenderExp", defaultValue = true, category = RENDER_SYSTEM),
    RENDER_JUMPBAR("optionRenderJumpbar", defaultValue = true, category = RENDER_SYSTEM),
    RENDER_HEALTHMOUNT("optionRenderHealthMount", defaultValue = true, category = RENDER_SYSTEM);

    override fun toString() = name

    @Deprecated("Should not be used anymore")
    val displayName: String by lazy { I18n.get(unformattedName) }

    @Deprecated("Should not be used anymore")
    val description: List<String> by lazy { listOf(I18n.get("$unformattedName.desc")) }

    private val setting = if (isCategory) null else BooleanSetting(
        key = ResourceLocation(category?.name?.lowercase() ?: "general", name.lowercase()),
        defaultValue = defaultValue,
        commentKey = "$unformattedName.desc"
    ).also { it.namespace = Settings.NS_BUILTIN }

    private var value: Boolean = defaultValue
        get() = setting?.let(Settings::get) ?: field
        set(value) = setting?.let { Settings[it] = value } ?: run { field = value }

    /**
     * This will flip the enabled state of the option and return the new value.
     * For category restrictions, this will always enable the option.
     */
    fun flip() {
        if (this.isRestricted) {
            entries.filter { it.category == this.category }.forEach {
                it.value = false
            }
            this.value = true
        } else {
            val newValue = !this.value
            this.value = newValue
//            if (this == CUSTOM_FONT) GLCore.setFont(Minecraft.getMinecraft(), newValue)
        }
//        MinecraftForge.EVENT_BUS.post(OptionTriggerEvent(this))
    }

    override val isEnabled: Boolean get() = value

    /**
     * @return Returns the Category
     */
    fun getCategoryName(): String {
        return this.category?.name ?: "Options"
    }

    /**
     * This will disable the Option when called
     */
    fun disable() {
        if (this.value) this.flip()
    }

    /**
     * This will enable the Option when called
     */
    fun enable() {
        if (!this.value) this.flip()
    }

    val subOptions
        get() = entries.filter { it.category == this }

    operator fun invoke() = isEnabled

    object Initializer {
        fun registerSettings() = entries.forEach {
            it.setting?.register()
        }
    }

    companion object {

        fun fromString(str: String): OptionCore {
            return valueOf(str)
        }

        /**
         * Easy downcaster to use with JEL.

         * @param o the option to downcast
         * *
         * @return downcasted option
         */
        operator fun get(o: OptionCore): IOption {
            return o
        }

        /**
         * Easy getter to use with JEL.

         * @param o the option to get
         * *
         * @return whether the option is enabled
         */
        @JvmStatic
        fun isEnabled(o: OptionCore): Boolean {
            return o.isEnabled
        }

        val tlOptions
            get() = entries.filter { it.category == null }
    }
}
