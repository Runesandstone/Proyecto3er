package com.example.proyecto_parcial3

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_parcial3.databinding.ActivityMenuBinding

class Menu_activity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMenuBinding
    private lateinit var imageViewPerfil: ShapeableImageView
    private lateinit var imageButtonEdit: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMenu.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_menu)

        val headerView = navView.getHeaderView(0)

        imageViewPerfil = headerView.findViewById(R.id.imageView)
        imageButtonEdit = headerView.findViewById(R.id.imageButton)

        imageViewPerfil.setOnClickListener {
            val intent = Intent(this, profileActivity::class.java)
            startActivity(intent)
        }

        imageButtonEdit.setOnClickListener {
            val intent = Intent(this, profileActivity::class.java)
            startActivity(intent)
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onResume() {
        super.onResume()
        actualizarDatosCabecera()
    }

    private fun actualizarDatosCabecera() {
        val prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
        val idUsuario = prefs.getInt("id_usuario", -1)

        if (idUsuario != -1) {
            val dbHelper = DBHelper(this)
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT nombre, foto_perfil FROM Usuarios WHERE id_usuario = ?",
                arrayOf(idUsuario.toString())
            )

            if (cursor.moveToFirst()) {
                val nombre = cursor.getString(0)
                val fotoGuardada = cursor.getString(1)

                val navView: NavigationView = binding.navView
                val headerView = navView.getHeaderView(0)

                val tvNombre = headerView.findViewById<TextView>(R.id.tvUserName)
                if (!nombre.isNullOrEmpty() && tvNombre != null) {
                    tvNombre.text = nombre
                }

                if (!fotoGuardada.isNullOrEmpty()) {
                    try {
                        val uri = Uri.parse(fotoGuardada)
                        imageViewPerfil.setImageURI(uri)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    imageViewPerfil.setImageResource(R.mipmap.ic_launcher_round)
                }
            }
            cursor.close()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity, menu)
        return true
    }

    // ==========================================
    // FUNCIÓN PARA DETECTAR EL CLIC EN EL MENÚ
    // ==========================================
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.log_out -> {
                cerrarSesion()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun cerrarSesion() {
        val prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
        prefs.edit().putInt("id_usuario", -1).apply()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_menu)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}