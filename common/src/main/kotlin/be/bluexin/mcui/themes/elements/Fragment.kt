package be.bluexin.mcui.themes.elements

import be.bluexin.mcui.themes.serde.legacyformat.xml.NamedExpressionIntermediate

/**
 * This is not an Element, just a holder for the data inside.
 */
data class Fragment(
    val group: Group,
    val expect: Map<String, NamedExpressionIntermediate>
)
