package com.eslamdev.islamic.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.eslamdev.islamic.data.repository.QuranRepository
import com.eslamdev.islamic.databinding.ActivitySurahBinding
import com.eslamdev.islamic.presentation.ui.adapter.SurahAdapter
import com.eslamdev.islamic.presentation.viewmodel.QuranViewModel
import com.eslamdev.islamic.presentation.viewmodel.QuranViewModelFactory
import com.eslamdev.islamic.presentation.ui.activity.TafseerDetailActivity
import kotlinx.coroutines.flow.collectLatest

class SurahListActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySurahBinding
    private lateinit var viewModel: QuranViewModel

    private var isTafseerMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurahBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isTafseerMode = intent.getBooleanExtra("IS_TAFSEER_MODE", false)

        binding.toolbar.findViewById<android.widget.TextView>(com.eslamdev.islamic.R.id.toolbar_title).text =
            if (isTafseerMode) "تفسير القرآن" else "القرآن الكريم"

        binding.toolbar.findViewById<android.view.View>(com.eslamdev.islamic.R.id.btn_back).setOnClickListener {
            finish()
        }

        setupViewModel()
        setupRecyclerView()
    }

    private fun setupViewModel() {
        val repository = QuranRepository(this)
        val factory = QuranViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[QuranViewModel::class.java]

        viewModel.loadSurahs()
    }

    private fun setupRecyclerView() {
        val adapter = SurahAdapter { surah ->
            if (isTafseerMode) {
                // الذهاب للتفسير
                val intent = Intent(this, TafseerDetailActivity::class.java)
                intent.putExtra("SURA_NO", surah.id)
                intent.putExtra("SURA_NAME", surah.name)
                startActivity(intent)
            } else {
                val intent = Intent(this, SurahDetailActivity::class.java)
                intent.putExtra("index", surah.id)
                intent.putExtra("name", surah.name)
                startActivity(intent)
            }
        }

        binding.surahRv.layoutManager = LinearLayoutManager(this)
        binding.surahRv.adapter = adapter

        lifecycleScope.launchWhenStarted {
            viewModel.surahList.collectLatest { list ->
                adapter.submitList(list)
            }
        }
    }
}