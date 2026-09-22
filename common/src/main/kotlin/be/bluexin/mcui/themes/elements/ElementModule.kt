package be.bluexin.mcui.themes.elements

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * Registers the Koin beans living in the `elements` tree (renderer, ...).
 */
@Module
@ComponentScan("be.bluexin.mcui.themes.elements.visitor.renderer")
class ElementModule