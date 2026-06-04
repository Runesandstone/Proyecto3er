package com.example.proyecto_parcial3

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
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
    private lateinit var imageViewPerfil: ImageView

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

        // Ahora al hacer clic en la foto de perfil del menú, te lleva a la actividad de perfil
        imageViewPerfil.setOnClickListener {
            val intent = Intent(this, profileActivity::class.java)
            startActivity(intent)
        }

        cargarFotoGuardada()

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun cargarFotoGuardada() {
        val sharedPreferences = getSharedPreferences("AjustesApp", Context.MODE_PRIVATE)
        val uriString = sharedPreferences.getString("foto_guardada", null)

        if (uriString != null) {
            try {
                val uri = Uri.parse(uriString)
                imageViewPerfil.setImageURI(uri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarFotoGuardada()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_menu)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
