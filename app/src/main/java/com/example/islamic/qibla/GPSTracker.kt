package com.example.islamic.qibla

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log

class GPSTracker(private val locationManager: LocationManager) : Service(), LocationListener {


    private var isGPSEnabled = false


    private var isNetworkEnabled = false


    private var canGetLocation = false
    private var location: Location? = null
        set(value) {
            latitude = value?.latitude ?: 0.0
            longitude = value?.longitude ?: 0.0
            field = value
        }
    var latitude = 0.0
    var longitude = 0.0

    init {
        fetchLocation()
    }

    @SuppressLint("MissingPermission")
    fun fetchLocation(): Location? {
        try {

            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGPSEnabled && !isNetworkEnabled) {

            } else {
                canGetLocation = true
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                    )
                    Log.d("Network", "Network")
                    location =
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }

                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                        )
                        Log.d("GPS Enabled", "GPS Enabled")
                        location =
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return location
    }

    fun stopUsingGPS() {
        locationManager.removeUpdates(this@GPSTracker)
    }


    fun canGetLocation(): Boolean {
        return canGetLocation
    }

    override fun onLocationChanged(location: Location) = Unit

    override fun onProviderDisabled(provider: String) = Unit

    override fun onProviderEnabled(provider: String) = Unit

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) = Unit

    override fun onBind(intent: Intent): IBinder? = null

    companion object {

        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 100 // 10 meters


        private const val MIN_TIME_BW_UPDATES = (1000 * 60 * 1).toLong() // 1 minute
    }
}