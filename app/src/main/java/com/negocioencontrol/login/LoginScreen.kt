package com.negocioencontrol.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import com.negocioencontrol.MainActivity
import com.negocioencontrol.login.RegistroActivity
import com.negocioencontrol.cajero.CajeroActivity
import com.negocioencontrol.personas.clientes.ClientesActivity

class LoginScreen : ComponentActivity() {

    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("sesion", MODE_PRIVATE)

        val logueado = prefs.getBoolean("logueado", false)
        val tiempoLogin = prefs.getLong("tiempo_login", 0)
        val tiempoActual = System.currentTimeMillis()
        val cincoMinutos = 59 * 60 * 1000

        if (logueado && tiempoActual - tiempoLogin < cincoMinutos) {

            val rol = prefs.getString("rol", "") ?: ""

            when (rol) {

                "admin" -> {
                    startActivity(Intent(this, MainActivity::class.java))
                }

                "cajero" -> {
                    startActivity(Intent(this, CajeroActivity::class.java))
                }

                else -> {
                    startActivity(Intent(this, ClientesActivity::class.java))
                }
            }

            finish()
            return
        }

        setContent {
            LoginScreenContent(prefs)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    prefs: SharedPreferences
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
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
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible)
                    Icons.Filled.Visibility
                else
                    Icons.Filled.VisibilityOff

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = "Mostrar contraseña")
                }
            },
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
                                val rol = json.getString("rol")

                                prefs.edit().apply {
                                    putBoolean("logueado", true)
                                    putLong("tiempo_login", System.currentTimeMillis())
                                    putInt("id_usuario", idUsuario)
                                    putString("nombre_negocio", nombreNegocio)
                                    putString("nombre_bd", nombreBD)
                                    putString("correo_usuario", correo)
                                    putString("rol", rol)
                                    apply()
                                }

                                Toast.makeText(
                                    context,
                                    "Bienvenido a $nombreNegocio",
                                    Toast.LENGTH_SHORT
                                ).show()

                                when (rol) {

                                    "admin" -> {
                                        context.startActivity(
                                            Intent(context, MainActivity::class.java)
                                        )
                                    }

                                    "cajero" -> {
                                        context.startActivity(
                                            Intent(context, CajeroActivity::class.java)
                                        )
                                    }

                                    else -> {
                                        context.startActivity(
                                            Intent(context, ClientesActivity::class.java)
                                        )
                                    }
                                }

                                activity?.finish()

                            } else {
                                Toast.makeText(
                                    context,
                                    json.getString("message"),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Error en respuesta del servidor",
                                Toast.LENGTH_SHORT
                            ).show()
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
                            "password" to password
                        )
                    }
                }

                queue.add(request)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (cargando) "Cargando..." else "Iniciar sesión")
        }

        Text(
            text = "¿No tienes cuenta? Regístrate",
            color = Color(0xFF1E88E5),
            modifier = Modifier.clickable {
                val intent = Intent(context, RegistroActivity::class.java)
                context.startActivity(intent)
            }
        )
    }
}