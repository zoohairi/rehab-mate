import com.example.rehabmate.MainActivity
import com.example.rehabmate.R
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class Exercise(
    val name: String,
    val type: String,
    val muscle: String,
    val equipment: String,
    val difficulty: String,
    val instructions: String
)


interface ApiService {
    @GET("exercises")
    fun getExercises(
        @Query("muscle") muscle: String
    ): Call<List<Exercise>>
}

object RetrofitInstance {

    private const val BASE_URL = "https://api.api-ninjas.com/v1/"

    private lateinit var API_KEY: String

    fun initialize(context: MainActivity) {
        API_KEY = context.getString(R.string.exerciseService_API) // Retrieve API Key here
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-Api-Key", API_KEY) // Add API key here
                .build()
            chain.proceed(request)
        }
        .build()

    val api: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Set the custom client with interceptor
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}
