package com.negocioencontrol.bodega

data class ProductoBodega(
    val id: String,
    val idProducto: String,

    val producto: String,
    val descripcion: String,

    val cantidad: Double,
    val precio: Double,

    val categoria: String,
    val codigoBarra: String,

    val stockInicial: Double,
    val fechaRegistro: String,

    val imagen: String,

    // UI
    val expandido: Boolean = false
)