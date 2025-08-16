package com.example.islamic

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*

interface LocationResultListener {
    fun onLocationResult(location: Location)
    fun onLocationFailed(reason: String)
}

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // --- بداية الإضافات والتعديلات ---

    /**
     * دالة جديدة ومهمة: لجلب آخر موقع معروف للجهاز بشكل فوري.
     * هذه الدالة لا تنتظر تحديثاً جديداً من GPS، بل تعيد الموقع المحفوظ في ذاكرة الجهاز.
     * @param listener كول باك لإرجاع الموقع عند العثور عليه.
     */
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(listener: (Location?) -> Unit) {
        if (!hasLocationPermission()) {
            listener(null)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // قد يكون الموقع null في حال كانت هذه المرة الأولى التي يفتح فيها التطبيق
                listener(location)
            }
            .addOnFailureListener {
                // في حالة الفشل، أرجع null
                listener(null)
            }
    }

    /**
     * دالة مساعدة للتحقق من وجود صلاحيات الموقع لتجنب تكرار الكود.
     */
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // --- نهاية الإضافات والتعديلات ---


    private fun isGooglePlayServicesAvailable(): Boolean {
        return try {
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
            resultCode == ConnectionResult.SUCCESS
        } catch (e: Exception) {
            false
        }
    }

    /**
     * هذه الدالة تبقى كما هي، ولكنها الآن تُستخدم لجلب تحديث دقيق للموقع في الخلفية.
     */
    fun requestSingleLocationUpdate(listener: LocationResultListener) {
        if (!hasLocationPermission()) { // استخدام الدالة المساعدة الجديدة
            listener.onLocationFailed("Location permission not granted.")
            return
        }

        if (isGooglePlayServicesAvailable()) {
            getLocationFromGoogleServices(listener)
        } else {
            getLocationFromAndroidFramework(listener)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocationFromGoogleServices(listener: LocationResultListener) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 1000
            numUpdates = 1 // تأكد من أنها تطلب تحديثاً واحداً فقط
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)
                locationResult.lastLocation?.let {
                    listener.onLocationResult(it)
                } ?: listener.onLocationFailed("Failed to get location from Google Services.")
            }
        }, Looper.getMainLooper())
    }

    private fun getLocationFromAndroidFramework(listener: LocationResultListener) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsProviderEnabled && !isNetworkProviderEnabled) {
            listener.onLocationFailed("No location provider is enabled.")
            return
        }

        val provider = if (isGpsProviderEnabled) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER

        try {
            locationManager.requestSingleUpdate(provider, { location ->
                listener.onLocationResult(location)
            }, Looper.getMainLooper())
        } catch(e: SecurityException) {
            listener.onLocationFailed("Location permission not granted.")
        }
    }
}