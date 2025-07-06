package com.example.islamic.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.islamic.LocationHelper
import com.example.islamic.LocationResultListener
import com.example.islamic.R
import com.example.islamic.adapter.PrayerAdapter
import com.example.islamic.databinding.ActivityPrayerBinding
import com.example.islamic.model.Day
import com.example.islamic.model.Timings
import com.example.islamic.viewmodel.PrayerHomeViewModel
import java.text.SimpleDateFormat
import java.util.*

class PrayerActivity : AppCompatActivity(), PrayerAdapter.OnClickDayListener {
    private lateinit var binding: ActivityPrayerBinding
    private lateinit var prayerViewModel: PrayerHomeViewModel
    private val calendar = Calendar.getInstance()
    private var currentDay = 0
    private var currentMonth = 0
    private var currentYear = 0
    private var day = 0
    private var month = 0
    private var year = 0
    private var mylat: String = ""
    private var myLong: String = ""
    private lateinit var daysAdapter: PrayerAdapter
    private lateinit var locationHelper: LocationHelper
    private val arabicLocale = Locale("ar", "EG")

    companion object {
        const val PERMISSION_ID = 42
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        prayerViewModel = ViewModelProvider(this)[PrayerHomeViewModel::class.java]
        daysAdapter = PrayerAdapter(emptyList(), this, arabicLocale)
        locationHelper = LocationHelper(this)

        observeViewModel()
        initUI()
        getDateToday()
        checkPermissionsAndFetchLocation()

        binding.btnRight.setOnClickListener { getNextMonth() }
        binding.btnLeft.setOnClickListener { getPrevMonth() }
        binding.btnQibla.setOnClickListener {
            startActivity(Intent(this, QiblaActivity::class.java))
        }
    }

    private fun observeViewModel() {
        prayerViewModel.nextPrayer.observe(this) { nextPrayer ->
            binding.nextPrayer.text = getString(R.string.next_prayer_format, getTranslatedPrayerName(nextPrayer))
            updatePrayerHighlightBackground(nextPrayer)
        }
        prayerViewModel.timeLeft.observe(this) { timeLeft ->
            binding.remainingTime.text = getString(R.string.time_remaining_format, convertToEasternArabic(timeLeft))
        }
        prayerViewModel.monthData.observe(this) { it?.let { loadMonthData(it.days) } }
        prayerViewModel.prayerData.observe(this) { it?.let { if (it.status == "OK") prayerViewModel.mapData(it) } }
    }

    private fun loadMonthData(days: List<Day>){
        daysAdapter.setData(days)
        binding.progressBar.visibility = View.GONE
        binding.prayersView.visibility = View.VISIBLE
        val tempCal = Calendar.getInstance().apply { set(Calendar.MONTH, month - 1) }
        val monthName = SimpleDateFormat("MMMM", arabicLocale).format(tempCal.time)
        binding.month.text = "$monthName ${convertToEasternArabic(year.toString())}"
        val position = if (month == currentMonth && year == currentYear) currentDay - 1 else 0
        binding.recyclerDays.scrollToPosition(position)
        if (position < days.size) {
            bindData(days[position].times)
        }
    }

