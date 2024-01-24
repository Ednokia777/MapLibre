package com.maplibr.maplibre.data

import android.util.Log
import com.maplibr.maplibre.domain.model.MarkerData

class MarkerRepository(private val apiService: ApiService) {

    suspend fun addMarker(markerData: MarkerData) {
        try {
            val response = apiService.addMarker(markerData)
            if (response.isSuccessful) {
                Log.d("RESPONCE_STATUS", "SUCCESS")
            } else {
                Log.d("RESPONCE_STATUS", response.message())
            }
        } catch (e: Exception) {
            Log.d("RESPONCE_STATUS", e.message.toString())
        }
    }

    suspend fun getMarkers(): List<MarkerData> {
        return try {
            apiService.getMarkers()
        } catch (e: Exception) {
            emptyList()
        }
    }
}