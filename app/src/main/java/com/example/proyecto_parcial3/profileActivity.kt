package com.example.proyecto_parcial3

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

class profileActivity : AppCompatActivity() {

    private lateinit var ivProfilePicture: ImageView
    private lateinit var dbHelper: DBHelper
    private var currentPhotoUri: String = ""

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, flag)
            ivProfilePicture.setImageURI(uri)
            currentPhotoUri = uri.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        dbHelper = DBHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        val tvChangePhoto = findViewById<TextView>(R.id.tvChangePhoto)
        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnCancel = findViewById<Button>(R.id.btnCancel)
        val btnDeletePhoto = findViewById<Button>(R.id.btnDeletePhoto)

        val prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
        val idUsuario = prefs.getInt("id_usuario", -1)

        if (idUsuario != -1) {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT nombre, contrasena, foto_perfil FROM Usuarios WHERE id_usuario = ?",
                arrayOf(idUsuario.toString())
            )
            if (cursor.moveToFirst()) {
                etUsername.setText(cursor.getString(0))
                etPassword.setText(cursor.getString(1))
                val fotoGuardada = cursor.getString(2)
                if (!fotoGuardada.isNullOrEmpty()) {
                    currentPhotoUri = fotoGuardada
                    ivProfilePicture.setImageURI(Uri.parse(fotoGuardada))
                } else {
                    currentPhotoUri = ""
                    ivProfilePicture.setImageResource(R.mipmap.ic_launcher_round)
                }
            }
            cursor.close()
        }

        tvChangePhoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        ivProfilePicture.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnDeletePhoto.setOnClickListener {
            currentPhotoUri = ""
            ivProfilePicture.setImageResource(R.mipmap.ic_launcher_round)
        }

        btnSave.setOnClickListener {
            val nombreNuevo = etUsername.text.toString().trim()
            val contrasenaNueva = etPassword.text.toString().trim()

            if (nombreNuevo.isEmpty() || contrasenaNueva.isEmpty()) {
                Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (idUsuario != -1) {
                val filas = dbHelper.actualizarPerfil(idUsuario, nombreNuevo, contrasenaNueva, currentPhotoUri)
                if (filas > 0) {
                    Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }
}