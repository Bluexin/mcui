package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.themes.elements.access.FragmentAccess
import be.bluexin.mcui.themes.miniscript.LibHelper
import be.bluexin.mcui.themes.miniscript.NamedExpressionIntermediate
import be.bluexin.mcui.themes.miniscript.serialization.json.ExpectJsonAdapter
import be.bluexin.mcui.themes.scripting.serialization.DeserializationOrder
import com.google.gson.annotations.JsonAdapter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlBefore
import nl.adaptivity.xmlutil.serialization.XmlNamespaceDeclSpec
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.luaj.vm2.LuaValue

@Serializable
@SerialName("bl:fragment")
@XmlNamespaceDeclSpec("bl=https://www.bluexin.be/com/saomc/saoui/fragment-schema")
@LuajExpose(LuajExpose.IncludeType.OPT_IN)
class Fragment(
    @XmlSerialName("expect")
    @XmlBefore("x", "children", "texture")
    @DeserializationOrder(0)
    val expect: Expect? = null
) : ElementGroupParent(), KoinComponent {
    init {
        if (expect != null) get<LibHelper>().popContext()
    }

    override fun toLua(): LuaValue = FragmentAccess(this)
}

@JsonAdapter(ExpectJsonAdapter::class)
@Serializable
data class Expect(
    val variables: Map<String, NamedExpressionIntermediate> = emptyMap()
) : KoinComponent {
    init {
        get<LibHelper>().pushContext(variables.mapValues { (_, value) -> value.type })
    }
}
