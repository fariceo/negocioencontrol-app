package com.negocioencontrol.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class RegistroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegistroScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen() {

    val context = LocalContext.current

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    var cargando by remember { mutableStateOf(false) }

    var negocios by remember { mutableStateOf(listOf<Negocio>()) }
    var negocioSeleccionado by remember { mutableStateOf<Negocio?>(null) }
    var expanded by remember { mutableStateOf(false) }

    /* =========================
       CARGAR NEGOCIOS
    ========================= */
    LaunchedEffect(true) {
        val queue = Volley.newRequestQueue(context)
        val url = "https://elpollovolantuso.com/negocioencontrol/api/listar_negocios.php"

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    val array = json.getJSONArray("data")

                    val list = mutableListOf<Negocio>()

                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        list.add(
                            Negocio(
                                item.getString("id_negocio"),
                                item.getString("nombre_negocio")
                            )
                        )
                    }

                    negocios = list

                } catch (e: Exception) {
                    Toast.makeText(context, "Error negocios", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(context, "Error conexión negocios", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(request)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Registro de Usuario", style = MaterialTheme.typography.titleLarge)

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
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar contraseña") },
            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                    Icon(
                        imageVector = if (confirmVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        /* =========================
           SELECTOR NEGOCIO
        ========================= */
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {

            OutlinedTextField(
                value = negocioSeleccionado?.nombre_negocio ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Negocio") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                negocios.forEach { negocio ->
                    DropdownMenuItem(
                        text = { Text(negocio.nombre_negocio) },
                        onClick = {
                            negocioSeleccionado = negocio
                            expanded = false
                        }
                    )
                }
            }
        }

        Button(
            onClick = {

                if (correo.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (negocioSeleccionado == null) {
                    Toast.makeText(context, "Selecciona un negocio", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (password != confirmPassword) {
                    Toast.makeText(context, "Contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                cargando = true

                val queue = Volley.newRequestQueue(context)
                val url = "https://elpollovolantuso.com/negocioencontrol/api/registro_usuario_api.php"

                val request = object : StringRequest(
                    Request.Method.POST, url,
                    { response ->

                        cargando = false

                        try {
                            val json = JSONObject(response)

                            if (json.getBoolean("success")) {

                                Toast.makeText(context, "Usuario registrado", Toast.LENGTH_SHORT).show()

                                val intent = Intent(context, com.negocioencontrol.MainActivity::class.java)
                                context.startActivity(intent)

                                (context as ComponentActivity).finish()

                            } else {
                                Toast.makeText(context, json.getString("message"), Toast.LENGTH_SHORT).show()
                            }

                        } catch (e: Exception) {
                            Toast.makeText(context, "ERROR JSON: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    {
                        cargando = false
                        Toast.makeText(context, "Error conexión", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    override fun getParams(): MutableMap<String, String> {
                        return hashMapOf(
                            "correo" to correo,
                            "password" to password,
                            "id_negocio" to (negocioSeleccionado?.id_negocio ?: "")
                        )
                    }
                }

                queue.add(request)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (cargando) "Registrando..." else "Registrar")
        }
    }
}

/* =========================
   MODELO
========================= */
data class Negocio(
    val id_negocio: String,
    val nombre_negocio: String
)