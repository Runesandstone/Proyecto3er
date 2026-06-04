package com.example.proyecto_parcial3.ui.slideshow

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_parcial3.DBHelper
import com.example.proyecto_parcial3.databinding.FragmentListviajesBinding
import com.example.proyecto_parcial3.model.Viaje
import com.example.proyecto_parcial3.ui.adapter.ViajeAdapter
import java.util.Calendar

class ListViajesFragment : Fragment() {

    private var _binding: FragmentListviajesBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: DBHelper
    private lateinit var viajeAdapter: ViajeAdapter
    private var idUsuarioActivo: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListviajesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBHelper(requireContext())

        idUsuarioActivo = activity?.intent?.getIntExtra("ID_USUARIO_ACTIVO", -1) ?: -1
        if (idUsuarioActivo == -1) {
            idUsuarioActivo = 1
        }

        binding.rvViajes.layoutManager = LinearLayoutManager(requireContext())
        configurarListaYAdapter()
    }

    private fun configurarListaYAdapter() {
        val viajes = dbHelper.obtenerListaViajesPorUsuario(idUsuarioActivo)

        viajeAdapter = ViajeAdapter(
            viajes,
            onEditarClick = { viaje -> mostrarDialogoEditar(viaje) },
            onEliminarClick = { viaje -> mostrarDialogoEliminar(viaje) }
        )
        binding.rvViajes.adapter = viajeAdapter
        comprobarListaVacia(viajes.isEmpty())
    }

    // FUNCIÓN PARA ELIMINAR VIAJE
    private fun mostrarDialogoEliminar(viaje: Viaje) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Viaje")
            .setMessage("¿Estás seguro de que deseas eliminar el viaje a ${viaje.destino}?")
            .setPositiveButton("Eliminar") { _, _ ->
                val filasAfectadas = dbHelper.eliminarViaje(viaje.id)
                if (filasAfectadas > 0) {
                    Toast.makeText(requireContext(), "Viaje eliminado con éxito", Toast.LENGTH_SHORT).show()
                    refrescarLista()
                } else {
                    Toast.makeText(requireContext(), "Error al eliminar el viaje", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // 🔴 FUNCIÓN AVANZADA PARA EDITAR VIAJE (INCLUYE CALENDARIOS)
    private fun mostrarDialogoEditar(viaje: Viaje) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Editar datos de tu viaje")

        // Contenedor principal del formulario emergente
        val contenedorEstructura = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 30, 60, 30)
        }

        // 1. Campo Presupuesto
        val inputPresupuesto = EditText(requireContext()).apply {
            hint = "Presupuesto"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(viaje.presupuesto.toString())
        }

        // 2. Campo Fecha de Inicio (Deshabilitamos escritura directa para obligar a usar el calendario)
        val inputFechaInicio = EditText(requireContext()).apply {
            hint = "Fecha de Inicio"
            isFocusable = false
            isClickable = true
            setText(viaje.fecha)
            setOnClickListener {
                mostrarCalendarioEditar { fechaSeleccionada -> setText(fechaSeleccionada) }
            }
        }

        // 3. Campo Fecha Fin (Igual, abre calendario al clickear)
        val inputFechaFin = EditText(requireContext()).apply {
            hint = "Fecha de Finalización"
            isFocusable = false
            isClickable = true
            setText(viaje.fFin)
            setOnClickListener {
                mostrarCalendarioEditar { fechaSeleccionada -> setText(fechaSeleccionada) }
            }
        }

        // 4. Campo Notas
        val inputNotas = EditText(requireContext()).apply {
            hint = "Notas del viaje"
            setText(viaje.notas)
        }

        // Añadimos todos los campos visuales ordenadamente en vertical
        contenedorEstructura.addView(inputPresupuesto)
        contenedorEstructura.addView(inputFechaInicio)
        contenedorEstructura.addView(inputFechaFin)
        contenedorEstructura.addView(inputNotas)
        builder.setView(contenedorEstructura)

        // Acción al guardar cambios
        builder.setPositiveButton("Guardar Cambios") { _, _ ->
            val nuevoPresupuesto = inputPresupuesto.text.toString().toDoubleOrNull() ?: viaje.presupuesto
            val nuevaFechaInicio = inputFechaInicio.text.toString().trim()
            val nuevaFechaFin = inputFechaFin.text.toString().trim()
            val nuevasNotas = inputNotas.text.toString().trim()

            // Mandamos a reescribir todo en SQLite pasándole los datos actualizados
            val resultado = dbHelper.actualizarViaje(
                idViaje = viaje.id,
                destino = viaje.destino,       // Se mantiene igual
                fechaInicio = nuevaFechaInicio, // 👈 Nueva Fecha Inicio guardada
                fechaFin = nuevaFechaFin,       // 👈 Nueva Fecha Fin guardada
                presupuesto = nuevoPresupuesto,
                transporte = viaje.transporte,   // Se mantiene igual
                notas = nuevasNotas
            )

            if (resultado > 0) {
                Toast.makeText(requireContext(), "Viaje actualizado correctamente", Toast.LENGTH_SHORT).show()
                refrescarLista()
            } else {
                Toast.makeText(requireContext(), "Error al actualizar en Base de Datos", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    // Selector de fechas interno para la ventana de edición
    private fun mostrarCalendarioEditar(alSeleccionar: (String) -> Unit) {
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

    private fun refrescarLista() {
        val viajesActualizados = dbHelper.obtenerListaViajesPorUsuario(idUsuarioActivo)
        viajeAdapter.actualizarLista(viajesActualizados)
        comprobarListaVacia(viajesActualizados.isEmpty())
    }

    private fun comprobarListaVacia(vacia: Boolean) {
        if (vacia) {
            Toast.makeText(requireContext(), "No hay viajes registrados aún.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viajeAdapter.isInitialized) {
            refrescarLista()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}