package com.negocioencontrol.productos

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import coil.compose.rememberAsyncImagePainter
import org.json.JSONObject
import com.negocioencontrol.Producto

@Composable
fun ProductosScreen(
    nombreBD: String,
    usuario: String
) {

    val context = LocalContext.current
    val productos = remember { mutableStateListOf<Producto>() }
    var loading by remember { mutableStateOf(false) }

    fun cargarProductos() {

        if (nombreBD.isBlank()) {
            Toast.makeText(context, "BD vacía", Toast.LENGTH_SHORT).show()
            return
        }

        loading = true

        val url = "https://elpollovolantuso.com/negocioencontrol/api/productos_api.php"

        val request = object : StringRequest(
            Request.Method.POST,
            url,
            { response ->

                try {

                    Log.d("PRODUCTOS_API", response)

                    val array = try {
                        val json = JSONObject(response)
                        json.getJSONArray("productos")
                    } catch (e: Exception) {
                        // si es array directo
                        org.json.JSONArray(response)
                    }

                    productos.clear()

                    for (i in 0 until array.length()) {

                        val obj = array.getJSONObject(i)

                        productos.add(
                            Producto(
                                idProducto = obj.optString("id_producto"),
                                producto = obj.optString("producto"),
                                precio = obj.optDouble("precio"),
                                imagen = obj.optString("imagen"),
                                descripcion = obj.optString("descripcion"),
                                categoria = obj.optString("categoria"),
                                stock = obj.optInt("stock")
                            )
                        )
                    }

                } catch (e: Exception) {
                    Toast.makeText(context, "Error JSON: ${e.message}", Toast.LENGTH_SHORT).show()
                }

                loading = false
            },
            {
                loading = false
                Toast.makeText(context, "Error conexión", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "nombre_bd" to nombreBD
                )
            }
        }

        Volley.newRequestQueue(context).add(request)
    }

    LaunchedEffect(Unit) {
        cargarProductos()
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {

        Text("📦 Productos", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { cargarProductos() }) {
            Text("Recargar")
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (loading) {
            Text("Cargando...")
        }

        LazyColumn {

            items(productos) { p ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {

                    Column(modifier = Modifier.padding(12.dp)) {

                        Text(p.producto)
                        Text("💲 ${p.precio}")
                        Text("📦 Stock: ${p.stock}")

                        if (p.imagen.isNotBlank()) {
                            Image(
                                painter = rememberAsyncImagePainter(p.imagen),
                                contentDescription = null,
                                modifier = Modifier
                                    .height(120.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}