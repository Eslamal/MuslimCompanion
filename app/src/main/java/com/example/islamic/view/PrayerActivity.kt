package com.example.islamic.view

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.islamic.MainActivity
import com.example.islamic.MyLocation
import com.example.islamic.R
import com.example.islamic.adapter.PrayerAdapter
import com.example.islamic.databinding.ActivityPrayerBinding
import com.example.islamic.model.Day
import com.example.islamic.model.Timings
import com.example.islamic.viewmodel.PrayerHomeViewModel
import java.text.NumberFormat
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
    private lateinit var myLocation: MyLocation
    private val arabicLocale = Locale("ar", "EG")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val actionBar = supportActionBar
        actionBar?.hide()

        prayerViewModel = ViewModelProvider(this)[PrayerHomeViewModel::class.java]
        daysAdapter = PrayerAdapter(emptyList(), this, arabicLocale)
        myLocation = MyLocation(this)

        prayerViewModel.nextPrayer.observe(this) { nextPrayer ->
            binding.nextPrayer.text = getString(R.string.next_prayer_format, getTranslatedPrayerName(nextPrayer))
            updatePrayerHighlightBackground(nextPrayer)
        }

        prayerViewModel.timeLeft.observe(this) { timeLeft ->
            binding.remainingTime.text = getString(R.string.time_remaining_format, convertToEasternArabic(timeLeft))
        }

        initUI()
        getDateToday()
        getDataFromMyLocation()
        sendDataToViewModelToEdit()
        loadUI()

        binding.btnRight.setOnClickListener {
            getNextMonth()
        }
        binding.btnLeft.setOnClickListener {
            getPrevMonth()
        }

        binding.btnQibla.setOnClickListener {
            val intent = Intent(this, QiblaActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initUI() {
        binding.recyclerDays.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = daysAdapter
            scrollToPosition(10)
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

    private fun getDataFromMyLocation() {
        myLocation.callback = { lat, long ->
            visibleTheView()
            mylat = lat
            myLong = long
            prayerViewModel.getPrayerData(lat, long, month.toString(), year.toString())
            updateLocationText(lat, long)
        }
        myLocation.getLastLocation()
    }

    private fun loadUI() {
        prayerViewModel.monthData.observe(this) {
            it?.let {
                daysAdapter.setData(it.days)

                binding.progressBar.visibility = View.GONE
                binding.prayersView.visibility = View.VISIBLE

                val tempCal = Calendar.getInstance()
                tempCal.set(Calendar.MONTH, month - 1)
                val monthName = SimpleDateFormat("MMMM", arabicLocale).format(tempCal.time)
                binding.month.text = "$monthName ${convertToEasternArabic(year.toString())}"

                if (month == currentMonth && day == currentDay && year == currentYear) {
                    bindData(it.days[currentDay - 1].times)
                    binding.recyclerDays.scrollToPosition(currentDay - 1)
                } else {
                    binding.recyclerDays.scrollToPosition(0)
                }
            }
        }
    }

    private fun bindData(it: Timings) {
        val inputFormat = SimpleDateFormat("HH:mm", Locale.US)
        val outputFormat = SimpleDateFormat("hh:mm a", arabicLocale)

        try {
            val fajrTime = inputFormat.parse(it.Fajr.substring(0, 5))
            binding.fajrTime.text = outputFormat.format(fajrTime)

            val dhuhrTime = inputFormat.parse(it.Dhuhr.substring(0, 5))
            binding.dherTime.text = outputFormat.format(dhuhrTime)

            val asrTime = inputFormat.parse(it.Asr.substring(0, 5))
            binding.asrTime.text = outputFormat.format(asrTime)

            val maghribTime = inputFormat.parse(it.Maghrib.substring(0, 5))
            binding.maghribTime.text = outputFormat.format(maghribTime)

            val ishaTime = inputFormat.parse(it.Isha.substring(0, 5))
            binding.ishaTime.text = outputFormat.format(ishaTime)
        } catch (e: Exception) {
            e.printStackTrace()
            binding.fajrTime.text = convertToEasternArabic(it.Fajr.substring(0, 5))
            binding.dherTime.text = convertToEasternArabic(it.Dhuhr.substring(0, 5))
            binding.asrTime.text = convertToEasternArabic(it.Asr.substring(0, 5))
            binding.maghribTime.text = convertToEasternArabic(it.Maghrib.substring(0, 5))
            binding.ishaTime.text = convertToEasternArabic(it.Isha.substring(0, 5))
        }
    }

    private fun sendDataToViewModelToEdit() {
        prayerViewModel.prayerData.observe(this) {
            it?.let {
                if (it.status == "OK") {
                    prayerViewModel.mapData(it)
                }
            }
        }
    }

    private fun getPrevMonth() {
        --month
        if (month == 0) {
            month = 12
            --year
        }
        prayerViewModel.getPrayerData(mylat, myLong, month.toString(), year.toString())
    }

    private fun getNextMonth() {
        ++month
        if (month == 13) {
            month = 1
            ++year
        }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                myLocation.getLastLocation()
            } else {
                binding.btnLeft.visibility = View.GONE
                binding.btnRight.visibility = View.GONE
            }
        }
    }

    companion object {
        const val PERMISSION_ID = 42
    }

    private fun updateLocationText(latitude: String, longitude: String) {
        val geocoder = Geocoder(this, arabicLocale)
        try {
            val addresses = geocoder.getFromLocation(latitude.toDouble(), longitude.toDouble(), 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val city = addresses[0].locality ?: getString(R.string.unknown_city)
                val country = addresses[0].countryName ?: getString(R.string.unknown_country)
                binding.location.text = getString(R.string.location_format, city, country)
            } else {
                binding.location.text = getString(R.string.location_not_available)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.location.text = getString(R.string.error_fetching_location)
        }
    }
    private fun updatePrayerHighlightBackground(nextPrayerName: String) {

        val prayersViewLayout = binding.prayersView

        prayersViewLayout.findViewById<LinearLayout>(R.id.fajr_item_layout)?.setBackgroundResource(R.color.background_main_neumorphic)
        prayersViewLayout.findViewById<LinearLayout>(R.id.dhuhr_item_layout)?.setBackgroundResource(R.color.background_main_neumorphic)
        prayersViewLayout.findViewById<LinearLayout>(R.id.asr_item_layout)?.setBackgroundResource(R.color.background_main_neumorphic)
        prayersViewLayout.findViewById<LinearLayout>(R.id.maghrib_item_layout)?.setBackgroundResource(R.color.background_main_neumorphic)
        prayersViewLayout.findViewById<LinearLayout>(R.id.isha_item_layout)?.setBackgroundResource(R.color.background_main_neumorphic)



        when (nextPrayerName) {
            "الفجر", "Fajr" -> prayersViewLayout.findViewById<LinearLayout>(R.id.fajr_item_layout)?.setBackgroundResource(R.color.prayer_highlight_background)
            "الظهر", "Dhuhr" -> prayersViewLayout.findViewById<LinearLayout>(R.id.dhuhr_item_layout)?.setBackgroundResource(R.color.prayer_highlight_background)
            "العصر", "Asr" -> prayersViewLayout.findViewById<LinearLayout>(R.id.asr_item_layout)?.setBackgroundResource(R.color.prayer_highlight_background)
            "المغرب", "Maghrib" -> prayersViewLayout.findViewById<LinearLayout>(R.id.maghrib_item_layout)?.setBackgroundResource(R.color.prayer_highlight_background)
            "العشاء", "Isha" -> prayersViewLayout.findViewById<LinearLayout>(R.id.isha_item_layout)?.setBackgroundResource(R.color.prayer_highlight_background)
        }
    }

    private fun getTranslatedPrayerName(prayerName: String): String {
        return when (prayerName) {
            "Fajr" -> getString(R.string.fajr)
            "Dhuhr" -> getString(R.string.Dhuhr)
            "Asr" -> getString(R.string.Asr)
            "Maghrib" -> getString(R.string.Maghrib)
            "Isha" -> getString(R.string.isha)
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