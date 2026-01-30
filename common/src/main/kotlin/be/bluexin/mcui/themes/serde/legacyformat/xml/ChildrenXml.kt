package be.bluexin.mcui.themes.serde.legacyformat.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("children")
data class ChildrenXml(
    val elements: List<ElementXml> = emptyList()
)
