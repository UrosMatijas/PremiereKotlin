package rs.edu.raf.premiereuros.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.Serializable

@Serializable(with = ConfigResponseDtoSerializer::class)
data class ConfigResponseDto(
    val value: List<ConfigEntryDto> = emptyList()
)

@Serializable
data class ConfigEntryDto(
    val key: String,
    val value: String
)

private object ConfigResponseDtoSerializer : KSerializer<ConfigResponseDto> {
    private val entriesSerializer = ListSerializer(ConfigEntryDto.serializer())

    override val descriptor = buildClassSerialDescriptor("ConfigResponseDto") {
        element("value", entriesSerializer.descriptor, isOptional = true)
    }

    override fun deserialize(decoder: Decoder): ConfigResponseDto {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("ConfigResponseDto can only be decoded from JSON")
        val element = jsonDecoder.decodeJsonElement()
        val entriesElement = when (element) {
            is JsonArray -> element
            is JsonObject -> element["value"] ?: JsonArray(emptyList())
            else -> JsonArray(emptyList())
        }

        return ConfigResponseDto(
            value = entriesElement.jsonArray.mapNotNull { entry ->
                jsonDecoder.json.decodeConfigEntry(entry)
            }
        )
    }

    override fun serialize(encoder: Encoder, value: ConfigResponseDto) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("ConfigResponseDto can only be encoded to JSON")
        val entries = jsonEncoder.json.encodeToJsonElement(entriesSerializer, value.value)
        jsonEncoder.encodeJsonElement(JsonObject(mapOf("value" to entries)))
    }

    private fun kotlinx.serialization.json.Json.decodeConfigEntry(
        element: JsonElement
    ): ConfigEntryDto? {
        return runCatching {
            decodeFromJsonElement<ConfigEntryDto>(element)
        }.getOrNull()
    }
}
