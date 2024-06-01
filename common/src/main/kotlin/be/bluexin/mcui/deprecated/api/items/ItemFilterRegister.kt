package be.bluexin.mcui.deprecated.api.items

import net.minecraft.world.item.ItemStack

object ItemFilterRegister {

    private val itemFilters: LinkedHashSet<IItemFilter> = linkedSetOf()
    private val defaultFilters: LinkedHashSet<IItemFilter> = getDefaults()

    val tlFilters
        get() = filters().filter { it.category == null }

    private fun getDefaults(): LinkedHashSet<IItemFilter> {
        val set: LinkedHashSet<IItemFilter> = linkedSetOf()
        BaseFilters.entries.toCollection(set)
        return set
    }

    fun registerFilter(filter: IItemFilter) {
        itemFilters.add(filter)
    }

    fun filters(): Set<IItemFilter> {
        return setOf<IItemFilter>().union(itemFilters).union(defaultFilters)
    }

    /**
     * Finds the first filter that matches the stack
     * Will always return BaseFilters.MATERIALS if no other
     * valid filter was found
     */
    fun findFilter(stack: ItemStack): IItemFilter {
        return itemFilters.firstOrNull { iItemFilter -> iItemFilter.invoke(stack) }
            ?: defaultFilters.asSequence().filter { it != BaseFilters.MATERIALS }.firstOrNull { iItemFilter -> iItemFilter.invoke(stack) }
            ?: BaseFilters.MATERIALS
    }
}
