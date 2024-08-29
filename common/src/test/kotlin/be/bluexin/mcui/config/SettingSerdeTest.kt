package be.bluexin.mcui.config

import be.bluexin.mcui.Constants
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import net.minecraft.resources.ResourceLocation
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class SettingSerdeTest {
    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        useAlternativeNames = false
        allowTrailingComma = true
        allowComments = true
        prettyPrint = true
    }

    @Suppress("UNCHECKED_CAST") // generic type is actually not needed here
    private val serializer = ListSerializer(Setting.serializer(String.serializer()))
            as KSerializer<List<Setting<*>>>

    @Test
    fun `serialization handles all types`() {
        val settings = listOf(
            StringSetting(
                key = ResourceLocation(Constants.MOD_ID, "key"),
                defaultValue = "default",
                comment = "comment",
                validate = { it.length > 2 }
            ),
            BooleanSetting(
                key = ResourceLocation(Constants.MOD_ID, "key"),
                defaultValue = true,
                comment = "comment",
            ),
            IntSetting(
                key = ResourceLocation(Constants.MOD_ID, "key"),
                defaultValue = 42,
                comment = "comment",
                min = -12,
                max = 420,
                validate = { it % 2 == 0 }
            ),
            DoubleSetting(
                key = ResourceLocation(Constants.MOD_ID, "key"),
                defaultValue = 42.0,
                comment = "comment",
                min = -12.0,
                max = 420.0,
                validate = { it % 2 == 0.0 }
            ),
            ChoiceSetting(
                key = ResourceLocation(Constants.MOD_ID, "key"),
                defaultValue = "hello",
                comment = "comment",
                values = setOf("hello", "world"),
            ),
            ResourceLocationSetting(
                key = ResourceLocation(Constants.MOD_ID, "key"),
                defaultValue = ResourceLocation("test", "default"),
                comment = "comment",
                validate = { it.path.length > 2 },
            ),
        )

        val s = json.encodeToString(serializer, settings)

        assertEquals(
            """
                [
                    {
                        "type": "string",
                        "key": "mcui:key",
                        "defaultValue": "default",
                        "comment": "comment"
                    },
                    {
                        "type": "boolean",
                        "key": "mcui:key",
                        "defaultValue": true,
                        "comment": "comment"
                    },
                    {
                        "type": "int",
                        "key": "mcui:key",
                        "defaultValue": 42,
                        "comment": "comment",
                        "min": -12,
                        "max": 420
                    },
                    {
                        "type": "double",
                        "key": "mcui:key",
                        "defaultValue": 42.0,
                        "comment": "comment",
                        "min": -12.0,
                        "max": 420.0
                    },
                    {
                        "type": "choice",
                        "key": "mcui:key",
                        "defaultValue": "hello",
                        "comment": "comment",
                        "values": [
                            "hello",
                            "world"
                        ]
                    },
                    {
                        "type": "resource_location",
                        "key": "mcui:key",
                        "defaultValue": "test:default",
                        "comment": "comment"
                    }
                ]
            """.trimIndent(),
            s
        )

        val parsed = json.decodeFromString(serializer, s)

        assertContentEquals(settings, parsed)
    }
}