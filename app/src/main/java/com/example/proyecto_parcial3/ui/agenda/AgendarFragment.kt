package com.example.proyecto_parcial3.ui.agenda

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.proyecto_parcial3.databinding.FragmentAgendarBinding
import java.util.Calendar

class AgendarFragment : Fragment() {

    private var _binding: FragmentAgendarBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentAgendarBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.editFecha.setOnClickListener {
            mostrarDatePicker()
        }

        return root
    }

    private fun mostrarDatePicker() {
        val calendario = Calendar.getInstance()
        val año = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val fechaSeleccionada = "$dayOfMonth/${month + 1}/$year"
            binding.editFecha.setText(fechaSeleccionada)
        }, año, mes, dia)

        val mañana = Calendar.getInstance()
        mañana.add(Calendar.DAY_OF_MONTH, 1)
        datePicker.datePicker.minDate = mañana.timeInMillis

        datePicker.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}