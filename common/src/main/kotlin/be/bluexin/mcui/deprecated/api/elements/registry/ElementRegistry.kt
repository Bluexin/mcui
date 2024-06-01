package be.bluexin.mcui.deprecated.api.elements.registry

import be.bluexin.mcui.config.OptionCore
import be.bluexin.mcui.deprecated.api.elements.*
import be.bluexin.mcui.deprecated.api.items.IItemFilter
import be.bluexin.mcui.deprecated.api.items.ItemFilterRegister
import be.bluexin.mcui.deprecated.api.screens.IIcon
import be.bluexin.mcui.deprecated.screens.menus.IngameMenu
import be.bluexin.mcui.deprecated.screens.util.PopupYesNo
import be.bluexin.mcui.deprecated.screens.util.itemList
import be.bluexin.mcui.util.Client
import be.bluexin.mcui.util.IconCore
import be.bluexin.mcui.util.math.vec
import net.minecraft.client.gui.screens.OptionsScreen
import net.minecraft.client.gui.screens.PauseScreen
import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener

object ElementRegistry : ResourceManagerReloadListener {

    /**
     * This is a fixed list of elements for the menu
     * that will be pulled everytime the menu is opened
     */
    val registeredElements = hashMapOf<Type, List<NeoElement>>()

    // TODO: this supports async stuff now
    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        initRegistry()
    }

    fun initRegistry() {
        registeredElements.clear()
//        val event = MenuBuildingEvent(JNLua.loadIngameMenu())
//        MinecraftForge.EVENT_BUS.post(event)
//        registeredElements[Type.INGAMEMENU] = event.elements
    }

    fun getDefaultElements(): MutableList<NeoElement> {
        var index = 0
        return arrayListOf(
            tlCategory(IconCore.PROFILE, index++) {
                ItemFilterRegister.tlFilters.forEach { baseFilter ->
                    addItemCategories(baseFilter)
                }
                category(IconCore.SKILLS, I18n.get("sao.element.skills")) {
                    setWip()
                    category(IconCore.SKILLS, "Test 1") {
                        category(IconCore.SKILLS, "1.1") {
                            for (i in 1..3) category(IconCore.SKILLS, "1.1.$i")
                        }
                        category(IconCore.SKILLS, "1.2") {
                            for (i in 1..3) category(IconCore.SKILLS, "1.2.$i")
                        }
                        category(IconCore.SKILLS, "1.3") {
                            for (i in 1..3) category(IconCore.SKILLS, "1.3.$i")
                        }
                    }
                    category(IconCore.SKILLS, "解散") {
                        onClick { _, _ ->
                            highlighted = true
                            controllingGUI?.openGui(
                                PopupYesNo(
                                    "Disolve",
                                    "パーチイを解散しますか？",
                                    ""
                                )
                            )?.plusAssign {
                                Client.mc.player?.sendSystemMessage(Component.literal("Result: $it"))
                                highlighted = false
                            }
                            true
                        }
                    }
                    category(IconCore.SKILLS, "3") {
                        category(IconCore.SKILLS, "3.1") {
                            for (i in 1..6) category(IconCore.SKILLS, "3.1.$i")
                        }
                        category(IconCore.SKILLS, "3.2") {
                            for (i in 1..7) category(IconCore.SKILLS, "3.2.$i")
                        }
                        category(IconCore.SKILLS, "3.3") {
                            for (i in 1..10) category(IconCore.SKILLS, "3.3.$i")
                        }
                    }
                }
                crafting()
                Client.mc.player?.let(::profile)
            },
            tlCategory(IconCore.SOCIAL, index++) {
                category(IconCore.GUILD, I18n.get("sao.element.guild")) {
                    setWip()
                    delegate.disabled = true //!SAOCore.isSAOMCLibServerSide
                }
                partyMenu()
                friendMenu()
            },
            tlCategory(IconCore.MESSAGE, index++) {
                setWip()
            },
            tlCategory(IconCore.NAVIGATION, index++) {
                setWip()
                category(IconCore.QUEST, I18n.get("sao.element.quest")) {
                    /*AdvancementUtil.getCategories().forEach {
                        advancementCategory(it)
                    }*/
                    +IconLabelElement(IconCore.CANCEL, "Not yet implemented").apply {
                        disabled = true
                    }
//                    Client.mc.player.connection.advancements
                }
                recipes()
            },
            tlCategory(IconCore.SETTINGS, index) {
                category(IconCore.OPTION, I18n.get("sao.element.options")) {
                    category(IconCore.OPTION, I18n.get("guiOptions")) {
                        onClick { _, _ ->
                            Client.mc.setScreen(OptionsScreen(controllingGUI!!, Client.mc.options))
                            true
                        }
                    }
                    OptionCore.tlOptions.forEach {
                        +optionCategory(it)
                    }
                }
                category(IconCore.HELP, I18n.get("sao.element.menu")) {
                    onClick { _, _ ->
                        Client.mc.setScreen(PauseScreen(true))
                        true
                    }
                }
                category(IconCore.LOGOUT, if (OptionCore.LOGOUT()) I18n.get("sao.element.logout") else "") {
                    onClick { _, _ ->
                        if (OptionCore.LOGOUT()) {
                            (controllingGUI as? IngameMenu)?.loggingOut = true
                            true
                        } else false
                    }
                }
            }
        )
    }

    fun CategoryButton.addItemCategories(filter: IItemFilter) {
        if (filter.isCategory) {
            category(filter.icon, filter.displayName) {
                filter.subFilters.forEach { subFilter -> addItemCategories(subFilter) }
            }
        } else {
            +object : CategoryButton(IconLabelElement(filter.icon, filter.displayName), this, null) {
                override fun show() {
                    super.show()
                    this.elements.clear()
                    itemList(Client.mc.player!!.inventoryMenu, filter)
                }

                override fun hide() {
                    super.hide()
                    elements.clear()
                }
            }
        }
    }

    fun tlCategory(icon: IIcon, index: Int, body: (CategoryButton.() -> Unit)? = null): CategoryButton {
        return CategoryButton(IconElement(icon, vec(0, 25 * index)), null, body)
    }

    fun CategoryButton.setWip() {
        disabled = true
        addDescription(I18n.get("saoui.wip"))
    }

    enum class Type {
        MAINMENU,
        INGAMEMENU;
    }
}

