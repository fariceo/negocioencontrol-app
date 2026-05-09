package com.negocioencontrol.productos

data class Producto(
    val idProducto: String,
    val producto: String,
    val precio: Double,
    val imagen: String,
    val descripcion: String,
    val categoria: String,
    val stock: Int
)