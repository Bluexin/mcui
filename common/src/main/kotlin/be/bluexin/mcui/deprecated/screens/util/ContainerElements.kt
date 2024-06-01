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

package be.bluexin.mcui.deprecated.screens.util

import be.bluexin.mcui.deprecated.api.elements.CategoryButton
import be.bluexin.mcui.deprecated.api.elements.IconLabelElement
import be.bluexin.mcui.deprecated.api.items.IItemFilter
import be.bluexin.mcui.deprecated.api.screens.IIcon
import be.bluexin.mcui.deprecated.screens.CoreGUI
import be.bluexin.mcui.deprecated.screens.MouseButton
import be.bluexin.mcui.util.Client
import be.bluexin.mcui.util.Client.mc
import be.bluexin.mcui.util.IconCore
import be.bluexin.mcui.util.math.Vec2d
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.resources.language.I18n
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.block.Block

fun CategoryButton.itemList(inventory: AbstractContainerMenu, filter: IItemFilter) {
    inventory.slots.forEach { slot ->
        +ItemStackElement(slot, filter.getValidSlots(), Vec2d.ZERO, filter.getValidSlots().contains(slot), filter)
    }
    +object : IconLabelElement(icon = IconCore.NONE, label = I18n.get("gui.empty")) {
        private var mark = false

        override var valid: Boolean
            get() {
                if (mark) return false
                mark = true
                val r = this@itemList.validElementsSequence.none()
                mark = false
                return r
            }
            set(_) {}

        override var disabled: Boolean
            get() = true
            set(_) {}
    }
}

