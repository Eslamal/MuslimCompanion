package com.eslamdev.islamic.presentation.ui.activity

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.webkit.WebView
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.eslamdev.islamic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SurahDetailActivity : AppCompatActivity() {

    private lateinit var surahNameTv: TextView
    private lateinit var quranWebView: WebView
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surah_detail)

        surahNameTv = findViewById(R.id.surah_name)
        quranWebView = findViewById(R.id.quran_webview)
        btnBack = findViewById(R.id.btn_back)

        btnBack.setOnClickListener { finish() }

        val surahIndex = intent.getIntExtra("index", 1)
        val surahName = intent.getStringExtra("name") ?: ""

        surahNameTv.text = "سورة $surahName"

        setupWebView()
        loadSurahContent(surahIndex)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        quranWebView.settings.javaScriptEnabled = false
        quranWebView.settings.defaultTextEncodingName = "utf-8"
        quranWebView.layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
        // جعل خلفية الويب فيو شفافة عشان متعملش ومضة بيضاء قبل التحميل
        quranWebView.setBackgroundColor(0)
    }

    private fun loadSurahContent(index: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val content = generateHtmlContent(index)
            withContext(Dispatchers.Main) {
                quranWebView.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null)
            }
        }
    }

    private fun generateHtmlContent(targetIndex: Int): String {
        // 1. الكشف عن الوضع الليلي
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES

        // 2. تحديد الألوان بناءً على الوضع
        val bgColor = if (isDarkMode) "#121212" else "#FFFBF2" // أسود رمادي للداكن / كريمي للفاتح
        val textColor = if (isDarkMode) "#E0E0E0" else "#000000" // أبيض للداكن / أسود للفاتح
        val borderColor = "#D4AF37" // ذهبي (يمشي مع الاتنين)
        val verseNumColor = if (isDarkMode) "#80CBC4" else "#006D5B" // أخضر فاتح للداكن / غامق للفاتح

        val htmlBuilder = StringBuilder()
        htmlBuilder.append("<!DOCTYPE html>")
            .append("<html dir='rtl' lang='ar'>")
            .append("<head>")
            .append("<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>")
            .append("<style>")
            .append("@font-face { font-family: 'Uthmanic'; src: url('file:///android_asset/me_quran.ttf'); }")

            .append("body {")
            .append("   background-color: $bgColor;") // استخدام اللون المتغير
            .append("   color: $textColor;")         // استخدام اللون المتغير
            .append("   margin: 0; padding: 10px;")
            .append("   font-family: 'Uthmanic', 'Amiri', serif;")
            .append("   text-align: justify;")
            .append("   line-height: 2.5;")
            .append("}")

            .append(".page-frame {")
            .append("   border: 2px solid $borderColor;")
            .append("   padding: 10px;")
            .append("   border-radius: 15px;")
            .append("   min-height: 95vh;")
            .append("}")

            .append(".bismillah {")
            .append("   text-align: center;")
            .append("   color: $verseNumColor;")
            .append("   font-size: 24px;")
            .append("   margin-bottom: 20px;")
            .append("   font-family: 'Uthmanic';")
            .append("   font-weight: bold;")
            .append("   border-bottom: 1px solid $borderColor;")
            .append("   padding-bottom: 10px;")
            .append("}")

            .append(".aya-container { display: inline; }")
            .append(".aya-text { font-size: 22px; color: $textColor; }") // تأكدنا أن النص يأخذ لون المتغير

            .append(".aya-symbol-container {")
            .append("   display: inline-block;")
            .append("   position: relative;")
            .append("   width: 40px; height: 40px;")
            .append("   text-align: center;")
            .append("   vertical-align: middle;")
            .append("   margin-right: 5px;")
            .append("}")

            .append(".aya-symbol-char {")
            .append("   font-family: 'Uthmanic';")
            .append("   font-size: 35px;")
            .append("   color: $verseNumColor;")
            .append("   position: absolute;")
            .append("   top: 0; left: 0; right: 0; bottom: 0;")
            .append("   line-height: 42px;")
            .append("   z-index: 1;")
            .append("}")

            .append(".aya-num {")
            .append("   position: absolute;")
            .append("   top: 0; left: 0; right: 0; bottom: 0;")
            .append("   font-size: 14px;")
            .append("   font-family: sans-serif;")
            .append("   font-weight: bold;")
            .append("   color: $verseNumColor;")
            .append("   line-height: 44px;")
            .append("   z-index: 2;")
            .append("}")

            .append("</style>")
            .append("</head>")
            .append("<body>")
            .append("<div class='page-frame'>")

        try {
            val inputStream = assets.open("QuranDetails.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val surahArray = jsonObject.getJSONArray("surah")

            for (i in 0 until surahArray.length()) {
                val surahObj = surahArray.getJSONObject(i)
                if (surahObj.getInt("index") == targetIndex) {

                    if (targetIndex != 9) {
                        htmlBuilder.append("<div class='bismillah'>بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ</div>")
                    }

                    val ayaArray = surahObj.getJSONArray("aya")
                    for (j in 0 until ayaArray.length()) {
                        val ayaObj = ayaArray.getJSONObject(j)
                        val rawText = ayaObj.getString("text")
                        val ayaIndex = ayaObj.getInt("index")

                        var displayText = rawText
                        if (displayText.contains("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ")) {
                            displayText = displayText.replace("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "").trim()
                        }

                        if (displayText.isNotEmpty() || targetIndex == 1) {
                            htmlBuilder.append("<span class='aya-container'>")
                                .append("<span class='aya-text'>$displayText</span>")
                                .append("<span class='aya-symbol-container'>")
                                .append("<span class='aya-symbol-char'>&#1757;</span>")
                                .append("<span class='aya-num'>$ayaIndex</span>")
                                .append("</span>")
                                .append("</span> ")
                        }
                    }
                    break
                }
            }
        } catch (e: Exception) {
            htmlBuilder.append("<p style='text-align:center; color:red'>حدث خطأ في تحميل السورة</p>")
        }

        htmlBuilder.append("</div>")
        htmlBuilder.append("</body></html>")

        return htmlBuilder.toString()
    }
}