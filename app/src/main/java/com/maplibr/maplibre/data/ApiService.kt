package com.maplibr.maplibre.data

import com.maplibr.maplibre.domain.model.MarkerData
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("/geojson")
    suspend fun addMarker(@Body markerData: MarkerData): Response<Unit>

    @GET("/geojson")
    suspend fun getMarkers(): List<MarkerData>

    companion object {
        fun create(): ApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://geo.stnpro.online")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}