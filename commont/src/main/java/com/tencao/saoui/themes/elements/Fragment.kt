package com.tencao.saoui.themes.elements

import com.google.gson.annotations.JsonAdapter
import com.tencao.saoui.Constants
import com.tencao.saoui.themes.util.AfterUnmarshal
import com.tencao.saoui.themes.util.LibHelper
import com.tencao.saoui.themes.util.NamedExpressionIntermediate
import com.tencao.saoui.themes.util.json.ExpectJsonAdapter
import jakarta.xml.bind.Unmarshaller
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlRootElement

@XmlRootElement(namespace = "http://www.bluexin.be/com/saomc/saoui/fragment-schema")
class Fragment @JvmOverloads constructor(
    val expect: Expect? = null
) : ElementGroup(), AfterUnmarshal {

    override fun afterUnmarshal(um: Unmarshaller?, parent: Any?) {
        Constants.LOGGER.info("afterUnmarshal in $name of $parent with $expect")
        if (expect != null) LibHelper.popContext()
    }
}

@JsonAdapter(ExpectJsonAdapter::class)
data class Expect @JvmOverloads constructor(
    @field:XmlElement(name = "variable")
    val variables: List<NamedExpressionIntermediate> = mutableListOf()
) : AfterUnmarshal {
    override fun toString(): String {
        return "Expect(variable=$variables)"
    }

    override fun afterUnmarshal(um: Unmarshaller?, parent: Any?) {
        Constants.LOGGER.info("afterUnmarshal in $this of $parent")
        LibHelper.pushContext(variables.associate { it.key to it.type })
    }
}
