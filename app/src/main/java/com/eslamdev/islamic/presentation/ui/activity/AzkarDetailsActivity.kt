package com.eslamdev.islamic.presentation.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eslamdev.islamic.R
import com.eslamdev.islamic.domain.repository.AzkarType
import com.eslamdev.islamic.presentation.ui.fragment.AzkarFragment

class AzkarDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_azkar_details)

        if (savedInstanceState == null) {
            val typeString = intent.getStringExtra("EXTRA_AZKAR_TYPE") ?: AzkarType.SABAH.name
            val type = AzkarType.valueOf(typeString)

            val fragment = AzkarFragment.newInstance(type)

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
}