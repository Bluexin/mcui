package be.bluexin.mcui.themes.serde.legacyformat.dto

import be.bluexin.mcui.themes.serde.legacyformat.dto.ElementXml.Companion.DEFAULT_NAME
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@SerialName("glRectangle")
internal data class RectangleXml(

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
    @SerialName("srcX")
    @XmlSerialName("srcX")
    val srcX: AnonymousExpressionIntermediate? = null,
    @SerialName("srcY")
    @XmlSerialName("srcY")
    val srcY: AnonymousExpressionIntermediate? = null,
    @SerialName("w")
    @XmlSerialName("w")
    val w: AnonymousExpressionIntermediate? = null,
    @SerialName("h")
    @XmlSerialName("h")
    val h: AnonymousExpressionIntermediate? = null,
    @SerialName("srcW")
    @XmlSerialName("srcW")
    val srcW: AnonymousExpressionIntermediate? = null,
    @SerialName("srcH")
    @XmlSerialName("srcH")
    val srcH: AnonymousExpressionIntermediate? = null,
    @SerialName("texture")
    @XmlSerialName("texture")
    val texture: AnonymousExpressionIntermediate? = null,
) : ElementXml
