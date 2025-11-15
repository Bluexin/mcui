package be.bluexin.mcui.themes.serde.legacyformat.dto

import be.bluexin.mcui.themes.elements.HudPartType
import be.bluexin.mcui.themes.meta.ThemeMetadata
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlNamespaceDeclSpec
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@OptIn(ExperimentalXmlUtilApi::class)
@Serializable
@SerialName("bl:hud")
@XmlNamespaceDeclSpec("bl=https://www.bluexin.be/be/bluexin/mcui/hud-schema")
internal data class HudXml(
    val name: String = "HUD",
    @XmlElement
    val version: String = ThemeMetadata.UNKNOWN_VERSION,
    @XmlSerialName("parts")
    val parts: Parts,
) {

    @Serializable
    data class Parts(
        @XmlSerialName("entry")
        val parts: List<Entry<HudPartType, ElementGroupXml>>
    ) {

        @Serializable
        data class Entry<K, V>(
            @XmlSerialName("key")
            val key: K,
            @XmlSerialName("value")
            val value: V
        )

    }
}
