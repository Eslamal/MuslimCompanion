package com.eslamdev.islamic.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.eslamdev.islamic.data.repository.HadithRepository
import com.eslamdev.islamic.databinding.ActivityHadithListBinding
import com.eslamdev.islamic.presentation.ui.adapter.HadithAdapter
import com.eslamdev.islamic.presentation.viewmodel.HadithViewModel
import com.eslamdev.islamic.presentation.viewmodel.HadithViewModelFactory
import kotlinx.coroutines.flow.collectLatest

class HadithListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHadithListBinding
    private lateinit var viewModel: HadithViewModel
    private val adapter = HadithAdapter { hadith ->
        val intent = Intent(this, HadithDetailActivity::class.java)
        intent.putExtra("hadith_object", hadith)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHadithListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = HadithRepository(this)
        val factory = HadithViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[HadithViewModel::class.java]
        binding.btnBack.setOnClickListener{
            finish()
        }
        setupRecyclerView()

        lifecycleScope.launchWhenStarted {
            viewModel.hadiths.collectLatest { list ->
                adapter.submitList(list)
            }
        }

        viewModel.loadHadiths()
    }

    private fun setupRecyclerView() {
        binding.hadithListRv.layoutManager = LinearLayoutManager(this)
        binding.hadithListRv.adapter = adapter
    }
}