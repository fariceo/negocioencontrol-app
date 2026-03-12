package com.example.negocioencontrol.scanner

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)
    val usuario = prefs.getString("correo_usuario", "") ?: ""
    val total = carrito.sumOf { it.precio * it.cantidad }

    // Función para recargar carrito desde BD
    fun recargarCarrito() {
        obtenerCarritoAPI(nombreBD, usuario, context) { lista: MutableList<ProductoCarrito> ->
            carrito = lista // ⚡ reasignar para recomposición
        }
    }

    LaunchedEffect(Unit) { recargarCarrito() }

    lateinit var iniciarScanner: () -> Unit

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val intentResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)

        if (intentResult != null && intentResult.contents != null) {
            val codigo = intentResult.contents.trim()
            if (codigo.isEmpty()) { iniciarScanner(); return@rememberLauncherForActivityResult }

            buscarProductoAPI(codigo, nombreBD, context) { prod: Producto? ->
                if (prod != null) {
                    producto = prod

                    // agregar a la BD y luego recargar carrito
                    agregarAlCarritoAPI(nombreBD, usuario, prod.id, "1", context) {
                        recargarCarrito()
                    }
                }
                iniciarScanner() // volver a abrir scanner automáticamente
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

        Button(onClick = { iniciarScanner() }, modifier = Modifier.fillMaxWidth()) {
            Text("Iniciar Escaneo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        producto?.let { p ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
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

        Text("Carrito", fontSize = 22.sp, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxHeight(0.7f)) {
            items(carrito) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Información del producto y control de cantidad
                        Column {
                            Text(item.nombre, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Botón "-" decrementa cantidad
                                IconButton(
                                    onClick = {
                                        val nuevaCantidad = item.cantidad - 1
                                        actualizarCantidadCarritoAPI(
                                            nombreBD, usuario, item.id, nuevaCantidad, context
                                        ) { recargarCarrito() }
                                    }
                                ) { Text("-", fontSize = 20.sp) }

                                // Cantidad actual
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                        .height(32.dp)
                                        .width(40.dp)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(6.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) { Text("${item.cantidad}", style = MaterialTheme.typography.titleMedium) }

                                // Botón "+" incrementa cantidad
                                IconButton(
                                    onClick = {
                                        val nuevaCantidad = item.cantidad + 1
                                        actualizarCantidadCarritoAPI(
                                            nombreBD, usuario, item.id, nuevaCantidad, context
                                        ) { recargarCarrito() }
                                    }
                                ) { Text("+", fontSize = 20.sp) }
                            }
                        }

                        // Precio total y botón eliminar
                        Column(horizontalAlignment = Alignment.End) {
                            Text("$${item.precio * item.cantidad}", style = MaterialTheme.typography.titleMedium)

                            // Botón eliminar: envía cantidad=0 a la API
                            TextButton(
                                onClick = {
                                    actualizarCantidadCarritoAPI(
                                        nombreBD, usuario, item.id, 0, context
                                    ) { recargarCarrito() }
                                }
                            ) { Text("Eliminar") }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("TOTAL: $${total}", fontSize = 22.sp)
    }
}

// ---------------- MODELOS ----------------
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
    val cantidad: Int
)

// ---------------- FUNCIONES DE API -----------------

fun buscarProductoAPI(codigo: String, nombreBD: String, context: Context, onResult: (Producto?) -> Unit) {
    val url = "https://elpollovolantuso.com/negocioencontrol/api/buscar_producto_api.php"
    val queue = Volley.newRequestQueue(context)
    val request = object : StringRequest(Method.POST, url,
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
                    onResult(null)
                }
            } catch (e: Exception) { e.printStackTrace(); onResult(null) }
        },
        { error -> error.printStackTrace(); Toast.makeText(context, "Error al obtener producto", Toast.LENGTH_SHORT).show(); onResult(null) }
    ) {
        override fun getParams(): MutableMap<String, String> = hashMapOf(
            "codigo" to codigo,
            "nombre_bd" to nombreBD
        )
    }
    queue.add(request)
}

