import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

interface Project {
    val name: String
}

/*@Serializable
abstract class Project {
    abstract val name: String
}*/

@Serializable
abstract class Response<out T> {
    abstract val data: T
}

//@Serializable
//@SerialName("OkResponse")
//open class OkResponse<out T>(open val data: T) : Response<T>()


@Serializable
abstract class Response2<out T>

@Serializable
@SerialName("OkResponse")
data class OkResponse<out T>(val data: T) : Response2<T>()

@Serializable
@SerialName("movies")
data class Movies(override val data: List<String>) : Response<List<String>>()


@Serializable
@SerialName("movie")
data class Movie(override val data: MovieDetails) : Response<MovieDetails>()

@Serializable
@SerialName("moviedetails")
data class MovieDetails(
    val name: String,
    val ratings: String
)

val projectModule = SerializersModule {
    polymorphic(Project::class) {
        subclass(OwnedProject::class)
        defaultDeserializer {
            BasicProject.serializer()
        }
    }
}

val responseModule = SerializersModule {
    /* polymorphic(Response::class){
         subclass(Movies::class)
         subclass(Movie::class)
     }*/

    polymorphic(Response2::class) {
        subclass(OkResponse.serializer(PolymorphicSerializer(Any::class)))
    }
}

val format = Json {
    serializersModule = projectModule + responseModule
}

@Serializable
class Data(val project: Project) // Project is an interface

@Serializable
@SerialName("owned")
class OwnedProject(override val name: String, val owner: String) : Project

@Serializable
data class BasicProject(override val name: String, val type: String) : Project


fun main() {
    /* val movies = Movies(listOf("Titanic", "Lagaan", "qurious case of Benjamin button", "Vendata"))
    println(format.encodeToString(movies))

    val movie = Movie(MovieDetails(
        name = "Titanic",
        ratings = "10"
    ))

    println(format.encodeToString(movie))
    */

    val data = Data(OwnedProject("kotlinx.coroutines", "kotlin"))
    println(format.encodeToString(data))



    val movie2 = OkResponse<MovieDetails>(MovieDetails("Lagaan", "10"))
    println(format.encodeToString(movie2))

}
