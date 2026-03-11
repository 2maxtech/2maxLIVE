package com.twomax.live.data.remote.api

import com.twomax.live.data.remote.dto.OmdbMovieResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OmdbApi {

    @GET("/")
    suspend fun searchByTitle(
        @Query("apikey") apiKey: String,
        @Query("t") title: String,
        @Query("y") year: String? = null
    ): OmdbMovieResponse
}