fun agregarAlCarritoAPI(negocio: String, usuario: String, id_producto: String, cantidad: String, context: Context, onResult: () -> Unit) {
    val url = "https://elpollovolantuso.com/negocioencontrol/api/agregar_carrito_api.php"
    val queue = Volley.newRequestQueue(context)
    val request = object : StringRequest(Method.POST, url,
        { response ->
            try {
                val json = JSONObject(response)
                Toast.makeText(context, json.getString("msg"), Toast.LENGTH_SHORT).show()
                onResult()
            } catch (e: Exception) { e.printStackTrace() }
        },
        { error -> error.printStackTrace(); Toast.makeText(context, "Error al agregar al carrito", Toast.LENGTH_SHORT).show() }
    ) {
        override fun getParams(): MutableMap<String, String> = hashMapOf(
            "negocio" to negocio,
            "usuario" to usuario,
            "id_producto" to id_producto,
            "cantidad" to cantidad,
            "estado" to "activo"
        )
    }
    queue.add(request)
}

fun actualizarCantidadCarritoAPI(
    negocio: String,
    usuario: String,
    id_producto: String,
    nuevaCantidad: Int,
    context: Context,
    onResult: () -> Unit
) {
    val url = "https://elpollovolantuso.com/negocioencontrol/api/actualizar_cantidad_carrito_api.php"
    val queue = Volley.newRequestQueue(context)

    // Evitar enviar cantidades negativas
    if (nuevaCantidad < 0) return

    val request = object : StringRequest(Method.POST, url,
        { response ->
            try {
                val json = JSONObject(response)
                if (json.getBoolean("success")) {
                    onResult() // recargar carrito
                } else {
                    Toast.makeText(context, json.getString("msg"), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Respuesta inválida del servidor", Toast.LENGTH_SHORT).show()
            }
        },
        { error ->
            error.printStackTrace()
            Toast.makeText(context, "Error al actualizar carrito", Toast.LENGTH_SHORT).show()
        }
    ) {
        override fun getParams(): MutableMap<String, String> = hashMapOf(
            "negocio" to negocio,
            "usuario" to usuario,
            "id_producto" to id_producto,
            "cantidad" to nuevaCantidad.toString()
        )
    }

    queue.add(request)
}
fun eliminarProductoCarritoAPI(negocio: String, usuario: String, id_producto: String, context: Context, onResult: () -> Unit) {
    val url = "https://elpollovolantuso.com/negocioencontrol/api/eliminar_producto_carrito_api.php"
    val queue = Volley.newRequestQueue(context)
    val request = object : StringRequest(Method.POST, url,
        { onResult() },
        { error -> error.printStackTrace(); Toast.makeText(context, "Error al eliminar producto", Toast.LENGTH_SHORT).show() }
    ) {
        override fun getParams(): MutableMap<String, String> = hashMapOf(
            "negocio" to negocio,
            "usuario" to usuario,
            "id_producto" to id_producto
        )
    }
    queue.add(request)
}

fun obtenerCarritoAPI(negocio: String, usuario: String, context: Context, onResult: (MutableList<ProductoCarrito>) -> Unit) {
    val url = "https://elpollovolantuso.com/negocioencontrol/api/obtener_carrito_api.php"
    val queue = Volley.newRequestQueue(context)
    val request = object : StringRequest(Method.POST, url,
        { response ->
            try {
                val json = JSONObject(response)
                val lista = mutableListOf<ProductoCarrito>()
                if (json.getBoolean("success")) {
                    val carritoJson = json.getJSONArray("carrito")
                    for (i in 0 until carritoJson.length()) {
                        val item = carritoJson.getJSONObject(i)
                        lista.add(
                            ProductoCarrito(
                                id = item.getString("id_producto"),
                                nombre = item.getString("producto"),
                                precio = item.getString("precio").toDouble(),
                                cantidad = item.getString("cantidad").toInt()
                            )
                        )
                    }
                }
                onResult(lista)
            } catch (e: Exception) { e.printStackTrace(); onResult(mutableListOf()) }
        },
        { error -> error.printStackTrace(); Toast.makeText(context, "Error al cargar carrito", Toast.LENGTH_SHORT).show(); onResult(mutableListOf()) }
    ) {
        override fun getParams(): MutableMap<String, String> = hashMapOf(
            "negocio" to negocio,
            "usuario" to usuario
        )
    }
    queue.add(request)
}