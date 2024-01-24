package com.maplibr.maplibre.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.maplibr.maplibre.R
import com.maplibr.maplibre.data.ApiService
import com.maplibr.maplibre.data.MarkerRepository
import com.maplibr.maplibre.domain.model.MarkerData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by lazy {
        MainViewModel(MarkerRepository(ApiService.create()))
    }

    private lateinit var mapView: MapView
    private lateinit var symbolManager: SymbolManager
    private var selectedLatLng: LatLng? = null
    private var dialogText: String? = null
    private var isAlertDialogVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            dialogText = savedInstanceState.getString("dialogText")
            isAlertDialogVisible = savedInstanceState.getBoolean("isAlertDialogVisible", false)
            selectedLatLng = savedInstanceState.getParcelable("selectedLatLng")
            if (isAlertDialogVisible) {
                showAlert(dialogText ?: "")
            }
        }
        Mapbox.getInstance(this)
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)
        initMap()
    }

    private fun initMap() {
        mapView.getMapAsync { map ->
            map.setStyle(styleUrl) { style ->
                val drawable = ResourcesCompat.getDrawable(
                    this.resources,
                    R.drawable.baseline_location_pin_24,
                    null
                )
                style.addImage(MARKER_NAME, BitmapUtils.getBitmapFromDrawable(drawable)!!)
                symbolManager = SymbolManager(mapView, map, style)
                symbolManager.iconAllowOverlap = true
                symbolManager.iconIgnorePlacement = true
                symbolManager.addClickListener { clickedSymbol ->
                    val markerInfo = viewModel.getMarkerInfo(clickedSymbol)
                    showAlert(markerInfo)
                    true
                }
                lifecycleScope.launch {
                    viewModel.getMarkersFromServer { markers ->
                        displayMarkersOnMap(markers)
                    }
                }
                // Adding a new point
                map.addOnMapLongClickListener { point ->
                    selectedLatLng = point
                    showAddMarkerDialog()
                    true
                }
            }
        }
    }

    private fun displayMarkersOnMap(markers: List<MarkerData>) {
        mapView.getMapAsync { map ->
            lifecycleScope.launch(Dispatchers.Main) {
                for (marker in markers) {
                    val drawable = ResourcesCompat.getDrawable(
                        this@MainActivity.resources,
                        R.drawable.baseline_location_pin_24,
                        null
                    )
                    val iconId = "${marker.id}_marker_icon"
                    map.style?.addImage(iconId, BitmapUtils.getBitmapFromDrawable(drawable)!!)
                    val symbol = symbolManager.create(
                        SymbolOptions()
                            .withLatLng(LatLng(marker.latitude, marker.longitude))
                            .withIconImage(iconId)
                            .withIconSize(1.25f)
                            .withIconAnchor("bottom")
                            .withData(JsonObject().apply {
                                addProperty("name", marker.name)
                                addProperty("description", marker.description)
                            })
                    )

                    // Update the symbol manager with the new symbol
                    symbolManager.update(symbol)
                }
            }
        }
    }

    private fun showAlert(dialogText: String) {
        this.dialogText = dialogText
        isAlertDialogVisible = true
        val alertDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Информация о маркере")
            .setMessage(dialogText)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                isAlertDialogVisible = false
            }
            .create()
        alertDialog.show()
    }

    private fun showAddMarkerDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_marker, null)
        val nameEditText: EditText = dialogView.findViewById(R.id.editTextName)
        val desEditText: EditText = dialogView.findViewById(R.id.editTextDes)
        if (::symbolManager.isInitialized) {
            val alertDialog = MaterialAlertDialogBuilder(this)
                .setTitle("Добавление новой метки на карте")
                .setView(dialogView)
                .setPositiveButton("Сохранить") { _, _ ->
                    val name = nameEditText.text.toString()
                    val des = desEditText.text.toString()
                    viewModel.saveMarker(name, des, selectedLatLng) { updatedMarkers ->
                        displayMarkersOnMap(updatedMarkers)
                    }
                }
                .setNegativeButton("Отмена") { _, _ ->
                    // Cancel, do nothing
                }
                .create()

            alertDialog.show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("dialogText", dialogText)
        outState.putBoolean("isAlertDialogVisible", isAlertDialogVisible)
    }

    companion object {
        private const val styleId = "jawg-streets"
        private const val accessToken = "pBUt70X7pUZj5b2vihdX5lmwrI7k2yUVBfPmlQtuuQKUeJAhHdybEodGLiA6Ndkt"
        private const val MARKER_NAME = "marker-pin"
        private val styleUrl = "https://api.jawg.io/styles/$styleId.json?access-token=$accessToken"
    }
}