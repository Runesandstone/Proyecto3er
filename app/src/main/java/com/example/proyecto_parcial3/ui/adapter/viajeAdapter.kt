package com.example.proyecto_parcial3.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_parcial3.databinding.ItemViajeBinding
import com.example.proyecto_parcial3.model.Viaje

class ViajeAdapter(private var listaViajes: List<Viaje>) :
    RecyclerView.Adapter<ViajeAdapter.ViajeViewHolder>() {

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
            tvTransporte.text = viaje.transporte

            // Usamos las variables que coinciden con su modelo de datos
            tvFecha.text = "Inicio: ${viaje.fecha}"
            tvFecha2.text = "Fin: ${viaje.fFin}" // 👈 Sincronizado con fFin
        }
    }

    override fun getItemCount(): Int = listaViajes.size

    // Función necesaria para refrescar la pantalla sin crasheos
    fun actualizarLista(nuevaLista: List<Viaje>) {
        this.listaViajes = nuevaLista
        notifyDataSetChanged()
    }
}