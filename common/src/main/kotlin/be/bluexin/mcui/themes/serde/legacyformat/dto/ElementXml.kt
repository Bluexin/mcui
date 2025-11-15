package be.bluexin.mcui.themes.serde.legacyformat.dto

import kotlinx.serialization.Serializable

@Serializable
sealed interface ElementXml {

    companion object {
        const val DEFAULT_NAME = "anonymous"
    }
}
