package com.eslamdev.islamic.presentation.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.eslamdev.islamic.R
import com.eslamdev.islamic.data.local.PrayerDatabase
import com.eslamdev.islamic.data.model.MainMenuItem
import com.eslamdev.islamic.data.model.PrayerRepository
import com.eslamdev.islamic.presentation.ui.adapter.MainMenuAdapter
import com.eslamdev.islamic.presentation.viewmodel.PrayerHomeViewModel
import com.eslamdev.islamic.presentation.viewmodel.PrayerViewModelFactory
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import android.icu.util.Calendar as IcuCalendar
import android.icu.util.ULocale
import com.eslamdev.islamic.core.PrayerScheduler

class MainActivity : AppCompatActivity() {

    private lateinit var prayerViewModel: PrayerHomeViewModel
    // استبدلنا LocationHelper بـ FusedLocationProviderClient المباشر لضمان العمل
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // تعريف عناصر الواجهة
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var tvGreetingAndDate: TextView
    private lateinit var tvGregorianDate: TextView // تمت الإضافة
    private lateinit var tvNextPrayer: TextView
    private lateinit var tvTimeLeft: TextView
    private lateinit var progressBarPrayer: ProgressBar
    private lateinit var mainMenuRv: RecyclerView
    private lateinit var dailyHadithCard: CardView
    private lateinit var dailyAyaCard: CardView
    private lateinit var dailyDuaCard: CardView
    private lateinit var dailyHadithText: TextView
    private lateinit var tvDailyAya: TextView
    private lateinit var tvDailyDua: TextView

    private var fullDailyHadith: String? = null
    private var fullDailyAya: String? = null
    private var fullDailyDua: String? = null

    companion object {
        const val PERMISSION_ID = 42
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // تهيئة خدمة الموقع
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // تعريف زر الريفرش
        val btnRefresh = findViewById<ImageView>(R.id.btn_refresh_prayers)

        btnRefresh.setOnClickListener {
            Toast.makeText(this, "جاري تحديث الموقع والمواقيت...", Toast.LENGTH_SHORT).show()
            getUserLocation() // الدالة دي بقت موجودة تحت
            setupHijriDate()

            // جدولة الأذان للصلاة القادمة
            PrayerScheduler.scheduleNextPrayer(this)
        }

        // ربط عناصر الواجهة
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        tvGreetingAndDate = findViewById(R.id.tv_greeting_and_date)
        tvGregorianDate = findViewById(R.id.tv_gregorian_date) // ربط التاريخ الميلادي
        tvNextPrayer = findViewById(R.id.tv_next_prayer)
        tvTimeLeft = findViewById(R.id.tv_time_left)
        progressBarPrayer = findViewById(R.id.progress_bar_prayer)
        mainMenuRv = findViewById(R.id.main_menu_rv)
        dailyHadithCard = findViewById(R.id.daily_hadith_card)
        dailyAyaCard = findViewById(R.id.daily_aya_card)
        dailyDuaCard = findViewById(R.id.daily_dua_card)
        dailyHadithText = findViewById(R.id.daily_hadith_text)
        tvDailyAya = findViewById(R.id.tv_daily_aya)
        tvDailyDua = findViewById(R.id.tv_daily_dua)

        // إعداد الـ ViewModel مع الـ Repository
        val prayerDao = PrayerDatabase.getDatabase(application).prayerTimingDao()
        val repository = PrayerRepository(prayerDao, this)
        val factory = PrayerViewModelFactory(repository)
        prayerViewModel = ViewModelProvider(this, factory)[PrayerHomeViewModel::class.java]

        setupMainMenu()
        observeViewModel()

        // طلب الموقع عند الفتح
        getUserLocation()

        setupHijriDate()
        setupDailyContent()
        setupClickListeners()
        setupSwipeToRefresh()
    }

