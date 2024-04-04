package be.bluexin.mcui.themes.util

import be.bluexin.luajksp.annotations.LKMapper
import be.bluexin.mcui.themes.util.typeadapters.*
import org.luaj.vm2.LuaValue

object CacheTypeMapper : LKMapper<CacheType> {
    override fun fromLua(value: LuaValue) = CacheType(value.checkjstring())
        ?: throw IllegalArgumentException("Invalid CacheType value: $value")

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
        else -> throw IllegalArgumentException("Unhandled type of $value")
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

        else -> throw IllegalArgumentException("Unhandled type of $value")
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
data object CIntMapper : CValueMapper<CInt, Int>(IntExpressionAdapter)

/**
 * Maps an expression that should return a double.
 */
data object CDoubleMapper : CValueMapper<CDouble, Double>(DoubleExpressionAdapter)

/**
 * Maps an expression that should return a String.
 */
data object CStringMapper : CValueMapper<CString, String>(StringExpressionAdapter)

/**
 * Maps an expression that should return a boolean.
 */
data object CBooleanMapper : CValueMapper<CBoolean, Boolean>(BooleanExpressionAdapter)

/**
 * Maps an expression that should return [Unit] (aka void).
 */
data object CUnitMapper : CValueMapper<CUnit, Unit>(UnitExpressionAdapter)
