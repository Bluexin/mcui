@file:OptIn(ExperimentalSerializationApi::class)

package be.bluexin.mcui.themes.miniscript

import be.bluexin.mcui.themes.miniscript.serialization.JelType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlOtherAttributes
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

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
) : KoinComponent {
    init {
        get<LibHelper>().pushContext(variable.mapValues { (_, value) -> value.type })
    }

    companion object : KoinComponent {
        val EMPTY = Variables(emptyMap()).also { get<LibHelper>().popContext() }
    }
}
