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

package be.bluexin.mcui.deprecated.api.elements

import be.bluexin.mcui.config.OptionCore
import be.bluexin.mcui.deprecated.api.screens.IIcon
import be.bluexin.mcui.deprecated.screens.CoreGUI
import be.bluexin.mcui.deprecated.screens.MouseButton
import be.bluexin.mcui.deprecated.screens.menus.IngameMenu
import be.bluexin.mcui.deprecated.screens.unaryPlus
import be.bluexin.mcui.deprecated.screens.util.PopupAdvancement
import be.bluexin.mcui.themes.meta.ThemeDefinition
import be.bluexin.mcui.themes.meta.ThemeManager
import be.bluexin.mcui.util.Client
import be.bluexin.mcui.util.IconCore
import be.bluexin.mcui.util.delegate
import be.bluexin.mcui.util.math.Vec2d
import be.bluexin.mcui.util.math.vec
import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.advancements.Advancement
import net.minecraft.client.resources.language.I18n
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import org.koin.core.component.get
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min

/**
 * Part of saoui by Bluexin, released under GNU GPLv3.
 *
 * @author Bluexin
 */
open class CategoryButton(
    val delegate: IconElement,
    parent: INeoParent? = null,
    private val init: (CategoryButton.() -> Unit)? = null
) : IconElement(delegate.icon, delegate.pos) {

    override var pos by delegate::pos.delegate
    override var destination by delegate::destination.delegate
    override val boundingBox by delegate::boundingBox.delegate
    override var idealBoundingBox by delegate::idealBoundingBox.delegate
    override val elements by delegate::elements.delegate
    override val childrenXOffset by delegate::childrenXOffset.delegate
    override val childrenYOffset by delegate::childrenYOffset.delegate
    override val childrenXSeparator by delegate::childrenXSeparator.delegate
    override val childrenYSeparator by delegate::childrenYSeparator.delegate
    override val listed by delegate::listed.delegate
    override var visible by delegate::visible.delegate
    override var highlighted by delegate::highlighted.delegate
    override var selected by delegate::selected.delegate
    override var disabled by delegate::disabled.delegate
    override var opacity by delegate::opacity.delegate
    override var scale by delegate::scale.delegate
    private var openAnim: WeakReference<IndexedScheduledCounter>? = null

    init {
        this.parent = parent
        delegate.parent = this

        delegate.onClick { _, _ ->
            if (elements.isNotEmpty() && !selected && !disabled) {
                open()
//                SoundCore.MENU_POPUP.play()
            } else if (selected) {
                close()
//                SoundCore.DIALOG_CLOSE.play()
            }

            true
        }

        delegate.onClickOut { _, _ ->
            if (selected) close()
        }

        delegate.init()

        init?.invoke(this)
    }

    override fun onClick(body: (Vec2d, MouseButton) -> Boolean) = delegate.onClick(body)

    override fun onClickOut(body: (Vec2d, MouseButton) -> Unit) = delegate.onClickOut(body)
    override fun hide() = delegate.hide()
    override fun show() = delegate.show()
    override fun drawBackground(poseStack: PoseStack, mouse: Vec2d, partialTicks: Float) = delegate.drawBackground(
        poseStack,
        mouse,
        partialTicks
    )

    override fun draw(poseStack: PoseStack, mouse: Vec2d, partialTicks: Float) = delegate.draw(
        poseStack, mouse,
        partialTicks
    )

    override fun drawForeground(poseStack: PoseStack, mouse: Vec2d, partialTicks: Float) = delegate.drawForeground(
        poseStack, mouse,
        partialTicks
    )

    override fun contains(pos: Vec2d) = delegate.contains(pos)
    override fun update() {
        super.update()
        delegate.update()
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return elementsSequence.firstOrNull { it.isOpen && it.selected }?.keyReleased(keyCode, scanCode, modifiers)
            ?: let {
                when {
                    Client.mc.options.keyUp.matches(keyCode, -1)
                            || keyCode == InputConstants.KEY_UP -> {

                        val selected = elementsSequence.firstOrNull { it.selected }
                        var index = elements.indexOf(selected)
                        if (index == -1) index = 0
                        else if (--index < 0) index = elements.size.minus(1)
                        selected?.selected = false
                        elements[index].selected = true
                        true
                    }

                    Client.mc.options.keyDown.matches(keyCode, -1)
                            || keyCode == InputConstants.KEY_DOWN -> {

                        val selected = elementsSequence.firstOrNull { it.selected }
                        var index = elements.indexOf(selected)
                        if (++index >= elements.size) index = 0
                        selected?.selected = false
                        elements[index].selected = true
                        true
                    }

                    Client.mc.options.keyRight.matches(keyCode, -1)
                            || keyCode == InputConstants.KEY_RIGHT
                            || Client.mc.options.keyJump.matches(keyCode, -1)
                            || keyCode == InputConstants.KEY_RETURN -> {

                        val selected = elementsSequence.firstOrNull { it.selected }
                        if (selected is CategoryButton) {
                            selected.open()
                            true
                        } else selected is IconElement && selected.onClickBody(selected.pos, MouseButton.LEFT)
                    }

                    Client.mc.options.keyLeft.matches(keyCode, -1)
                            || keyCode == InputConstants.KEY_LEFT
                            || Client.mc.options.keyAttack.matches(keyCode, -1) -> {

                        close(false)
                        true
                    }

                    else -> false
                }
            }
    }

    fun open(reInit: Boolean = false) {
        highlighted = true
        selected = true
        isOpen = true

        if (reInit) elementsSequence.forEach(NeoElement::show)
        else {
            val children = childrenOrderedForAppearing().toList()
            val anim = IndexedScheduledCounter(3f, maxIdx = children.count() - 1) {
                children.elementAt(it).show()
                if (it == children.count() - 1) elementsSequence.forEach(NeoElement::show)
            }
            +anim
            openAnim = WeakReference(anim)

            if (controllingParent is CoreGUI<*>) tlParent.move(Vec2d.ZERO)
            else tlParent.move(vec(-boundingBox.width(), 0))
        }
    }

    fun close(reInit: Boolean = false) {
        delegate.scroll = -3
        elementsSequence.forEach {
            it.hide()
            if (it is CategoryButton && it.highlighted) {
                it.close()
            }
        }
        highlighted = false
        selected = false
        isOpen = false
        if (!reInit && tlParent != parent) {
            tlParent.move(vec(boundingBox.width(), 0))
        }
        openAnim?.get()?.terminated = true
        openAnim = null
    }

    override operator fun plusAssign(element: NeoElement) {
        delegate += element
        element.hide()
    }

    override fun mouseClicked(pos: Vec2d, mouseButton: MouseButton): Boolean =
        if ((mouseButton == MouseButton.SCROLL_DOWN || mouseButton == MouseButton.SCROLL_UP) && openAnim?.get()?.finished == false) true
        else delegate.mouseClicked(pos, mouseButton)

    private fun childrenOrderedForAppearing(): Sequence<NeoElement> {
        val count = validElementsSequence.count()
        return if (count == 0) emptySequence()
        else {
            val selectedIdx =
                if (validElementsSequence.any { it is CategoryButton }) validElementsSequence.indexOfFirst { it.highlighted }
                else -1
            when {
                selectedIdx >= 0 -> {
                    val skipFront = (selectedIdx - (count / 2 - (count + 1) % 2) + count) % count
                    validElementsSequence.drop(skipFront) + validElementsSequence.take(skipFront)
                }

                count >= 7 -> {
                    val s = validElementsSequence + validElementsSequence
                    s.drop(min(max((scroll + count) % count, 0), count)).take(7)
                }

                else -> validElementsSequence
            }
        }
    }

    fun addDescription(description: MutableList<String>) {
        delegate.description.addAll(description)
    }

    fun addDescription(description: String) {
        delegate.description.add(description)
    }

    fun category(
        icon: IIcon,
        label: String,
        description: MutableList<String> = mutableListOf(),
        body: (CategoryButton.() -> Unit)? = null
    ): CategoryButton {
        val cat = CategoryButton(IconLabelElement(icon, label, description = description), this, body)
        +cat
        return cat
    }

    fun partyMenu(): CategoryButton {
        val partyElement = PartyElement()
        if (true/*!SAOCore.isSAOMCLibServerSide*/) {
            partyElement.disabled = true
            partyElement.description.add(I18n.get("saoui.server"))
        }
        val cat = CategoryButton(partyElement, this)
        +cat
        return cat
    }

    fun friendMenu(): CategoryButton {
        val friendElement = FriendElement(this)
        val cat = CategoryButton(friendElement, this)
        +cat
        return cat
    }

    fun profile(player: Player, body: (CategoryButton.() -> Unit)? = null): CategoryButton {
        val cat = CategoryButton(ProfileElement(player, player != Client.mc.player), this, body)
        +cat
        return cat
    }

    fun crafting(): CategoryButton {
        val cat = CategoryButton(CraftingElement(), this)
        +cat
        return cat
    }

    fun recipes(): CategoryButton {
        val cat = CategoryButton(IconLabelElement(IconCore.CRAFTING, I18n.get("sao.element.recipes")), this) {
            +IconLabelElement(IconCore.CANCEL, "Not yet implemented").apply {
                disabled = true
            }
//            addRecipes(AdvancementUtil.getRecipes())
        }
        +cat
        return cat
    }

    /*fun advancementCategory(advancement: Advancement): CategoryButton {
        return if (advancement.getProgress() != null && advancement.getProgress()!!.isDone) {
            val cat = CategoryButton(AdvancementElement(advancement, true), this) {
                category(Items.WRITTEN_BOOK.toIcon(), I18n.get("sao.element.quest.completed")) {
                    addAdvancements(AdvancementUtil.getAdvancements(advancement, true))
                }
                category(Items.WRITABLE_BOOK.toIcon(), I18n.get("sao.element.quest.inProgress")) {
                    addAdvancements(AdvancementUtil.getAdvancements(advancement, false))
                }
            }
            +cat
            cat
        } else advancement(advancement)
    }*/

    fun addAdvancements(advancements: Sequence<Advancement>) {
        advancements.forEach {
            advancement(it)
        }
    }

    fun advancement(advancement: Advancement): CategoryButton {
        val parent = this
        val cat = CategoryButton(be.bluexin.mcui.deprecated.api.elements.AdvancementElement(advancement), this) {
            onClick { vec, mouse ->
                (tlParent as CoreGUI<*>).openGui(PopupAdvancement(advancement)) += {
                    var index = parent.elements.indexOf(this)
                    when (it) {
                        PopupAdvancement.Result.NEXT -> {
                            if (++index >= parent.elements.size) {
                                index = 0
                            }
                            parent.elements[index].mouseClicked(vec, mouse)
                        }

                        PopupAdvancement.Result.PREVIOUS -> {
                            if (--index < 0) {
                                index = parent.elements.size.minus(1)
                            }
                            parent.elements[index].mouseClicked(vec, mouse)
                        }

                        else -> {}
                    }
                }
                true
            }
        }
        +cat
        return cat
    }

    fun addRecipes(advancements: List<Advancement>) {
        advancements.forEach { advancement ->
            advancement.rewards.recipes.forEach { recipe ->
                Client.mc.level?.recipeManager?.byKey(recipe)?.ifPresent {
                    +CategoryButton(RecipeElement(it), this)
                }
            }
        }
    }

    fun reInit() {
        val wasOpen = this.highlighted
        if (wasOpen) this.close(true)
        this.elements.clear()
        this.delegate.elements.clear()
        this.init?.invoke(this)
        if (wasOpen) this.open(true)
    }

    override fun toString(): String {
        return "CategoryButton(delegate=$delegate, elements=${elements.size})"
    }
}

