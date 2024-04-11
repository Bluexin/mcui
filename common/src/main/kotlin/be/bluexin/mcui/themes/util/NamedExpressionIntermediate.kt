@file:OptIn(ExperimentalSerializationApi::class)

package be.bluexin.mcui.themes.util

import be.bluexin.mcui.themes.util.typeadapters.JelType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlOtherAttributes
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@SerialName("variable")
@Serializable
data class NamedExpressionIntermediate(
    @XmlOtherAttributes
    val type: JelType,
    @XmlValue
    @SerialName("expression")
    override val expression: String = "",
    @SerialName("cache")
    @XmlOtherAttributes
    override val cacheType: CacheType = CacheType.PER_FRAME
) : ExpressionIntermediate() {

    fun hasDefault() = expression.isNotEmpty()
}

@SerialName("variables")
@Serializable
data class Variables(
    @XmlSerialName("variable")
    val variable: Map<String, NamedExpressionIntermediate>
) {
    init {
        LibHelper.pushContext(variable.mapValues { (_, value) -> value.type })
    }

    companion object {
        val EMPTY = Variables(emptyMap()).also { LibHelper.popContext() }
    }
}
