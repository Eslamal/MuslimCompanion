package com.example.islamic.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.islamic.LocationHelper
import com.example.islamic.LocationResultListener
import com.example.islamic.R
import com.example.islamic.adapter.PrayerAdapter
import com.example.islamic.databinding.ActivityPrayerBinding
import com.example.islamic.model.Day
import com.example.islamic.model.PrayerTimingEntity
import com.example.islamic.viewmodel.PrayerHomeViewModel
import java.text.SimpleDateFormat
import java.util.*

class PrayerActivity : AppCompatActivity(), PrayerAdapter.OnClickDayListener {

    private lateinit var binding: ActivityPrayerBinding
    private lateinit var prayerViewModel: PrayerHomeViewModel
    private lateinit var locationHelper: LocationHelper
    private lateinit var daysAdapter: PrayerAdapter
    private val calendar = Calendar.getInstance()
    private var lastKnownLocation: Location? = null

    // *** تعريف متغير الريفرش ***
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    companion object {
        const val PERMISSION_ID = 42
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrayerBinding.inflate(layoutInflater)
        // **ملاحظة:** تأكد من أن layout الخاص بك هو activity_prayer.xml
        // وإذا كنت تستخدم ViewBinding، لا تحتاج لـ findViewById. إذا لم تكن تستخدمه، تجاهل هذه الملاحظة.
        setContentView(binding.root)
        supportActionBar?.hide()

        // *** ربط متغير الريفرش بالواجهة ***
        // إذا كنت لا تستخدم ViewBinding، استخدم هذا السطر:
        // swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_prayer)
        // أما إذا كنت تستخدم ViewBinding كما هو واضح من الكود، استخدم هذا:
        swipeRefreshLayout = binding.root as SwipeRefreshLayout

        prayerViewModel = ViewModelProvider(this)[PrayerHomeViewModel::class.java]
        locationHelper = LocationHelper(this)
        daysAdapter = PrayerAdapter(emptyList(), this, Locale("ar"))

        initUI()
        observeViewModel()
        setupClickListeners()
        setupSwipeToRefresh() // *** استدعاء دالة الريفرش ***
        checkPermissionsAndFetchLocation()
    }

