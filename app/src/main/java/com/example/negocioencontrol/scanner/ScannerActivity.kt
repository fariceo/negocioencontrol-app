package com.example.negocioencontrol.scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class ScannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val nombreBD = intent.getStringExtra("nombreBD") ?: ""
        setContent {
            ScannerScreen(nombreBD = nombreBD)
        }
    }
}