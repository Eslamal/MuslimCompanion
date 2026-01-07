package com.eslamdev.islamic.presentation.ui.activity

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eslamdev.islamic.R
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs

class QiblaActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var imageDial: ImageView
    private lateinit var imagePointer: ImageView
    private lateinit var tvLocationName: TextView
    private lateinit var tvDegree: TextView

    private lateinit var sensorManager: SensorManager
    private var sensorAccelerometer: Sensor? = null
    private var sensorMagnetometer: Sensor? = null

    private var currentDegree = 0f
    private var qiblaAngle = 0f

    // مصفوفات القراءات
    private var lastAccelerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false

    private var isQiblaAligned = false
    private var lastToastTime: Long = 0

    // ### التعديل الجوهري هنا ###
    // غيرنا القيمة لـ 0.05f (بدل 0.97f)
    // ده معناه: خد 5% بس من القراءة الجديدة، وحافظ على 95% من استقرار القديمة
    private val ALPHA = 0.05f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qibla)

        imageDial = findViewById(R.id.iv_compass_dial)
        imagePointer = findViewById(R.id.iv_qibla_pointer)
        tvLocationName = findViewById(R.id.tv_location_name)
        tvDegree = findViewById(R.id.tv_degree)

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorMagnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        setupLocationAndQibla()
    }

    private fun setupLocationAndQibla() {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("LATITUDE", 30.0444f).toDouble()
        val lon = prefs.getFloat("LONGITUDE", 31.2357f).toDouble()

        val isManual = prefs.getBoolean("IS_MANUAL_LOCATION", false)
        if (isManual) {
            tvLocationName.text = prefs.getString("MANUAL_CITY_NAME", "موقع يدوي")
        } else {
            try {
                val geocoder = Geocoder(this, Locale("ar"))
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val city = addresses[0].locality ?: addresses[0].subAdminArea ?: "مدينة غير معروفة"
                    tvLocationName.text = city
                } else {
                    tvLocationName.text = "الموقع الحالي"
                }
            } catch (e: Exception) {
                tvLocationName.text = "الموقع الحالي"
            }
        }
        qiblaAngle = calculateQiblaAngle(lat, lon).toFloat()
    }

    private fun calculateQiblaAngle(lat: Double, lon: Double): Double {
        val kaabaLat = 21.422487
        val kaabaLon = 39.826206
        val phiK = Math.toRadians(kaabaLat)
        val lambdaK = Math.toRadians(kaabaLon)
        val phi = Math.toRadians(lat)
        val lambda = Math.toRadians(lon)
        val y = sin(lambdaK - lambda)
        val x = cos(phi) * java.lang.Math.tan(phiK) - sin(phi) * cos(lambdaK - lambda)
        var resultDegree = Math.toDegrees(atan2(y, x))
        if (resultDegree < 0) {
            resultDegree += 360.0
        }
        return resultDegree
    }

    override fun onResume() {
        super.onResume()
        // استخدمنا SENSOR_DELAY_UI (أبطأ قليلاً وأكثر ثباتاً من GAME)
        sensorAccelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        sensorMagnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    // دالة الفلتر المعدلة
    private fun lowPass(input: FloatArray, output: FloatArray?): FloatArray {
        if (output == null) return input
        for (i in input.indices) {
            // المعادلة: القيمة الجديدة = القديمة + نسبة صغيرة من الفرق
            output[i] = output[i] + ALPHA * (input[i] - output[i])
        }
        return output
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            lastAccelerometer = lowPass(event.values.clone(), lastAccelerometer)
            lastAccelerometerSet = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            lastMagnetometer = lowPass(event.values.clone(), lastMagnetometer)
            lastMagnetometerSet = true
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, lastAccelerometer, lastMagnetometer)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)

                val azimuthInRadians = orientation[0]
                val degree = (Math.toDegrees(azimuthInRadians.toDouble()) + 360).toFloat() % 360

                // ### منع التحديث لو التغيير بسيط جداً (أقل من درجة) لزيادة الثبات ###
                if (abs(currentDegree - (-degree)) < 1.0) return

                tvDegree.text = "${degree.toInt()}° N"

                // معالجة اللفة الكاملة (عشان ميلفش العكس لما يعدي الشمال)
                var targetDegree = -degree
                if (currentDegree - targetDegree > 180) {
                    targetDegree += 360
                } else if (targetDegree - currentDegree > 180) {
                    targetDegree -= 360
                }

                // تدوير القرص
                val rotateDial = RotateAnimation(
                    currentDegree,
                    targetDegree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                )
                rotateDial.duration = 200 // وقت مناسب مع SENSOR_DELAY_UI
                rotateDial.fillAfter = true
                imageDial.startAnimation(rotateDial)

                // تدوير سهم القبلة
                val rotatePointer = RotateAnimation(
                    currentDegree + qiblaAngle,
                    targetDegree + qiblaAngle,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                )
                rotatePointer.duration = 200
                rotatePointer.fillAfter = true
                imagePointer.startAnimation(rotatePointer)

                currentDegree = targetDegree

                checkQiblaAlignment(degree)
            }
        }
    }

    private fun checkQiblaAlignment(currentCompassDegree: Float) {
        val difference = Math.abs(currentCompassDegree - qiblaAngle)
        val validDifference = if (difference > 180) 360 - difference else difference

        if (validDifference < 3) {
            if (!isQiblaAligned) {
                vibrate()
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastToastTime > 5000) {
                    Toast.makeText(this, "هذا هو اتجاه القبلة 🕋", Toast.LENGTH_SHORT).show()
                    lastToastTime = currentTime
                }
                imagePointer.setColorFilter(android.graphics.Color.parseColor("#00FF00"))
                isQiblaAligned = true
            }
        } else {
            if (isQiblaAligned) {
                imagePointer.clearColorFilter()
                isQiblaAligned = false
            }
        }
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}