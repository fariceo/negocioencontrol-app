package com.negocioencontrol.gastos

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun GastosListScreen(
    navController: NavHostController,
    nombreBD: String
) {

    val context = LocalContext.current

    val gastos = remember { mutableStateListOf<Gasto>() }
    var loading by remember { mutableStateOf(false) }
    var totalGeneral by remember { mutableStateOf(0.0) }

    fun cargarGastos() {

        loading = true

        val url = "https://elpollovolantuso.com/negocioencontrol/api/listado_gastos_api.php"

        val request = object : StringRequest(
            Request.Method.POST,
            url,
            { response ->

                try {
                    val json = JSONObject(response)

                    if (json.getBoolean("success")) {

                        val arr = json.getJSONArray("gastos")
                        val lista = mutableListOf<Gasto>()

                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)

                            lista.add(
                                Gasto(
                                    id = obj.optString("id"),
                                    descripcion = obj.optString("descripcion"),
                                    usuario = obj.optString("usuario", "admin"),
                                    monto = obj.optDouble("monto", 0.0),
                                    fecha = obj.optString("fecha")
                                )
                            )
                        }

                        gastos.clear()
                        gastos.addAll(lista)

                        totalGeneral = lista.sumOf { it.monto }

                    }

                } catch (e: Exception) {
                    Toast.makeText(context, "Error JSON", Toast.LENGTH_SHORT).show()
                }

                loading = false
            },
            {
                loading = false
                Toast.makeText(context, "Error conexión", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("nombre_bd" to nombreBD)
            }
        }

        Volley.newRequestQueue(context).add(request)
    }

    LaunchedEffect(Unit) { cargarGastos() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text("📊 Lista de Gastos")

        Text("Total: $${"%.2f".format(totalGeneral)}")

        Spacer(modifier = Modifier.height(12.dp))

        if (loading) {
            CircularProgressIndicator()
        } else {

            LazyColumn {

                items(gastos) { gasto ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {

                        Column(modifier = Modifier.padding(16.dp)) {

                            Text("👤 ${gasto.usuario}")
                            Text("📝 ${gasto.descripcion}")
                            Text("💰 ${gasto.monto}")
                            Text("📅 ${gasto.fecha}")
                        }
                    }
                }
            }
        }
    }
}