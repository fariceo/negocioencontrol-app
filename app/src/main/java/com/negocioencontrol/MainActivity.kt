package com.negocioencontrol

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.content.Context

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.negocioencontrol.scanner.ScannerActivity
import com.google.firebase.messaging.FirebaseMessaging

import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import com.negocioencontrol.login.LoginScreen

class MainActivity : ComponentActivity() {

    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("sesion", MODE_PRIVATE)

        // 👇 obtener nombre del negocio guardado en sesión
        val nombreNegocio = prefs.getString("nombre_negocio", "NEGOCIO EN CONTROL") ?: "NEGOCIO EN CONTROL"

        // Obtener token de Firebase
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->

                if (!task.isSuccessful) {
                    Log.e("FCM", "Error obteniendo token", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.d("FCM_TOKEN", token)

                val usuario = prefs.getString("correo_usuario", "") ?: ""
                val negocio = prefs.getString("nombre_bd", "") ?: ""

                guardarTokenFCM(token, usuario, negocio, this)

            }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        setContent {
            MainScreen(
                nombreNegocio = nombreNegocio, // 👈 se lo mandamos al composable
                onScanClick = {
                    val intent = Intent(this, ScannerActivity::class.java)
                    startActivity(intent)
                },
                onLogout = {
                    prefs.edit().clear().apply()

                    val intent = Intent(this, LoginScreen::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun MainScreen(
    nombreNegocio: String,
    onScanClick: () -> Unit,
    onLogout: () -> Unit
) {

    Column(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D1B2A))
                .padding(16.dp)
        ) {
            Text(
                text = nombreNegocio, // 👈 aquí ya es dinámico
                color = Color.White,
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D1B2A))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                onClick = onScanClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar producto")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(
                    "Cerrar Sesión",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
    }
}

fun guardarTokenFCM(
    token: String,
    usuario: String,
    negocio: String,
    context: Context
) {
    val url = "https://elpollovolantuso.com/negocioencontrol/api/guardar_token.php"
    val queue = Volley.newRequestQueue(context)

    val request = object : StringRequest(
        Request.Method.POST,
        url,
        { response ->
            Log.d("FCM_SAVE", "Respuesta API token: $response")
        },
        { error ->
            Log.e("FCM_SAVE", "Error guardando token", error)
            error.printStackTrace()
        }
    ) {
        override fun getParams(): MutableMap<String, String> {
            return hashMapOf(
                "token" to token,
                "usuario" to usuario,
                "negocio" to negocio,
                "dispositivo" to "android"
            )
        }
    }

    queue.add(request)
}