    // ==========================================
    // 📍 دوال تحديد الموقع (تمت إضافتها هنا لتعويض LocationHelper)
    // ==========================================

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                swipeRefreshLayout.isRefreshing = true
                fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        updatePrayerTimes(location.latitude, location.longitude)
                        swipeRefreshLayout.isRefreshing = false
                    } else {
                        requestNewLocationData()
                    }
                }
            } else {
                Toast.makeText(this, "يرجى تفعيل GPS", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                swipeRefreshLayout.isRefreshing = false
            }
        } else {
            requestPermissions()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
            numUpdates = 1
        }

        fusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback, Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            mLastLocation?.let { updatePrayerTimes(it.latitude, mLastLocation.longitude) }
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    // ==========================================
    // 🕌 تحديث البيانات والواجهة
    // ==========================================

    private fun updatePrayerTimes(latitude: Double, longitude: Double) {
        // 1. حفظ الموقع في SharedPreferences
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putFloat("LATITUDE", latitude.toFloat())
            putFloat("LONGITUDE", longitude.toFloat())
            apply()
        }

        // 2. تحديث ViewModel
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        prayerViewModel.getMonthlyPrayerData(year, month, latitude, longitude)
    }

    private fun setupSwipeToRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            getUserLocation()
            setupDailyContent()
            setupHijriDate()
        }
    }

    private fun setupMainMenu() {
        val menuItems = listOf(
            MainMenuItem("القرآن", R.drawable.quran, SurahListActivity::class.java),
            MainMenuItem("التفسير", R.drawable.tafseer, SurahListActivity::class.java),
            MainMenuItem("الأذكار", R.drawable.beads, Azkar::class.java),
            MainMenuItem("الأحاديث", R.drawable.prophet, HadithListActivity::class.java),
            MainMenuItem("الأدعية", R.drawable.doaa, DuaActivity::class.java),
            MainMenuItem("المسبحة", R.drawable.beads, TasbeehActivity::class.java),
            MainMenuItem("المواقيت", R.drawable.prayer, PrayerActivity::class.java),
            MainMenuItem("القبلة", R.drawable.qibla, QiblaActivity::class.java)
        )

        // هنا بنمرر Listener عشان نعالج الضغطات ونعرف نفرق بين القرآن والتفسير
        val adapter = MainMenuAdapter(menuItems) { item ->
            val intent = Intent(this, item.activity)
            if (item.title == "التفسير") {
                intent.putExtra("IS_TAFSEER_MODE", true)
            } else if (item.title == "القرآن") {
                intent.putExtra("IS_TAFSEER_MODE", false)
            }
            startActivity(intent)
        }

        // لازم نتأكد إن الـ MainMenuAdapter عندك بيقبل Listener في الـ Constructor
        // لو مش بيقبل، ممكن نستخدم الطريقة القديمة بس التفسير مش هيفتح كـ تفسير
        mainMenuRv.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            prayerViewModel.nextPrayer.collectLatest { prayerName ->
                tvNextPrayer.text = prayerName
            }
        }

        lifecycleScope.launch {
            prayerViewModel.timeLeft.collectLatest { timeLeftString ->
                tvTimeLeft.text = timeLeftString
            }
        }

        lifecycleScope.launch {
            prayerViewModel.prayerProgress.collectLatest { progress ->
                progressBarPrayer.progress = progress
            }
        }
    }

    private fun setupHijriDate() {
        // 1. التاريخ الهجري
        val locale = ULocale("ar@calendar=islamic-civil")
        val hijriCalendar = IcuCalendar.getInstance(locale)
        val day = hijriCalendar.get(IcuCalendar.DAY_OF_MONTH)
        val month = hijriCalendar.get(IcuCalendar.MONTH)
        val year = hijriCalendar.get(IcuCalendar.YEAR)
        val hijriMonths = arrayOf("محرم", "صفر", "ربيع الأول", "ربيع الثاني", "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة")
        val monthName = if (month in hijriMonths.indices) hijriMonths[month] else ""

        tvGreetingAndDate.text = "$day $monthName $year هـ"

        // 2. التاريخ الميلادي
        val gregorianCalendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar"))
        val gregorianDate = dateFormat.format(gregorianCalendar.time)
        tvGregorianDate.text = gregorianDate
    }

    private fun setupDailyContent() {
        try {
            val hadithInputStream = assets.open("hadith.json")
            val hadithJson = hadithInputStream.bufferedReader().use { it.readText() }
            val hadithArray = JSONArray(hadithJson)
            if (hadithArray.length() > 0) {
                val randomHadith = hadithArray.getJSONObject((0 until hadithArray.length()).random())

                val rawHadith = randomHadith.getString("hadith")

                // ### التعديل هنا: تنظيف النص ###
                // بنشوف لو النص فيه نقطتين ":" زي "الحديث الأول: إنما الأعمال..."
                // بناخد الكلام اللي بعد النقطتين بس
                fullDailyHadith = if (rawHadith.contains(":")) {
                    rawHadith.substringAfter(":").trim()
                } else {
                    // لو مفيش نقطتين، بنحاول نشيل أول سطر لو هو عنوان
                    if (rawHadith.contains("\n")) {
                        rawHadith.substringAfter("\n").trim()
                    } else {
                        rawHadith
                    }
                }

                // تنظيف المسافات الزائدة
                dailyHadithText.text = fullDailyHadith

            }

            val quranInputStream = assets.open("QuranDetails.json")
            val quranJson = quranInputStream.bufferedReader().use { it.readText() }
            val quranObject = org.json.JSONObject(quranJson)
            val surahArray = quranObject.getJSONArray("surah")
            if(surahArray.length() > 0) {
                val randomSurah = surahArray.getJSONObject((0 until surahArray.length()).random())
                val ayaArray = randomSurah.getJSONArray("aya")
                if(ayaArray.length() > 0) {
                    val randomAya = ayaArray.getJSONObject((0 until ayaArray.length()).random())
                    fullDailyAya = randomAya.getString("text")
                    tvDailyAya.text = fullDailyAya
                }
            }

            val duaInputStream = assets.open("duas_collection.json")
            val duaJson = duaInputStream.bufferedReader().use { it.readText() }
            val categoriesArray = JSONArray(duaJson)
            if (categoriesArray.length() > 0) {
                val randomCategory = categoriesArray.getJSONObject((0 until categoriesArray.length()).random())
                val duasArray = randomCategory.getJSONArray("duas")
                if(duasArray.length() > 0) {
                    val randomDua = duasArray.getJSONObject((0 until duasArray.length()).random())
                    fullDailyDua = randomDua.getString("dua")
                    tvDailyDua.text = fullDailyDua
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dailyHadithCard.visibility = View.GONE
            dailyAyaCard.visibility = View.GONE
            dailyDuaCard.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        // حديث -> أيقونة النبي (prophet)
        dailyHadithCard.setOnClickListener {
            showContentDialog("حديث شريف", fullDailyHadith, R.drawable.prophet)
        }

        // آية -> أيقونة القرآن (quran)
        dailyAyaCard.setOnClickListener {
            showContentDialog("آية كريمة", fullDailyAya, R.drawable.quran)
        }

        // دعاء -> أيقونة الدعاء (doaa)
        dailyDuaCard.setOnClickListener {
            showContentDialog("دعاء", fullDailyDua, R.drawable.doaa)
        }
    }

    // ضيفنا معامل جديد اسمه iconRes من نوع Int
    private fun showContentDialog(title: String, content: String?, iconRes: Int) {
        if (content == null) {
            Toast.makeText(this, "المحتوى غير متوفر حالياً", Toast.LENGTH_SHORT).show()
            return
        }
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_daily_content, null)

        // تعريف العناصر
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
        val dialogText = dialogView.findViewById<TextView>(R.id.dialog_text)
        val dialogIcon = dialogView.findViewById<ImageView>(R.id.dialog_icon) // تعريف الصورة
        val closeButton = dialogView.findViewById<Button>(R.id.dialog_close_button)
        val copyButton = dialogView.findViewById<Button>(R.id.btn_copy)

        // تعيين البيانات
        dialogTitle.text = title
        dialogText.text = content
        dialogIcon.setImageResource(iconRes) // تغيير الصورة بناءً على الباراميتر

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        closeButton.setOnClickListener { dialog.dismiss() }

        copyButton.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Islamic App Content", content)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "تم نسخ النص", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation()
            } else {
                Toast.makeText(this, "تم رفض صلاحية الموقع.", Toast.LENGTH_SHORT).show()
                tvNextPrayer.text = "صلاحية الموقع مطلوبة"
            }
        }
    }
}