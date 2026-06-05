package com.example.proyecto_parcial3

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class profileActivity : AppCompatActivity() {

    // 1. Declaramos la variable para el ImageView
    private lateinit var ivProfilePicture: ImageView

    // 2. Registramos la actividad que abrirá la galería nativa de Android
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Este bloque se ejecuta cuando el usuario regresa de la galería
        if (uri != null) {

            // --- INICIO DEL PASO 4 ---

            // A. Pedimos permiso permanente a Android para poder ver esta foto siempre (incluso si cierras la app)
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, flag)

            // B. Colocamos la foto en la pantalla de perfil
            ivProfilePicture.setImageURI(uri)

            // C. Guardamos la "ruta" (URI) de la foto para que el menú la pueda leer después
            val prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
            prefs.edit().putString("foto_perfil", uri.toString()).apply()

            // --- FIN DEL PASO 4 ---
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        // Ajuste de los insets (barras de estado y navegación) usando el ID "main" de tu XML
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 3. Enlazamos las vistas usando los IDs de tu archivo XML
        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        val tvChangePhoto = findViewById<TextView>(R.id.tvChangePhoto)

        // 4. Abrir galería al presionar el texto "Cambiar foto de perfil"
        tvChangePhoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Extra: Abrir la galería si el usuario toca directamente el círculo de la imagen
        ivProfilePicture.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
}