package com.maplibr.maplibre.ui

import androidx.lifecycle.ViewModel
import com.maplibr.maplibre.data.MarkerRepository
import androidx.lifecycle.viewModelScope
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.maplibr.maplibre.domain.model.MarkerData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val repository: MarkerRepository) : ViewModel() {
    suspend fun getMarkersFromServer(callback: (List<MarkerData>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val markers = repository.getMarkers()
            callback(markers)
        }
    }

    fun getMarkerInfo(clickedSymbol: Symbol): String {
        val clickedMarkerName =
            clickedSymbol.data?.getAsJsonObject()?.getAsJsonPrimitive("name")?.asString ?: ""
        val clickedMarkerDescription =
            clickedSymbol.data?.getAsJsonObject()?.getAsJsonPrimitive("description")?.asString ?: ""

        val clickedLatitude = clickedSymbol.latLng?.latitude ?: 0.0
        val clickedLongitude = clickedSymbol.latLng?.longitude ?: 0.0

        return "Название: $clickedMarkerName" +
                "\nОписание: $clickedMarkerDescription" +
                "\nШирота: $clickedLatitude" +
                "\nДолгота: $clickedLongitude"
    }

    fun saveMarker(name: String, des: String, selectedLatLng: LatLng?, callback: (List<MarkerData>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val newMarker = MarkerData(
                id = null,
                name = name,
                description = des,
                latitude = selectedLatLng?.latitude ?: 0.0,
                longitude = selectedLatLng?.longitude ?: 0.0
            )
            repository.addMarker(newMarker)
            val updatedMarkers = repository.getMarkers()
            callback(updatedMarkers)
        }
    }
}


