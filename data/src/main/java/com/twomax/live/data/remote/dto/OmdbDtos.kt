package com.twomax.live.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OmdbMovieResponse(
    @Json(name = "Response") val response: String? = null,
    @Json(name = "Title") val title: String? = null,
    @Json(name = "Year") val year: String? = null,
    @Json(name = "Rated") val rated: String? = null,
    @Json(name = "Released") val released: String? = null,
    @Json(name = "Runtime") val runtime: String? = null,
    @Json(name = "Genre") val genre: String? = null,
    @Json(name = "Director") val director: String? = null,
    @Json(name = "Writer") val writer: String? = null,
    @Json(name = "Actors") val actors: String? = null,
    @Json(name = "Plot") val plot: String? = null,
    @Json(name = "Poster") val poster: String? = null,
    @Json(name = "imdbRating") val imdbRating: String? = null,
    @Json(name = "imdbID") val imdbId: String? = null,
    @Json(name = "Ratings") val ratings: List<OmdbRating>? = null
)

@JsonClass(generateAdapter = true)
data class OmdbRating(
    @Json(name = "Source") val source: String? = null,
    @Json(name = "Value") val value: String? = null
)
