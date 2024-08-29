package be.bluexin.mcui.config

import be.bluexin.luajksp.annotations.LKExposed
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.Constants
import be.bluexin.mcui.config.access.*
import be.bluexin.mcui.themes.miniscript.LKResourceLocation
import be.bluexin.mcui.themes.miniscript.serialization.json.JsonSettingAdapterFactory
import com.google.gson.annotations.JsonAdapter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.resources.ResourceLocation
import org.luaj.vm2.LuaValue
import kotlin.reflect.KProperty

@LuajExpose(LuajExpose.IncludeType.OPT_IN)
@JsonAdapter(JsonSettingAdapterFactory::class)
@Serializable
// TODO : add schema for settings
sealed class Setting<T : Any> : LKExposed {

    @Transient
    @LuajExpose
    var namespace: LKResourceLocation = ResourceLocation(Constants.MOD_ID, "error")

    @LuajExpose
    abstract val key: LKResourceLocation

    abstract val defaultValue: T

    @LuajExpose
    abstract val comment: String?

    @Transient
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

    override fun toString(): String {
        return "${this::class.simpleName}(type=$type, comment=$comment, defaultValue=$defaultValue, key=$key, namespace=$namespace)"
    }
}

@LuajExpose(LuajExpose.IncludeType.OPT_IN)
@Serializable
@SerialName("string")
open class StringSetting(
    override val key: LKResourceLocation,
    override val defaultValue: String,
    override val comment: String? = null,
    @Transient
    private val validate: ((String) -> Boolean)? = null
) : Setting<String>() {
    override fun read(serialized: String) = serialized
    override fun write(value: String) = value
    override fun validate(value: String) = validate?.invoke(value) ?: true
    override fun toLua(): LuaValue = StringSettingAccess(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StringSetting

        if (namespace != other.namespace) return false
        if (key != other.key) return false
        if (defaultValue != other.defaultValue) return false
        if (comment != other.comment) return false

        return true
    }

    override fun hashCode(): Int {
        var result = namespace.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + defaultValue.hashCode()
        result = 31 * result + (comment?.hashCode() ?: 0)
        return result
    }
}

@LuajExpose(LuajExpose.IncludeType.OPT_IN)
@Serializable
@SerialName("boolean")
class BooleanSetting(
    override val key: LKResourceLocation,
    override val defaultValue: Boolean,
    override val comment: String? = null
) : Setting<Boolean>() {
    override val type get() = Property.Type.BOOLEAN

    override fun read(serialized: String) = serialized.toBooleanStrictOrNull()
    override fun write(value: Boolean) = value.toString()
    override fun validate(value: Boolean): Boolean = true
    override fun toLua(): LuaValue = BooleanSettingAccess(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BooleanSetting) return false

        if (namespace != other.namespace) return false
        if (key != other.key) return false
        if (defaultValue != other.defaultValue) return false
        if (comment != other.comment) return false

        return true
    }

    override fun hashCode(): Int {
        var result = namespace.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + defaultValue.hashCode()
        result = 31 * result + (comment?.hashCode() ?: 0)
        return result
    }
}

@LuajExpose(LuajExpose.IncludeType.OPT_IN)
@Serializable
@SerialName("int")
class IntSetting(
    override val key: LKResourceLocation,
    override val defaultValue: Int,
    override val comment: String? = null,
    @LuajExpose
    val min: Int? = null,
    @LuajExpose
    val max: Int? = null,
    @Transient
    private val validate: ((Int) -> Boolean)? = null
) : Setting<Int>() {
    override val type get() = Property.Type.INTEGER

    override fun read(serialized: String): Int? = serialized.toIntOrNull()
    override fun write(value: Int): String = value.toString()
    override fun validate(value: Int): Boolean = validate?.invoke(value) ?: defaultValidate(this, value)
    override fun toLua(): LuaValue = IntSettingAccess(this)

    override fun toString(): String {
        return "IntSetting(min=$min, max=$max) ${super.toString()}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IntSetting) return false

        if (namespace != other.namespace) return false
        if (key != other.key) return false
        if (defaultValue != other.defaultValue) return false
        if (comment != other.comment) return false
        if (min != other.min) return false
        if (max != other.max) return false

        return true
    }

    override fun hashCode(): Int {
        var result = namespace.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + defaultValue
        result = 31 * result + (comment?.hashCode() ?: 0)
        result = 31 * result + (min ?: 0)
        result = 31 * result + (max ?: 0)
        return result
    }


    private companion object {
        fun defaultValidate(setting: IntSetting, newValue: Int) =
            (setting.min == null || newValue >= setting.min) &&
                    (setting.max == null || newValue <= setting.max)
    }
}