fun INeoParent.optionButton(option: OptionCore): IconLabelElement {
    val but = object :
        IconLabelElement(IconCore.OPTION, option.displayName, description = option.description.toMutableList()) {
        override var highlighted: Boolean
            get() = option.isEnabled
            set(value) = if (value) option.enable() else option.disable()
    }
    but.onClick { _, _ ->
        option.flip()
        true
    }
    but.parent = this
    return but
}

fun INeoParent.optionCategory(option: OptionCore): CategoryButton {
    val cat = CategoryButton(
        IconLabelElement(
            IconCore.OPTION,
            option.displayName,
            description = option.description.toMutableList()
        )
    )
    if (option == OptionCore.THEME) {
        get<ThemeManager>().themeList.forEach { (_, theme) ->
            cat += themeButton(theme)
        }
    }
    option.subOptions.forEach {
        cat += if (it.isCategory) {
            optionCategory(it)
        } else optionButton(it)
    }
    cat.parent = this
    return cat
}

private fun translateIfExists(key: String, default: String) =
    if (I18n.exists(key)) I18n.get(key) else default

private val ResourceLocation.tl get() = toString().replace(':', '.')

fun INeoParent.themeButton(theme: ThemeDefinition): IconLabelElement {
    val realName = translateIfExists(theme.nameTranslationKey, theme.name)
    val themeManager = get<ThemeManager>()
    val but = object :
        IconLabelElement(
            IconCore.OPTION, realName,
            description = mutableListOf(
                "Change theme to $realName",
                translateIfExists(theme.descTranslationKey, "No description for $realName (${theme.id})")
            )
        ) {
        override var highlighted: Boolean
            get() = themeManager.currentTheme === theme && !OptionCore.VANILLA_UI.isEnabled
            set(_) = (Unit)
    }
    but.onClick { _, _ ->
        themeManager.load(Client.mc.resourceManager, theme.id)
        OptionCore.VANILLA_UI.disable()
        Client.mc.setScreen(null)
        Client.mc.setScreen(IngameMenu())
        true
    }
    but.parent = this
    return but
}
