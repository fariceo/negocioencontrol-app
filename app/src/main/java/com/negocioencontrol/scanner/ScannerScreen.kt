        package com.negocioencontrol.scanner

        import android.app.Activity
        import android.content.Context
        import android.widget.Toast
        import androidx.activity.compose.rememberLauncherForActivityResult
        import androidx.activity.result.contract.ActivityResultContracts
        import androidx.compose.foundation.layout.*

        import androidx.compose.foundation.shape.RoundedCornerShape
        import androidx.compose.material3.*
        import androidx.compose.runtime.*
        import androidx.compose.ui.Alignment
        import androidx.compose.ui.Modifier
        import androidx.compose.ui.platform.LocalContext
        import androidx.compose.ui.unit.dp
        import androidx.compose.ui.unit.sp
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
        import androidx.compose.ui.graphics.Color
        import com.negocioencontrol.MainActivity
        import androidx.compose.foundation.clickable

        import com.negocioencontrol.subir_productos.SubirProductosActivity
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

            var codigoParaSubir by remember { mutableStateOf("") }
            var seccionActiva by remember { mutableStateOf("scanner") }

            fun recargarCarrito() {
                obtenerCarritoAPI(nombreBD, usuario, context) {
                    carrito = it
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

                    if (codigo.isEmpty()) {
                        iniciarScanner()
                        return@rememberLauncherForActivityResult
                    }

                    buscarProductoAPI(codigo, nombreBD, context) { prod ->

                        if (prod != null) {

                            producto = prod

                            agregarAlCarritoAPI(nombreBD, usuario, prod.id, "1", context) {
                                recargarCarrito()
                                seccionActiva = "carrito"

                                // ✅ SOLO AQUÍ sigue escaneando
                                iniciarScanner()
                            }

                        } else {

                            // ❌ NO relanzar scanner aquí

                            android.app.AlertDialog.Builder(context)
                                .setTitle("Producto no encontrado")
                                .setMessage("¿Qué deseas hacer?")

                                .setPositiveButton("Agregar producto") { _, _ ->

                                    val intent = Intent(context, SubirProductosActivity::class.java)

                                    intent.putExtra("codigo_barra", codigo)
                                    intent.putExtra("nombre_bd", nombreBD)

                                    context.startActivity(intent)
                                }

                                .setNegativeButton("Seguir escaneando") { _, _ ->
                                    iniciarScanner()
                                }

                                .show()
                        }

                    }

                } else if (result.resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
                }
            }

            iniciarScanner = {
                val activity = context as? Activity

                if (activity != null) {
                    val integrator = IntentIntegrator(activity)
                    integrator.setCaptureActivity(CustomScannerActivity::class.java)
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                    integrator.setPrompt("Escanea el código de barras")
                    integrator.setBeepEnabled(true)
                    integrator.setOrientationLocked(true)
                    launcher.launch(integrator.createScanIntent())
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {

                // =========================
                // 🔹 AGREGAR PRODUCTOS
                // =========================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            seccionActiva = if (seccionActiva == "scanner") "" else "scanner"
                        },
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            if (seccionActiva == "scanner") "Agregar productos ▲" else "Agregar productos ▼",
                            style = MaterialTheme.typography.titleLarge
                        )

                        AnimatedVisibility(
                            visible = seccionActiva == "scanner",
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {

                            Column {

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { iniciarScanner() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF16A34A)
                                    )
                                ) {
                                    Text("📷 Escanear código")
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                BusquedaManualProductoPanel(
                                    nombreBD = nombreBD,
                                    usuario = usuario,
                                    recargarCarrito = { recargarCarrito() }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // =========================
                // 🔹 PRODUCTO ESCANEADO
                // =========================
                producto?.let { p ->

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            Text(p.producto)
                            Text("Stock: ${p.stock}")
                            Text("$${p.precio}")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // =========================
                // 🔹 CARRITO
                // =========================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            seccionActiva = if (seccionActiva == "carrito") "" else "carrito"
                        },
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            if (seccionActiva == "carrito") "🛒 Carrito ▲" else "🛒 Carrito ▼",
                            style = MaterialTheme.typography.titleLarge
                        )

                        AnimatedVisibility(
                            visible = seccionActiva == "carrito",
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {

                            Column {

                                Spacer(modifier = Modifier.height(10.dp))

                                if (carrito.isEmpty()) {
                                    Text("Tu carrito está vacío", color = Color.Gray)
                                }

                                carrito.forEach { item ->

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {

                                        Column(modifier = Modifier.padding(12.dp)) {

                                            Text(
                                                item.nombre,
                                                style = MaterialTheme.typography.titleMedium
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                "$${"%.2f".format(item.precio * item.cantidad)}",
                                                color = Color(0xFF059669)
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Row(verticalAlignment = Alignment.CenterVertically) {

                                                IconButton(onClick = {
                                                    val nuevaCantidad = item.cantidad - 1
                                                    actualizarCantidadCarritoAPI(
                                                        nombreBD, usuario, item.id, nuevaCantidad, context
                                                    ) { recargarCarrito() }
                                                }) {
                                                    Text("-")
                                                }

                                                Text("${item.cantidad}")

                                                IconButton(onClick = {
                                                    val nuevaCantidad = item.cantidad + 1
                                                    actualizarCantidadCarritoAPI(
                                                        nombreBD, usuario, item.id, nuevaCantidad, context
                                                    ) { recargarCarrito() }
                                                }) {
                                                    Text("+")
                                                }

                                                Spacer(modifier = Modifier.weight(1f))

                                                TextButton(onClick = {
                                                    actualizarCantidadCarritoAPI(
                                                        nombreBD, usuario, item.id, 0, context
                                                    ) { recargarCarrito() }
                                                }) {
                                                    Text("Eliminar", color = Color.Red)
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    "TOTAL: $${"%.2f".format(total)}",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // =========================
                // 🔹 DATOS COMPRA
                // =========================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            seccionActiva = if (seccionActiva == "datos") "" else "datos"
                        },
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {

                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            if (seccionActiva == "datos") "Datos de la compra ▲" else "Datos de la compra ▼",
                            style = MaterialTheme.typography.titleLarge
                        )

                        AnimatedVisibility(
                            visible = seccionActiva == "datos"
                        ) {

                            DatosCompraScreen(
                                nombreBD = nombreBD,
                                usuario = usuario,
                                carrito = carrito,
                                total = total
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

        // =========================
        // 🔹 SUBIR PRODUCTO
        // =========================

                }
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF16A34A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (mostrarBusqueda) "🔽 Ocultar búsqueda" else "🔍 Buscar producto manual",
                        fontSize = 16.sp
                    )
                }

                // 🔥 AQUÍ ESTÁ LA CLAVE
                AnimatedVisibility(visible = mostrarBusqueda) {

                    BusquedaManualProducto(
                        nombreBD = nombreBD,
                        usuario = usuario,
                        recargarCarrito = recargarCarrito,
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

                var buscando by remember { mutableStateOf(false) }

                var mostrarDialogo by remember { mutableStateOf(false) }

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

                                        if (textoBusqueda.length >= 2 && lista.isEmpty()) {
                                            mostrarDialogo = true
                                        }

                                    }

                                }
                            ){
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



                        if (textoBusqueda.length >= 2 && resultados.isEmpty() && !mostrarDialogo) {

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                "Producto no encontrado",
                                color = Color.Red
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {

                                    val intent = Intent(context, SubirProductosActivity::class.java)

                                    intent.putExtra("codigo_barra", textoBusqueda)
                                    intent.putExtra("nombre_bd", nombreBD)

                                    context.startActivity(intent)

                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("➕ Agregar producto")
                            }
                        }

                    }

                    if (mostrarDialogo) {

                        android.app.AlertDialog.Builder(context)
                            .setTitle("Producto no encontrado")
                            .setMessage("¿Qué deseas hacer?")

                            .setPositiveButton("Agregar producto") { _, _ ->

                                val intent = Intent(context, SubirProductosActivity::class.java)

                                intent.putExtra("codigo_barra", textoBusqueda)
                                intent.putExtra("nombre_bd", nombreBD)

                                context.startActivity(intent)

                                mostrarDialogo = false
                            }

                            .setNegativeButton("Seguir buscando") { _, _ ->
                                mostrarDialogo = false
                            }

                            .setOnDismissListener {
                                mostrarDialogo = false
                            }

                            .show()
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
        //holita
            val context = LocalContext.current

            var cliente by remember { mutableStateOf("") }
            var correo by remember { mutableStateOf("") }
            var telefono by remember { mutableStateOf("") }
            var direccion by remember { mutableStateOf("") }
            var ruc by remember { mutableStateOf("") }
            var tipoIdentificacion by remember { mutableStateOf("07") }
            var metodoPago by remember { mutableStateOf("") }

            var generarFactura by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp) // 🔥 ajustado para que no se vea doble padding
            ) {

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
                            selected = metodoPago == "transferencia",
                            onClick = { metodoPago = "transferencia" }
                        )
                        Text("transferencia")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = metodoPago == "Credito",
                            onClick = { metodoPago = "Credito" }
                        )
                        Text("Credito")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Tipo de identificación")

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = tipoIdentificacion == "05",
                            onClick = { tipoIdentificacion = "05" }
                        )
                        Text("Cédula")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = tipoIdentificacion == "04",
                            onClick = { tipoIdentificacion = "04" }
                        )
                        Text("RUC")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = tipoIdentificacion == "06",
                            onClick = { tipoIdentificacion = "06" }
                        )
                        Text("Pasaporte")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = tipoIdentificacion == "07",
                            onClick = { tipoIdentificacion = "07" }
                        )
                        Text("Consumidor Final")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = generarFactura,
                        onCheckedChange = { generarFactura = it }
                    )
                    Text("Generar factura electrónica")
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
                            Toast.makeText(context, "El carrito está vacío", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (generarFactura) {
                            if (
                                cliente.isBlank() ||
                                correo.isBlank() ||
                                telefono.isBlank() ||
                                direccion.isBlank() ||
                                ruc.isBlank()
                            ) {
                                Toast.makeText(
                                    context,
                                    "Complete todos los datos para la factura",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }
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
                            cliente = if (cliente.isBlank()) "Consumidor Final" else cliente,
                            correo = correo,
                            telefono = telefono,
                            direccion = direccion,
                            ruc = ruc,
                            tipoIdentificacion = tipoIdentificacion,
                            carrito = carrito,
                            total = total,
                            metodoPago = metodoPago,
                            rolDestino = "cliente",
                            generarFactura = generarFactura,
                            context = context
                        )

                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("FINALIZAR COMPRA")
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
            tipoIdentificacion: String,
            carrito: List<ProductoCarrito>,
            total: Double,
            metodoPago: String,
            rolDestino: String,
            generarFactura: Boolean,
            context: Context
        ) {

            val url = "https://elpollovolantuso.com/negocioencontrol/api/registrar_venta_api.php"

            val queue = Volley.newRequestQueue(context)

            val productosJSON = JSONArray()
            val timezone = java.util.TimeZone.getDefault().id

            carrito.forEach {
                val obj = JSONObject()
                obj.put("producto", it.nombre)
                obj.put("precio", it.precio)
                obj.put("cantidad", it.cantidad)
                obj.put("total", it.precio * it.cantidad)
                productosJSON.put(obj)
            }

            val request = object : StringRequest(
                Method.POST,
                url,
                responseListener@{ response ->

                    try {
                        if (response.isNullOrEmpty()) {
                            Toast.makeText(context, "Respuesta vacía del servidor", Toast.LENGTH_LONG).show()
                            return@responseListener
                        }

                        val json = JSONObject(response)

                        if (json.optBoolean("success", false)) {

                            val idVenta = json.optInt("id_venta", 0)

                            if (generarFactura) {

                                if (idVenta > 0) {
                                    registrarFacturaSRI(
                                        negocio = negocio,
                                        idVenta = idVenta,
                                        clienteNombre = if (cliente.isBlank()) "Consumidor Final" else cliente,
                                        clienteIdentificacion = ruc,
                                        tipoIdentificacion = tipoIdentificacion,
                                        correo = correo,
                                        direccion = direccion,
                                        telefono = telefono,
                                        metodoPago = metodoPago,
                                        context = context,
                                        onSuccess = {
                                            eliminarCarritoUsuario(negocio, vendedor, context)

                                            Toast.makeText(
                                                context,
                                                "Venta + factura registrada correctamente",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            val intent = Intent(context, MainActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            context.startActivity(intent)
                                        },
                                        onError = { errorFactura ->
                                            Toast.makeText(
                                                context,
                                                "Venta guardada, pero falló factura: $errorFactura",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    )
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Venta registrada, pero no se recibió id_venta para facturar",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                            } else {
                                eliminarCarritoUsuario(negocio, vendedor, context)

                                Toast.makeText(
                                    context,
                                    "Venta registrada correctamente",
                                    Toast.LENGTH_LONG
                                ).show()

                                val intent = Intent(context, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                            }

                        } else {
                            Toast.makeText(
                                context,
                                json.optString("msg", "Error desconocido"),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            context,
                            "Error procesando respuesta: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                },
                { error ->

                    error.printStackTrace()

                    val mensaje = error.networkResponse?.data?.toString(Charsets.UTF_8)
                        ?: error.message

                    Toast.makeText(
                        context,
                        "Error servidor: $mensaje",
                        Toast.LENGTH_LONG
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
                        "tipo_identificacion" to tipoIdentificacion,
                        "productos" to productosJSON.toString(),
                        "total" to total.toString(),
                        "rol" to rolDestino,
                        "metodo_pago" to metodoPago,
                        "timezone" to timezone
                    )
                }

            }

            queue.add(request)
        }
        fun registrarFacturaSRI(
            negocio: String,
            idVenta: Int,
            clienteNombre: String,
            clienteIdentificacion: String,
            tipoIdentificacion: String,
            correo: String,
            direccion: String,
            telefono: String,
            metodoPago: String,
            context: Context,
            onSuccess: () -> Unit,
            onError: (String) -> Unit
        ) {

            val url = "https://elpollovolantuso.com/negocioencontrol/api/factura_sri_api.php"
            val queue = Volley.newRequestQueue(context)

            val request = object : StringRequest(
                Method.POST,
                url,
                { response ->
                    try {
                        val json = JSONObject(response)

                        if (json.optBoolean("ok", false)) {
                            onSuccess()
                        } else {
                            val mensaje = json.optString("mensaje", "No se pudo registrar factura")
                            val errorDetalle = json.optString("error", "")
                            onError("$mensaje $errorDetalle")
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        onError("Respuesta inválida de factura: ${e.message}")
                    }
                },
                { error ->
                    error.printStackTrace()
                    val mensaje = error.networkResponse?.data?.toString(Charsets.UTF_8)
                        ?: error.message
                        ?: "Error desconocido al facturar"

                    onError(mensaje)
                }
            ) {

                override fun getParams(): MutableMap<String, String> {
                    return hashMapOf(
                        "negocio" to negocio,
                        "id_venta" to idVenta.toString(),
                        "cliente_nombre" to clienteNombre,
                        "cliente_identificacion" to clienteIdentificacion,
                        "tipo_identificacion" to tipoIdentificacion,
                        "correo" to correo,
                        "direccion" to direccion,
                        "telefono" to telefono,
                        "metodo_pago" to metodoPago.lowercase()
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