@LuajExpose(LuajExpose.IncludeType.OPT_IN)
@Serializable
@SerialName("double")
class DoubleSetting(
    override val key: LKResourceLocation,
    override val defaultValue: Double,
    override val comment: String? = null,
    @LuajExpose
    val min: Double? = null,
    @LuajExpose
    val max: Double? = null,
    @Transient
    private val validate: ((Double) -> Boolean)? = null
) : Setting<Double>() {
    override val type get() = Property.Type.DOUBLE

    override fun read(serialized: String): Double? = serialized.toDoubleOrNull()
    override fun write(value: Double): String = value.toString()
    override fun validate(value: Double): Boolean = validate?.invoke(value) ?: defaultValidate(this, value)
    override fun toLua(): LuaValue = DoubleSettingAccess(this)

    override fun toString(): String {
        return "DoubleSetting(min=$min, max=$max) ${super.toString()}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DoubleSetting) return false

        if (namespace != other.namespace) return false
        if (key != other.key) return false
        if (defaultValue != other.defaultValue) return false
        if (comment != other.comment) return false
        if (min != other.min) return false
        if (max != other.max) return false

        return true
    }

    override fun hashCode(): Int {
        var result = namespace.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + defaultValue.hashCode()
        result = 31 * result + (comment?.hashCode() ?: 0)
        result = 31 * result + (min?.hashCode() ?: 0)
        result = 31 * result + (max?.hashCode() ?: 0)
        return result
    }


    private companion object {
        fun defaultValidate(setting: DoubleSetting, newValue: Double) =
            (setting.min == null || newValue >= setting.min) &&
                    (setting.max == null || newValue <= setting.max)
    }
}

@LuajExpose(LuajExpose.IncludeType.OPT_IN)
@Serializable
@SerialName("choice")
class ChoiceSetting(
    override val key: LKResourceLocation,
    override val defaultValue: String,
    override val comment: String? = null,
    @LuajExpose
    val values: Set<String>
) : Setting<String>() {
    override fun read(serialized: String) = serialized
    override fun write(value: String) = value
    override fun validate(value: String) = value in values

    override fun toLua(): LuaValue = ChoiceSettingAccess(this)

    override fun toString(): String {
        return "ChoiceSetting(values=$values) ${super.toString()}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChoiceSetting) return false

        if (namespace != other.namespace) return false
        if (key != other.key) return false
        if (defaultValue != other.defaultValue) return false
        if (comment != other.comment) return false
        if (values != other.values) return false

        return true
    }

    override fun hashCode(): Int {
        var result = namespace.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + defaultValue.hashCode()
        result = 31 * result + (comment?.hashCode() ?: 0)
        result = 31 * result + values.hashCode()
        return result
    }
}

@LuajExpose(LuajExpose.IncludeType.OPT_IN)
@Serializable
@SerialName("resource_location")
class ResourceLocationSetting(
    override val key: LKResourceLocation,
    override val defaultValue: LKResourceLocation,
    override val comment: String? = null,
    @Transient
    private val validate: ((LKResourceLocation) -> Boolean)? = null
) : Setting<LKResourceLocation>() {
    override fun read(serialized: String) = if (serialized.contains(':')) LKResourceLocation(serialized) else null
    override fun write(value: LKResourceLocation) = value.toString()
    override fun validate(value: LKResourceLocation) = validate?.invoke(value) ?: true
    override fun toLua(): LuaValue = ResourceLocationSettingAccess(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResourceLocationSetting) return false

        if (namespace != other.namespace) return false
        if (key != other.key) return false
        if (defaultValue != other.defaultValue) return false
        if (comment != other.comment) return false

        return true
    }

    override fun hashCode(): Int {
        var result = namespace.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + defaultValue.hashCode()
        result = 31 * result + (comment?.hashCode() ?: 0)
        return result
    }
}