class ItemStackElement(private val slot: Slot, val equipSlots: Set<Slot>, pos: Vec2d, override var highlighted: Boolean, private val filter: (iss: ItemStack) -> Boolean) :
    IconLabelElement(icon = slot.item.toIcon(), pos = pos) {

    init {
        onClick { _, button ->
            if (button == MouseButton.LEFT) {
                if (tlParent is CoreGUI<*>) {
                    itemStack
                    (tlParent as CoreGUI<*>).openGui(PopupItem(label, itemStack.itemDesc(), if (mc.options.advancedItemTooltips) Item.getId(itemStack.item).toString() else "")) += {
                        when (it) {
                            PopupItem.Result.EQUIP -> handleEquip()
                            PopupItem.Result.DROP -> handleDrop()
                            else -> {}
                        }
                        highlighted = false
                    }
                }
            }
            true
        }
        // itemStack.getTooltip(mc.player, if (mc.gameSettings.advancedItemTooltips) ITooltipFlag.TooltipFlags.ADVANCED else ITooltipFlag.TooltipFlags.NORMAL)
    }

    private fun handleEquip() {
        if (equipSlots.size > 1) {
            (tlParent as CoreGUI<*>).openGui(PopupSlotSelection(label, listOf("Select a slot"), "", equipSlots.filter { it != slot }.toSet())) += {
                if (it != -1) {
                    swapItems(equipSlots.first { slot -> slot.index == it })
                }
            }
        } else if (equipSlots.first() == slot) {
            var stack = itemStack
            mc.player!!.inventoryMenu.slots
                .filter { it.index !in IntRange(0, 4) && it.mayPlace(stack) && it.hasItem() && it.item.count != it.item.maxStackSize /*&& ContainerPlayer.canAddItemToSlot(it, stack, true)*/ }
                .any slotCheck@{
                    stack = moveItems(it)
                    stack.isEmpty
                }
            if (!stack.isEmpty) {
                mc.player!!.inventoryMenu.slots
                    .filter { it.index !in IntRange(0, 4) && !it.hasItem() && it.mayPlace(stack) }
                    .any slotCheck@{
                        stack = moveItems(it)
                        stack.isEmpty
                    }
            }
            if (!stack.isEmpty) {
                throwItem()
            }
        } else {
            swapItems(equipSlots.first())
        }

        mc.player?.containerMenu?.broadcastChanges()
        (controllingParent as? CategoryButton)?.reInit()
    }

    private fun handleDrop() {
        (tlParent as CoreGUI<*>).openGui(PopupYesNo(label, "Are you sure you want to discard this item?", "")) += {
            if (it == PopupYesNo.Result.YES) {
                throwItem()
            }
        }
    }

    /**
     * Will pickup or place item, and return
     * the currently held item
     */
    fun moveItems(slot: Slot): ItemStack {
//        return mc.player.windowClick(mc.player.openContainer.windowId, slot.index, 0, ClickType.PICKUP, mc.player)
        return ItemStack.EMPTY
    }

    fun swapItems(otherSlot: Slot) {
//        mc.player.windowClick(mc.player.inventoryContainer.windowId, otherSlot.index, 0, ClickType.PICKUP, mc.player)
//        mc.player.windowClick(mc.player.inventoryContainer.windowId, slotID, 0, ClickType.PICKUP, mc.player)
//        mc.player.windowClick(mc.player.inventoryContainer.windowId, otherSlot.index, 0, ClickType.PICKUP, mc.player)
    }

    fun throwItem() {
//        mc.player.windowClick(mc.player.inventoryContainer.windowId, slotID, 0, ClickType.THROW, mc.player)
    }

    /**
     * TODO Finish this
     * Compares two items together
     * Types:   0 - Armor
     *          1 - Weapons
     *          2 - Tools
     *          3 - Food
     *          4 - Misc
     */
    fun compare(other: ItemStack, type: Int): List<String> {
        val stringBuilder = mutableListOf<String>()
        when (type) {
            0 -> {
                val desc = itemStack.itemDesc()
                other.itemDesc().forEachIndexed { index, s ->
                    if (index <= 3) {
                        stringBuilder.add("$s -> ${desc[index]}")
                    }
                }
            }
            else -> {
            }
        }
        return stringBuilder
    }

    private val itemStack
        get() = with(slot.item) {
            return@with if (filter(this)) this
            else ItemStack.EMPTY
        }

    private val slotID
        get() = slot.index

    override var valid: Boolean
        get() = !itemStack.isEmpty
        set(_) {}

    override var label: String = ""
        get() = if (!itemStack.isEmpty) {
            if (itemStack.count > 1) I18n.get("saoui.formatItems", itemStack.getTooltipLines(mc.player, TooltipFlag.NORMAL)[0], itemStack.count)
            else I18n.get("saoui.formatItem", itemStack.getTooltipLines(mc.player, TooltipFlag.NORMAL)[0])
        } else I18n.get("gui.empty")
}

class ItemIcon(private val itemStack: () -> ItemStack) :
    IIcon {
    private val itemRenderer by lazy { Client.mc.itemRenderer }

    override fun glDraw(x: Int, y: Int, z: Float, poseStack: PoseStack) {
        /*val f = itemStack().animationsToGo.toFloat()*//* - partialTicks*//*
        RenderHelper.disableStandardItemLighting()

        itemRenderer.zLevel += z

        if (f > 0.0f) {
            poseStack.pushPose()
            val f1 = 1.0f + f / 5.0f
            GLCore.translate((x + 8).toFloat(), (y + 12).toFloat(), z)
            GLCore.scale(1.0f / f1, (f1 + 1.0f) / 2.0f, z.plus(1))
            GLCore.translate((-(x + 8)).toFloat(), (-(y + 12)).toFloat(), z)
        }

        RenderHelper.enableGUIStandardItemLighting()
        itemRenderer.renderItemAndEffectIntoGUI(itemStack(), x, y)
        GLCore.depth(false)

        itemRenderer.zLevel -= z

        if (f > 0.0f) poseStack.popPose()*/

//        itemRenderer.renderItemOverlays(fontRenderer, itemStack, x, y)
    }
}

