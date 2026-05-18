package com.negocioencontrol.usuarios

import android.os.Bundle
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.shape.RoundedCornerShape

import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import org.json.JSONObject

// =========================
// ACTIVITY
// =========================
class RolesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("sesion", MODE_PRIVATE)
        val nombreBD = prefs.getString("nombre_bd", "") ?: ""

        setContent {
            RolesScreen(nombreBD = nombreBD)
        }
    }
}

// =========================
// MODELO
// =========================
data class UsuarioRol(
    val id_usuario: Int,
    val nombre: String,
    val correo: String,
    var rol: String
)

// =========================
// SCREEN
// =========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RolesScreen(nombreBD: String) {

    val context = LocalContext.current

    val usuarios = remember { mutableStateListOf<UsuarioRol>() }
    val roles = remember { mutableStateListOf<String>() }

    var cargando by remember { mutableStateOf(false) }

    // =========================
    // USUARIOS
    // =========================
    fun cargarUsuarios() {

        cargando = true

        val queue = Volley.newRequestQueue(context)

        val url =
            "https://elpollovolantuso.com/negocioencontrol/api/listar_usuarios_api.php"

        val request = object : StringRequest(
            Request.Method.POST,
            url,

            { response ->

                cargando = false

                try {

                    val json = JSONObject(response)

                    if (json.getBoolean("success")) {

                        usuarios.clear()

                        val array = json.getJSONArray("usuarios")

                        for (i in 0 until array.length()) {

                            val item = array.getJSONObject(i)

                            usuarios.add(
                                UsuarioRol(
                                    id_usuario = item.getInt("id_usuario"),
                                    nombre = item.getString("nombre"),
                                    correo = item.getString("correo"),
                                    rol = item.getString("rol")
                                )
                            )
                        }
                    }

                } catch (e: Exception) {
                    Toast.makeText(context, "Error JSON usuarios", Toast.LENGTH_SHORT).show()
                }
            },

            {
                cargando = false
                Toast.makeText(context, "Error conexión usuarios", Toast.LENGTH_SHORT).show()
            }

        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("nombre_bd" to nombreBD)
            }
        }

        queue.add(request)
    }

    // =========================
    // ROLES (DESDE ENUM/API)
    // =========================
    fun cargarRoles() {

        val queue = Volley.newRequestQueue(context)

        val url =
            "https://elpollovolantuso.com/negocioencontrol/api/listar_roles_api.php"

        val request = object : StringRequest(
            Request.Method.POST,
            url,

            { response ->

                try {

                    val json = JSONObject(response)

                    if (json.getBoolean("success")) {

                        roles.clear()

                        val array = json.getJSONArray("roles")

                        for (i in 0 until array.length()) {
                            roles.add(array.getString(i))
                        }
                    }

                } catch (e: Exception) {
                    Toast.makeText(context, "Error JSON roles", Toast.LENGTH_SHORT).show()
                }
            },

            {
                Toast.makeText(context, "Error conexión roles", Toast.LENGTH_SHORT).show()
            }

        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("nombre_bd" to nombreBD)
            }
        }

        queue.add(request)
    }

    // =========================
    // INIT
    // =========================
    LaunchedEffect(Unit) {
        cargarUsuarios()
        cargarRoles()
    }

    // =========================
    // UI
    // =========================
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .padding(16.dp)
    ) {

        Text(
            text = "Gestión de Roles",
            color = Color.White,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (cargando) {

            Text("Cargando...", color = Color.White)

        } else {

            LazyColumn {

                items(usuarios) { usuario ->

                    RolCard(
                        usuario = usuario,
                        nombreBD = nombreBD,
                        roles = roles
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

// =========================
// CARD
// =========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RolCard(
    usuario: UsuarioRol,
    nombreBD: String,
    roles: List<String>
) {

    val context = LocalContext.current

    var expanded by remember { mutableStateOf(false) }
    var rolSeleccionado by remember { mutableStateOf(usuario.rol) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Text(usuario.nombre, fontSize = 20.sp)
            Text(usuario.correo)
            Text("Rol actual: $rolSeleccionado")

            Spacer(modifier = Modifier.height(10.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {

                OutlinedTextField(
                    value = rolSeleccionado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rol") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {

                    roles.forEach { rol ->

                        DropdownMenuItem(
                            text = { Text(rol) },
                            onClick = {
                                rolSeleccionado = rol
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {

                    val queue = Volley.newRequestQueue(context)

                    val url =
                        "https://elpollovolantuso.com/negocioencontrol/api/actualizar_rol_api.php"

                    val request = object : StringRequest(
                        Request.Method.POST,
                        url,

                        { response ->

                            try {

                                val json = JSONObject(response)

                                Toast.makeText(
                                    context,
                                    json.getString("message"),
                                    Toast.LENGTH_SHORT
                                ).show()

                            } catch (e: Exception) {
                                Toast.makeText(context, "Error JSON update", Toast.LENGTH_SHORT).show()
                            }
                        },

                        {
                            Toast.makeText(context, "Error conexión", Toast.LENGTH_SHORT).show()
                        }

                    ) {
                        override fun getParams(): MutableMap<String, String> {
                            return hashMapOf(
                                "id_usuario" to usuario.id_usuario.toString(),
                                "rol" to rolSeleccionado,
                                "nombre_bd" to nombreBD
                            )
                        }
                    }

                    queue.add(request)
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Guardar")
            }
        }
    }
}