package com.example.proyecto_parcial3

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

        // 1. Inflamos el diseño de activity_main.xml usando ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Inicializamos tu manejador de Base de Datos SQLite
        dbHelper = DBHelper(this)

        // ========================================================
        // ACCIÓN 1: Al presionar el botón "Iniciar Sesión" (btnLogIn)
        // ========================================================
        binding.btnLogIn.setOnClickListener {
            // Obtenemos los textos usando los IDs exactos de tu XML
            val usuario = binding.etUser.text.toString().trim()
            val contra = binding.etPassword.text.toString().trim()

            // Validación rápida de campos vacíos
            if (usuario.isEmpty() || contra.isEmpty()) {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Consultamos a la Base de Datos
            val idUsuarioLogueado = dbHelper.verificarUsuario(usuario, contra)

            if (idUsuarioLogueado != -1) {
                Toast.makeText(this, "¡Bienvenido, $usuario!", Toast.LENGTH_SHORT).show()

                // Si el Login es exitoso, viajamos a Menu_activity pasándole el ID del usuario activo
                val intent = Intent(this, Menu_activity::class.java).apply {
                    putExtra("ID_USUARIO_ACTIVO", idUsuarioLogueado)
                }
                startActivity(intent)
                finish()
            } else {
                // Mensaje si no se encuentra el usuario en la Base de Datos
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }


        binding.btnregistrarse.setOnClickListener {
            // Abre la pantalla de registro
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }
}