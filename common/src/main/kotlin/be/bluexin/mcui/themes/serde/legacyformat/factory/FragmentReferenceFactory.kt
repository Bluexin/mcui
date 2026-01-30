package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.themes.elements.FragmentReference
import be.bluexin.mcui.themes.miniscript.CValue
import be.bluexin.mcui.themes.miniscript.LibHelper
import be.bluexin.mcui.themes.serde.Factory
import be.bluexin.mcui.themes.serde.Factory.Context.Companion.tryRun
import be.bluexin.mcui.themes.serde.legacyformat.xml.FragmentReferenceXml
import net.minecraft.resources.ResourceLocation
import org.koin.core.annotation.Single

@Single
internal class FragmentReferenceFactory(
    private val libHelper: LibHelper
) : LegacyFactory<FragmentReferenceXml, FragmentReference>(FragmentReferenceXml::class) {

    override fun create(
        input: FragmentReferenceXml,
        context: Factory.Context
    ): Result<FragmentReference> = context.tryRun("fragmentReference[${input.name}]") {
        val renderState = createRenderState(input, context)
        val transform = createTransform(input, context)

        val fragment = input.id.let(::ResourceLocation)
            .let(context::loadFragment)

        if (fragment == null) error("Missing fragment with id ${input.id}")

        val variables = mutableMapOf<String, CValue<*>>()

        input.variables?.values?.forEach { (varName, it) ->
            if (it.hasExpression()) variables[varName] = it.type.expressionAdapter.compile(it)
        }

        val expect = fragment.expect
        val missing = expect.filter { (key, it) ->
            variables[key]?.type != it.type
        }
        libHelper.pushContext(expect.mapValues { (_, it) -> it.type })
        val defaults = missing.onEach { (varName, it) ->
            if (it.hasExpression()) variables[varName] = it.type.expressionAdapter.compile(it)
        }.keys
        libHelper.popContext()
        val realMissing = missing - defaults
        if (realMissing.isNotEmpty()) {
            val present = variables.keys
            error("Missing variables $realMissing for ${input.id} (present : $present)")
        }

        FragmentReference(renderState, transform, fragment.group, variables)
    }
}