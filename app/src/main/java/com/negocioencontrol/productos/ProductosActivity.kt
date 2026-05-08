    package com.negocioencontrol.productos

    import android.os.Bundle
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent

    class ProductosActivity : ComponentActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val prefs = getSharedPreferences("sesion", MODE_PRIVATE)


            val usuario = prefs.getString("correo_usuario", "") ?: ""
            val nombreBD = prefs.getString("nombre_bd", "") ?: ""
            val nombreNegocio = prefs.getString("nombre_negocio", "") ?: ""

            setContent {
                ProductosScreen(
                    nombreBD = nombreBD,
                    usuario = usuario
                )
            }
        }
    }