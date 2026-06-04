package com.example.proyecto_parcial3.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_parcial3.DBHelper
import com.example.proyecto_parcial3.databinding.FragmentListviajesBinding
import com.example.proyecto_parcial3.ui.adapter.ViajeAdapter

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
            idUsuarioActivo = 1 // ID de pruebas
        }

        // rvViajes mapea directamente a @+id/rv_viajes del XML real
        binding.rvViajes.layoutManager = LinearLayoutManager(requireContext())

        val viajes = dbHelper.obtenerListaViajesPorUsuario(idUsuarioActivo)

        if (viajes.isNotEmpty()) {
            viajeAdapter = ViajeAdapter(viajes)
            binding.rvViajes.adapter = viajeAdapter
        } else {
            Toast.makeText(requireContext(), "No hay viajes registrados aún.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viajeAdapter.isInitialized) {
            val viajesActualizados = dbHelper.obtenerListaViajesPorUsuario(idUsuarioActivo)
            viajeAdapter.actualizarLista(viajesActualizados)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}