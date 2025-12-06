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
    override val name: String = DEFAULT_NAME,

    /**
     * X position.
     */
    @SerialName("x")
    @XmlSerialName("x")
    override val x: AnonymousExpressionIntermediate? = null,

    /**
     * Y position.
     */
    @SerialName("y")
    @XmlSerialName("y")
    override val y: AnonymousExpressionIntermediate? = null,

    /**
     * Z position.
     */
    @SerialName("z")
    @XmlSerialName("z")
    override val z: AnonymousExpressionIntermediate? = null,

    /**
     * Whether this element should be enabled.
     */
    @SerialName("enabled")
    @XmlSerialName("enabled")
    override val enabled: AnonymousExpressionIntermediate? = null,

    /**
     * Global scale for this element
     */
    @SerialName("scale")
    @XmlSerialName("scale")
    override val scale: AnonymousExpressionIntermediate? = null,

    @SerialName("rgba")
    @XmlSerialName("rgba")
    val rgba: AnonymousExpressionIntermediate? = null,
    @SerialName("srcX")
    @XmlSerialName("srcX")
    val sourceX: AnonymousExpressionIntermediate? = null,
    @SerialName("srcY")
    @XmlSerialName("srcY")
    val sourceY: AnonymousExpressionIntermediate? = null,
    @SerialName("w")
    @XmlSerialName("w")
    val width: AnonymousExpressionIntermediate? = null,
    @SerialName("h")
    @XmlSerialName("h")
    val height: AnonymousExpressionIntermediate? = null,
    @SerialName("srcW")
    @XmlSerialName("srcW")
    val sourceWidth: AnonymousExpressionIntermediate? = null,
    @SerialName("srcH")
    @XmlSerialName("srcH")
    val sourceHeight: AnonymousExpressionIntermediate? = null,
    @SerialName("textureWidth")
    @XmlSerialName("textureWidth")
    val textureWidth: AnonymousExpressionIntermediate? = null,
    @SerialName("textureHeight")
    @XmlSerialName("textureHeight")
    val textureHeight: AnonymousExpressionIntermediate? = null,
    @SerialName("texture")
    @XmlSerialName("texture")
    val texture: AnonymousExpressionIntermediate? = null,
) : ElementXml, ElementXml.WithRenderState, ElementXml.WithTransform
