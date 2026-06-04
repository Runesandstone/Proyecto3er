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

        // Recuperar ID de usuario (Modo seguro para Fragments)
        idUsuarioActivo = activity?.intent?.getIntExtra("ID_USUARIO_ACTIVO", -1) ?: -1
        if (idUsuarioActivo == -1) {
            idUsuarioActivo = 1 // ID de respaldo para tus pruebas locales
        }

        // =======================================================
        // 1. SELECTOR DEL PRIMER CALENDARIO (FECHA INICIO)
        // =======================================================
        val lanzarCalendarioInicio = {
            mostrarCalendario { fecha ->
                binding.editFecha.setText(fecha)
                binding.layoutFecha.error = null
            }
        }
        binding.editFecha.setOnClickListener { lanzarCalendarioInicio() }
        binding.layoutFecha.setOnClickListener { lanzarCalendarioInicio() }

        // =======================================================
        // 2. SELECTOR DEL SEGUNDO CALENDARIO (FECHA FIN)
        // =======================================================
        val lanzarCalendarioFin = {
            mostrarCalendario { fecha ->
                binding.editFechaFin.setText(fecha)
                binding.layoutFechaFin.error = null
            }
        }
        binding.editFechaFin.setOnClickListener { lanzarCalendarioFin() }
        binding.layoutFechaFin.setOnClickListener { lanzarCalendarioFin() }

        // =======================================================
        // 3. ACCIÓN Y VALIDACIÓN DEL BOTÓN GUARDAR VIAJE
        // =======================================================
        binding.btnGuardar.setOnClickListener {
            // Limpiar indicadores visuales de error previos
            binding.layoutFecha.error = null
            binding.layoutFechaFin.error = null
            binding.layoutPresupuesto.error = null

            // Captura de datos básicos
            val destino = binding.spinnerDestino.selectedItem?.toString() ?: "No seleccionado"
            val fechaInicio = binding.editFecha.text.toString().trim()
            val fechaFin = binding.editFechaFin.text.toString().trim()
            val presupuestoStr = binding.editPresupuesto.text.toString().trim()
            val notas = binding.editNotas.text.toString().trim()

            // Validar uno o varios chips seleccionados a la vez
            val transportesSeleccionados = mutableListOf<String>()

            if (binding.chipAvion.isChecked) transportesSeleccionados.add("Avión")
            if (binding.chipAuto.isChecked) transportesSeleccionados.add("Auto")
            if (binding.chipBus.isChecked) transportesSeleccionados.add("Bus")

            // Convertimos la lista a un solo texto separado por comas (Ej: "Avión, Auto")
            val transporte = transportesSeleccionados.joinToString(", ")

            // =======================================================
            // VALIDADOR DETALLADO DE CAMPOS VACÍOS
            // =======================================================
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

            // Si la cadena está vacía significa que no marcó ningún Chip
            if (transporte.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Por favor, selecciona al menos un medio de transporte",
                    Toast.LENGTH_LONG
                ).show()
                esFormularioValido = false
            }

            // Si algo falló, no guardamos y detenemos la ejecución aquí
            if (!esFormularioValido) {
                return@setOnClickListener
            }

            // Conversión segura de presupuesto
            val presupuesto = presupuestoStr.toDoubleOrNull() ?: 0.0

            // 4. ALMACENAMIENTO EN SQLITE
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

                // Limpiar el formulario para un nuevo registro
                binding.editFecha.text?.clear()
                binding.editFechaFin.text?.clear()
                binding.editPresupuesto.text?.clear()
                binding.editNotas.text?.clear()
                binding.chipAvion.isChecked = false
                binding.chipAuto.isChecked = false
                binding.chipBus.isChecked = false
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error al guardar en la base de datos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // AÑADIDO: Función encargada de instanciar y mostrar el DatePickerDialog de Android
    private fun mostrarCalendario(alSeleccionar: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val fechaFormateada = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                alSeleccionar(fechaFormateada)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}