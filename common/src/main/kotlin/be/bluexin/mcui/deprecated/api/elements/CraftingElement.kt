package be.bluexin.mcui.deprecated.api.elements

import be.bluexin.mcui.util.IconCore
import net.minecraft.client.resources.language.I18n

class CraftingElement : IconLabelElement(IconCore.CRAFTING, I18n.get("guiCrafting")) {

    var init: Boolean = false

    val EMPTY = object : IconLabelElement(icon = IconCore.NONE, label = I18n.get("gui.empty")) {
        private var mark = false

        override var valid: Boolean
            get() {
                if (mark) return false
                mark = true
                val r = validElementsSequence.none()
                mark = false
                return r
            }
            set(_) {}

        override var disabled: Boolean
            get() = true
            set(_) {}
    }

    override fun update() {
        if (!init) {
//            CraftingUtil.updateItemHelper(true)
            initRecipes()
            init = true
            /*
            BlockPos.getAllInBox(Minecraft().player.position.add(-3, -3, -3), Minecraft().player.position.add(3, 3, 3)).forEach {
                val state = Minecraft().world.getBlockState(it)
                BlockWorkbench.
                state.block.onBlockActivated(Minecraft().world, it, state, Minecraft().player, EnumHand.MAIN_HAND, EnumFacing.UP, 0f, 0f, 0f)
            }*/
        }
       /* if (CraftingUtil.updateItemHelper()) {
            initRecipes()
        }*/
        super.update()
    }

    fun initRecipes() {
        if (this.highlighted) {
            (this.parent as CategoryButton).close(true)
            (this.parent as CategoryButton).open(true)
        }
        elements.clear()
        /*CraftingUtil.getCategories().forEach { tab ->
            if (CraftingUtil.anyValidRecipes(tab)) {
                +CategoryButton(IconLabelElement(tab.iconItemStack.toIcon(), getTabName(tab))) {
                    CraftingUtil.getRecipes(tab).forEach { recipe ->
                        +IconLabelElement(recipe.recipeOutput.toIcon(), recipe.recipeOutput.displayName).onClick { _, _ ->
                            (tlParent as? CoreGUI<*>)?.openGui(
                                PopupCraft(recipe)
                            )?.plusAssign {
                                initRecipes()
                            }
                            true
                        }
                    }
                }
            }
        }*/
        if (validElementsSequence.none()) +EMPTY
        if (!this.highlighted) elementsSequence.forEach { it.hide() }
    }

    /*fun getTabName(tab: CreativeTabs): String {
        val name = tab.tabLabel.trim()
        if (name.isEmpty()) {
            return ""
        }
        val newText = StringBuilder(name.length * 2)

        for (i in name.indices) {
            if (i != 0 && name[i].isUpperCase() && name[i - 1] != ' ') {
                newText.append(' ')
            }
            newText.append(name[i])
        }
        return newText.toString().capitalize()
    }*/
}
