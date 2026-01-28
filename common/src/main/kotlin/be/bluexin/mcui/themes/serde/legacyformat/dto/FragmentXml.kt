package be.bluexin.mcui.themes.serde.legacyformat.dto

import be.bluexin.mcui.themes.serde.legacyformat.dto.ElementXml.Companion.DEFAULT_NAME
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XmlNamespaceDeclSpec
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@OptIn(ExperimentalXmlUtilApi::class)
@Serializable
@SerialName("bl:fragment")
@XmlNamespaceDeclSpec("bl=https://www.bluexin.be/com/saomc/saoui/fragment-schema")
internal data class FragmentXml(

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

    val children: ChildrenXml? = null,

    /**
     * Texture to bind before rendering this group. More efficient than repeating the texture on each child element.
     */
    @SerialName("texture")
    @XmlSerialName("texture")
    val texture: AnonymousExpressionIntermediate? = null,

    val expect: ExpectXml? = null,
) : ElementXml, ElementXml.WithRenderState, ElementXml.WithTransform