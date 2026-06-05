package com.example.proyecto_parcial3.ui.agenda

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.proyecto_parcial3.DBHelper
import com.example.proyecto_parcial3.R
import com.example.proyecto_parcial3.databinding.FragmentAgendarBinding
import java.util.Calendar

class AgendarFragment : Fragment() {

    private var _binding: FragmentAgendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBHelper
    private var idUsuarioActivo: Int = -1
    private var fechaInicioMilis: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBHelper(requireContext())

        idUsuarioActivo = activity?.intent?.getIntExtra("ID_USUARIO_ACTIVO", -1) ?: -1
        if (idUsuarioActivo == -1) {
            idUsuarioActivo = 1
        }

        val lanzarCalendarioInicio = {
            mostrarCalendarioInicio { fecha, milisegundos ->
                binding.etFecha.setText(fecha)
                binding.layoutFecha.error = null
                fechaInicioMilis = milisegundos
                binding.etFechaFin.text?.clear()
            }
        }
        binding.etFecha.setOnClickListener { lanzarCalendarioInicio() }
        binding.layoutFecha.setOnClickListener { lanzarCalendarioInicio() }

        val lanzarCalendarioFin = {
            if (fechaInicioMilis == 0L) {
                Toast.makeText(requireContext(), "Por favor, selecciona primero la fecha de inicio", Toast.LENGTH_SHORT).show()
            } else {
                mostrarCalendarioFin { fecha ->
                    binding.etFechaFin.setText(fecha)
                    binding.layoutFechaFin.error = null
                }
            }
        }
        binding.etFechaFin.setOnClickListener { lanzarCalendarioFin() }
        binding.layoutFechaFin.setOnClickListener { lanzarCalendarioFin() }

        binding.btnGuardar.setOnClickListener {
            binding.layoutFecha.error = null
            binding.layoutFechaFin.error = null
            binding.layoutPresupuesto.error = null

            val destino = binding.spinnerDestino.selectedItem?.toString() ?: "No seleccionado"
            val fechaInicio = binding.etFecha.text.toString().trim()
            val fechaFin = binding.etFechaFin.text.toString().trim()
            val presupuestoStr = binding.etPresupuesto.text.toString().trim()
            val notas = binding.etNotas.text.toString().trim()

            val transportesSeleccionados = mutableListOf<String>()

            if (binding.chipAvion.isChecked) transportesSeleccionados.add("Avión")
            if (binding.chipAuto.isChecked) transportesSeleccionados.add("Auto")
            if (binding.chipBus.isChecked) transportesSeleccionados.add("Bus")

            val transporte = transportesSeleccionados.joinToString(", ")

            var esFormularioValido = true

            if (fechaInicio.isEmpty()) {
                binding.layoutFecha.error = "Debes seleccionar una fecha de inicio"
                esFormularioValido = false
            }

            if (fechaFin.isEmpty()) {
                binding.layoutFechaFin.error = "Debes seleccionar una fecha de finalización"
                esFormularioValido = false
            }

            if (presupuestoStr.isEmpty()) {
                binding.layoutPresupuesto.error = "El presupuesto es obligatorio"
                esFormularioValido = false
            }

            if (transporte.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Por favor, selecciona al menos un medio de transporte",
                    Toast.LENGTH_LONG
                ).show()
                esFormularioValido = false
            }

            if (!esFormularioValido) {
                return@setOnClickListener
            }

            val presupuesto = presupuestoStr.toDoubleOrNull() ?: 0.0

            val resultado = dbHelper.insertarViaje(
                idUsuarioActivo,
                destino,
                fechaInicio,
                fechaFin,
                presupuesto,
                transporte,
                notas
            )

            if (resultado != -1L) {
                Toast.makeText(
                    requireContext(),
                    "¡Viaje a $destino guardado con éxito!",
                    Toast.LENGTH_LONG
                ).show()

                binding.etFecha.text?.clear()
                binding.etFechaFin.text?.clear()
                binding.etPresupuesto.text?.clear()
                binding.etNotas.text?.clear()
                binding.chipAvion.isChecked = false
                binding.chipAuto.isChecked = false
                binding.chipBus.isChecked = false
                fechaInicioMilis = 0
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error al guardar en la base de datos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun mostrarCalendarioInicio(alSeleccionar: (String, Long) -> Unit) {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val fechaFormateada = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                alSeleccionar(fechaFormateada, selectedCalendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.datePicker.minDate = System.currentTimeMillis() - 1000
        dialog.show()
    }

    private fun mostrarCalendarioFin(alSeleccionar: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val fechaFormateada = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                alSeleccionar(fechaFormateada)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        if (fechaInicioMilis > 0) {
            dialog.datePicker.minDate = fechaInicioMilis
        } else {
            dialog.datePicker.minDate = System.currentTimeMillis() - 1000
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}