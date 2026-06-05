package com.example.proyecto_parcial3

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_parcial3.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHelper(this)

        binding.btnLogIn.setOnClickListener {
            val usuario = binding.etUser.text.toString().trim()
            val contra = binding.etPassword.text.toString().trim()

            if (usuario.isEmpty() || contra.isEmpty()) {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val idUsuarioLogueado = dbHelper.verificarUsuario(usuario, contra)

            if (idUsuarioLogueado != -1) {
                // GUARDAMOS EL ID PARA QUE profileActivity LO PUEDA LEER
                val prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
                prefs.edit().putInt("id_usuario", idUsuarioLogueado).apply()
                // -----------------------------------------------------------

                Toast.makeText(this, "¡Bienvenido, $usuario!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, Menu_activity::class.java).apply {
                    putExtra("ID_USUARIO_ACTIVO", idUsuarioLogueado)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnregistrarse.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }
}