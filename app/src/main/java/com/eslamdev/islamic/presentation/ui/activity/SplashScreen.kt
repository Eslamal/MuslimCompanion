package com.eslamdev.islamic.presentation.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.eslamdev.islamic.R

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // لو حابب تخفي البار العلوي تماماً وتخليها Full Screen
        // window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        // اللوجو الثابت هيظهر فوراً من الثيم، وأول ما الـ Layout ده يحمل
        // الـ Lottie Animation هيبدأ يشتغل فوقه، فالمستخدم هيحس إن اللوجو "دبت فيه الروح"

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // مهم جداً عشان لما يرجع ميرجعش للسبلاش
        }, 2000)
    }
}