package com.example.islamic

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.islamic.adapter.MainMenuAdapter
import com.example.islamic.model.MainMenuItem
import com.example.islamic.view.*
import com.example.islamic.viewmodel.PrayerHomeViewModel
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import android.icu.util.Calendar
import android.icu.util.ULocale

class MainActivity : AppCompatActivity() {

    private lateinit var prayerViewModel: PrayerHomeViewModel
    private lateinit var locationHelper: LocationHelper

    // تعريف عناصر الواجهة
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var tvGreetingAndDate: TextView
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

        // ربط عناصر الواجهة
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        tvGreetingAndDate = findViewById(R.id.tv_greeting_and_date)
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

        prayerViewModel = ViewModelProvider(this)[PrayerHomeViewModel::class.java]
        locationHelper = LocationHelper(this)

        setupMainMenu()
        observeViewModel()
        checkPermissionsAndFetchLocation()
        setupHijriDate()
        setupDailyContent()
        setupClickListeners()
        setupSwipeToRefresh()
    }

    private fun setupSwipeToRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            Toast.makeText(this, "جاري التحديث...", Toast.LENGTH_SHORT).show()
            fetchLocation() // استدعاء الدالة الجديدة عند السحب
            setupDailyContent()
        }
    }

    private fun setupMainMenu() {
        val menuItems = listOf(
            MainMenuItem("القرآن", R.drawable.quran, Surah::class.java),
            MainMenuItem("التفسير", R.drawable.tafseer, TafseerSurahActivity::class.java),
            MainMenuItem("الأذكار", R.drawable.prophet, Azkar::class.java),
            MainMenuItem("الأحاديث", R.drawable.prophet, HadithListActivity::class.java),
            MainMenuItem("الأدعية", R.drawable.doaa, DuaActivity::class.java),
            MainMenuItem("المسبحة", R.drawable.beads, TasbeehActivity::class.java),
            MainMenuItem("المواقيت", R.drawable.prayer, PrayerActivity::class.java),
            MainMenuItem("القبلة", R.drawable.qibla, QiblaActivity::class.java)
        )
        mainMenuRv.adapter = MainMenuAdapter(menuItems)
    }

    private fun observeViewModel() {
        prayerViewModel.nextPrayer.observe(this) { prayerName ->
            tvNextPrayer.text = prayerName
        }
        prayerViewModel.timeLeft.observe(this) { timeLeftString ->
            tvTimeLeft.text = timeLeftString
            if (swipeRefreshLayout.isRefreshing) {
                swipeRefreshLayout.isRefreshing = false
            }
        }
        prayerViewModel.prayerProgress.observe(this) { progress ->
            progressBarPrayer.progress = progress
        }
    }

    private fun checkPermissionsAndFetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_ID)
        }
    }

    // --- بداية التعديلات لحل مشكلة التأخير ---

    /**
     * الدالة الرئيسية الجديدة لجلب الموقع.
     * تطبق استراتيجية التحميل السريع: عرض البيانات بالموقع القديم، ثم تحديثها بالموقع الجديد.
     */
    private fun fetchLocation() {
        swipeRefreshLayout.isRefreshing = true

        // الخطوة 1: حاول جلب آخر موقع معروف فوراً
        locationHelper.getLastKnownLocation { location ->
            if (location != null) {
                // إذا وجدنا موقعاً محفوظاً، اعرض المواقيت فوراً باستخدامه
                prayerViewModel.calculateAndStartLiveTimer(
                    location.latitude,
                    location.longitude
                )
            }

            // الخطوة 2: في كل الحالات، اطلب تحديثاً جديداً ودقيقاً في الخلفية
            requestFreshLocation()
        }
    }

    /**
     * دالة مساعدة لطلب موقع جديد ودقيق في الخلفية وتحديث الواجهة عند وصوله.
     */
    private fun requestFreshLocation() {
        locationHelper.requestSingleLocationUpdate(object : LocationResultListener {
            override fun onLocationResult(location: Location) {
                // عند وصول الموقع الجديد والدقيق، قم بتحديث الواجهة مرة أخرى
                prayerViewModel.calculateAndStartLiveTimer(
                    location.latitude,
                    location.longitude
                )
            }

            override fun onLocationFailed(reason: String) {
                // إذا فشلت كل المحاولات (القديمة والجديدة)، أبلغ المستخدم
                // التحقق إذا لم يتم عرض أي شيء بعد لتجنب إظهار رسالة خطأ غير ضرورية
                if (tvNextPrayer.text.isNullOrEmpty() || tvNextPrayer.text == "فشل تحديد الموقع") {
                    tvNextPrayer.text = "فشل تحديد الموقع"
                    tvTimeLeft.text = "يرجى تفعيل الـ GPS."
                }
                Toast.makeText(this@MainActivity, "فشل تحديد الموقع. يرجى تفعيل الـ GPS.", Toast.LENGTH_LONG).show()
            }
        })

        // أوقف علامة التحديث بعد فترة قصيرة لأن العملية الآن تتم في الخلفية
        Handler(Looper.getMainLooper()).postDelayed({
            if (swipeRefreshLayout.isRefreshing) {
                swipeRefreshLayout.isRefreshing = false
            }
        }, 2000)
    }

    // --- نهاية التعديلات ---


    private fun setupHijriDate() {
        val locale = ULocale("ar@calendar=islamic-civil")
        val hijriCalendar = Calendar.getInstance(locale)
        val dayName = SimpleDateFormat("EEEE", Locale("ar")).format(hijriCalendar.time)
        val day = hijriCalendar.get(Calendar.DAY_OF_MONTH)
        val month = hijriCalendar.get(Calendar.MONTH)
        val year = hijriCalendar.get(Calendar.YEAR)
        val hijriMonths = arrayOf("محرم", "صفر", "ربيع الأول", "ربيع الثاني", "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة")
        val monthName = hijriMonths[month]
        tvGreetingAndDate.text = "$dayName، $day $monthName $year هـ"
    }

    private fun setupDailyContent() {
        try {
            val hadithInputStream = assets.open("hadith.json")
            val hadithJson = hadithInputStream.bufferedReader().use { it.readText() }
            val hadithArray = JSONArray(hadithJson)
            if (hadithArray.length() > 0) {
                val randomHadith = hadithArray.getJSONObject((0 until hadithArray.length()).random())
                fullDailyHadith = randomHadith.getString("hadith")
                var hadithBody = fullDailyHadith 

                if (fullDailyHadith != null && fullDailyHadith!!.contains(":")) {

                    hadithBody = fullDailyHadith!!.substringAfter(":").trim()
                }

                dailyHadithText.text = hadithBody
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
        dailyHadithCard.setOnClickListener {
            showContentDialog("حديث ", fullDailyHadith)
        }
        dailyAyaCard.setOnClickListener {
            showContentDialog("آية ", fullDailyAya)
        }
        dailyDuaCard.setOnClickListener {
            showContentDialog("دعاء ", fullDailyDua)
        }
    }

    private fun showContentDialog(title: String, content: String?) {
        if (content == null) {
            Toast.makeText(this, "المحتوى غير متوفر حالياً", Toast.LENGTH_SHORT).show()
            return
        }
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_daily_content, null)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
        val dialogText = dialogView.findViewById<TextView>(R.id.dialog_text)
        val closeButton = dialogView.findViewById<Button>(R.id.dialog_close_button)

        dialogTitle.text = title
        dialogText.text = content

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation()
            } else {
                Toast.makeText(this, "تم رفض صلاحية الموقع.", Toast.LENGTH_SHORT).show()
                tvNextPrayer.text = "صلاحية الموقع مطلوبة"
            }
        }
    }
}