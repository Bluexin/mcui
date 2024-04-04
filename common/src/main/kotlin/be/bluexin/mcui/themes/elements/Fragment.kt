package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.themes.util.LibHelper
import be.bluexin.mcui.themes.util.NamedExpressionIntermediate
import be.bluexin.mcui.themes.util.json.ExpectJsonAdapter
import be.bluexin.mcui.util.DeserializationOrder
import com.google.gson.annotations.JsonAdapter
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XmlBefore
import nl.adaptivity.xmlutil.serialization.XmlNamespaceDeclSpec
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@OptIn(ExperimentalXmlUtilApi::class)
@Serializable
@XmlSerialName(
    value = "bl:fragment"
)
@XmlNamespaceDeclSpec("bl=https://www.bluexin.be/com/saomc/saoui/fragment-schema")
@LuajExpose(LuajExpose.IncludeType.OPT_IN)
class Fragment(
    @XmlSerialName("expect")
    @XmlBefore("x", "children", "texture")
    @DeserializationOrder(0)
    val expect: Expect? = null
) : ElementGroupParent() {

    init {
        if (expect != null) LibHelper.popContext()
    }
}

@JsonAdapter(ExpectJsonAdapter::class)
@Serializable
data class Expect(
    val variables: Map<String, NamedExpressionIntermediate> = emptyMap()
) {
    init {
        LibHelper.pushContext(variables.mapValues { (_, value) -> value.type })
    }
}
