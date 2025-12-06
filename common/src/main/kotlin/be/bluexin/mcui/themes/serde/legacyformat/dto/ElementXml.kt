package be.bluexin.mcui.themes.serde.legacyformat.dto

import kotlinx.serialization.Serializable

@Serializable
sealed interface ElementXml {

    companion object {
        const val DEFAULT_NAME = "anonymous"
    }

    interface WithRenderState {
        val name: String
        val enabled: AnonymousExpressionIntermediate?
    }

    interface WithTransform {
        val x: AnonymousExpressionIntermediate?
        val y: AnonymousExpressionIntermediate?
        val z: AnonymousExpressionIntermediate?
        val scale: AnonymousExpressionIntermediate?
    }
}
