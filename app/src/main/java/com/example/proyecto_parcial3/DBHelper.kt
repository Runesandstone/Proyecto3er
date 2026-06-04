package com.example.proyecto_parcial3

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "TravelJournal.db"

        // ¡SUPER IMPORTANTE! Cambiamos a versión 2 para que Android actualice las tablas
        private const val DATABASE_VERSION = 2

        // Sentencia SQL para crear la tabla de Usuarios (Sin cambios)
        private const val CREATE_TABLE_USUARIOS = """
            CREATE TABLE Usuarios (
                id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT,
                contrasena TEXT,
                foto_perfil TEXT
            );
        """

        // Sentencia SQL para crear la tabla de Viajes (Actualizada al Fragment)
        private const val CREATE_TABLE_VIAJES = """
            CREATE TABLE Viajes (
                id_viaje INTEGER PRIMARY KEY AUTOINCREMENT,
                id_usuario INTEGER,
                destino TEXT,
                fecha_inicio TEXT,
                fecha_fin TEXT,
                presupuesto REAL,
                transporte TEXT,
                notas TEXT,
                FOREIGN KEY(id_usuario) REFERENCES Usuarios(id_usuario) ON DELETE CASCADE
            );
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_USUARIOS)
        db.execSQL(CREATE_TABLE_VIAJES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Al subir la versión a 2, este método se ejecuta automáticamente borrando la estructura vieja
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

    fun registrarUsuario(nombre: String, contrasena: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("contrasena", contrasena)
            put("foto_perfil", "")
        }
        return db.insert("Usuarios", null, values)
    }

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

    fun actualizarPerfil(
        idUsuario: Int,
        nombre: String,
        contrasena: String,
        fotoPerfil: String
    ): Int {
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

    // Guardar un viaje (Actualizado con los nuevos campos)
    fun insertarViaje(
        idUsuario: Int,
        destino: String,
        fechaInicio: String,
        fechaFin: String,
        presupuesto: Double,
        transporte: String,
        notas: String
    ): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("id_usuario", idUsuario)
            put("destino", destino)
            put("fecha_inicio", fechaInicio)
            put("fecha_fin", fechaFin)
            put("presupuesto", presupuesto)
            put("transporte", transporte)
            put("notas", notas)
        }
        return db.insert("Viajes", null, values)
    }

    // Obtener todos los viajes de UN usuario específico
    fun obtenerViajesPorUsuario(idUsuario: Int): Cursor {
        val db = this.readableDatabase
        val query = "SELECT * FROM Viajes WHERE id_usuario = ?"
        return db.rawQuery(query, arrayOf(idUsuario.toString()))
    }

    // Editar un viaje existente (Actualizado con los nuevos campos)
    fun actualizarViaje(
        idViaje: Int,
        destino: String,
        fechaInicio: String,
        fechaFin: String,
        presupuesto: Double,
        transporte: String,
        notas: String
    ): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("destino", destino)
            put("fecha_inicio", fechaInicio)
            put("fecha_fin", fechaFin)
            put("presupuesto", presupuesto)
            put("transporte", transporte)
            put("notas", notas)
        }
        return db.update("Viajes", values, "id_viaje = ?", arrayOf(idViaje.toString()))
    }

    // Eliminar un viaje
    fun eliminarViaje(idViaje: Int): Int {
        val db = this.writableDatabase
        return db.delete("Viajes", "id_viaje = ?", arrayOf(idViaje.toString()))
    }

    fun obtenerListaViajesPorUsuario(idUsuario: Int): List<com.example.proyecto_parcial3.model.Viaje> {
        val lista = mutableListOf<com.example.proyecto_parcial3.model.Viaje>()
        val db = this.readableDatabase

        // Corregido: "Viajes" con V mayúscula para coincidir con tu CREATE_TABLE
        val cursor =
            db.rawQuery("SELECT * FROM Viajes WHERE id_usuario = ?", arrayOf(idUsuario.toString()))

        if (cursor.moveToFirst()) {
            do {
                // Buscamos los índices de las columnas usando tus nombres reales de base de datos
                val idxIdViaje =
                    cursor.getColumnIndex("id_viaje") // 👈 Cambiado de "id" a "id_viaje"
                val idxDestino = cursor.getColumnIndex("destino")
                val idxFechaInicio = cursor.getColumnIndex("fecha_inicio")
                val idxFechaFin = cursor.getColumnIndex("fecha_fin")
                val idxPresupuesto = cursor.getColumnIndex("presupuesto")
                val idxTransporte = cursor.getColumnIndex("transporte")
                val idxNotas = cursor.getColumnIndex("notas")

                // Lectura de datos segura (Si no encuentra una columna por error de dedo, no cierra la app)
                val id = if (idxIdViaje != -1) cursor.getInt(idxIdViaje) else 0
                val destino = if (idxDestino != -1) cursor.getString(idxDestino) else "Sin destino"
                val fInicio =
                    if (idxFechaInicio != -1) cursor.getString(idxFechaInicio) else "00/00/0000"
                val fFin = if (idxFechaFin != -1) cursor.getString(idxFechaFin) else "00/00/0000"
                val presupuesto =
                    if (idxPresupuesto != -1) cursor.getDouble(idxPresupuesto) else 0.0
                val transporte =
                    if (idxTransporte != -1) cursor.getString(idxTransporte) else "No especificado"
                val notas = if (idxNotas != -1) cursor.getString(idxNotas) else ""

                // Agregamos el objeto a la lista respetando el parámetro 'fFin' que usa el adaptador
                lista.add(
                    com.example.proyecto_parcial3.model.Viaje(
                        id,
                        destino,
                        fInicio,
                        fFin,
                        presupuesto,
                        transporte,
                        notas
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }
}