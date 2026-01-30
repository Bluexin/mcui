package be.bluexin.mcui.themes.serde.legacyformat.xml

import be.bluexin.mcui.themes.serde.legacyformat.xml.ElementXml.Companion.DEFAULT_NAME
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@SerialName("widget")
@XmlSerialName(value = "widget")
internal data class WidgetXml(

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
     * Global scale for this element
     */
    @SerialName("scale")
    @XmlSerialName("scale")
    val scale: AnonymousExpressionIntermediate? = null,

    /**
     * Whether this element should be enabled.
     */
    @SerialName("enabled")
    @XmlSerialName("enabled")
    val enabled: AnonymousExpressionIntermediate? = null,

    val children: ChildrenXml? = null,

    /**
     * Texture to bind before rendering this group. More efficient than repeating the texture on each child element.
     */
    @SerialName("texture")
    @XmlSerialName("texture")
    val texture: AnonymousExpressionIntermediate? = null,

    @XmlElement
    val expect: ExpectXml? = null,

    /**
     * Width used for hover events
     */
    @XmlSerialName("contentWidth")
    val contentWidth: AnonymousExpressionIntermediate? = null,

    /**
     * Height used for hover events
     */
    @XmlSerialName("contentHeight")
    val contentHeight: AnonymousExpressionIntermediate? = null,

    @XmlElement
    @XmlSerialName
    val tooltip: AnonymousExpressionIntermediate? = null,

    /**
     * Whether this widget is interactive
     */
    @XmlSerialName("active")
    val active: AnonymousExpressionIntermediate? = null,

    @XmlElement
    @XmlSerialName("onClick")
    val onClickScript: String? = null,

    @XmlElement
    @XmlSerialName("onMouseOver")
    val onMouseOverEventScript: String? = null,

    @XmlElement
    @XmlSerialName("onLoseFocus")
    val onLoseFocusScript: String? = null,

    @XmlSerialName("extra")
    val extraScripts: ExtraWrapper? = null,
) : ElementXml {

    @Serializable
    data class ExtraWrapper(
        val values: Map<String, String> = emptyMap()
    )


}
