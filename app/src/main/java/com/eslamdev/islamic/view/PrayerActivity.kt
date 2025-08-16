package com.eslamdev.islamic.view

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
import com.eslamdev.islamic.LocationHelper
import com.eslamdev.islamic.LocationResultListener
import com.eslamdev.islamic.R
import com.eslamdev.islamic.adapter.PrayerAdapter
import com.eslamdev.islamic.databinding.ActivityPrayerBinding
import com.eslamdev.islamic.model.Day
import com.eslamdev.islamic.model.PrayerTimingEntity
import com.eslamdev.islamic.viewmodel.PrayerHomeViewModel
import java.util.*

class PrayerActivity : AppCompatActivity(), PrayerAdapter.OnClickDayListener {

    private lateinit var binding: ActivityPrayerBinding
    private lateinit var prayerViewModel: PrayerHomeViewModel
    private lateinit var locationHelper: LocationHelper
    private lateinit var daysAdapter: PrayerAdapter
    private val calendar = Calendar.getInstance()
    private var lastKnownLocation: Location? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    companion object {
        const val PERMISSION_ID = 42
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        swipeRefreshLayout = binding.root as SwipeRefreshLayout

        prayerViewModel = ViewModelProvider(this)[PrayerHomeViewModel::class.java]
        locationHelper = LocationHelper(this)
        daysAdapter = PrayerAdapter(emptyList(), this, Locale("ar"))

        initUI()
        observeViewModel()
        setupClickListeners()
        setupSwipeToRefresh()
        checkPermissionsAndFetchLocation()
    }

    private fun initUI() {
        binding.recyclerDays.apply {
            layoutManager = LinearLayoutManager(this@PrayerActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = daysAdapter
        }
    }

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
            swipeRefreshLayout.isRefreshing = false
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

    // --- بداية التعديلات لحل مشكلة التأخير ---

    /**
     * الدالة الرئيسية الجديدة التي تطبق استراتيجية التحميل السريع.
     */
    private fun fetchLocationAndCalculateTimes() {
        if (!swipeRefreshLayout.isRefreshing) {
            binding.progressBar.visibility = View.VISIBLE
        }

        // الخطوة 1: جلب آخر موقع معروف فوراً
        locationHelper.getLastKnownLocation { location ->
            if (location != null) {
                // إذا وجدنا موقعاً، احفظه وابدأ الحساب فوراً
                lastKnownLocation = location
                calculateTimesForCurrentMonth()
                updateLocationText(location)
            }
            // الخطوة 2: اطلب تحديثاً جديداً في الخلفية في كل الحالات
            requestFreshLocation()
        }
    }

    /**
     * دالة مساعدة لطلب موقع جديد ودقيق في الخلفية.
     */
    private fun requestFreshLocation() {
        locationHelper.requestSingleLocationUpdate(object : LocationResultListener {
            override fun onLocationResult(location: Location) {
                // عند وصول الموقع الجديد، قم بتحديث الواجهة مرة أخرى
                lastKnownLocation = location
                calculateTimesForCurrentMonth()
                updateLocationText(location)
            }

            override fun onLocationFailed(reason: String) {
                swipeRefreshLayout.isRefreshing = false
                // أظهر رسالة الخطأ فقط إذا لم يتم عرض أي بيانات بعد
                if (binding.prayersView.visibility != View.VISIBLE) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@PrayerActivity, "لم يتمكن من تحديد الموقع. يرجى تفعيل الـ GPS.", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    // --- نهاية التعديلات ---

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
        binding.fajrItemLayout.background = ContextCompat.getDrawable(this, R.drawable.prayer_item_neumorphic_background)
        binding.dhuhrItemLayout.background = ContextCompat.getDrawable(this, R.drawable.prayer_item_neumorphic_background)
        binding.asrItemLayout.background = ContextCompat.getDrawable(this, R.drawable.prayer_item_neumorphic_background)
        binding.maghribItemLayout.background = ContextCompat.getDrawable(this, R.drawable.prayer_item_neumorphic_background)
        binding.ishaItemLayout.background = ContextCompat.getDrawable(this, R.drawable.prayer_item_neumorphic_background)

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