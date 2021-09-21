import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import java.io.FileWriter
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.system.exitProcess

fun check(lat : String, lon : String, apikey : String?){

    if (apikey == null) { println("API Ключ не найден")
        exitProcess(1) }

    if ((lat.toDouble()<-90) || (lat.toDouble()>90)) {
        println("Некорректный формат широты")
        exitProcess(1) }

    if ((lon.toDouble()<0) || (lon.toDouble()>360)) {
        println("Некорректный формат долготы")
        exitProcess(1) }
}

fun getJson(lat: String, lon: String, apikey: String?): String? {
    val res: String?
    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        // .writeTimeout(5, TimeUnit.SECONDS)
        // .readTimeout(5, TimeUnit.SECONDS)
        // .callTimeout(5, TimeUnit.SECONDS)
        .build()
    try {
        val request = Request.Builder()
            .url("https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apikey")
            .build()
        res = client.newCall(request).execute().use { response -> response.body?.string() }
    } catch (e: Exception){
        println("Ошибка в Http-ответе API")
        exitProcess(1)
    }

    return res
}

class MainClass{

    @JsonClass(generateAdapter = true)
    data class MainX(
        @Json(name = "feels_like")
        val feels_Like: Double,
        @Json(name = "grnd_level")
        val grndLevel: Int,
        val humidity: Int,
        val pressure: Int,
        @Json(name = "sea_level")
        val seaLevel: Int,
        val temp: Double,
        @Json(name = "temp_max")
        val tempMax: Double,
        @Json(name = "temp_min")
        val tempMin: Double
    )

    @JsonClass(generateAdapter = true)
    data class Sys(val country: String, val sunrise: Int, val sunset: Int)

    @JsonClass(generateAdapter = true)
    data class Weather(val description: String, val icon: String, val id: Int, val main: String)

    @JsonClass(generateAdapter = true)
    data class Wind(val deg: Int, val gust: Double, val speed: Double)

    @JsonClass(generateAdapter = true)
    data class Clouds(val all: Int)

    @JsonClass(generateAdapter = true)
    data class Coord(val lat: Double, val lon: Double)

    @JsonClass(generateAdapter = true)
    data class CurrentWeather(
        val base: String,
        val clouds: Clouds,
        val cod: Int,
        val coord: Coord,
        val dt: Int,
        val id: Int,
        val main: MainX,
        val name: String,
        val sys: Sys,
        val timezone: Int,
        val visibility: Int,
        val weather: List<Weather>,
        val wind: Wind
    )
}

fun getTemp(json : String): Double? {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val jsonAdapter: JsonAdapter<MainClass.CurrentWeather>? = moshi.adapter(MainClass.CurrentWeather::class.java)
    val obj: MainClass.CurrentWeather? = jsonAdapter?.fromJson(json)
    return obj?.main?.temp
}

fun main(args: Array<String>) {

    if (args.size!=2)  {  println("Неверное число аргументов")
      exitProcess(1) }
    val apikey : String? = System.getenv("WEATHER_API_KEY")
    val lat : String = args[0]
    val lon : String = args[1]

    check(lat, lon, apikey)
    val json: String? = getJson(lat,lon,apikey)
    val temperature: Double? = json?.let { getTemp(it)?.minus(273.15) }
    val temp = temperature?.roundToInt()
    println("Температура получена. Записать в файл? Введите 1 для записи")
    when(readLine()){
        "1" -> { val fileWriter = FileWriter("test.txt", true)
            fileWriter.write("$temp")
            fileWriter.close() }
     else ->  { println(temp)
         exitProcess(0)}
    }
}


