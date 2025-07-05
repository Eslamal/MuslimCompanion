package com.example.islamic

import android.content.Intent
import android.os.Bundle
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.islamic.view.Azkar
import com.example.islamic.view.PrayerActivity
import com.example.islamic.view.Surah
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val cardQuran: CardView = findViewById(R.id.card_quran)
        val cardAzkar: CardView = findViewById(R.id.card_azkar)
        val cardAzan: CardView = findViewById(R.id.card_azan)

        cardQuran.setOnClickListener {

            val intent = Intent(this, Surah::class.java)
            startActivity(intent)
        }

        cardAzkar.setOnClickListener {

            val intent = Intent(this, Azkar::class.java)
            startActivity(intent)
        }

        cardAzan.setOnClickListener {

            val intent = Intent(this, PrayerActivity::class.java)
            startActivity(intent)
        }
    }
    }