fun Item.toIcon(): ItemIcon = ItemIcon { ItemStack(this) }
fun Block.toIcon(): ItemIcon = ItemIcon { ItemStack(this) }
fun ItemStack.toIcon(): ItemIcon = ItemIcon { this }

fun ItemStack.itemDesc(): List<String> {
    val stringBuilder = mutableListOf<String>()
    /*val desc = getTooltip(mc.player, ITooltipFlag.TooltipFlags.NORMAL)
    desc.removeAt(0)
    if (item is ItemTool) {
        if (toolClasses.isNotEmpty()) {
            toolClasses.forEachIndexed { index, s ->
                stringBuilder.add(I18n.get("itemDesc.type", I18n.get("itemDesc.tool")))
                if (toolClasses.size > 1) {
                    if (index == 0) {
                        stringBuilder.add(I18n.get("itemDesc.toolClasses", s.capitalize(), item.getHarvestLevel(this, s, mc.player, null)))
                    } else {
                        stringBuilder.add(I18n.get("itemDesc.toolClassSpace", s.capitalize(), item.getHarvestLevel(this, s, mc.player, null)))
                    }
                } else {
                    stringBuilder.add(I18n.get("itemDesc.toolClass", s.capitalize(), item.getHarvestLevel(this, s, mc.player, null)))
                }
            }
        }

        if (isItemEnchantable) {
            stringBuilder.add(I18n.get("itemDesc.enchantability", (item as ItemTool).itemEnchantability))
        } else {
            stringBuilder.add(I18n.get("itemDesc.enchantable", isItemEnchantable.toString().capitalize()))
        }
        stringBuilder.add(I18n.get("itemDesc.repairable", (item as ItemTool).isRepairable.toString().capitalize()))
    } else if (toolClasses.isNotEmpty()) {
        toolClasses.forEachIndexed { index, s ->
            stringBuilder.add(I18n.get("itemDesc.type", I18n.get("itemDesc.tool")))
            if (toolClasses.size > 1) {
                if (index == 0) {
                    stringBuilder.add(I18n.get("itemDesc.toolClasses", s.capitalize(), item.getHarvestLevel(this, s, mc.player, null)))
                } else {
                    stringBuilder.add(I18n.get("itemDesc.toolClassSpace", s.capitalize(), item.getHarvestLevel(this, s, mc.player, null)))
                }
            } else {
                stringBuilder.add(I18n.get("itemDesc.toolClass", s.capitalize(), item.getHarvestLevel(this, s, mc.player, null)))
            }
            stringBuilder.add(I18n.get("itemDesc.enchantable", isItemEnchantable.toString().capitalize()))
        }

        // TODO Item tool
    } else if (item is ItemHoe) {
        stringBuilder.add(I18n.get("itemDesc.type", I18n.get("itemDesc.tool")))
        stringBuilder.add(I18n.get("itemDesc.toolClass", I18n.get("itemDesc.hoe"), 0))
    } else if (item is ItemSword) {
        stringBuilder.add(I18n.get("itemDesc.type", I18n.get("itemDesc.sword")))
        if (isItemEnchantable) {
            stringBuilder.add(I18n.get("itemDesc.enchantability", (item as ItemSword).itemEnchantability))
        } else {
            stringBuilder.add(I18n.get("itemDesc.enchantable", isItemEnchantable.toString().capitalize()))
        }
    } else if (item is ItemBlock) {
        val block = (item as ItemBlock).block
        val state = block.getStateFromMeta(metadata)
        if (block.hasTileEntity(state)) {
            stringBuilder.add(I18n.get("itemDesc.type", I18n.get("itemDesc.tileEntity")))
        } else {
            stringBuilder.add(I18n.get("itemDesc.type", I18n.get("itemDesc.block")))
        }
        stringBuilder.add(I18n.get("itemDesc.hardness", block.getBlockHardness(state, mc.player.world, mc.player.position)))
        stringBuilder.add(I18n.get("itemDesc.solid", state.material.isSolid.toString().capitalize()))
        if (state.material.isToolNotRequired) {
            stringBuilder.add(I18n.get("itemDesc.toolRequired.false"))
        } else {
            stringBuilder.add(I18n.get("itemDesc.toolRequired.true"))
            stringBuilder.add(
                I18n.get(
                    "itemDesc.mostEffective",
                    mc.player.inventory.asSequence().filter { it.canHarvestBlock(state) }.maxBy { it.getDestroySpeed(state) }?.displayName
                        ?: I18n.get("itemDesc.none")
                )
            )
        }

        // TODO Block Desc
    } else if (item is ItemFood) {
        stringBuilder.add(I18n.get("itemDesc.type", I18n.get("itemDesc.food")))
        stringBuilder.add(I18n.get("itemDesc.foodValue", (item as ItemFood).getHealAmount(this)))
        stringBuilder.add(I18n.get("itemDesc.saturationValue", (item as ItemFood).getSaturationModifier(this)))
        // TODO Food Desc
    } else if (item is ItemEnchantedBook) {
        // TODO Enchant Desc
    } else if (item is ItemWrittenBook) {
        // TODO Book Desk
    } else if (item is ItemArmor) {
        stringBuilder.add(I18n.get("itemDesc.type", I18n.get("itemDesc.armor")))
        val equipSlot = (item as ItemArmor).getEquipmentSlot(this) ?: (item as ItemArmor).equipmentSlot
        stringBuilder.add(I18n.get("itemDesc.slot", equipSlot.getName().capitalize()))
        if (isItemEnchantable) {
            stringBuilder.add(I18n.get("itemDesc.enchantability", (item as ItemArmor).itemEnchantability))
        } else {
            stringBuilder.add(I18n.get("itemDesc.enchantable", isItemEnchantable.toString().capitalize()))
        }
        if ((item as ItemArmor).isRepairable) {
            stringBuilder.add(I18n.get("itemDesc.repairItem", (item as ItemArmor).armorMaterial.repairItemStack.displayName))
        } else {
            stringBuilder.add(I18n.get("itemDesc.repairable", (item as ItemArmor).isRepairable.toString().capitalize()))
        }
        stringBuilder.add(I18n.get("itemDesc.toughness", (item as ItemArmor).armorMaterial.toughness))
    } else if (hasTagCompound()) {
        // TODO Potion Desc
        tagCompound?.get("Potion")?.let {
            val potion = ForgeRegistries.POTION_TYPES.getValue(ResourceLocation(it.toString()))
        }
    } else {
        stringBuilder.add(I18n.get("itemDesc.type", I18n.get("itemDesc.material")))
    }

    stringBuilder.addAll(desc)*/

    return stringBuilder
}

/*
fun IInventory.asSequence(): Sequence<ItemStack> {
    return Sequence {
        object : Iterator<ItemStack> {
            private var index = 0
            private val size get() = this@asSequence.sizeInventory - 1

            override fun hasNext() = index < size

            override fun next(): ItemStack {
                if (!hasNext()) throw IndexOutOfBoundsException("index: $index, size: $size")
                return this@asSequence[index++]
            }
        }
    }
}

fun IInventory.asNumberedSequence(): Sequence<Pair<ItemStack, Int>> {
    return Sequence {
        object : Iterator<Pair<ItemStack, Int>> {
            private var index = 0
            private val size get() = this@asNumberedSequence.sizeInventory - 1

            override fun hasNext() = index < size

            override fun next(): Pair<ItemStack, Int> {
                if (!hasNext()) throw IndexOutOfBoundsException("index: $index, size: $size")
                return this@asNumberedSequence[index] to index++
            }
        }
    }
}

inline fun IInventory.forEach(body: (ItemStack) -> Unit) {
    (0 until sizeInventory).forEach { body(this[it]) }
}

operator fun IInventory.get(index: Int): ItemStack = getStackInSlot(index)
*/
