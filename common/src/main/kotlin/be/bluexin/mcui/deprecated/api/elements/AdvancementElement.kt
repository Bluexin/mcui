package be.bluexin.mcui.deprecated.api.elements

import be.bluexin.mcui.deprecated.screens.util.toIcon
import net.minecraft.advancements.Advancement
import net.minecraft.world.item.Items

data class AdvancementElement(
    val advancement: Advancement,
    val category: Boolean = false,
    val desc: List<String> = advancement.getRequirementDesc()
) : be.bluexin.mcui.deprecated.api.elements.IconLabelElement(
    icon = advancement.display?.icon?.toIcon() ?: if (/*advancement.getProgress()?.isDone ==*/ true) Items.MAP.toIcon() else Items.MAP.toIcon(),
    label = advancement.display?.title?.string ?: "unknown"
)

fun Advancement.getRequirementDesc(): List<String> {
    val builder = StringBuilder()
    builder.append("Requirements: \n")
    this.requirements.forEach {
        builder.append(it.joinToString())
    }
    return builder.lines()
}
