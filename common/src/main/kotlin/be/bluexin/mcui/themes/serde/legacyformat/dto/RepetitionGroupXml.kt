package be.bluexin.mcui.themes.serde.legacyformat.dto

import be.bluexin.mcui.themes.serde.legacyformat.dto.ElementXml.Companion.DEFAULT_NAME
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@SerialName("repetitionGroup")
internal data class RepetitionGroupXml(

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

    val children: ChildrenXml? = null,

    /**
     * Texture to bind before rendering this group. More efficient than repeating the texture on each child element.
     */
    @SerialName("texture")
    @XmlSerialName("texture")
    val texture: AnonymousExpressionIntermediate? = null,

    /**
     * Number of repetitions for this group
     */
    @SerialName("amount")
    @XmlSerialName("amount")
    val amount: AnonymousExpressionIntermediate,
) : ElementXml