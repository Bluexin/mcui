package be.bluexin.mcui.deprecated.screens.menus
/*

import be.bluexin.mcui.themes.AbstractThemeLoader
import be.bluexin.mcui.themes.ThemeManager
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiLabel
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.resources.I18n

class MainMenuGui : GuiMainMenu() {
    private lateinit var errorDetailsLabel: GuiLabel

    override fun initGui() {
        super.initGui()

        errorDetailsLabel = GuiLabel(
            mc.fontRenderer,
            0x02001,
            width / 2 - 150, height / 4 + 190,
            100, 34,
            16777215
        ).apply {
            visible = false
            AbstractThemeLoader.Reporter.errors.asSequence().flatMap {
                it.split('\n')
            }.forEach(::addLine)

            labelList.add(this)
        }

        buttonList.add(
            GuiButton(
                0x03001, width / 2 - 100, height / 4 + 156,
                I18n.get("saoui.menu.errors", AbstractThemeLoader.Reporter.errors.size, ThemeManager.currentTheme.name)
            )
        )
    }

    override fun actionPerformed(button: GuiButton) {
        super.actionPerformed(button)
        if (button.id == 0x03001) errorDetailsLabel.visible = !errorDetailsLabel.visible
    }
}
*/
