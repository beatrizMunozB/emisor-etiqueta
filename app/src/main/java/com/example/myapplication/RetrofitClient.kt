package com.example.myapplication


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {



      //private const val BASE_URL = "http://172.16.1.19:3001/"
        private const val BASE_URL = "http://172.16.1.206:3024/"

        private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }



}