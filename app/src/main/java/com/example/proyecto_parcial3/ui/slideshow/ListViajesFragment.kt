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
import java.io.File
import java.io.FileOutputStream
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
            onEliminarClick = { viaje -> mostrarDialogoEliminar(viaje) },
            onCompartirClick = { viaje, vistaTarjeta ->
                //Llamamos a la función encargada de hacer el render de la imagen y compartir
                compartirTarjetaComoImagen(viaje, vistaTarjeta)
            }
        )
        binding.rvViajes.adapter = viajeAdapter
        comprobarListaVacia(viajes.isEmpty())
    }

    private fun compartirTarjetaComoImagen(viaje: Viaje, vista: View) {
        try {
            // 1. Crear un Bitmap del tamaño de la tarjeta
            val bitmap = android.graphics.Bitmap.createBitmap(
                vista.width, vista.height, android.graphics.Bitmap.Config.ARGB_8888
            )

            val canvas = android.graphics.Canvas(bitmap)
            vista.draw(canvas)

            // 2. guardar temporalmente la imagen para poder compartirla
            val carpetaCache = File(requireContext().cacheDir, "viajes_compartidos")
            carpetaCache.mkdirs()
            val archivo = File(carpetaCache, "viaje_${viaje.id}.png")
            val flujoSalida = FileOutputStream(archivo)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, flujoSalida)
            flujoSalida.flush()
            flujoSalida.close()

            // 3. Obtener la URI segura usando FileProvider
            val uriImagen = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                archivo
            )

            // 4. Lanzar el menú para compartir de Android
            val intentCompartir = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(android.content.Intent.EXTRA_STREAM, uriImagen)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "¡Mi próximo viaje a ${viaje.destino}")
                putExtra(android.content.Intent.EXTRA_TEXT, "Mira los detalles de mi viaje a ${viaje.destino} planeado para el ${viaje.fecha}")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(android.content.Intent.createChooser(intentCompartir, "Compartir viaje vía:"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al generar la imagen de vaije", Toast.LENGTH_SHORT).show()
        }
    }

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

    private fun mostrarDialogoEditar(viaje: Viaje) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Editar datos de tu viaje")

        val contenedorEstructura = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 30, 60, 30)
        }

        val inputPresupuesto = EditText(requireContext()).apply {
            hint = "Presupuesto"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(viaje.presupuesto.toString())
        }

        var fechaInicioMilis = obtenerMilisDesdeFecha(viaje.fecha)

        val inputFechaFin = EditText(requireContext()).apply {
            hint = "Fecha de Finalización"
            isFocusable = false
            isClickable = true
            setText(viaje.fFin)
        }

        val inputFechaInicio = EditText(requireContext()).apply {
            hint = "Fecha de Inicio"
            isFocusable = false
            isClickable = true
            setText(viaje.fecha)
            setOnClickListener {
                mostrarCalendarioInicioEditar { fechaSeleccionada, milisegundos ->
                    setText(fechaSeleccionada)
                    fechaInicioMilis = milisegundos
                    inputFechaFin.setText("")
                }
            }
        }

        inputFechaFin.setOnClickListener {
            if (fechaInicioMilis == 0L) {
                Toast.makeText(requireContext(), "Por favor, selecciona primero la fecha de inicio", Toast.LENGTH_SHORT).show()
            } else {
                mostrarCalendarioFinEditar(fechaInicioMilis) { fechaSeleccionada ->
                    inputFechaFin.setText(fechaSeleccionada)
                }
            }
        }

        val inputNotas = EditText(requireContext()).apply {
            hint = "Notas del viaje"
            setText(viaje.notas)
        }

        contenedorEstructura.addView(inputPresupuesto)
        contenedorEstructura.addView(inputFechaInicio)
        contenedorEstructura.addView(inputFechaFin)
        contenedorEstructura.addView(inputNotas)
        builder.setView(contenedorEstructura)

        builder.setPositiveButton("Guardar Cambios") { _, _ ->
            val nuevoPresupuesto = inputPresupuesto.text.toString().toDoubleOrNull() ?: viaje.presupuesto
            val nuevaFechaInicio = inputFechaInicio.text.toString().trim()
            val nuevaFechaFin = inputFechaFin.text.toString().trim()
            val nuevasNotas = inputNotas.text.toString().trim()

            if (nuevaFechaInicio.isEmpty() || nuevaFechaFin.isEmpty()) {
                Toast.makeText(requireContext(), "Las fechas no pueden estar vacías", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val resultado = dbHelper.actualizarViaje(
                idViaje = viaje.id,
                destino = viaje.destino,
                fechaInicio = nuevaFechaInicio,
                fechaFin = nuevaFechaFin,
                presupuesto = nuevoPresupuesto,
                transporte = viaje.transporte,
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

    private fun obtenerMilisDesdeFecha(fecha: String): Long {
        return try {
            val partes = fecha.split("/")
            Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, partes[0].toInt())
                set(Calendar.MONTH, partes[1].toInt() - 1)
                set(Calendar.YEAR, partes[2].toInt())
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        } catch (e: Exception) {
            0L
        }
    }

    private fun mostrarCalendarioInicioEditar(alSeleccionar: (String, Long) -> Unit) {
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

    private fun mostrarCalendarioFinEditar(minDate: Long, alSeleccionar: (String) -> Unit) {
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
        if (minDate > 0) {
            dialog.datePicker.minDate = minDate
        } else {
            dialog.datePicker.minDate = System.currentTimeMillis() - 1000
        }
        dialog.show()
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