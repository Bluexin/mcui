package be.bluexin.mcui.deprecated.api.elements

import be.bluexin.mcui.deprecated.screens.util.toIcon
import be.bluexin.mcui.util.Client
import net.minecraft.world.item.crafting.Recipe

class RecipeElement(recipe: Recipe<*>) : IconLabelElement(
    recipe.getResultItem(Client.mc.level!!.registryAccess()).toIcon(),
    recipe.getResultItem(Client.mc.level!!.registryAccess()).displayName.string
)
