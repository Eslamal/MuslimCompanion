package com.eslamdev.islamic.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.eslamdev.islamic.R
import com.eslamdev.islamic.databinding.ActivityAzkarBinding
import com.eslamdev.islamic.domain.repository.AzkarType

class Azkar : AppCompatActivity() {

    private lateinit var binding: ActivityAzkarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAzkarBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.cardAzkarAlsabah.setOnClickListener {
            openAzkarDetails(AzkarType.SABAH)
        }

        binding.cardAzkarAlmassa.setOnClickListener {
            openAzkarDetails(AzkarType.MASSA)
        }

        binding.cardAzkarAfter.setOnClickListener {
            openAzkarDetails(AzkarType.AFTER_PRAYER)
        }
    }

    private fun openAzkarDetails(type: AzkarType) {
        val intent = Intent(this, AzkarDetailsActivity::class.java)
        intent.putExtra("EXTRA_AZKAR_TYPE", type.name)
        startActivity(intent)
    }
}