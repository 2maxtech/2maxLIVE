package com.twomax.live.data.remote.api

import com.twomax.live.data.remote.dto.DeviceResponse
import com.twomax.live.data.remote.dto.DeviceStatusResponse
import com.twomax.live.data.remote.dto.PlaylistResponse
import com.twomax.live.data.remote.dto.RegisterDeviceRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ActivationApi {
    @POST("api/device/register")
    suspend fun registerDevice(@Body body: RegisterDeviceRequest): DeviceResponse

    @GET("api/device/status")
    suspend fun checkStatus(@Query("mac") mac: String): DeviceStatusResponse

    @GET("api/playlist")
    suspend fun getPlaylists(@Query("mac") mac: String): List<PlaylistResponse>
}
