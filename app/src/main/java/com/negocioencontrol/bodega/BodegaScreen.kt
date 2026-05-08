package com.negocioencontrol.bodega

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

@Composable
fun BodegaScreen(nombreBD: String) {

    val context = LocalContext.current

    val productos = remember {
        mutableStateListOf<ProductoBodega>()
    }

    var loading by remember {
        mutableStateOf(false)
    }

    var search by remember {
        mutableStateOf("")
    }

    var productoSeleccionado by remember {
        mutableStateOf<ProductoBodega?>(null)
    }

    var mostrarDialog by remember {
        mutableStateOf(false)
    }

    var productoEditar by remember {
        mutableStateOf<ProductoBodega?>(null)
    }

    // =========================
    // CARGAR PRODUCTOS
    // =========================
    fun cargarProductos(busqueda: String = "") {

        if (nombreBD.isBlank()) {

            Toast.makeText(
                context,
                "BD no configurada",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        loading = true

        val url =
            "https://elpollovolantuso.com/negocioencontrol/api/bodega_api.php"

        val request = object : StringRequest(
            Request.Method.POST,
            url,

            { response ->

                try {

                    val json = JSONObject(response)

                    val success =
                        json.getBoolean("success")



                    val arr =
                        json.getJSONArray("productos")

                    val lista =
                        mutableListOf<ProductoBodega>()

                    for (i in 0 until arr.length()) {

                        val obj = arr.getJSONObject(i)

                        lista.add(

                            ProductoBodega(

                                id = obj.optString("id"),

                                idProducto =
                                obj.optString("id_producto"),

                                producto =
                                obj.optString("producto"),

                                descripcion =
                                obj.optString("descripcion"),

                                cantidad =
                                obj.optDouble("cantidad"),

                                precio =
                                obj.optDouble("precio"),

                                categoria =
                                obj.optString("categoria"),

                                codigoBarra =
                                obj.optString("codigo_barra"),

                                stockInicial =
                                obj.optDouble("stock_inicial"),

                                fechaRegistro =
                                obj.optString("fecha_registro"),

                                imagen =
                                obj.optString("imagen"),

                                expandido = false
                            )
                        )
                    }

                    productos.clear()
                    productos.addAll(lista)

                } catch (e: Exception) {

                    e.printStackTrace()

                    Toast.makeText(
                        context,
                        "Error JSON: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                loading = false
            },

            {

                loading = false

                Toast.makeText(
                    context,
                    "Error de conexión",
                    Toast.LENGTH_SHORT
                ).show()
            }

        ) {

            override fun getParams():
                    MutableMap<String, String> {

                return hashMapOf(

                    "nombre_bd" to nombreBD,

                    "buscar" to busqueda
                )
            }
        }

        Volley.newRequestQueue(context)
            .add(request)
    }

    // =========================
    // ELIMINAR PRODUCTO
    // =========================
    fun eliminarProducto(id: String) {

        val url =
            "https://elpollovolantuso.com/negocioencontrol/api/bodega_api.php"

        val request = object : StringRequest(
            Method.POST,
            url,

            { response ->

                try {

                    val json = JSONObject(response)

                    Toast.makeText(
                        context,
                        json.optString("msg"),
                        Toast.LENGTH_SHORT
                    ).show()

                    cargarProductos(search)

                } catch (e: Exception) {

                    e.printStackTrace()
                }
            },

            {

                Toast.makeText(
                    context,
                    "Error eliminando",
                    Toast.LENGTH_SHORT
                ).show()
            }

        ) {

            override fun getParams():
                    MutableMap<String, String> {

                return hashMapOf(

                    "eliminar" to "1",

                    "id" to id,

                    "nombre_bd" to nombreBD
                )
            }
        }

        Volley.newRequestQueue(context)
            .add(request)
    }

    // =========================
    // INIT
    // =========================
    LaunchedEffect(Unit) {
        cargarProductos()
    }

    // =========================
    // UI
    // =========================
    Scaffold(

        floatingActionButton = {

            FloatingActionButton(

                onClick = {

                    productoEditar = null
                    mostrarDialog = true
                }
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {

            Text(
                text = "📦 Inventario",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(

                value = search,

                onValueChange = {

                    search = it
                    cargarProductos(it)
                },

                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("Buscar producto")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (loading) {

                CircularProgressIndicator()

            } else {

                LazyColumn {

                    itemsIndexed(productos) { index, producto ->

                        BodegaCard(

                            producto = producto,

                            onExpandir = {

                                productos[index] =
                                    producto.copy(
                                        expandido =
                                        !producto.expandido
                                    )
                            },

                            onDetalle = {

                                productoSeleccionado =
                                    producto
                            },

                            onEliminar = {

                                eliminarProducto(it.id)
                            },

                            onEditar = {

                                productoEditar = it
                                mostrarDialog = true
                            }
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )
                    }
                }
            }
        }
    }

    // =========================
    // DIALOG AGREGAR / EDITAR
    // =========================
    if (mostrarDialog) {

        AgregarEditarProductoDialog(

            producto = productoEditar,

            onDismiss = {

                mostrarDialog = false
            },

            onGuardar = {
                    producto,
                    descripcion,
                    cantidad,
                    precio,
                    categoria,
                    codigoBarra,
                    stockInicial,
                    imageBitmap: Bitmap?,
                    imagenUri ->

                guardarProducto(

                    context = context,

                    nombreBD = nombreBD,

                    productoEditar = productoEditar,

                    producto = producto,

                    descripcion = descripcion,

                    cantidad = cantidad,

                    precio = precio,

                    categoria = categoria,

                    codigoBarra = codigoBarra,

                    stockInicial = stockInicial,

                    imageBitmap = imageBitmap,

                    imagenUri = imagenUri,

                    onSuccess = {

                        mostrarDialog = false

                        cargarProductos()
                    }
                )
            }
        )
    }

    // =========================
    // DIALOG DETALLE
    // =========================
    productoSeleccionado?.let {

        DetalleBodegaDialog(

            producto = it,

            onDismiss = {

                productoSeleccionado = null
            }
        )
    }
}