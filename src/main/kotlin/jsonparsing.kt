import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.element

@Serializable
data class NewProject(val name: String/* val owner: String*/)

val formatter = Json {
    allowStructuredMapKeys = true
    allowSpecialFloatingPointValues = true
}

@Serializable
data class DataDoub(
    val value: Double
)

fun main() {
    val map = mapOf(
        NewProject("kotlinx.serialization"/*, "Arin"*/) to "Serialization",
        NewProject("kotlinx.coroutines"/*, "Arindom"*/) to "Coroutines"
    )
    println(formatter.encodeToString(map))

    val input = """
        {"value":NaN}
    """.trimIndent()

    println(formatter.decodeFromString<DataDoub>(input))

    val success = """
        {"name":"kotlinx.serialization"}
    """.trimIndent()
    println(Json.decodeFromString<Responses<NewProject>>(success))
    val err = """
        {"error":"Not found"}
    """.trimIndent()
    println(Json.decodeFromString<Responses<NewProject>>(err))

    println(Responses.serializer(NewProject.serializer()).descriptor)
}

@Serializable(with = ResponseSerializer::class)
sealed class Responses<out T> {
    data class Ok<out T>(val data: T) : Responses<T>()
    data class Error(val message: String) : Responses<Nothing>()
}

class ResponseSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<Responses<T>> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildSerialDescriptor("Response", PolymorphicKind.SEALED) {
        element("Ok", buildClassSerialDescriptor("Ok") {
            element<String>("message")
        })
        element("Error", dataSerializer.descriptor)
    }

    override fun deserialize(decoder: Decoder): Responses<T> {
        // Decoder -> JsonDecoder
        require(decoder is JsonDecoder) // this class can be decoded only by Json
        // JsonDecoder -> JsonElement
        val element = decoder.decodeJsonElement()
        // JsonElement -> value
        if (element is JsonObject && "error" in element)
            return Responses.Error(element["error"]!!.jsonPrimitive.content)
        return Responses.Ok(decoder.json.decodeFromJsonElement(dataSerializer, element))
    }

    override fun serialize(encoder: Encoder, value: Responses<T>) {
        // Encoder -> JsonEncoder
        require(encoder is JsonEncoder) // This class can be encoded only by Json
        // value -> JsonElement
        val element = when (value) {
            is Responses.Ok -> encoder.json.encodeToJsonElement(dataSerializer, value.data)
            is Responses.Error -> buildJsonObject { put("error", value.message) }
        }
        // JsonElement -> JsonEncoder
        encoder.encodeJsonElement(element)
    }
}


