package com.eslamdev.islamic.presentation.ui.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eslamdev.islamic.R
import com.eslamdev.islamic.data.repository.DuaRepository
import com.eslamdev.islamic.presentation.ui.adapter.DuaAdapter
import com.eslamdev.islamic.presentation.viewmodel.DuaViewModel
import com.eslamdev.islamic.presentation.viewmodel.DuaViewModelFactory
import kotlinx.coroutines.flow.collectLatest

class DuaActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: DuaViewModel
    private lateinit var adapter: DuaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dua)

        recyclerView = findViewById(R.id.dua_rv)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = DuaAdapter { selectedDuaContent ->
            showContentDialog("دعاء", selectedDuaContent, R.drawable.doaa)
        }

        recyclerView.adapter = adapter

        val repository = DuaRepository(this)
        val factory = DuaViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[DuaViewModel::class.java]

        lifecycleScope.launchWhenStarted {
            viewModel.duaList.collectLatest { list ->
                adapter.submitList(list)
            }
        }

        viewModel.loadDuas()

        findViewById<View>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun showContentDialog(title: String, content: String, iconRes: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_daily_content, null)

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
        val dialogText = dialogView.findViewById<TextView>(R.id.dialog_text)
        val dialogIcon = dialogView.findViewById<ImageView>(R.id.dialog_icon)
        val closeButton = dialogView.findViewById<Button>(R.id.dialog_close_button)
        val copyButton = dialogView.findViewById<Button>(R.id.btn_copy)

        dialogTitle.text = title
        dialogText.text = content
        dialogIcon.setImageResource(iconRes)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        closeButton.setOnClickListener { dialog.dismiss() }

        copyButton.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Islamic App Dua", content)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "تم نسخ الدعاء", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }
}