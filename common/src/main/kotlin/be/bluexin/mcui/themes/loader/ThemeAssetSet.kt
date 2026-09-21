package be.bluexin.mcui.themes.loader

import be.bluexin.mcui.themes.elements.Hud

/**
 * Everything [AbstractThemeLoader.load] produced for a theme's HUD.
 *
 * The legacy tree ([hud]) is kept for A/B comparison until the modern tree ([modernHud]) matches it
 * visually ; [modernHud] is only present when the theme's format loader builds one.
 */
data class ThemeAssetSet(
    val hud: be.bluexin.mcui.themes.elements.legacy.Hud,
    val modernHud: Hud? = null,
)