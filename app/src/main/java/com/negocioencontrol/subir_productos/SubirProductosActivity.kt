package com.negocioencontrol.subir_productos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.toolbox.Volley
import com.android.volley.Response
import com.negocioencontrol.network.VolleyMultipartRequest
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class SubirProductosActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val nombreBD = prefs.getString("nombre_bd", "") ?: ""

        Toast.makeText(this, "BD: $nombreBD", Toast.LENGTH_LONG).show()

        val codigoBarra = intent.getStringExtra("codigo_barra") ?: ""

        setContent {
            SubirProductoScreen(nombreBD, codigoBarra)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubirProductoScreen(nombreBD: String, codigoBarra: String) {

    val context = LocalContext.current

    var producto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf(codigoBarra) }
    var cantidad by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }

    // 📸 IMAGENES
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // 📸 CÁMARA
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            imageBitmap = it
            imageUri = null
        }
    }

    // 🖼 GALERÍA
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = it
            imageBitmap = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        OutlinedTextField(producto, { producto = it }, label = { Text("Producto") })
        OutlinedTextField(descripcion, { descripcion = it }, label = { Text("Descripción") })
        OutlinedTextField(codigo, { codigo = it }, label = { Text("Código de barras") })
        OutlinedTextField(cantidad, { cantidad = it }, label = { Text("Cantidad") })
        OutlinedTextField(stock, { stock = it }, label = { Text("Stock inicial") })
        OutlinedTextField(precio, { precio = it }, label = { Text("Precio") })
        OutlinedTextField(categoria, { categoria = it }, label = { Text("Categoría") })

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            cameraLauncher.launch(null)
        }) {
            Text("Tomar Foto")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            galleryLauncher.launch("image/*")
        }) {
            Text("Seleccionar de Galería")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 📸 PREVIEW CÁMARA
        imageBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        // 🖼 PREVIEW GALERÍA
        imageUri?.let { uri ->
            val bitmap = remember(uri) {
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
            }

            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                subirProducto(
                    context,
                    nombreBD,
                    producto,
                    descripcion,
                    codigo,
                    cantidad,
                    stock,
                    precio,
                    categoria,
                    imageBitmap,
                    imageUri
                )
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color(0xFF27AE60)
            )
        ) {
            Text("Guardar Producto")
        }
    }
}

// 🔥 CONVERTIR BITMAP
fun bitmapToBytes(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
    return stream.toByteArray()
}

// 🚀 SUBIDA
fun subirProducto(
    context: Context,
    negocio: String,
    producto: String,
    descripcion: String,
    codigo: String,
    cantidad: String,
    stock: String,
    precio: String,
    categoria: String,
    imageBitmap: Bitmap?,
    imageUri: Uri?
) {

    val url = "https://elpollovolantuso.com/negocioencontrol/api/subir-productos.php"

    val request = object : VolleyMultipartRequest(
        Request.Method.POST,
        url,
        Response.Listener { response ->

            val responseStr = String(response.data)
            val json = JSONObject(responseStr)

            Toast.makeText(context, responseStr, Toast.LENGTH_LONG).show()

            if (json.optBoolean("ok", false)) {
                (context as? ComponentActivity)?.finish()
            }

        },
        Response.ErrorListener { error ->

            val mensaje = error.networkResponse?.data?.toString(Charsets.UTF_8)
                ?: error.message
                ?: "Error desconocido"

            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
        }
    ) {

        override fun getParamsMultipart(): Map<String, String> {
            return hashMapOf(
                "action" to "crear",
                "nombre_bd" to negocio,
                "producto" to producto,
                "descripcion" to descripcion,
                "codigo_barra" to codigo,
                "cantidad" to cantidad,
                "stock_inicial" to stock,
                "precio" to precio,
                "categoria" to categoria
            )
        }

        override fun getByteData(): Map<String, DataPart> {

            val params = HashMap<String, DataPart>()

            // 📸 Cámara
            imageBitmap?.let {
                params["imagen"] = DataPart(
                    "producto.jpg",
                    bitmapToBytes(it),
                    "image/jpeg"
                )
            }

            // 🖼 Galería
            imageUri?.let { uri ->
                val bytes = context.contentResolver.openInputStream(uri)?.use {
                    it.readBytes()
                }

                if (bytes != null) {
                    params["imagen"] = DataPart(
                        "producto.jpg",
                        bytes,
                        "image/jpeg"
                    )
                }
            }

            return params
        }
    }

    Volley.newRequestQueue(context).add(request)
}