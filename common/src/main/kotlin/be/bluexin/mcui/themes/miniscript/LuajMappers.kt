package be.bluexin.mcui.themes.miniscript

import be.bluexin.luajksp.annotations.LKMapper
import be.bluexin.luajksp.annotations.LuajMapped
import be.bluexin.mcui.themes.miniscript.serialization.*
import be.bluexin.mcui.themes.serde.legacyformat.xml.AnonymousExpressionIntermediate
import be.bluexin.mcui.themes.serde.legacyformat.xml.ExpressionIntermediate
import be.bluexin.mcui.themes.serde.legacyformat.xml.NamedExpressionIntermediate
import be.bluexin.mcui.util.RLSerializer
import kotlinx.serialization.Serializable
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.HumanoidArm
import org.luaj.vm2.LuaValue

object CacheTypeMapper : LKMapper<CacheType> {
    override fun fromLua(value: LuaValue) = CacheType(value.checkjstring())
        ?: argError(1, "Invalid CacheType value: $value")

    override fun toLua(value: CacheType): LuaValue = LuaValue.valueOf(value.name)
}

sealed class CValueMapper<CValueType : CValue<T>, T : Any>(
    private val compile: (ExpressionIntermediate) -> CValueType
) : LKMapper<CValueType> {
    override fun fromLua(value: LuaValue): CValueType = when {
        value.isboolean() || value.isnumber() -> compile(
            AnonymousExpressionIntermediate(
                expression = value.tojstring(),
                cacheType = CacheType.STATIC
            )
        )

        value.isstring() -> compile(AnonymousExpressionIntermediate(value.checkjstring()))
        value.istable() -> {
            val expression = value["expression"].checkjstring()
            val cacheType = if (value["cache"].isnil()) null else CacheTypeMapper.fromLua(value["cache"])

            compile(
                if (cacheType == null) AnonymousExpressionIntermediate(expression)
                else AnonymousExpressionIntermediate(expression, cacheType)
            )
        }
        else -> typesafeArgError(1, "Expected number, string or table - found $value")
    }

    override fun toLua(value: CValueType): LuaValue = when (val ei = value.value.expressionIntermediate) {
        null -> LuaValue.NIL
        else -> LuaValue.tableOf(
            arrayOf(
                LuaValue.valueOf("expression"), LuaValue.valueOf(ei.expression),
                LuaValue.valueOf("cache"), LuaValue.valueOf(ei.cacheType.toString()),
                //        LuaValue.valueOf("value"), TODO(),
            )
        ).apply {
            if (ei is NamedExpressionIntermediate) {
                set("type", ei.type.toString())
            }
        }
    }

    protected abstract fun typesafeArgError(arg: Int, message: String): Nothing
}

data object UnknownCValueMapper: LKMapper<CValue<*>> {

    override fun fromLua(value: LuaValue): CValue<*> = when {
        value.istable() -> {
            val expression = value["expression"].checkjstring()
            val cacheType = if (value["cache"].isnil()) null else CacheTypeMapper.fromLua(value["cache"])
            val type = value["type"].checkjstring()?.let(JelType::valueOf) ?: JelType.ERROR

            type.expressionAdapter.compile(
                if (cacheType == null) NamedExpressionIntermediate(type, expression)
                else NamedExpressionIntermediate(type, expression, cacheType)
            )
        }

        else -> argError(1, "Expected table - found $value")
    }

    override fun toLua(value: CValue<*>): LuaValue {
        val ei = value.value.expressionIntermediate as? NamedExpressionIntermediate
            ?: return LuaValue.NIL
        return LuaValue.tableOf(
            arrayOf(
                LuaValue.valueOf("expression"), LuaValue.valueOf(ei.expression),
                LuaValue.valueOf("cache"), LuaValue.valueOf(ei.cacheType.toString()),
                LuaValue.valueOf("type"), LuaValue.valueOf(ei.type.name),
            ))
    }
}

/**
 * Maps an expression that should return an int.
 */
data object CIntMapper : CValueMapper<CInt, Int>(IntExpressionAdapter::compile) {
    override fun typesafeArgError(arg: Int, message: String) = argError(arg, message)
}

/**
 * Maps an expression that should return a double.
 */
data object CDoubleMapper : CValueMapper<CDouble, Double>(DoubleExpressionAdapter::compile) {
    override fun typesafeArgError(arg: Int, message: String) = argError(arg, message)
}

/**
 * Maps an expression that should return a String.
 */
data object CStringMapper : CValueMapper<CString, String>(StringExpressionAdapter::compile) {
    override fun typesafeArgError(arg: Int, message: String) = argError(arg, message)
}

data object CResourceLocationMapper : CValueMapper<CResourceLocation, LKResourceLocation>(
    { CResourceLocation(StringExpressionAdapter.compile(it)) }
) {
    override fun typesafeArgError(arg: Int, message: String) = argError(arg, message)
}

/**
 * Maps an expression that should return a boolean.
 */
data object CBooleanMapper : CValueMapper<CBoolean, Boolean>(BooleanExpressionAdapter::compile) {
    override fun typesafeArgError(arg: Int, message: String) = argError(arg, message)
}

/**
 * Maps an expression that should return [Unit] (aka void).
 */
data object CUnitMapper : CValueMapper<CUnit, Unit>(UnitExpressionAdapter::compile) {
    override fun typesafeArgError(arg: Int, message: String) = argError(arg, message)
}

object ResourceLocationMapper : LKMapper<ResourceLocation> {
    override fun fromLua(value: LuaValue): ResourceLocation = when {
        value.isstring() -> ResourceLocation(value.checkjstring())
        value.istable() -> ResourceLocation(
            value["namespace"].checkjstring(),
            value["path"].checkjstring()
        )

        else -> argError(1, "Expected string or table - found $value")
    }

    override fun toLua(value: ResourceLocation): LuaValue = LuaValue.tableOf(
        arrayOf(
            LuaValue.valueOf("namespace"), LuaValue.valueOf(value.namespace),
            LuaValue.valueOf("path"), LuaValue.valueOf(value.path),
            LuaValue.valueOf("string"), LuaValue.valueOf(value.toString())
        )
    )
}

@LuajMapped(ResourceLocationMapper::class, import = "support")
typealias LKResourceLocation = @Serializable(RLSerializer::class) ResourceLocation

object HumanoidArmMapper : LKMapper<HumanoidArm> {
    override fun fromLua(value: LuaValue): HumanoidArm = when {
        value.isint() -> HumanoidArm.entries.find { it.id == value.checkint() }
            ?: argError(1, "Unknown id: $value")

        value.isstring() -> HumanoidArm.valueOf(value.checkjstring())
        else -> argError(1, "Expected id (int) or name (string) - found $value")
    }

    override fun toLua(value: HumanoidArm): LuaValue = LuaValue.valueOf(value.name)
}

private inline fun <reified T : Any> LKMapper<T>.argError(arg: Int, message: String): Nothing {
    LuaValue.argerror(arg, "Couldn't read ${T::class.simpleName}: $message")
    error("Never reach here, argerror throws")
}
