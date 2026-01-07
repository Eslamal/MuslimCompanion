package com.eslamdev.islamic.presentation.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eslamdev.islamic.data.model.HadithModel
import com.eslamdev.islamic.databinding.ActivityHadithDetailBinding

class HadithDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHadithDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHadithDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // استقبال البيانات بالمودل الجديد
        @Suppress("DEPRECATION")
        val hadith = intent.getSerializableExtra("hadith_object") as? HadithModel

        if (hadith != null) {
            binding.hadithDetailTitle.text = hadith.title
            binding.hadithDetailText.text = hadith.content

            // لو مفيش شرح، نخفي العنوان بتاعه عشان الشكل العام
            if (hadith.description.isNotBlank()) {
                binding.hadithDetailDescription.text = hadith.description
            } else {
                binding.hadithDetailDescription.text = "لا يوجد شرح متاح حالياً"
            }
        }

        binding.btnBack.setOnClickListener{
            finish()
        }
    }
}