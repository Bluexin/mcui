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

package be.bluexin.mcui.deprecated.api.items

import be.bluexin.mcui.deprecated.api.screens.IIcon
import be.bluexin.mcui.deprecated.screens.util.toIcon
import be.bluexin.mcui.util.Client
import be.bluexin.mcui.util.IconCore
import net.minecraft.client.resources.language.I18n
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.inventory.InventoryMenu.ARMOR_SLOT_END
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.*
import net.minecraft.world.level.block.Blocks

/**
 * TODO: use [ItemTool.getToolClasses] for moar modded compat
 */
enum class BaseFilters(val filter: (ItemStack, Boolean) -> Boolean) : IItemFilter { // Todo: support for TConstruct

    EQUIPMENT({ _, _ -> false }) {
        override val icon: IIcon
            get() = IconCore.EQUIPMENT
        override val displayName: String
            get() = I18n.get("sao.element.equipment")
        override val isCategory: Boolean
            get() = true
    },

    ARMOR({ _, _ -> false }) {
        override val icon: IIcon
            // get() = IconCore.ARMOR
            get() = Items.IRON_HORSE_ARMOR.toIcon()
        override val category: IItemFilter
            get() = EQUIPMENT
        override val displayName: String
            get() = I18n.get("sao.element.armor")
        override val isCategory: Boolean
            get() = true
    },

    HELMET({ stack, _ ->
        EquipmentSlot.HEAD == LivingEntity.getEquipmentSlotForItem(stack)
    }) {
        override val icon: IIcon
            get() = Items.CHAINMAIL_HELMET.toIcon()

        override val displayName: String
            get() = I18n.get("sao.element.helmet")

        override val category: IItemFilter
            get() = ARMOR

        override fun getValidSlots() = IItemFilter.getPlayerSlots(ARMOR_SLOT_END - 1 - EquipmentSlot.HEAD.index)
    },

    CHESTPLATES({ stack, _ ->
        EquipmentSlot.CHEST == LivingEntity.getEquipmentSlotForItem(stack)
    }) {
        override val icon: IIcon
            get() = Items.CHAINMAIL_CHESTPLATE.toIcon()

        override val displayName: String
            get() = I18n.get("sao.element.chestplates")

        override val category: IItemFilter
            get() = ARMOR

        override fun getValidSlots() = IItemFilter.getPlayerSlots(ARMOR_SLOT_END - 1 - EquipmentSlot.CHEST.index)
    },

    LEGGINS({ stack, _ ->
        net.minecraft.world.entity.EquipmentSlot.LEGS == net.minecraft.world.entity.LivingEntity.getEquipmentSlotForItem(stack)
    }) {
        override val icon: IIcon
            get() = Items.CHAINMAIL_LEGGINGS.toIcon()

        override val displayName: String
            get() = I18n.get("sao.element.leggings")

        override val category: IItemFilter
            get() = ARMOR

        override fun getValidSlots(): Set<Slot> =
            IItemFilter.getPlayerSlots(ARMOR_SLOT_END - 1 - EquipmentSlot.LEGS.index)
    },

    BOOTS({ stack, _ ->
        net.minecraft.world.entity.EquipmentSlot.FEET == net.minecraft.world.entity.LivingEntity.getEquipmentSlotForItem(stack)
    }) {
        override val icon: IIcon
            get() = Items.CHAINMAIL_BOOTS.toIcon()

        override val displayName: String
            get() = I18n.get("sao.element.boots")

        override val category: IItemFilter
            get() = ARMOR

        override fun getValidSlots(): Set<Slot> =
            IItemFilter.getPlayerSlots(ARMOR_SLOT_END - 1 - EquipmentSlot.FEET.index)
    },

    /*
    SHIELDS({ stack, _ -> stack.item.isShield(stack, null) ||
            stack.item is com.teamwizardry.librarianlib.features.base.item.IShieldItem
    }) {
        override val icon: IIcon
            get() = Items.SHIELD.toIcon()

        override val displayName: String
            get() = I18n.get("sao.element.shields")

        override val category: IItemFilter
            get() = ARMOR

        override fun getValidSlots(): Set<Slot> {
            return IItemFilter.getPlayerSlots(45)
        }
    },

     */

    WEAPONS({ _, _ -> false }) {
        override val icon: IIcon
            get() = Items.SPECTRAL_ARROW.toIcon()

        override val category: IItemFilter
            get() = EQUIPMENT

        override val displayName: String
            get() = I18n.get("sao.element.weapons")

        override val isCategory: Boolean
            get() = true
    },

