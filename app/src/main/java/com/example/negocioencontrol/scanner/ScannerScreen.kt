package com.example.negocioencontrol.scanner

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*

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
import org.json.JSONArray

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import android.content.Intent
import com.example.negocioencontrol.MainActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(nombreBD: String) {

    val context = LocalContext.current
    var producto by remember { mutableStateOf<Producto?>(null) }
    var carrito by remember { mutableStateOf(mutableListOf<ProductoCarrito>()) }
    var cliente by remember { mutableStateOf("") }
    var metodoPago by remember { mutableStateOf("Efectivo") }

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
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {



        Button(onClick = { iniciarScanner() }, modifier = Modifier.fillMaxWidth()) {
            Text("Iniciar Escaneo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        BusquedaManualProductoPanel(
            nombreBD = nombreBD,
            usuario = usuario,
            recargarCarrito = { recargarCarrito() }
        )

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
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            carrito.forEach { item ->

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

                            Text(
                                "Precio unitario: $${"%.2f".format(item.precio)}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {

                                // Botón "-"
                                IconButton(
                                    onClick = {
                                        val nuevaCantidad = item.cantidad - 1
                                        actualizarCantidadCarritoAPI(
                                            nombreBD, usuario, item.id, nuevaCantidad, context
                                        ) { recargarCarrito() }
                                    }
                                ) {
                                    Text("-", fontSize = 20.sp)
                                }

                                // Cantidad
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
                                ) {
                                    Text("${item.cantidad}", style = MaterialTheme.typography.titleMedium)
                                }

                                // Botón "+"
                                IconButton(
                                    onClick = {
                                        val nuevaCantidad = item.cantidad + 1
                                        actualizarCantidadCarritoAPI(
                                            nombreBD, usuario, item.id, nuevaCantidad, context
                                        ) { recargarCarrito() }
                                    }
                                ) {
                                    Text("+", fontSize = 20.sp)
                                }

                            }

                        }

                        // Precio total y eliminar
                        Column(horizontalAlignment = Alignment.End) {

                            Text(
                                "$${"%.2f".format(item.precio * item.cantidad)}",
                                style = MaterialTheme.typography.titleMedium
                            )

                            TextButton(
                                onClick = {
                                    actualizarCantidadCarritoAPI(
                                        nombreBD, usuario, item.id, 0, context
                                    ) { recargarCarrito() }
                                }
                            ) {
                                Text("Eliminar")
                            }

                        }

                    }

                }

            }

        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "TOTAL: $${"%.2f".format(total)}",
            fontSize = 22.sp
        )


        Spacer(modifier = Modifier.height(16.dp))

        DatosCompraScreen(
            nombreBD = nombreBD,
            usuario = usuario,
            carrito = carrito,
            total = total
        )

    }


}
@Composable
fun BusquedaManualProductoPanel(
    nombreBD: String,
    usuario: String,
    recargarCarrito: () -> Unit
) {

    var mostrarBusqueda by remember { mutableStateOf(false) }

    Column {

        Button(
            onClick = { mostrarBusqueda = !mostrarBusqueda },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                if (mostrarBusqueda)
                    "Ocultar búsqueda"
                else
                    "Buscar producto manual"
            )
        }

        AnimatedVisibility(
            visible = mostrarBusqueda,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {

            BusquedaManualProducto(
                nombreBD = nombreBD,
                usuario = usuario,
                recargarCarrito = { recargarCarrito() },
                onClose = { mostrarBusqueda = false }
            )

        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusquedaManualProducto(
    nombreBD: String,
    usuario: String,
    recargarCarrito: () -> Unit,
    onClose: () -> Unit
) {

    val context = LocalContext.current

    var textoBusqueda by remember { mutableStateOf("") }
    var resultados by remember { mutableStateOf<List<Producto>>(emptyList()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                "Buscar producto manualmente",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row {

                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = {

                        textoBusqueda = it

                        if (it.length >= 2) {

                            buscarProductoManualAPI(
                                it,
                                nombreBD,
                                context
                            ) { lista ->

                                resultados = lista

                            }

                        } else {

                            resultados = emptyList()

                        }

                    },
                    label = { Text("Código o nombre") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {

                        buscarProductoManualAPI(
                            textoBusqueda,
                            nombreBD,
                            context
                        ) { lista ->

                            resultados = lista

                        }

                    }
                ) {
                    Text("Buscar")
                }

            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {

                items(resultados) { prod ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            val imagenFinal =
                                if (prod.imagen.startsWith("http"))
                                    prod.imagen
                                else
                                    "https://elpollovolantuso.com/negocioencontrol/assets/images/${prod.imagen}"

                            AsyncImage(
                                model = imagenFinal,
                                contentDescription = prod.producto,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    prod.producto,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    "Stock: ${prod.stock}",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Text(
                                    "$${prod.precio}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                            }

                            Button(
                                onClick = {

                                    agregarAlCarritoAPI(
                                        nombreBD,
                                        usuario,
                                        prod.id,
                                        "1",
                                        context
                                    ) {

                                        recargarCarrito()
                                        onClose()

                                    }

                                }
                            ) {
                                Text("Agregar")
                            }

                        }

                    }

                }

            }

        }

    }

}


fun buscarProductoManualAPI(
    texto: String,
    nombreBD: String,
    context: Context,
    callback: (List<Producto>) -> Unit
) {

    val url = "https://elpollovolantuso.com/negocioencontrol/api/buscar_producto_manual_api.php"

    val queue = Volley.newRequestQueue(context)

    val request = object : StringRequest(
        Method.POST,
        url,
        { response ->

            try {

                val lista = mutableListOf<Producto>()
                val json = JSONArray(response)

                for (i in 0 until json.length()) {

                    val obj = json.getJSONObject(i)

                    lista.add(
                        Producto(
                            id = obj.getString("id_producto"),
                            codigo = obj.optString("codigo_barra", ""),
                            producto = obj.getString("producto"),
                            precio = obj.getString("precio"),
                            categoria = obj.optString("categoria", ""),
                            stock = obj.optString("stock_inicial", "0"),
                            imagen = obj.optString("imagen", "")
                        )
                    )

                }

                callback(lista)

            } catch (e: Exception) {

                Toast.makeText(
                    context,
                    "Error JSON: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

            }

        },
        { error ->

            Toast.makeText(
                context,
                "Error conexión: ${error.networkResponse?.statusCode ?: error.message}",
                Toast.LENGTH_LONG
            ).show()

        }
    ) {

        override fun getParams(): MutableMap<String, String> {

            return hashMapOf(
                "nombre_bd" to nombreBD,
                "texto" to texto
            )

        }

    }

    queue.add(request)

}// ---------------- MODELOS ----------------
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatosCompraScreen(
    nombreBD: String,
    usuario: String,
    carrito: List<ProductoCarrito>,
    total: Double
) {

    val context = LocalContext.current

    var cliente by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var ruc by remember { mutableStateOf("") }

    var metodoPago by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                "Datos de la compra",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cliente,
                onValueChange = { cliente = it },
                label = { Text("Cliente (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = ruc,
                onValueChange = { ruc = it },
                label = { Text("RUC / Cédula") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Método de pago")

            Column {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    RadioButton(
                        selected = metodoPago == "Efectivo",
                        onClick = { metodoPago = "Efectivo" }
                    )

                    Text("Efectivo")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {

                    RadioButton(
                        selected = metodoPago == "Tarjeta",
                        onClick = { metodoPago = "Tarjeta" }
                    )

                    Text("Tarjeta")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {

                    RadioButton(
                        selected = metodoPago == "Transferencia",
                        onClick = { metodoPago = "Transferencia" }
                    )

                    Text("Transferencia")
                }

            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "TOTAL: $${"%.2f".format(total)}",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {

                    if (carrito.isEmpty()) {
                        Toast.makeText(context,"El carrito está vacío",Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (
                        correo.isBlank() ||
                        telefono.isBlank() ||
                        direccion.isBlank() ||
                        ruc.isBlank()
                    ) {

                        Toast.makeText(
                            context,
                            "Complete todos los datos del cliente",
                            Toast.LENGTH_LONG
                        ).show()

                        return@Button
                    }

                    if (metodoPago.isBlank()) {

                        Toast.makeText(
                            context,
                            "Seleccione método de pago",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@Button
                    }

                    registrarVentaAPI(
                        negocio = nombreBD,
                        vendedor = usuario,
                        cliente = cliente,
                        correo = correo,
                        telefono = telefono,
                        direccion = direccion,
                        ruc = ruc,
                        carrito = carrito,
                        total = total,
                        metodoPago = metodoPago,
                        context = context
                    )

                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("FINALIZAR COMPRA")
            }

        }

    }

}
fun registrarVentaAPI(
    negocio: String,
    vendedor: String,
    cliente: String,
    correo: String,
    telefono: String,
    direccion: String,
    ruc: String,
    carrito: List<ProductoCarrito>,
    total: Double,
    metodoPago: String,
    context: Context
) {

    val url = "https://elpollovolantuso.com/negocioencontrol/api/registrar_venta_api.php"

    val queue = Volley.newRequestQueue(context)

    val productosJSON = org.json.JSONArray()

    carrito.forEach {

        val obj = org.json.JSONObject()

        obj.put("producto", it.nombre)
        obj.put("precio", it.precio)
        obj.put("cantidad", it.cantidad)
        obj.put("total", it.precio * it.cantidad)

        productosJSON.put(obj)

    }

    val request = object : StringRequest(Method.POST, url,
        { response ->

            println("RESPUESTA SERVER: $response")
            try {

                val json = JSONObject(response)

                if(json.getBoolean("success")){

                    eliminarCarritoUsuario(
                        negocio,
                        vendedor,
                        context
                    )

                    Toast.makeText(
                        context,
                        "Venta registrada correctamente",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }

            }catch (e:Exception){
                e.printStackTrace()
            }

        },
        { error ->

            error.printStackTrace()

            Toast.makeText(
                context,
                "Error al registrar venta",
                Toast.LENGTH_SHORT
            ).show()

        }
    ) {

        override fun getParams(): MutableMap<String, String> {

            return hashMapOf(
                "negocio" to negocio,
                "vendedor" to vendedor,
                "cliente" to cliente,
                "correo" to correo,
                "telefono" to telefono,
                "direccion" to direccion,
                "ruc" to ruc,
                "productos" to productosJSON.toString(),
                "total" to total.toString(),
                "metodo_pago" to metodoPago
            )

        }

    }

    queue.add(request)

}


fun eliminarCarritoUsuario(
    negocio:String,
    usuario:String,
    context: Context
){

    val url="https://elpollovolantuso.com/negocioencontrol/api/eliminar_venta_carrito_api.php"

    val queue=Volley.newRequestQueue(context)

    val request=object: StringRequest(Method.POST,url,
        { },
        { error-> error.printStackTrace() }
    ){

        override fun getParams(): MutableMap<String,String>{

            return hashMapOf(
                "negocio" to negocio,
                "usuario" to usuario
            )

        }

    }

    queue.add(request)

}