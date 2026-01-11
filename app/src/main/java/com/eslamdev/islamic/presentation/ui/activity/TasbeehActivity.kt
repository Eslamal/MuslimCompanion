package com.eslamdev.islamic.presentation.ui.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.eslamdev.islamic.R
import com.eslamdev.islamic.databinding.ActivityTasbeehBinding

class TasbeehActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTasbeehBinding

    private val PREFS_NAME = "tasbeehSharedPrefs"
    private val KEY_SUBHAN = "subhanAllahKey"
    private val KEY_HAMD = "alhamdulillahKey"
    private val KEY_AKBAR = "allahuAkbarKey"
    private val KEY_ASTAGHFIR = "astaghfirullahKey"

    private var subhanCount = 0
    private var hamdCount = 0
    private var akbarCount = 0
    private var astaghfirCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasbeehBinding.inflate(layoutInflater)
        setContentView(binding.root)

       binding.btnBack.setOnClickListener {
            finish()
        }

        loadCounts()
        updateUI()

        binding.cardSubhanView.setOnClickListener {
            subhanCount++
            saveCount(KEY_SUBHAN, subhanCount)
            updateUI()
        }

        binding.cardAlhamdView.setOnClickListener {
            hamdCount++
            saveCount(KEY_HAMD, hamdCount)
            updateUI()
        }

        binding.cardAkbarView.setOnClickListener {
            akbarCount++
            saveCount(KEY_AKBAR, akbarCount)
            updateUI()
        }

        binding.cardAstaghfarView.setOnClickListener {
            astaghfirCount++
            saveCount(KEY_ASTAGHFIR, astaghfirCount)
            updateUI()
        }

        binding.tvResetSubhanallah.setOnClickListener {
            subhanCount = 0
            saveCount(KEY_SUBHAN, 0)
            updateUI()
        }

        binding.tvResetAlhamdulillah.setOnClickListener {
            hamdCount = 0
            saveCount(KEY_HAMD, 0)
            updateUI()
        }

        binding.tvResetAllahuakbar.setOnClickListener {
            akbarCount = 0
            saveCount(KEY_AKBAR, 0)
            updateUI()
        }

        binding.tvResetAstaghfirullah.setOnClickListener {
            astaghfirCount = 0
            saveCount(KEY_ASTAGHFIR, 0)
            updateUI()
        }

        binding.cardResetAll.setOnClickListener {
            subhanCount = 0
            hamdCount = 0
            akbarCount = 0
            astaghfirCount = 0

            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            updateUI()
        }
    }

    private fun updateUI() {
        binding.tvSubhanAllahCount.text = subhanCount.toString()
        binding.tvAlhamdulillahCount.text = hamdCount.toString()
        binding.tvAllahuAkbarCount.text = akbarCount.toString()
        binding.tvAstaghfirullahCount.text = astaghfirCount.toString()
    }

    private fun loadCounts() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        subhanCount = prefs.getInt(KEY_SUBHAN, 0)
        hamdCount = prefs.getInt(KEY_HAMD, 0)
        akbarCount = prefs.getInt(KEY_AKBAR, 0)
        astaghfirCount = prefs.getInt(KEY_ASTAGHFIR, 0)
    }

    private fun saveCount(key: String, value: Int) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(key, value).apply()
    }
}