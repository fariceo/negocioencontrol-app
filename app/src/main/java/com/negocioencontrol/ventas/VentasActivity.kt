package com.negocioencontrol.ventas

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

// ===================== ACTIVITY =====================
class VentasActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("sesion", MODE_PRIVATE)
        val nombreBD = prefs.getString("nombre_bd", "") ?: ""

        setContent {
            VentasScreen(nombreBD = nombreBD)
        }
    }
}

// ===================== UI =====================
@Composable
fun VentasScreen(nombreBD: String) {

    val context = LocalContext.current
    val ventas = remember { mutableStateListOf<Venta>() }

    var totalGeneral by remember { mutableStateOf(0.0) }
    var rangoTexto by remember { mutableStateOf("Todos") }
    var loading by remember { mutableStateOf(false) }

    fun cargarVentas(filtro: String) {

        if (nombreBD.isBlank()) {
            Toast.makeText(context, "BD no configurada", Toast.LENGTH_SHORT).show()
            return
        }

        loading = true

        val url = "https://elpollovolantuso.com/negocioencontrol/api/ventas_api.php"

        val request = object : StringRequest(
            Request.Method.POST,
            url,
            { response ->

                try {

                    val json = JSONObject(response)

                    totalGeneral = json.getDouble("total_general")
                    rangoTexto = json.getString("rango_texto")

                    val arr = json.getJSONArray("ventas")
                    val lista = mutableListOf<Venta>()

                    for (i in 0 until arr.length()) {

                        val obj = arr.getJSONObject(i)

                        // 🔥 PRODUCTOS VIENEN EN JSON STRING
                        val productosJson = obj.optString("productos", "[]")
                        val productosArray = JSONArray(productosJson)

                        val productos = mutableListOf<DetalleVenta>()

                        for (j in 0 until productosArray.length()) {

                            val p = productosArray.getJSONObject(j)

                            productos.add(
                                DetalleVenta(
                                    producto = p.optString("producto", ""),
                                    cantidad = p.optInt("cantidad", 0),
                                    precio = p.optDouble("precio", 0.0),
                                    total = p.optDouble("total", 0.0)
                                )
                            )
                        }

                        lista.add(
                            Venta(
                                id = obj.getString("id"),
                                cliente = obj.optString("cliente", ""),
                                vendedor = obj.optString("vendedor", ""),
                                total = obj.optDouble("total", 0.0),
                                fecha = obj.getString("fecha_hora"),
                                metodo = obj.optString("metodo_pago", "otro"),
                                productos = productos,
                                expandido = false
                            )
                        )
                    }

                    ventas.clear()
                    ventas.addAll(lista)

                } catch (e: Exception) {
                    e.printStackTrace()
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
                    "nombre_bd" to nombreBD,
                    "filtro" to filtro
                )
            }
        }

        Volley.newRequestQueue(context).add(request)
    }

    LaunchedEffect(Unit) {
        cargarVentas("todos")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        Text("📊 Ventas ($rangoTexto)", style = MaterialTheme.typography.titleLarge)

        Text(
            "Total general: $${"%.2f".format(totalGeneral)}",
            color = Color(0xFF16A34A)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { cargarVentas("hoy") }) { Text("Hoy") }
            Button(onClick = { cargarVentas("semana") }) { Text("Semana") }
            Button(onClick = { cargarVentas("mes") }) { Text("Mes") }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { cargarVentas("todos") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Todos")
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (loading) {
            Text("Cargando ventas...")
        } else {

            LazyColumn {

                itemsIndexed(ventas) { index, v ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {

                        Column(modifier = Modifier.padding(12.dp)) {

                            Text("Cliente: ${v.cliente}")
                            Text("Fecha: ${v.fecha}")

                            Text(
                                "TOTAL: $${v.total}",
                                color = Color(0xFF16A34A)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    ventas[index] = v.copy(expandido = !v.expandido)
                                }
                            ) {
                                Text(if (v.expandido) "Ocultar detalle" else "Ver detalle")
                            }

                            // ================= DETALLES =================
                            if (v.expandido) {

                                Column(modifier = Modifier.padding(top = 8.dp)) {

                                    v.productos.forEach { d ->

                                        Text("• ${d.producto} x${d.cantidad}")
                                        Text(
                                            "  $${"%.2f".format(d.total)}",
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===================== MODELOS =====================
data class Venta(
    val id: String,
    val cliente: String,
    val vendedor: String,
    val total: Double,
    val fecha: String,
    val metodo: String,
    val productos: List<DetalleVenta> = emptyList(),
    val expandido: Boolean = false
)

data class DetalleVenta(
    val producto: String,
    val cantidad: Int,
    val precio: Double,
    val total: Double
)