package com.example.proyecto_parcial3

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "TravelJournal.db"
        private const val DATABASE_VERSION = 1

        // Sentencia SQL para crear la tabla de Usuarios
        private const val CREATE_TABLE_USUARIOS = """
            CREATE TABLE Usuarios (
                id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT,
                contrasena TEXT,
                foto_perfil TEXT
            );
        """

        // Sentencia SQL para crear la tabla de Viajes
        private const val CREATE_TABLE_VIAJES = """
            CREATE TABLE Viajes (
                id_viaje INTEGER PRIMARY KEY AUTOINCREMENT,
                id_usuario INTEGER,
                destino TEXT,
                presupuesto REAL,
                transporte TEXT,
                fecha TEXT,
                hora TEXT,
                FOREIGN KEY(id_usuario) REFERENCES Usuarios(id_usuario) ON DELETE CASCADE
            );
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_USUARIOS)
        db.execSQL(CREATE_TABLE_VIAJES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Viajes")
        db.execSQL("DROP TABLE IF EXISTS Usuarios")
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // ==========================================
    // MÉTODOS PARA USUARIOS
    // ==========================================

    // Registrar un nuevo usuario (Sign Up)
    fun registrarUsuario(nombre: String, contrasena: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("contrasena", contrasena)
            put("foto_perfil", "")
        }
        return db.insert("Usuarios", null, values)
    }

    // Validar inicio de sesión
    fun verificarUsuario(nombre: String, contrasena: String): Int {
        val db = this.readableDatabase
        val query = "SELECT id_usuario FROM Usuarios WHERE nombre = ? AND contrasena = ?"
        val cursor = db.rawQuery(query, arrayOf(nombre, contrasena))

        var idUsuario = -1
        if (cursor.moveToFirst()) {
            idUsuario = cursor.getInt(0)
        }
        cursor.close()
        return idUsuario
    }

    // Actualizar datos del perfil
    fun actualizarPerfil(idUsuario: Int, nombre: String, contrasena: String, fotoPerfil: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("contrasena", contrasena)
            put("foto_perfil", fotoPerfil)
        }
        return db.update("Usuarios", values, "id_usuario = ?", arrayOf(idUsuario.toString()))
    }

    // ==========================================
    // MÉTODOS PARA VIAJES
    // ==========================================

    // Guardar un viaje
    fun insertarViaje(idUsuario: Int, destino: String, presupuesto: Double, transporte: String, fecha: String, hora: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("id_usuario", idUsuario)
            put("destino", destino)
            put("presupuesto", presupuesto)
            put("transporte", transporte)
            put("fecha", fecha)
            put("hora", hora)
        }
        return db.insert("Viajes", null, values)
    }

    // Obtener todos los viajes de UN usuario específico
    fun obtenerViajesPorUsuario(idUsuario: Int): Cursor {
        val db = this.readableDatabase
        val query = "SELECT * FROM Viajes WHERE id_usuario = ?"
        return db.rawQuery(query, arrayOf(idUsuario.toString()))
    }

    // Editar un viaje existente
    fun actualizarViaje(idViaje: Int, destino: String, presupuesto: Double, transporte: String, fecha: String, hora: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("destino", destino)
            put("presupuesto", presupuesto)
            put("transporte", transporte)
            put("fecha", fecha)
            put("hora", hora)
        }
        return db.update("Viajes", values, "id_viaje = ?", arrayOf(idViaje.toString()))
    }

    // Eliminar un viaje
    fun eliminarViaje(idViaje: Int): Int {
        val db = this.writableDatabase
        return db.delete("Viajes", "id_viaje = ?", arrayOf(idViaje.toString()))
    }
}