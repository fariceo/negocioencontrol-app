package com.negocioencontrol.bodega

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class BodegaActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("sesion", MODE_PRIVATE)

        val nombreBD = prefs.getString("nombre_bd", "") ?: ""

        setContent {
            BodegaScreen(nombreBD = nombreBD)
        }
    }
}