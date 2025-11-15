package be.bluexin.mcui.themes.serde.legacyformat.dto

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("expect")
data class ExpectXml(
    val variables: Map<String, NamedExpressionIntermediate> = emptyMap()
)