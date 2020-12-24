package com.example.flo

import com.example.flo.viewmodel.Song
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

object Retrofit2 {
    private const val BASE_URL =
        "https://grepp-programmers-challenges.s3.ap-northeast-2.amazonaws.com/2020-flo/"

    private fun retrofit(): Retrofit { // Singleton

        return Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // JSON
            .build()
    }

    val SongService: RequestSong by lazy {
        retrofit().create(RequestSong::class.java)
    }
}

    interface RequestSong {
        @GET("song.json")
        fun requestSong(): Call<Song>
    }
