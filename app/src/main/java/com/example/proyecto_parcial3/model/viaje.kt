package com.example.proyecto_parcial3.model

data class Viaje(
    val id: Int,
    val destino: String,
    val fecha: String,
    val fFin: String,
    val presupuesto: Double,
    val transporte: String,
    val notas: String
)