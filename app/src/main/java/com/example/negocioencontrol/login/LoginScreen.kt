package com.example.negocioencontrol

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class LoginScreen : ComponentActivity() {

    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("sesion", MODE_PRIVATE)

        // Verificar sesión activa de 5 minutos
        val logueado = prefs.getBoolean("logueado", false)
        val tiempoLogin = prefs.getLong("tiempo_login", 0)
        val tiempoActual = System.currentTimeMillis()
        val cincoMinutos = 59 * 60 * 1000

        if (logueado && tiempoActual - tiempoLogin < cincoMinutos) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContent {
            LoginScreenContent(prefs) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    prefs: SharedPreferences,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }  // <- debe llamarse password
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Iniciar Sesión", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (correo.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                cargando = true
                val queue = Volley.newRequestQueue(context)
                val url = "https://elpollovolantuso.com/negocioencontrol/api/api_login.php"

                val request = object : StringRequest(
                    Request.Method.POST, url,
                    { response ->
                        cargando = false
                        try {
                            val json = JSONObject(response)
                            val success = json.getBoolean("success")
                            if (success) {
                                val idUsuario = json.getInt("id_usuario")
                                val nombreNegocio = json.getString("nombre_negocio")
                                val nombreBD = json.getString("nombre_bd")

                                // Guardar sesión
                                prefs.edit().apply {
                                    putBoolean("logueado", true)
                                    putLong("tiempo_login", System.currentTimeMillis())
                                    putInt("id_usuario", idUsuario)
                                    putString("nombre_negocio", nombreNegocio)
                                    putString("nombre_bd", nombreBD)
                                    putString("correo_usuario", correo)
                                    apply()
                                }

                                Toast.makeText(context, "Bienvenido $nombreNegocio", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } else {
                                Toast.makeText(context, json.getString("message"), Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error en respuesta del servidor", Toast.LENGTH_SHORT).show()
                        }
                    },
                    {
                        cargando = false
                        Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    override fun getParams(): MutableMap<String, String> {
                        return hashMapOf(
                            "correo" to correo,
                            "password" to password   // <- coincide con tu API
                        )
                    }
                }

                queue.add(request)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (cargando) "Cargando..." else "Iniciar sesión")
        }
    }
}