    private fun checkPermissionsAndFetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_ID)
        } else {
            fetchLocationData()
        }
    }

    private fun fetchLocationData() {
        binding.progressBar.visibility = View.VISIBLE
        locationHelper.requestSingleLocationUpdate(object : LocationResultListener {
            override fun onLocationResult(location: Location) {
                visibleTheView()
                mylat = location.latitude.toString()
                myLong = location.longitude.toString()
                prayerViewModel.getPrayerData(mylat, myLong, month.toString(), year.toString())
                updateLocationText(location.latitude, location.longitude)
            }

            override fun onLocationFailed(reason: String) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@PrayerActivity, "Failed to get location: $reason", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocationData()
        } else {
            Toast.makeText(this, "Permission denied. Location features are disabled.", Toast.LENGTH_LONG).show()
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun initUI() {
        binding.recyclerDays.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = daysAdapter
        }
    }

    private fun getDateToday() {
        calendar.time = Date()
        currentDay = calendar[Calendar.DAY_OF_MONTH]
        currentMonth = calendar[Calendar.MONTH] + 1
        currentYear = calendar[Calendar.YEAR]
        day = currentDay
        month = currentMonth
        year = currentYear
    }

    private fun bindData(it: Timings) {
        val inputFormat = SimpleDateFormat("HH:mm", Locale.US)
        val outputFormat = SimpleDateFormat("hh:mm a", arabicLocale)
        try {
            binding.fajrTime.text = outputFormat.format(inputFormat.parse(it.Fajr.substring(0, 5)))
            binding.dherTime.text = outputFormat.format(inputFormat.parse(it.Dhuhr.substring(0, 5)))
            binding.asrTime.text = outputFormat.format(inputFormat.parse(it.Asr.substring(0, 5)))
            binding.maghribTime.text = outputFormat.format(inputFormat.parse(it.Maghrib.substring(0, 5)))
            binding.ishaTime.text = outputFormat.format(inputFormat.parse(it.Isha.substring(0, 5)))
        } catch (e: Exception) {
            // Fallback for safety
        }
    }

    private fun getPrevMonth() {
        calendar.set(year, month - 1, 1)
        calendar.add(Calendar.MONTH, -1)
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        prayerViewModel.getPrayerData(mylat, myLong, month.toString(), year.toString())
    }

    private fun getNextMonth() {
        calendar.set(year, month - 1, 1)
        calendar.add(Calendar.MONTH, 1)
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        prayerViewModel.getPrayerData(mylat, myLong, month.toString(), year.toString())
    }

    private fun visibleTheView() {
        binding.btnLeft.visibility = View.VISIBLE
        binding.btnRight.visibility = View.VISIBLE
    }

    override fun onDayClick(item: Day) {
        bindData(item.times)
        daysAdapter.setSelectedDay(item)
    }

    private fun updateLocationText(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, arabicLocale)
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val city = addresses[0].locality ?: getString(R.string.unknown_city)
                val country = addresses[0].countryName ?: getString(R.string.unknown_country)
                binding.location.text = getString(R.string.location_format, city, country)
            } else {
                binding.location.text = getString(R.string.location_not_available)
            }
        } catch (e: Exception) {
            binding.location.text = getString(R.string.error_fetching_location)
        }
    }

    private fun updatePrayerHighlightBackground(nextPrayerName: String) {
        val defaultBg = R.color.background_main_neumorphic
        val highlightBg = R.color.prayer_highlight_background
        binding.fajrItemLayout.setBackgroundResource(if (nextPrayerName.equals("Fajr", true)) highlightBg else defaultBg)
        binding.dhuhrItemLayout.setBackgroundResource(if (nextPrayerName.equals("Dhuhr", true)) highlightBg else defaultBg)
        binding.asrItemLayout.setBackgroundResource(if (nextPrayerName.equals("Asr", true)) highlightBg else defaultBg)
        binding.maghribItemLayout.setBackgroundResource(if (nextPrayerName.equals("Maghrib", true)) highlightBg else defaultBg)
        binding.ishaItemLayout.setBackgroundResource(if (nextPrayerName.equals("Isha", true)) highlightBg else defaultBg)
    }

    private fun getTranslatedPrayerName(prayerName: String): String {
        return when (prayerName.lowercase()) {
            "fajr" -> getString(R.string.fajr)
            "dhuhr" -> getString(R.string.Dhuhr)
            "asr" -> getString(R.string.Asr)
            "maghrib" -> getString(R.string.Maghrib)
            "isha" -> getString(R.string.isha)
            else -> prayerName
        }
    }

    private fun convertToEasternArabic(numberString: String): String {
        val arabicNumbers = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val builder = StringBuilder()
        for (char in numberString) {
            if (char.isDigit()) {
                builder.append(arabicNumbers[char.toString().toInt()])
            } else {
                builder.append(char)
            }
        }
        return builder.toString()
    }
}
