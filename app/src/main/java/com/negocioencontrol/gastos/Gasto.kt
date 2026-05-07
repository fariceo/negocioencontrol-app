package com.negocioencontrol.gastos

data class Gasto(
    val id: String,
    val descripcion: String,
    val usuario: String,
    val monto: Double,
    val fecha: String
)