    package com.negocioencontrol.productos

    import android.content.Context
    import android.widget.Toast
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
    import org.json.JSONObject
    import com.negocioencontrol.scanner.ScannerScreen


    @Composable
    fun ProductosScreen(
        categoria: String,
        onBack: () -> Unit
    ) {

        val context = LocalContext.current

        // 🔥 SHAREDPREFERENCES GLOBAL
        val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val nombreBD = prefs.getString("nombre_bd", "") ?: ""
        val usuario = prefs.getString("correo_usuario", "") ?: ""

        val productos = remember { mutableStateListOf<Producto>() }
        var loading by remember { mutableStateOf(false) }
        var search by remember { mutableStateOf("") }



        var carritoCount by remember { mutableStateOf(0) }
        // =========================
        // CARGAR PRODUCTOS
        // =========================
        fun cargarProductos() {

            if (nombreBD.isEmpty()) {
                Toast.makeText(context, "BD no encontrada", Toast.LENGTH_SHORT).show()
                return
            }

            loading = true

            // 🔥 SI HAY TEXTO -> BUSQUEDA GLOBAL
            val url = if (search.isBlank()) {
                "https://elpollovolantuso.com/negocioencontrol/api/productos_api.php"
            } else {
                "https://elpollovolantuso.com/negocioencontrol/api/buscar_productos_api.php"
            }

            val request = object : StringRequest(
                Request.Method.POST,
                url,
                { response ->

                    try {

                        val json = JSONObject(response)

                        val success = json.optBoolean("success", false)

                        if (!success) {

                            Toast.makeText(
                                context,
                                json.optString("msg", "Error"),
                                Toast.LENGTH_SHORT
                            ).show()

                            loading = false

                        }

                        val array = json.getJSONArray("productos")

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

                        Toast.makeText(
                            context,
                            "Error JSON: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    loading = false
                },
                {

                    loading = false

                    Toast.makeText(
                        context,
                        "Error conexión",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ) {

                override fun getParams(): MutableMap<String, String> {

                    return if (search.isBlank()) {

                        hashMapOf(
                            "nombre_bd" to nombreBD,
                            "categoria" to categoria
                        )

                    } else {

                        hashMapOf(
                            "nombre_bd" to nombreBD,
                            "texto" to search
                        )
                    }
                }
            }

            Volley.newRequestQueue(context).add(request)
        }

        LaunchedEffect(search) {
            cargarProductos()
        }

        Column(Modifier.fillMaxSize().padding(12.dp)) {

            // 🔙 BACK
            Button(onClick = onBack) {
                Text("⬅ Volver")
            }

            Spacer(Modifier.height(8.dp))

            // 📦 TITULO
            Text("📦 $categoria", style = MaterialTheme.typography.titleLarge)

            Button(
                onClick = {
                    val intent = android.content.Intent(
                        context,
                        com.negocioencontrol.scanner.ScannerActivity::class.java
                    )
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🛒 Ver carrito ($carritoCount)")
            }   
            Spacer(Modifier.height(10.dp))

            // 🔎 BUSCADOR
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar producto...") },
                singleLine = true
            )


            Spacer(Modifier.height(10.dp))

            // ⏳ LOADING
            if (loading) {
                Text("Cargando productos...")
            }

            // 📋 LISTA
            LazyColumn {

                items(productos) { p -> 

                    ProductoCard(producto = p) {

                        agregarAlCarrito(
                            context = context,
                            producto = p,
                            cantidad = 1,
                            nombreBD = nombreBD,
                            usuario = usuario
                        )

                        carritoCount++
                    }
                }
            }
        }
    }
    fun agregarAlCarrito(
        context: Context,
        producto: Producto,
        cantidad: Int,
        nombreBD: String,
        usuario: String
    ) {

        if (usuario.isEmpty()) {
            Toast.makeText(context, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://elpollovolantuso.com/negocioencontrol/api/agregar_carrito_api.php"

        val request = object : StringRequest(
            Request.Method.POST,
            url,
            { response ->

                try {
                    val json = JSONObject(response)

                    val ok = json.optBoolean("ok", false)
                    val msg = json.optString("msg", "Sin mensaje")

                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    Toast.makeText(context, "Error respuesta", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(context, "Error conexión", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "nombre_bd" to nombreBD,
                    "id_producto" to producto.idProducto,
                    "cantidad" to cantidad.toString(),
                    "usuario" to usuario
                )
            }
        }

        Volley.newRequestQueue(context).add(request)
    }