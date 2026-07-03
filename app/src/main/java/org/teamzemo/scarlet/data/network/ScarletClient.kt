package org.teamzemo.scarlet.data.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.teamzemo.scarlet.Constants
import org.teamzemo.scarlet.data.api.ScarletApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ScarletClient {
    private var apiInstance: ScarletApi? = null
    var cookieJarInstance: SimpleCookieJar? = null
        private set

    fun getApi(context: Context): ScarletApi {
        if (apiInstance == null) {
            val jar = SimpleCookieJar(context.applicationContext)
            cookieJarInstance = jar

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .cookieJar(jar)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", "ScarletAndroid/1.0")
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiInstance = retrofit.create(ScarletApi::class.java)
        }
        return apiInstance!!
    }
}
