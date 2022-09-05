import kotlinx.coroutines.*

fun main() {
    val job = Job()
    val coroutineScope = SupervisorJob(job)

    runBlocking{
//        delay(10000)
//        println("main function")
//        launch {
//            job1()
//        }
//        launch {
//            job2()
//        }
//        job3()
//        job4()
        println("start1")
        launch {
            println("launch1")
        }
        delay(1000)
        println("end1")
    }
}

suspend fun job1(){
    delay(5000)
    println("job1")
}
suspend fun job2(){
    delay(4000)
    println("job2")
}
suspend fun job3(){
    delay(3000)
    println("job3")
}
suspend fun job4(){
    delay(2000)
    println("job4")
}