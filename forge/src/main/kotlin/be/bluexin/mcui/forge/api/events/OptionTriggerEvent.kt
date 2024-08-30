package be.bluexin.mcui.forge.api.events

import be.bluexin.mcui.deprecated.api.info.IOption
import net.minecraftforge.eventbus.api.Event

/**
 * This is called when an option is clicked
 * Use this to fire any logic you want tied
 * to said event.
 */
class OptionTriggerEvent(val option: IOption) : Event()
