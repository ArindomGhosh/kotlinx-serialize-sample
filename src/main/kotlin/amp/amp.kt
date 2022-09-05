package amp

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*


interface AmpMetaData<T> {
    val schema: Schema
    val data: T
}

@kotlinx.serialization.Serializable
data class AmpResponse<T : Any>(
    val data: Amp<T>
) {
    @kotlinx.serialization.Serializable
    data class Amp<T>(
        val amp: AmpAsset<T>
    )
}

@kotlinx.serialization.Serializable
data class AmpAsset<T>(
    override val schema: Schema,
    override val data: T
) : AmpMetaData<T>

@kotlinx.serialization.Serializable
data class Schema(
    val name: String,
    val version: String
)

//====================================FAQS==========================

@kotlinx.serialization.Serializable
data class FAQList(
    val faqList: List<FAQScreens>
)

@kotlinx.serialization.Serializable(with = FAQListSerializer::class)
sealed class FAQScreens {
    @kotlinx.serialization.Serializable
    data class FAQXGroup(
        override val schema: Schema,
        override val data: FAQGroup
    ) : AmpMetaData<FAQXGroup.FAQGroup>, FAQScreens() {
        @kotlinx.serialization.Serializable
        data class FAQGroup(
            val listTitle: String,
            val faqList: List<FAQXList>
        )
    }

    @kotlinx.serialization.Serializable
    data class FAQXList(
        override val schema: Schema,
        override val data: FAQs
    ) : AmpMetaData<FAQXList.FAQs>, FAQScreens() {
        @kotlinx.serialization.Serializable
        data class FAQs(
            val listTitle: String,
            val faqs: List<FAQXs>
        )
    }

    @kotlinx.serialization.Serializable
    data class FAQXs(
        override val schema: Schema,
        override val data: QuestionAnswer
    ) : AmpMetaData<FAQXs.QuestionAnswer>, FAQScreens() {
        @kotlinx.serialization.Serializable
        data class QuestionAnswer(
            val question: String,
            val answer: String
        )
    }
}

object FAQListSerializer : JsonContentPolymorphicSerializer<FAQScreens>(FAQScreens::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out FAQScreens> {
        return when (val schemaName = element.jsonObject["schema"]?.jsonObject?.get("name")?.jsonPrimitive?.content) {
            "xgroup" -> FAQScreens.FAQXGroup.serializer()
            "xlists" -> FAQScreens.FAQXList.serializer()
            "xfaq" -> FAQScreens.FAQXs.serializer()
            else -> error("Unknown $schemaName")
        }
    }

}

fun main() {
    val input = object {}.javaClass.classLoader.getResourceAsStream("sample1.json")?.bufferedReader()?.readText()!!
    val element = Json.parseToJsonElement(input)
    val elementFaqList = element.jsonObject["data"]
        ?.jsonObject?.get("amp")
        ?.jsonObject?.get("data")
        ?.jsonObject?.get("faqList")
        ?.jsonArray
//    println(elementFaqList)
    val faqs = Json.decodeFromString<AmpResponse<FAQList>>(input)
//    println(faqs.data.amp.data.faqList)
    faqs.data.amp.data.faqList.forEach {
        println(it)
        println("+++++++++++++++++")
    }

}



