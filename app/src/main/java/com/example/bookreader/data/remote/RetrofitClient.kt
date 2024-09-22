package com.example.bookreader.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class RetrofitClient {
    //https://www.dbooks.org/api/  recent
    companion object {
        private var retrofit: Retrofit? = null
        private const val BASE_URL = "https://www.dbooks.org/api/"
        fun getInstance(): Retrofit {
            if (retrofit == null) {
                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()

                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()
            }
            return retrofit!!
        }
    }
}