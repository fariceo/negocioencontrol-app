package com.example.negocioencontrol.scanner

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class ScannerActivity : ComponentActivity() {

    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val nombreBD = prefs.getString("nombre_bd", "") ?: ""
        val usuario = prefs.getString("usuario", "") ?: ""

        setContent {
            ScannerScreen(nombreBD = nombreBD, usuario = usuario)
        }
    }
}