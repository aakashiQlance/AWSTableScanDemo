package com.aakash.playstoresubscriptionwithautorenewal.retrofit

import android.content.Context
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

object RetrofitBuilder {


    fun getRetrofitClient(ctx: Context, BASE_URl: String): Retrofit {

        return Retrofit.Builder()
            .baseUrl(BASE_URl)
            .client(getSimpleOkHttpClient(ctx))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getSimpleOkHttpClient(ctx: Context): OkHttpClient {
        // Define the OkHttp Client with its cache!
        // Assigning a CacheDirectory
        val myCacheDir = File(ctx.cacheDir, "OkHttpCache")
        // You should create it...
        val cacheSize = 1024 * 1024
        val cacheDir = Cache(myCacheDir, cacheSize.toLong())
        val httpLogInterceptor = HttpLoggingInterceptor()
        httpLogInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder() //add a cache
            .cache(cacheDir)
            .addInterceptor(httpLogInterceptor)
            .build()
    }

}