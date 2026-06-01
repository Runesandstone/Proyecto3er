package com.example.proyecto_parcial3

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_parcial3.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración del botón para navegar a Menu_activity
        binding.btnLogIn.setOnClickListener {
            val intent = Intent(this, Menu_activity::class.java)
            startActivity(intent)
            // finish() // Descomenta si no quieres que el usuario vuelva al "login" con el botón atrás
        }
    }
}