package com.negocioencontrol.gastos

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

@Composable
fun IngresarGastoScreen(
    navController: NavHostController,
    nombreBD: String
) {

    val context = LocalContext.current

    var descripcion by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("gasto") }
    var loading by remember { mutableStateOf(false) }

    fun guardar() {

        if (descripcion.isBlank() || cantidad.isBlank() || precio.isBlank()) {
            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        loading = true


        val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val usuario = prefs.getString("correo_usuario", "admin") ?: "admin"

        val url = "https://elpollovolantuso.com/negocioencontrol/api/ingresar_gasto_api.php"

        val request = object : StringRequest(
            Request.Method.POST,
            url,
            { response ->

                loading = false

                try {
                    val json = JSONObject(response)

                    if (json.getBoolean("success")) {
                        Toast.makeText(context, "Guardado correctamente", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, json.optString("msg"), Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(context, "Error JSON", Toast.LENGTH_SHORT).show()
                }
            },
            {
                loading = false
                Toast.makeText(context, "Error conexión", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {

                val cant = cantidad.toDoubleOrNull() ?: 0.0
                val pre = precio.toDoubleOrNull() ?: 0.0
                val total = cant * pre

                return hashMapOf(
                    "nombre_bd" to nombreBD,
                    "usuario" to usuario,   // 👈 REAL LOGIN
                    "tipo" to tipo,
                    "descripcion" to descripcion,
                    "cantidad" to cantidad,
                    "precio" to precio,
                    "total" to total.toString()
                )
            }
        }

        Volley.newRequestQueue(context).add(request)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text("➕ Ingresar Movimiento")

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = cantidad,
            onValueChange = { cantidad = it },
            label = { Text("Cantidad") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = precio,
            onValueChange = { precio = it },
            label = { Text("Precio") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row {

            Row {
                RadioButton(selected = tipo == "gasto", onClick = { tipo = "gasto" })
                Text("Gasto")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row {
                RadioButton(selected = tipo == "compra", onClick = { tipo = "compra" })
                Text("Compra")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { guardar() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }
    }
}