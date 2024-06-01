package be.bluexin.mcui.themes.miniscript

import be.bluexin.luajksp.annotations.LKMapper
import be.bluexin.luajksp.annotations.LuajMapped
import be.bluexin.mcui.themes.miniscript.serialization.*
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.HumanoidArm
import org.luaj.vm2.LuaValue

object CacheTypeMapper : LKMapper<CacheType> {
    override fun fromLua(value: LuaValue) = CacheType(value.checkjstring())
        ?: argError(1, "Invalid CacheType value: $value")

    override fun toLua(value: CacheType): LuaValue = LuaValue.valueOf(value.name)
}

sealed class CValueMapper<CValueType : CValue<T>, T : Any>(
    private val expressionAdapter: BasicExpressionAdapter<CValueType, T>
) : LKMapper<CValueType> {
    override fun fromLua(value: LuaValue): CValueType = when {
        value.isnumber() -> expressionAdapter.compile(AnonymousExpressionIntermediate(value.checkjstring(), cacheType = CacheType.STATIC))
        value.isstring() -> expressionAdapter.compile(AnonymousExpressionIntermediate(value.checkjstring()))
        value.istable() -> {
            val expression = value["expression"].checkjstring()
            val cacheType = if (value["cache"].isnil()) null else CacheTypeMapper.fromLua(value["cache"])

            expressionAdapter.compile(
                if (cacheType == null) AnonymousExpressionIntermediate(expression)
                else AnonymousExpressionIntermediate(expression, cacheType)
            )
        }
        else -> typesafeArgError(1, "Expected number, string or table - found $value")
    }

    override fun toLua(value: CValueType): LuaValue = LuaValue.tableOf(arrayOf(
        LuaValue.valueOf("expression"), LuaValue.valueOf(value.expression!!),
        LuaValue.valueOf("cache"), LuaValue.valueOf(value.expressionIntermediate!!.cacheType.toString()),
    )).apply {
        val ei = value.value.expressionIntermediate
        if (ei is NamedExpressionIntermediate) {
            set("type", ei.type.toString())
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
        val ei = value.value.expressionIntermediate as NamedExpressionIntermediate
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
data object CIntMapper : CValueMapper<CInt, Int>(IntExpressionAdapter) {
    override fun typesafeArgError(arg: Int, message: String) = argError(arg, message)
}

/**
 * Maps an expression that should return a double.
 */
data object CDoubleMapper : CValueMapper<CDouble, Double>(DoubleExpressionAdapter) {
    override fun typesafeArgError(arg: Int, message: String) = argError(arg, message)
}

/**
 * Maps an expression that should return a String.
 */
data object CStringMapper : CValueMapper<CString, String>(StringExpressionAdapter) {
    override fun typesafeArgError(arg: Int, message: String) = argError(arg, message)
}

/**
 * Maps an expression that should return a boolean.
 */
data object CBooleanMapper : CValueMapper<CBoolean, Boolean>(BooleanExpressionAdapter) {
    override fun typesafeArgError(arg: Int, message: String) = argError(arg, message)
}

/**
 * Maps an expression that should return [Unit] (aka void).
 */
data object CUnitMapper : CValueMapper<CUnit, Unit>(UnitExpressionAdapter) {
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

@LuajMapped(ResourceLocationMapper::class)
typealias LKResourceLocation = ResourceLocation

object HumanoidArmMapper : LKMapper<HumanoidArm> {
    override fun fromLua(value: LuaValue): HumanoidArm = when {
        value.isint() -> HumanoidArm.entries.find { it.id == value.checkint() }
            ?: argError(1, "Unknown id: $value")

        value.isstring() -> HumanoidArm.valueOf(value.checkjstring())
        else -> argError(1, "Expected id (int) or name (string) - found $value")
    }

    override fun toLua(value: HumanoidArm): LuaValue = LuaValue.valueOf(value.name)
}

@Suppress("UnusedReceiverParameter") // Used to get T
private inline fun <reified T : Any> LKMapper<T>.argError(arg: Int, message: String): Nothing {
    LuaValue.argerror(arg, "Couldn't read ${T::class.simpleName}: $message")
    error("Never reach here, argerror throws")
}
