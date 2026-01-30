package be.bluexin.mcui.themes.serde.legacyformat.xml

import be.bluexin.mcui.themes.serde.legacyformat.xml.ElementXml.Companion.DEFAULT_NAME
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@SerialName("fragmentReference")
internal data class FragmentReferenceXml(

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

    val id: String,

    val variables: Variables? = null,
) : ElementXml, ElementXml.WithRenderState, ElementXml.WithTransform {

    @SerialName("variables")
    @Serializable
    data class Variables(
        val values: Map<String, NamedExpressionIntermediate>
    )
}
