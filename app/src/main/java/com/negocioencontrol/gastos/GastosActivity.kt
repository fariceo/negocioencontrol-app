package com.negocioencontrol.gastos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class GastosActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("sesion", MODE_PRIVATE)
        val nombreBD = prefs.getString("nombre_bd", "") ?: ""

        setContent {

            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "gastos_screen"
            ) {

                composable("gastos_screen") {
                    GastosScreen(navController, nombreBD)
                }

                composable("lista_gastos_screen") {
                    GastosListScreen(navController, nombreBD)
                }

                composable("ingresar_gasto_screen") {
                    IngresarGastoScreen(navController, nombreBD)
                }
            }
        }
    }
}