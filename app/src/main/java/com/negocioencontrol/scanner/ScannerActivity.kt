package com.negocioencontrol.scanner

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class ScannerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("sesion", MODE_PRIVATE)

        val nombreBD = prefs.getString("nombre_bd", "") ?: ""
        val nombreNegocio = prefs.getString("nombre_negocio", "") ?: ""
        setContent {

            val usuario = prefs.getString("correo_usuario", "") ?: ""

            ScannerScreen(
                nombreBD = nombreBD,
                nombreNegocio = nombreNegocio,
                usuario = usuario,
                carrito = emptyList() // inicial vacío
            )
        }
    }
}