package com.eslamdev.islamic.qibla

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.eslamdev.islamic.LocationHelper
import com.eslamdev.islamic.LocationResultListener
import com.eslamdev.islamic.R
import com.eslamdev.islamic.databinding.FragmentCompassBinding
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class CompassFragment : Fragment() {

    private var _binding: FragmentCompassBinding? = null
    private val binding get() = _binding!!

    private var compass: Compass? = null
    private var currentAzimuth: Float = 0f
    private lateinit var prefs: SharedPreferences
    private lateinit var locationHelper: LocationHelper
    private var userLocation: Location? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCompassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = requireContext().getSharedPreferences("qibla_prefs", Context.MODE_PRIVATE)
        locationHelper = LocationHelper(requireContext())
        setupCompass()
        binding.btnGps.setOnClickListener { checkPermissionsAndFetchLocation() }
    }

    override fun onResume() {
        super.onResume()
        compass?.start()
        checkPermissionsAndFetchLocation()
    }

    override fun onPause() {
        super.onPause()
        compass?.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupCompass() {
        val sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        compass = Compass(sensorManager)
        compass?.setListener(object : Compass.CompassListener {
            override fun onNewAzimuth(azimuth: Float) {
                adjustUI(azimuth)
            }
        })
    }

    private fun checkPermissionsAndFetchLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        } else {
            fetchLocation()
        }
    }

    private fun fetchLocation() {
        binding.textKaabaDir.text = getString(R.string.getting_location)
        locationHelper.requestSingleLocationUpdate(object : LocationResultListener {
            override fun onLocationResult(location: Location) {
                userLocation = location
                val qiblaDirection = calculateQiblaDirection(location)
                saveFloat(qiblaDirection)
                binding.textKaabaDir.text = getString(R.string.qibla_direction, qiblaDirection)
                binding.textCurrentLoc.text = getString(R.string.your_location, location.latitude, location.longitude)
                binding.imgQiblaArrow.visibility = View.VISIBLE
            }

            override fun onLocationFailed(reason: String) {
                binding.textKaabaDir.text = getString(R.string.location_fetch_failed)
                Toast.makeText(context, reason, Toast.LENGTH_SHORT).show()
                if (reason.contains("provider", true)) {
                    showSettingsAlert()
                }
            }
        })
    }

    private fun adjustUI(azimuth: Float) {
        val qiblaDirection = retrieveFloat()

        val compassAnimation = RotateAnimation(-currentAzimuth, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        compassAnimation.duration = 500
        compassAnimation.repeatCount = 0
        compassAnimation.fillAfter = true
        binding.imgCompass.startAnimation(compassAnimation)

        val qiblaAnimation = RotateAnimation(-currentAzimuth + qiblaDirection, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        qiblaAnimation.duration = 500
        qiblaAnimation.repeatCount = 0
        qiblaAnimation.fillAfter = true
        binding.imgQiblaArrow.startAnimation(qiblaAnimation)

        currentAzimuth = azimuth
    }

    private fun calculateQiblaDirection(location: Location): Float {
        val userLatRad = Math.toRadians(location.latitude)
        val userLonRad = Math.toRadians(location.longitude)
        val kaabaLatRad = Math.toRadians(KA_BA_POSITION_LATITUDE)
        val kaabaLonRad = Math.toRadians(KA_BA_POSITION_LONGITUDE)
        val lonDiff = kaabaLonRad - userLonRad
        val y = sin(lonDiff) * cos(kaabaLatRad)
        val x = cos(userLatRad) * sin(kaabaLatRad) - sin(userLatRad) * cos(kaabaLatRad) * cos(lonDiff)
        return (Math.toDegrees(atan2(y, x)).toFloat() + 360) % 360
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocation()
        } else {
            binding.textKaabaDir.text = getString(R.string.msg_permission_not_granted_yet)
            showToast(getString(R.string.toast_permission_required))
        }
    }

    private fun showSettingsAlert() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.gps_settings_title))
            setMessage(getString(R.string.gps_settings_text))
            setPositiveButton(getString(R.string.settings_button_ok)) { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            setNegativeButton(getString(R.string.settings_button_cancel)) { dialog, _ -> dialog.cancel() }
            show()
        }
    }

    private fun saveFloat(value: Float?) = prefs.edit().putFloat(KEY_LOC, value ?: 0f).apply()
    private fun retrieveFloat(key: String = KEY_LOC): Float = prefs.getFloat(key, 0f)

    companion object {
        private const val KEY_LOC = "qibla_direction_v2"
        private const val KA_BA_POSITION_LONGITUDE = 39.826206
        private const val KA_BA_POSITION_LATITUDE = 21.422487
        private const val PERMISSION_REQUEST_CODE = 101
    }
}
