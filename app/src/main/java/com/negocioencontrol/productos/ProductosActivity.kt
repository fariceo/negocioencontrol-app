package com.negocioencontrol.productos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*

class ProductosActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("sesion", MODE_PRIVATE)
        val nombreBD = prefs.getString("nombre_bd", "") ?: ""

        setContent {

            var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }

            if (categoriaSeleccionada == null) {

                CategoriasScreen(
                    nombreBD = nombreBD,
                    onCategoriaClick = { categoria ->
                        categoriaSeleccionada = categoria
                    }
                )

            } else {

                ProductosScreen(

                    categoria = categoriaSeleccionada!!,
                    onBack = {
                        categoriaSeleccionada = null
                    }
                )
            }
        }
    }
}