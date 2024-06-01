package be.bluexin.mcui.config

import be.bluexin.luajksp.annotations.LKExposed
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.config.access.*
import be.bluexin.mcui.themes.miniscript.LKResourceLocation
import be.bluexin.mcui.themes.miniscript.serialization.json.JsonSettingAdapterFactory
import com.google.gson.annotations.JsonAdapter
import net.minecraft.resources.ResourceLocation
import org.luaj.vm2.LuaValue
import kotlin.reflect.KProperty

@LuajExpose(LuajExpose.IncludeType.OPT_IN)
@JsonAdapter(JsonSettingAdapterFactory::class)
sealed class Setting<T : Any>(
    @LuajExpose
    val namespace: LKResourceLocation,
    @LuajExpose
    val key: LKResourceLocation,
    val defaultValue: T,
    @LuajExpose
    val comment: String?
) : LKExposed {
    lateinit var property: Property
        private set

    operator fun component1() = namespace
    operator fun component2() = key
    operator fun component3() = defaultValue
    operator fun component4() = comment
    operator fun component5(): (serialized: String) -> T? = ::read
    operator fun component6(): (value: T) -> String = ::write
    operator fun component7(): (value: T) -> Boolean = ::validate
    operator fun component8() = type

    operator fun getValue(caller: Any, property: KProperty<*>): T = Settings[this]

    operator fun setValue(caller: Any, property: KProperty<*>, value: T) = Settings.set(this, value)

    open val type get() = Property.Type.STRING

    fun register() {
        property = Settings.register(this)
    }

    abstract fun read(serialized: String): T?
    abstract fun write(value: T): String
    abstract fun validate(value: T): Boolean
}

@LuajExpose(LuajExpose.IncludeType.OPT_IN)
open class StringSetting(
    namespace: ResourceLocation,
    key: ResourceLocation,
    defaultValue: String,
    comment: String? = null,
    private val validate: ((String) -> Boolean)? = null
) : Setting<String>(
    namespace, key,
    defaultValue, comment
) {
    override fun read(serialized: String) = serialized
    override fun write(value: String) = value
    override fun validate(value: String) = validate?.invoke(value) ?: true
    override fun toLua(): LuaValue = StringSettingAccess(this)
}

@LuajExpose(LuajExpose.IncludeType.OPT_IN)
class BooleanSetting(
    namespace: ResourceLocation,
    key: ResourceLocation,
    defaultValue: Boolean,
    comment: String? = null
) : Setting<Boolean>(
    namespace, key,
    defaultValue, comment
) {
    override val type get() = Property.Type.BOOLEAN

    override fun read(serialized: String) = serialized.toBooleanStrictOrNull()
    override fun write(value: Boolean) = value.toString()
    override fun validate(value: Boolean): Boolean = true
    override fun toLua(): LuaValue = BooleanSettingAccess(this)
}

@LuajExpose(LuajExpose.IncludeType.OPT_IN)
class IntSetting(
    namespace: ResourceLocation,
    key: ResourceLocation,
    defaultValue: Int,
    comment: String? = null,
    @LuajExpose
    val min: Int? = null,
    @LuajExpose
    val max: Int? = null,
    private val validate: ((Int) -> Boolean)? = null
) : Setting<Int>(
    namespace, key,
    defaultValue, comment
) {
    override val type get() = Property.Type.INTEGER

    override fun read(serialized: String): Int? = serialized.toIntOrNull()
    override fun write(value: Int): String = value.toString()
    override fun validate(value: Int): Boolean = validate?.invoke(value) ?: defaultValidate(this, value)
    override fun toLua(): LuaValue = IntSettingAccess(this)

    private companion object {
        fun defaultValidate(setting: IntSetting, newValue: Int) =
            (setting.min == null || newValue >= setting.min) &&
                    (setting.max == null || newValue <= setting.max)
    }
}

@LuajExpose(LuajExpose.IncludeType.OPT_IN)
class DoubleSetting(
    namespace: ResourceLocation,
    key: ResourceLocation,
    defaultValue: Double,
    comment: String? = null,
    @LuajExpose
    val min: Double? = null,
    @LuajExpose
    val max: Double? = null,
    private val validate: ((Double) -> Boolean)? = null
) : Setting<Double>(
    namespace, key,
    defaultValue, comment
) {
    override val type get() = Property.Type.DOUBLE

    override fun read(serialized: String): Double? = serialized.toDoubleOrNull()
    override fun write(value: Double): String = value.toString()
    override fun validate(value: Double): Boolean = validate?.invoke(value) ?: defaultValidate(this, value)
    override fun toLua(): LuaValue = DoubleSettingAccess(this)

    private companion object {
        fun defaultValidate(setting: DoubleSetting, newValue: Double) =
            (setting.min == null || newValue >= setting.min) &&
                    (setting.max == null || newValue <= setting.max)
    }
}

@LuajExpose(LuajExpose.IncludeType.OPT_IN)
class ChoiceSetting(
    namespace: ResourceLocation,
    key: ResourceLocation,
    defaultValue: String,
    comment: String? = null,
    @LuajExpose
    val values: Set<String>
) : StringSetting(
    namespace, key,
    defaultValue, comment,
    values::contains
) {
    override fun toLua(): LuaValue = ChoiceSettingAccess(this)
}

@LuajExpose(LuajExpose.IncludeType.OPT_IN)
class ResourceLocationSetting(
    namespace: ResourceLocation,
    key: ResourceLocation,
    defaultValue: ResourceLocation,
    comment: String? = null,
    private val validate: ((ResourceLocation) -> Boolean)? = null
) : Setting<ResourceLocation>(
    namespace, key,
    defaultValue, comment
) {
    override fun read(serialized: String) = if (serialized.contains(':')) ResourceLocation(serialized) else null
    override fun write(value: ResourceLocation) = value.toString()
    override fun validate(value: ResourceLocation) = validate?.invoke(value) ?: true
    override fun toLua(): LuaValue = ResourceLocationSettingAccess(this)
}
