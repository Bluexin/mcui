package be.bluexin.mcui.themes.serde.legacyformat.dto

import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.luajksp.annotations.LuajMapped
import be.bluexin.mcui.themes.miniscript.HumanoidArmMapper
import be.bluexin.mcui.themes.serde.legacyformat.dto.ElementXml.Companion.DEFAULT_NAME
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.world.entity.HumanoidArm
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@SerialName("glHotbarItem")
internal data class HotbarItemXml(

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

    @SerialName("slot")
    @XmlSerialName("slot")
    @LuajExpose
    val slot: AnonymousExpressionIntermediate? = null,

    @SerialName("itemXoffset")
    @XmlSerialName("itemXoffset")
    @LuajExpose
    val itemXoffset: AnonymousExpressionIntermediate? = null,

    @SerialName("itemYoffset")
    @XmlSerialName("itemYoffset")
    @LuajExpose
    val itemYoffset: AnonymousExpressionIntermediate? = null,

    @SerialName("hand")
    @XmlSerialName("hand")
    @LuajExpose
    val hand: @LuajMapped(HumanoidArmMapper::class, import = "support") HumanoidArm? = null
) : ElementXml
