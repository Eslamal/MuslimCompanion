package com.eslamdev.islamic.presentation.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.eslamdev.islamic.R
import com.eslamdev.islamic.data.repository.AzkarRepositoryImpl
import com.eslamdev.islamic.databinding.FragmentAzkarBinding
import com.eslamdev.islamic.domain.repository.AzkarType
import com.eslamdev.islamic.presentation.ui.adapter.AzkarAdapter
import com.eslamdev.islamic.presentation.viewmodel.AzkarState
import com.eslamdev.islamic.presentation.viewmodel.AzkarViewModel
import com.eslamdev.islamic.presentation.viewmodel.AzkarViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AzkarFragment : Fragment() {

    private var _binding: FragmentAzkarBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AzkarViewModel
    private val azkarAdapter = AzkarAdapter()
    private var azkarType: AzkarType = AzkarType.SABAH // Default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val typeName = it.getString(ARG_AZKAR_TYPE)
            if (typeName != null) {
                azkarType = AzkarType.valueOf(typeName)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAzkarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // تفعيل زر الرجوع
        // بما إننا داخل Fragment بنستخدم requireActivity().finish() لغلق الـ Activity الحالية
        val btnBack = view.findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            requireActivity().finish()
        }

        setupViewModel()
        setupRecyclerView()
        setupTitle()
        observeState()

        viewModel.loadAzkar(azkarType)
    }

    private fun setupViewModel() {
        val repository = AzkarRepositoryImpl(requireContext())
        val factory = AzkarViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AzkarViewModel::class.java]
    }

    private fun setupRecyclerView() {
        binding.rvAzkar.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = azkarAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupTitle() {
        val title = when (azkarType) {
            AzkarType.SABAH -> "أذكار الصباح"
            AzkarType.MASSA -> "أذكار المساء"
            AzkarType.AFTER_PRAYER -> "أذكار بعد الصلاة"
        }
        // استخدام الـ ID الجديد للعنوان في التول بار
        binding.toolbar.findViewById<TextView>(R.id.toolbar_title).text = title
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.azkarState.collectLatest { state ->
                when (state) {
                    is AzkarState.Loading -> {
                        binding.progressBar.isVisible = true
                        binding.rvAzkar.isVisible = false
                        binding.tvError.isVisible = false
                    }
                    is AzkarState.Success -> {
                        binding.progressBar.isVisible = false
                        binding.rvAzkar.isVisible = true
                        azkarAdapter.submitList(state.azkar)
                    }
                    is AzkarState.Error -> {
                        binding.progressBar.isVisible = false
                        binding.tvError.isVisible = true
                        binding.tvError.text = state.message
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_AZKAR_TYPE = "azkar_type"

        fun newInstance(type: AzkarType): AzkarFragment {
            val fragment = AzkarFragment()
            val args = Bundle()
            args.putString(ARG_AZKAR_TYPE, type.name)
            fragment.arguments = args
            return fragment
        }
    }
}