    SWORDS({ stack, _ -> stack.item is SwordItem /*|| stack.toolClasses.contains("sword")*/ }) {
        override val icon: IIcon
            get() = Items.IRON_SWORD.toIcon()

        override val displayName: String
            get() = I18n.get("sao.element.swords")

        override val category: IItemFilter
            get() = WEAPONS
    },

    BOWS({ stack, _ ->
        stack.item is BowItem || stack.item.getUseAnimation(stack) == UseAnim.BOW /*|| stack.toolClasses.contains(
            "bow"
        )*/
    }) {
        override val icon: IIcon
            get() = Items.BOW.toIcon()

        override val displayName: String
            get() = I18n.get("sao.element.bows")

        override val category: IItemFilter
            get() = WEAPONS
    },

    TOOLS({ _, _ -> false }) {
        override val icon: IIcon
            get() = Items.IRON_HOE.toIcon()

        override val category: IItemFilter
            get() = EQUIPMENT

        override val displayName: String
            get() = I18n.get("sao.element.tools")

        override val isCategory: Boolean
            get() = true
    },

    PICKAXES({ stack, _ -> stack.item is PickaxeItem/* || stack.toolClasses.contains("pickaxe")*/ }) {
        override val icon: IIcon
            get() = Items.IRON_PICKAXE.toIcon()

        override val displayName: String
            get() = I18n.get("sao.element.pickaxe")

        override val category: IItemFilter
            get() = TOOLS
    },

    AXES({ stack, _ -> stack.item is AxeItem/* || stack.toolClasses.contains("axe")*/ }) {
        override val icon: IIcon
            get() = Items.IRON_AXE.toIcon()

        override val displayName: String
            get() = I18n.get("sao.element.axe")

        override val category: IItemFilter
            get() = TOOLS
    },

    SHOVELS({ stack, _ -> stack.item is ShovelItem }) {
        override val icon: IIcon
            get() = Items.IRON_SHOVEL.toIcon()

        override val displayName: String
            get() = I18n.get("sao.element.shovel")

        override val category: IItemFilter
            get() = TOOLS
    },

    COMPATTOOLS({ stack, _ ->
        val item = stack.item
//        @Suppress("UNINITIALIZED_ENUM_COMPANION_WARNING") // used in a callback
        entries.filter { it.category == TOOLS && it != COMPATTOOLS }.none { it.invoke(stack) } &&
                (item is DiggerItem || item is ShearsItem)
    }) {
        override val icon: IIcon
            get() = Items.SHEARS.toIcon()

        override val displayName: String
            get() = I18n.get("sao.element.compattools")

        override val category: IItemFilter
            get() = TOOLS
    },

    // TODO : replace with Curio or whatever is the modern alternative
    /*ACCESSORY({ stack, _ -> baublesLoaded && stack.item is IBauble }) {
        override val icon: IIcon
            get() = Items.GHAST_TEAR.toIcon()

        override val displayName: String
            get() = I18n.get("sao.element.accessory")

        override val category: IItemFilter
            get() = EQUIPMENT
    },*/

    ITEMS({ _, _ -> false }) {
        override val displayName: String
            get() = I18n.get("sao.element.items")
        override val isCategory: Boolean
            get() = true
    },

    CONSUMABLES({ stack, _ ->
        val action = stack.useAnimation
        action == UseAnim.DRINK || action == UseAnim.EAT
    }) {
        override val icon: IIcon
            get() = Items.APPLE.toIcon()

        override val displayName: String
            get() = I18n.get("sao.element.consumables")

        override val category: IItemFilter
            get() = ITEMS
    },

    BLOCKS({ stack, _ -> stack.item is BlockItem }) {
        override val icon: IIcon
            get() = Blocks.COBBLESTONE.toIcon()
        override val displayName: String
            get() = I18n.get("sao.element.blocks")

        override val category: IItemFilter
            get() = ITEMS
    },

    /**
     * Default fallback filter
     */
    MATERIALS({ stack, _ -> ItemFilterRegister.findFilter(stack) == MATERIALS }) {
        override val icon: IIcon
            get() = Items.IRON_INGOT.toIcon()

        override val displayName: String
            get() = I18n.get("sao.element.materials")

        override val category: IItemFilter
            get() = ITEMS
    };

    override fun invoke(stack: ItemStack, equipped: Boolean) = filter(stack, equipped)

    companion object {
        val mc by lazy { Client.mc }

//        val baublesLoaded by lazy { Loader.isModLoaded("baubles") }

        /*fun getBaubles(player: Player): IInventory? {
            return if (!baublesLoaded) {
                null
            } else {
                BaublesApi.getBaubles(player)
            }
        }*/
    }
}
