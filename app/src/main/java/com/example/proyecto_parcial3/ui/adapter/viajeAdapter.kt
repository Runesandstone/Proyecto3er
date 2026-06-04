package com.example.proyecto_parcial3.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_parcial3.databinding.ItemViajeBinding
import com.example.proyecto_parcial3.model.Viaje

class ViajeAdapter(
    private var listaViajes: List<Viaje>,
    private val onEditarClick: (Viaje) -> Unit,   // Función callback para editar
    private val onEliminarClick: (Viaje) -> Unit // Función callback para eliminar
) : RecyclerView.Adapter<ViajeAdapter.ViajeViewHolder>() {

    class ViajeViewHolder(val binding: ItemViajeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViajeViewHolder {
        val binding = ItemViajeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViajeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViajeViewHolder, position: Int) {
        val viaje = listaViajes[position]
        with(holder.binding) {
            tvDestino.text = viaje.destino
            tvPresupuesto.text = String.format("$%.2f", viaje.presupuesto)
            tvFecha.text = "Inicio: ${viaje.fecha}"
            tvFecha2.text = "Fin: ${viaje.fFin}"
            tvTransporte.text = viaje.transporte

            // 🛠️ VINCULACIÓN DE NOTAS: Ahora se asigna correctamente al TextView del XML
            tvNotas.text = if (viaje.notas.isNotEmpty()) "Notas: ${viaje.notas}" else "Sin notas adicionales"

            ibtnEditar.setOnClickListener { onEditarClick(viaje) }
            ibtnBorrar.setOnClickListener { onEliminarClick(viaje) }
            ibtnCompartir.setOnClickListener { /* Acción compartir (de tu otro compañero) */ }
        }
    }

    override fun getItemCount(): Int = listaViajes.size

    // 🛠️ FUNCIÓN AGREGADA: Crucial para que los cambios de edición/eliminación se reflejen al instante
    fun actualizarLista(nuevaLista: List<Viaje>) {
        this.listaViajes = nuevaLista
        notifyDataSetChanged()
    }
} // Se corrigió el cierre de llaves aquí