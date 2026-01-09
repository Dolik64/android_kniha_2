package com.example.kniha_20.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

// --- KOŘENOVÝ OBJEKT ---
@Serializable
data class BookRoot(
    val type: String,
    val options: BookOptions? = null,
    val slots: List<BookComponent> = emptyList()
)

@Serializable
data class BookOptions(
    @SerialName("background_image_url") val backgroundImageUrl: String? = null,
    @SerialName("frontpage_image_url") val frontPageUrl: String? = null,
    @SerialName("backpage_image_url") val backPageUrl: String? = null,
    @SerialName("oddpage_image_url") val oddPageUrl: String? = null,
    @SerialName("evenpage_image_url") val evenPageUrl: String? = null,
    @SerialName("turnleft_image_url") val turnLeftUrl: String? = null,
    @SerialName("turnright_image_url") val turnRightUrl: String? = null
)

// --- HLAVNÍ ROZHRANÍ (Polymorfismus podle pole "type") ---
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed interface BookComponent

// --- 1. DEKORÁTORY (Mají vnořený slot "content") ---

@Serializable
@SerialName("background-decorator")
data class BackgroundDecorator(
    val options: ImageOptions? = null,
    val slots: ContentSlot? = null
) : BookComponent

@Serializable
@SerialName("inset-decorator")
data class InsetDecorator(
    val options: InsetOptions? = null,
    val slots: ContentSlot? = null
) : BookComponent

@Serializable
@SerialName("opacity-decorator")
data class OpacityDecorator(
    val options: OpacityOptions? = null,
    val slots: ContentSlot? = null
) : BookComponent

@Serializable
@SerialName("audio-decorator")
data class AudioDecorator(
    val options: AudioOptions? = null,
    val slots: ContentSlot? = null
) : BookComponent

// --- 2. LAYOUTY (Určují rozložení) ---

@Serializable
@SerialName("grid-layout")
data class GridLayout(
    val options: GridOptions? = null,
    // Zde je "slots" seznam (hranaté závorky v JSONu)
    val slots: List<BookComponent> = emptyList()
) : BookComponent

@Serializable
@SerialName("split-layout")
data class SplitLayout(
    val options: SplitOptions? = null,
    // Zde je "slots" objekt s first/second (složené závorky v JSONu)
    val slots: SplitSlots? = null
) : BookComponent

// --- 3. OBSAHOVÉ PRVKY (Listy stromu) ---

@Serializable
@SerialName("imager")
data class ImagerThing(
    val options: ImageOptions? = null
) : BookComponent

@Serializable
@SerialName("text-thing")
data class TextThing(
    val options: TextOptions? = null
) : BookComponent

@Serializable
@SerialName("video-thing")
data class VideoThing(
    val options: VideoOptions? = null
) : BookComponent

@Serializable
@SerialName("empty")
data class EmptyThing(
    val options: CssOptions? = null
) : BookComponent


// --- POMOCNÉ TŘÍDY PRO STRUKTURU (SLOTS) ---

@Serializable
data class ContentSlot(
    val content: BookComponent
)

@Serializable
data class SplitSlots(
    val first: BookComponent,
    val second: BookComponent
)

// --- POMOCNÉ TŘÍDY PRO NASTAVENÍ (OPTIONS) ---

@Serializable
data class ImageOptions(
    val url: String? = null,
    val image: String? = null,
    val repeat: String? = null,
    val position: String? = null,
    val color: String? = null,
    val size: String? = null
)

@Serializable
data class InsetOptions(
    val top: Double? = 0.0,
    val left: Double? = 0.0,
    val right: Double? = 0.0,
    val bottom: Double? = 0.0
)

@Serializable
data class GridOptions(
    val rows: Int = 1,
    val columns: Int = 1,
    val gap: Int = 0
)

@Serializable
data class SplitOptions(
    val ratio: Int = 50,
    val gap: Int = 0,
    val horizontal: Boolean = false
)

@Serializable
data class TextOptions(
    val html: String = "",
    val css: String? = null,
    @SerialName("h-align") val hAlign: String? = null,
    @SerialName("v-align") val vAlign: String? = null,
    @SerialName("font-size") val fontSize: String? = null,
    @SerialName("font-family") val fontFamily: String? = null
)

@Serializable
data class VideoOptions(
    val uri: String? = null,
    val webm: String? = null,
    val mp4: String? = null,
    val ogg: String? = null,
    val autoplay: Boolean = false,
    val loop: Boolean = false,
    val volume: Int = 100
)

@Serializable
data class AudioOptions(
    val uri: String? = null,
    val ogg: String? = null,
    val mp3: String? = null,
    val autoplay: Boolean = true,
    val loop: Boolean = true,
    val volume: Int = 50
)

@Serializable
data class OpacityOptions(
    val opacity: Float = 1.0f
)

@Serializable
data class CssOptions(
    val css: String? = ""
)