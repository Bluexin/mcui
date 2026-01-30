package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.LuajExclude
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.themes.elements.access.FragmentReferenceAccess
import be.bluexin.mcui.themes.miniscript.CValue
import org.luaj.vm2.LuaValue

@LuajExpose
data class FragmentReference(
    val renderState: RenderState,
    override val transform: Transform,
    val group: Group,
    @LuajExclude
    val variables: MutableMap<String, CValue<*>>,
) : Element {

    @LuajExclude
    override fun visit(visitor: ElementVisitor, context: ElementVisitor.Context) {
        if (visitor.start(renderState, context)) {
            visitor.transform(transform, context)
            group.visit(visitor, context)
        }
    }

    @LuajExclude
    override fun toLua(): LuaValue = FragmentReferenceAccess(this)

    /**
     * Add or overwrite a variable to the context
     */
    @LuajExpose
    fun setVariable(key: String, variable: CValue<*>) {
        variables[key] = variable
    }

    /**
     * Get a variable from the context
     */
    @LuajExpose
    fun getVariable(key: String): CValue<*>? = variables[key]
}