    private fun initUI() {
        binding.recyclerDays.apply {
            layoutManager = LinearLayoutManager(this@PrayerActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = daysAdapter
        }
    }

    // *** دالة جديدة لإعداد الريفرش ***
    private fun setupSwipeToRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            Toast.makeText(this, "جاري التحديث...", Toast.LENGTH_SHORT).show()
            fetchLocationAndCalculateTimes()
        }
    }

    private fun observeViewModel() {
        prayerViewModel.monthData.observe(this) { monthData ->
            binding.progressBar.visibility = View.GONE
            binding.prayersView.visibility = View.VISIBLE
            swipeRefreshLayout.isRefreshing = false // *** إيقاف علامة الريفرش عند وصول البيانات ***
            binding.month.text = monthData.name
            daysAdapter.setData(monthData.days)

            val selectedDay = monthData.days.find { it.isToday } ?: monthData.days.firstOrNull()
            selectedDay?.let { onDayClick(it) }

            val position = monthData.days.indexOfFirst { it.isToday }
            if (position != -1) {
                binding.recyclerDays.scrollToPosition(position)
            }
        }

        prayerViewModel.nextPrayer.observe(this) { nextPrayer ->
            // إزالة كلمة "صلاة " من النص ليتوافق مع دالة التظليل
            val prayerNameOnly = nextPrayer.replace("صلاة ", "")
            binding.nextPrayer.text = "الصلاة القادمة: $prayerNameOnly"
            updatePrayerHighlight(prayerNameOnly)
        }
        prayerViewModel.timeLeft.observe(this) {
            binding.remainingTime.text = it
        }
    }

    private fun setupClickListeners() {
        binding.btnQibla.setOnClickListener { startActivity(Intent(this, QiblaActivity::class.java)) }
        binding.btnRight.setOnClickListener { changeMonth(1) }
        binding.btnLeft.setOnClickListener { changeMonth(-1) }
    }

    private fun changeMonth(amount: Int) {
        binding.prayersView.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE
        calendar.add(Calendar.MONTH, amount)
        calculateTimesForCurrentMonth()
    }

    private fun checkPermissionsAndFetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocationAndCalculateTimes()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_ID)
        }
    }

    private fun fetchLocationAndCalculateTimes() {
        // إظهار ProgressBar فقط إذا لم يكن السحب للتحديث نشطًا
        if (!swipeRefreshLayout.isRefreshing) {
            binding.progressBar.visibility = View.VISIBLE
        }
        locationHelper.requestSingleLocationUpdate(object : LocationResultListener {
            override fun onLocationResult(location: Location) {
                lastKnownLocation = location
                calculateTimesForCurrentMonth()
                updateLocationText(location)
            }

            override fun onLocationFailed(reason: String) {
                binding.progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false // *** إيقاف الريفرش عند الفشل ***
                Toast.makeText(this@PrayerActivity, "لم يتمكن من تحديد الموقع. يرجى تفعيل الـ GPS.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun calculateTimesForCurrentMonth() {
        lastKnownLocation?.let {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            prayerViewModel.getMonthlyPrayerData(year, month, it.latitude, it.longitude)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocationAndCalculateTimes()
        } else {
            Toast.makeText(this, "تم رفض صلاحية الموقع. لا يمكن حساب مواقيت الصلاة.", Toast.LENGTH_LONG).show()
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onDayClick(item: Day) {
        bindData(item.times)
        daysAdapter.setSelectedDay(item)

        if (item.isToday) {
            // أعد طلب قيمة الصلاة القادمة للتأكد من تحديث التظليل
            prayerViewModel.nextPrayer.value?.let {
                val prayerNameOnly = it.replace("صلاة ", "")
                updatePrayerHighlight(prayerNameOnly)
            }
        } else {
            updatePrayerHighlight("")
        }
    }

    private fun bindData(times: PrayerTimingEntity) {
        binding.fajrTime.text = times.fajr
        binding.dherTime.text = times.dhuhr
        binding.asrTime.text = times.asr
        binding.maghribTime.text = times.maghrib
        binding.ishaTime.text = times.isha
    }

    private fun updateLocationText(location: Location) {
        val geocoder = Geocoder(this, Locale("ar"))
        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val city = addresses[0].locality ?: "مكان غير معروف"
                val country = addresses[0].countryName ?: ""
                binding.location.text = "$city، $country"
            }
        } catch (e: Exception) {
            binding.location.text = "خطأ في تحديد الموقع"
        }
    }

    private fun updatePrayerHighlight(nextPrayerName: String) {
        // إعادة تعيين كل الخلفيات أولاً
        binding.fajrItemLayout.background = ContextCompat.getDrawable(this, R.drawable.prayer_item_neumorphic_background)
        binding.dhuhrItemLayout.background = ContextCompat.getDrawable(this, R.drawable.prayer_item_neumorphic_background)
        binding.asrItemLayout.background = ContextCompat.getDrawable(this, R.drawable.prayer_item_neumorphic_background)
        binding.maghribItemLayout.background = ContextCompat.getDrawable(this, R.drawable.prayer_item_neumorphic_background)
        binding.ishaItemLayout.background = ContextCompat.getDrawable(this, R.drawable.prayer_item_neumorphic_background)

        // التظليل فقط إذا كان اليوم المحدد هو اليوم الحالي
        if (daysAdapter.isCurrentDaySelectedToday()) {
            val highlightBg = ContextCompat.getDrawable(this,R.drawable.today_day_background)
            when (nextPrayerName) {
                "الفجر" -> binding.fajrItemLayout.background = highlightBg
                "الظهر" -> binding.dhuhrItemLayout.background = highlightBg
                "العصر" -> binding.asrItemLayout.background = highlightBg
                "المغرب" -> binding.maghribItemLayout.background = highlightBg
                "العشاء" -> binding.ishaItemLayout.background = highlightBg
            }
        }
    }
}