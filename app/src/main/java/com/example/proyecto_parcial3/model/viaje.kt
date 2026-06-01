package com.example.proyecto_parcial3.model


data class Viaje(
    val id: Int = 0, // El 0 indica que es nuevo y la DB le asignará uno
    val destino: String,
    val fecha: String,
    val presupuesto: Double,
    val transporte: String
)