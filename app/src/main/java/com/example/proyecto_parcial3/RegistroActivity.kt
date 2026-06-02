package com.example.proyecto_parcial3

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_parcial3.DBHelper

class RegistroActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Inicializamos el DBHelper
        dbHelper = DBHelper(this)

        // Vinculamos los componentes visuales del XML
        val etNuevoUsuario = findViewById<EditText>(R.id.etNuevoUsuario)
        val etNuevaContrasena = findViewById<EditText>(R.id.etNuevaContrasena)
        val etConfirmarContrasena = findViewById<EditText>(R.id.etConfirmarContrasena)
        val btnRegistrarUsuario = findViewById<Button>(R.id.btnRegistrarUsuario)

        // Configuración del botón de Registro
        btnRegistrarUsuario.setOnClickListener {
            val usuario = etNuevoUsuario.text.toString().trim()
            val contra = etNuevaContrasena.text.toString().trim()
            val confirmarContra = etConfirmarContrasena.text.toString().trim()

            // Validación de campos
            if (usuario.isEmpty() || contra.isEmpty() || confirmarContra.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validación de contraseña
            if (contra != confirmarContra) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Registrar el usuario en la base de datos
            val resultado = dbHelper.registrarUsuario(usuario, contra)
            if (resultado != -1L) {
                Toast.makeText(this, "¡Usuario registrado con éxito!", Toast.LENGTH_LONG).show()
                finish()
            } else {

                Toast.makeText(this, "Error al registrar el usuario. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}