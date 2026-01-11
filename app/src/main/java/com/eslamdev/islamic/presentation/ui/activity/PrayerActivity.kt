package com.eslamdev.islamic.presentation.ui.activity

import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eslamdev.islamic.R
import com.eslamdev.islamic.core.MyLocation
import com.eslamdev.islamic.data.local.PrayerDatabase
import com.eslamdev.islamic.data.model.EgyptianCities
import com.eslamdev.islamic.data.model.PrayerRepository
import com.eslamdev.islamic.data.model.PrayerTimingEntity
import com.eslamdev.islamic.presentation.ui.adapter.DayAdapter
import com.eslamdev.islamic.presentation.ui.adapter.PrayerDisplayItem
import com.eslamdev.islamic.presentation.ui.adapter.PrayerTimesAdapter
import com.eslamdev.islamic.presentation.viewmodel.PrayerHomeViewModel
import com.eslamdev.islamic.presentation.viewmodel.PrayerViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class PrayerActivity : AppCompatActivity() {

    private lateinit var viewModel: PrayerHomeViewModel
    private val dayAdapter = DayAdapter()
    private val prayerAdapter = PrayerTimesAdapter()

    private lateinit var tvMonthTitle: TextView
    private lateinit var tvHijriDate: TextView
    private lateinit var tvLocationName: TextView

    private var currentCalendar = Calendar.getInstance()

    private val adhanNames = arrayOf("أذان مكة المكرمة", "أذان القاهرة (عبد الباسط)", "أذان المدينة المنورة")
    private val adhanResIds = arrayOf(R.raw.adhan_mecca, R.raw.adhan_cairo, R.raw.adhan_medina)

    private var previewMediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prayer)

        tvMonthTitle = findViewById(R.id.tv_month_title)
        tvHijriDate = findViewById(R.id.tv_hijri_date)
        tvLocationName = findViewById(R.id.tv_location_name)

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<ImageButton>(R.id.btn_settings).setOnClickListener {
            showSettingsDialog()
        }

        val btnNextMonth = findViewById<ImageButton>(R.id.btn_next_month)
        val btnPrevMonth = findViewById<ImageButton>(R.id.btn_prev_month)

        btnNextMonth.setOnClickListener { changeMonth(1) }
        btnPrevMonth.setOnClickListener { changeMonth(-1) }

        setupViewModel()
        setupRecyclers()

        displayLocationName()
        updateDateAndLoadData()
    }

    override fun onDestroy() {
        super.onDestroy()
        previewMediaPlayer?.release()
        previewMediaPlayer = null
    }

    private fun updateDateAndLoadData() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("ar"))
        tvMonthTitle.text = monthFormat.format(currentCalendar.time)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                val localDate = java.time.LocalDate.of(
                    currentCalendar.get(Calendar.YEAR),
                    currentCalendar.get(Calendar.MONTH) + 1,
                    currentCalendar.get(Calendar.DAY_OF_MONTH)
                )
                val hijriDate = java.time.chrono.HijrahDate.from(localDate)

                val hijriMonths = arrayOf(
                    "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
                    "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
                    "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
                )

                val day = hijriDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
                val monthIndex = hijriDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR) - 1
                val year = hijriDate.get(java.time.temporal.ChronoField.YEAR)

                tvHijriDate.text = "$day ${hijriMonths[monthIndex]} $year هـ"

            } catch (e: Exception) {
                tvHijriDate.text = "-"
            }
        } else {
            tvHijriDate.text = ""
        }

        val days = getDaysOfMonth(currentCalendar)
        dayAdapter.submitList(days, currentCalendar)

        val todayCalendar = Calendar.getInstance()
        val selectedDay: Int = if (isSameMonth(currentCalendar, todayCalendar)) todayCalendar.get(Calendar.DAY_OF_MONTH) else 1

        dayAdapter.selectDay(selectedDay)
        findViewById<RecyclerView>(R.id.recycler_days).scrollToPosition(selectedDay - 1)

        loadPrayerTimesForDay(selectedDay)
    }

    private fun getNextPrayerIndex(timing: PrayerTimingEntity): Int {
        val sdf = SimpleDateFormat("hh:mm a", Locale("ar"))
        val now = Calendar.getInstance()
        val times = listOf(timing.fajr, timing.sunrise, timing.dhuhr, timing.asr, timing.maghrib, timing.isha)

        for (i in times.indices) {
            try {
                val date = sdf.parse(times[i])
                val prayerCal = Calendar.getInstance()
                prayerCal.time = date ?: Date()
                prayerCal.set(Calendar.YEAR, now.get(Calendar.YEAR))
                prayerCal.set(Calendar.MONTH, now.get(Calendar.MONTH))
                prayerCal.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))

                if (prayerCal.after(now)) {
                    return i
                }
            } catch (e: Exception) { continue }
        }

        return 0
    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("إعدادات التطبيق")

        val scrollView = android.widget.ScrollView(this)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)
        scrollView.addView(layout)

        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        val labelAdhan = TextView(this)
        labelAdhan.text = "تنبيهات الأذان:"
        labelAdhan.setTextColor(Color.parseColor("#006D5B"))
        labelAdhan.textSize = 16f
        labelAdhan.setTypeface(null, android.graphics.Typeface.BOLD)
        layout.addView(labelAdhan)

        val switchEnableAdhan = androidx.appcompat.widget.SwitchCompat(this)
        switchEnableAdhan.text = "تفعيل صوت الأذان"
        switchEnableAdhan.isChecked = prefs.getBoolean("IS_ADHAN_ENABLED", true)
        layout.addView(switchEnableAdhan)

        val soundLayout = LinearLayout(this)
        soundLayout.orientation = LinearLayout.VERTICAL
        soundLayout.setPadding(0, 20, 0, 20)

        val labelSound = TextView(this)
        labelSound.text = "اختر صوت المؤذن:"
        labelSound.setTextColor(Color.BLACK)
        soundLayout.addView(labelSound)

        val spinnerSound = Spinner(this)
        val soundAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, adhanNames)
        spinnerSound.adapter = soundAdapter
        soundLayout.addView(spinnerSound)

        layout.addView(soundLayout)

        soundLayout.visibility = if (switchEnableAdhan.isChecked) View.VISIBLE else View.GONE
        switchEnableAdhan.setOnCheckedChangeListener { _, isChecked ->
            soundLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                previewMediaPlayer?.release()
                previewMediaPlayer = null
            }
        }

        var isInitialSelection = true
        spinnerSound.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isInitialSelection) {
                    isInitialSelection = false
                    return
                }

                try {
                    previewMediaPlayer?.release()

                    val selectedResId = if (position in adhanResIds.indices) adhanResIds[position] else adhanResIds[0]
                    previewMediaPlayer = MediaPlayer.create(this@PrayerActivity, selectedResId)
                    previewMediaPlayer?.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val divider = View(this)
        divider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
        divider.setBackgroundColor(Color.LTGRAY)
        divider.setPadding(0, 20, 0, 20)
        layout.addView(divider)

        val labelLocationSection = TextView(this)
        labelLocationSection.text = "\nالموقع والحساب:"
        labelLocationSection.setTextColor(Color.parseColor("#006D5B"))
        labelLocationSection.textSize = 16f
        labelLocationSection.setTypeface(null, android.graphics.Typeface.BOLD)
        layout.addView(labelLocationSection)

        val labelLocation = TextView(this)
        labelLocation.text = "تحديد الموقع:"
        labelLocation.setTextColor(Color.BLACK)
        layout.addView(labelLocation)

        val spinnerLocationMode = Spinner(this)
        val locationModes = arrayOf("تلقائي (GPS)", "اختر المحافظة والمدينة")
        spinnerLocationMode.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locationModes)
        layout.addView(spinnerLocationMode)

        val citySelectionLayout = LinearLayout(this)
        citySelectionLayout.orientation = LinearLayout.VERTICAL
        citySelectionLayout.visibility = View.GONE

        val labelGov = TextView(this)
        labelGov.text = "المحافظة:"
        citySelectionLayout.addView(labelGov)
        val spinnerGov = Spinner(this)
        citySelectionLayout.addView(spinnerGov)

        val labelCity = TextView(this)
        labelCity.text = "المدينة:"
        citySelectionLayout.addView(labelCity)
        val spinnerCity = Spinner(this)
        citySelectionLayout.addView(spinnerCity)

        layout.addView(citySelectionLayout)

        val governoratesList = EgyptianCities.governorates.keys.toList()
        val govAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, governoratesList)
        spinnerGov.adapter = govAdapter

        var selectedLat = 0.0
        var selectedLon = 0.0
        var selectedCityName = ""

        spinnerGov.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGov = governoratesList[position]
                val cities = EgyptianCities.governorates[selectedGov] ?: emptyList()
                val cityNames = cities.map { it.name }
                val cityAdapter = ArrayAdapter(this@PrayerActivity, android.R.layout.simple_spinner_dropdown_item, cityNames)
                spinnerCity.adapter = cityAdapter

                spinnerCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, cityPos: Int, p3: Long) {
                        val city = cities[cityPos]
                        selectedLat = city.lat
                        selectedLon = city.lon
                        selectedCityName = "${city.name}، $selectedGov"
                    }
                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val labelMethod = TextView(this)
        labelMethod.text = "\nطريقة الحساب:"
        labelMethod.setTextColor(Color.BLACK)
        layout.addView(labelMethod)
        val spinnerMethod = Spinner(this)
        val methods = arrayOf("رابطة العالم الإسلامي", "جامعة كراتشي", "أمريكا الشمالية", "دبي", "المساحة المصرية", "أم القرى")
        spinnerMethod.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, methods)
        layout.addView(spinnerMethod)

        val labelMadhab = TextView(this)
        labelMadhab.text = "\nالمذهب:"
        labelMadhab.setTextColor(Color.BLACK)
        layout.addView(labelMadhab)
        val spinnerMadhab = Spinner(this)
        val madhabs = arrayOf("شافعي / مالكي / حنبلي", "حنفي")
        spinnerMadhab.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, madhabs)
        layout.addView(spinnerMadhab)

        spinnerSound.setSelection(prefs.getInt("ADHAN_SOUND_INDEX", 0))
        spinnerMethod.setSelection(prefs.getInt("CALC_METHOD_INDEX", 4))
        spinnerMadhab.setSelection(prefs.getInt("MADHAB_INDEX", 0))
        val isManual = prefs.getBoolean("IS_MANUAL_LOCATION", false)
        spinnerLocationMode.setSelection(if (isManual) 1 else 0)

        if (isManual) citySelectionLayout.visibility = View.VISIBLE

        spinnerLocationMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                citySelectionLayout.visibility = if (position == 1) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        builder.setView(scrollView)

        builder.setPositiveButton("حفظ") { _, _ ->
            previewMediaPlayer?.release()
            previewMediaPlayer = null

            val editor = prefs.edit()
            editor.putBoolean("IS_ADHAN_ENABLED", switchEnableAdhan.isChecked)
            editor.putInt("ADHAN_SOUND_INDEX", spinnerSound.selectedItemPosition)
            editor.putInt("CALC_METHOD_INDEX", spinnerMethod.selectedItemPosition)
            editor.putInt("MADHAB_INDEX", spinnerMadhab.selectedItemPosition)

            val selectedMode = spinnerLocationMode.selectedItemPosition
            if (selectedMode == 1) {
                editor.putBoolean("IS_MANUAL_LOCATION", true)
                editor.putFloat("LATITUDE", selectedLat.toFloat())
                editor.putFloat("LONGITUDE", selectedLon.toFloat())
                editor.putString("MANUAL_CITY_NAME", selectedCityName)
                editor.apply()
                Toast.makeText(this, "تم حفظ: $selectedCityName", Toast.LENGTH_SHORT).show()
                recreate()
            } else {
                editor.putBoolean("IS_MANUAL_LOCATION", false)
                editor.apply()
                updateLocationAuto()
            }
        }

        builder.setNegativeButton("إلغاء") { dialog, _ ->

            previewMediaPlayer?.release()
            previewMediaPlayer = null
            dialog.dismiss()
        }


        builder.setOnDismissListener {
            previewMediaPlayer?.release()
            previewMediaPlayer = null
        }

        builder.show()
    }

    private fun updateLocationAuto() {
        Toast.makeText(this, "جاري تحديد الموقع...", Toast.LENGTH_SHORT).show()
        val myLocation = MyLocation(this)
        myLocation.callback = { lat, lon ->
            val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putFloat("LATITUDE", lat.toFloat())
                putFloat("LONGITUDE", lon.toFloat())
                apply()
            }
            Toast.makeText(this, "تم تحديث الموقع بنجاح", Toast.LENGTH_SHORT).show()
            recreate()
        }
        myLocation.getLastLocation()
    }

    private fun displayLocationName() {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("LATITUDE", 30.0444f).toDouble()
        val lon = prefs.getFloat("LONGITUDE", 31.2357f).toDouble()
        val isManual = prefs.getBoolean("IS_MANUAL_LOCATION", false)

        if (isManual) {
            val savedName = prefs.getString("MANUAL_CITY_NAME", "")
            if (!savedName.isNullOrEmpty()) {
                tvLocationName.text = savedName
                return
            }
        }

        val geocoder = android.location.Geocoder(this, Locale("ar"))
        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val city = addresses[0].locality ?: addresses[0].subAdminArea ?: "مدينة غير معروفة"
                val country = addresses[0].countryName ?: ""
                tvLocationName.text = "$city، $country"
            } else {
                tvLocationName.text = "Lat: ${String.format("%.2f", lat)}, Lon: ${String.format("%.2f", lon)}"
            }
        } catch (e: Exception) {
            tvLocationName.text = "الموقع الحالي"
        }
    }

    private fun changeMonth(amount: Int) {
        currentCalendar.add(Calendar.MONTH, amount)
        currentCalendar.set(Calendar.DAY_OF_MONTH, 1)
        updateDateAndLoadData()
    }
    private fun setupViewModel() {
        val prayerDao = PrayerDatabase.getDatabase(application).prayerTimingDao()
        val repository = PrayerRepository(prayerDao, this)
        val factory = PrayerViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[PrayerHomeViewModel::class.java]
    }
    private fun setupRecyclers() {
        val rvDays = findViewById<RecyclerView>(R.id.recycler_days)
        rvDays.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvDays.adapter = dayAdapter
        val rvPrayers = findViewById<RecyclerView>(R.id.rv_prayer_times)
        rvPrayers.layoutManager = LinearLayoutManager(this)
        rvPrayers.adapter = prayerAdapter
        dayAdapter.onDayClick = { day ->
            currentCalendar.set(Calendar.DAY_OF_MONTH, day)
            loadPrayerTimesForDay(day)
        }
    }
    private fun loadPrayerTimesForDay(day: Int) {
        val month = currentCalendar.get(Calendar.MONTH) + 1
        val year = currentCalendar.get(Calendar.YEAR)
        val realToday = Calendar.getInstance()
        val isTodayReal = (day == realToday.get(Calendar.DAY_OF_MONTH) && month == (realToday.get(Calendar.MONTH) + 1) && year == realToday.get(Calendar.YEAR))
        viewModel.getPrayerTimingForSpecificDay(day, month, year, this) { timing ->
            if (timing != null) updatePrayerList(timing, isTodayReal)
        }
    }
    private fun updatePrayerList(timing: PrayerTimingEntity, isToday: Boolean) {
        val prayerList = mutableListOf<PrayerDisplayItem>()
        var nextPrayerIndex = -1
        if (isToday) nextPrayerIndex = getNextPrayerIndex(timing)
        val names = listOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء")
        val times = listOf(timing.fajr, timing.sunrise, timing.dhuhr, timing.asr, timing.maghrib, timing.isha)
        for (i in names.indices) {
            prayerList.add(PrayerDisplayItem(name = names[i], time = times[i], isNext = (i == nextPrayerIndex)))
        }
        prayerAdapter.submitList(prayerList)
    }
    private fun getDaysOfMonth(calendar: Calendar): List<Int> {
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (1..maxDays).toList()
    }
    private fun isSameMonth(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
    }
}