package com.example.negocioencontrol.scanner

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONObject

@Composable

fun ScannerScreen(nombreBD: String, usuario: String) {

    val context = LocalContext.current
    var producto by remember { mutableStateOf<Producto?>(null) }

    var carrito by remember { mutableStateOf(mutableListOf<ProductoCarrito>()) }

    val total = carrito.sumOf { it.precio * it.cantidad }

    lateinit var iniciarScanner: () -> Unit

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        val intentResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)

        if (intentResult != null && intentResult.contents != null) {

            val codigo = intentResult.contents?.trim() ?: ""

            if (codigo.isEmpty()) {
                iniciarScanner()
                return@rememberLauncherForActivityResult
            }
            buscarProductoAPI(codigo, nombreBD, context) { prod ->

                if (prod != null) {

                    producto = prod

                    val existente = carrito.find { it.id == prod.id }

                    if (existente != null) {
                        existente.cantidad += 1
                    } else {
                        carrito.add(
                            ProductoCarrito(
                                id = prod.id,
                                nombre = prod.producto,
                                precio = prod.precio.toDouble(),
                                cantidad = 1
                            )
                        )
                    }

                    carrito = carrito.toMutableList()

                    agregarAlCarritoAPI(
                        negocio = nombreBD,
                        usuario = usuario,
                        id_producto = prod.id,
                        cantidad = "1",
                        context = context
                    )
                }

                // volver a abrir scanner
                iniciarScanner()
            }

        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    iniciarScanner = {
        val integrator = IntentIntegrator(context as Activity)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Escanea el código de barras")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(true)

        launcher.launch(integrator.createScanIntent())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(
            onClick = { iniciarScanner() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar Escaneo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        producto?.let { p ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {

                Column(modifier = Modifier.padding(16.dp)) {

                    Text("Producto: ${p.producto}", fontSize = 18.sp)
                    Text("Precio: $${p.precio}", fontSize = 16.sp)
                    Text("Categoría: ${p.categoria}", fontSize = 16.sp)
                    Text("Stock: ${p.stock}", fontSize = 16.sp)

                    if (p.imagen.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                "https://elpollovolantuso.com/negocioencontrol/assets/images/${p.imagen}"
                            ),
                            contentDescription = p.producto,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Carrito", fontSize = 20.sp)

        carrito.forEach { item ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text("${item.nombre} x${item.cantidad}")

                Text("$${item.precio * item.cantidad}")
            }
        }

        Text(
            text = "TOTAL: $${total}",
            fontSize = 22.sp
        )




    }


}



// Modelo de producto
data class Producto(
    val id: String,
    val codigo: String,
    val producto: String,
    val precio: String,
    val categoria: String,
    val stock: String,
    val imagen: String
)


data class ProductoCarrito(
    val id: String,
    val nombre: String,
    val precio: Double,
    var cantidad: Int
)
// API para obtener producto
fun buscarProductoAPI(codigo: String, nombreBD: String, context: Context, onResult: (Producto?) -> Unit) {
    val url = "https://elpollovolantuso.com/negocioencontrol/api/buscar_producto_api.php"
    val queue = Volley.newRequestQueue(context)

    val request = object : StringRequest(
        Method.POST, url,
        { response ->
            try {
                val json = JSONObject(response)
                if (json.getBoolean("success")) {

                    val prodJson = json.getJSONObject("producto")

                    val producto = Producto(
                        id = prodJson.getString("id_producto"),
                        codigo = prodJson.getString("codigo_barra"),
                        producto = prodJson.getString("producto"),
                        precio = prodJson.getString("precio"),
                        categoria = prodJson.getString("categoria"),
                        stock = prodJson.getString("stock_inicial"),
                        imagen = prodJson.getString("imagen")
                    )

                    onResult(producto)

                } else {
                    Toast.makeText(context, json.getString("msg"), Toast.LENGTH_SHORT).show()

                    onResult(null)                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        },
        { error ->
            error.printStackTrace()
            Toast.makeText(context, "Error al obtener producto", Toast.LENGTH_SHORT).show()
        }
    ) {
        override fun getParams(): MutableMap<String, String> {
            return mutableMapOf(
                "codigo" to codigo,
                "nombre_bd" to nombreBD
            )
        }
    }

    queue.add(request)
}

// API para agregar al carrito
fun agregarAlCarritoAPI(
    negocio: String,
    usuario: String,
    id_producto: String,
    cantidad: String,
    context: Context
) {
    val url = "https://elpollovolantuso.com/negocioencontrol/api/agregar_carrito_api.php"
    val queue = Volley.newRequestQueue(context)

    val request = object : StringRequest(
        Method.POST, url,
        { response ->
            try {
                val json = JSONObject(response)
                Toast.makeText(context, json.getString("msg"), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        },
        { error ->
            error.printStackTrace()
            Toast.makeText(context, "Error al agregar al carrito", Toast.LENGTH_SHORT).show()
        }
    ) {
        override fun getParams(): MutableMap<String, String> {
            return mutableMapOf(
                "negocio" to negocio,
                "usuario" to usuario,
                "id_producto" to id_producto,
                "cantidad" to cantidad,
                "estado" to "1"
            )
        }
    }

    queue.add(request)
}