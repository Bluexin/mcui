package be.bluexin.mcui.themes.serde.legacyformat.dto

import be.bluexin.mcui.themes.serde.legacyformat.dto.ElementXml.Companion.DEFAULT_NAME
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@SerialName("glString")
internal data class StringXml(

    /**
     * Friendly name for this element. Mostly used for debug purposes.
     */
    val name: String = DEFAULT_NAME,

    /**
     * X position.
     */
    @SerialName("x")
    @XmlSerialName("x")
    val x: AnonymousExpressionIntermediate? = null,

    /**
     * Y position.
     */
    @SerialName("y")
    @XmlSerialName("y")
    val y: AnonymousExpressionIntermediate? = null,

    /**
     * Z position.
     */
    @SerialName("z")
    @XmlSerialName("z")
    val z: AnonymousExpressionIntermediate? = null,

    /**
     * Whether this element should be enabled.
     */
    @SerialName("enabled")
    @XmlSerialName("enabled")
    val enabled: AnonymousExpressionIntermediate? = null,

    /**
     * Global scale for this element
     */
    @SerialName("scale")
    @XmlSerialName("scale")
    val scale: AnonymousExpressionIntermediate? = null,

    @SerialName("rgba")
    @XmlSerialName("rgba")
    val rgba: AnonymousExpressionIntermediate? = null,

    @XmlSerialName("text")
    val text: AnonymousExpressionIntermediate? = null,

    @XmlSerialName("shadow")
    val shadow: AnonymousExpressionIntermediate? = null,

    @XmlSerialName("centered")
    val centered: AnonymousExpressionIntermediate? = null,

    @Deprecated("This was used in place of <centered> property in some old themes")
    @SerialName("h")
    @XmlSerialName("h")
    val legacyHeight: AnonymousExpressionIntermediate? = null,
) : ElementXml
