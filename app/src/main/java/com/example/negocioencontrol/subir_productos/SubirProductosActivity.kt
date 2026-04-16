package com.example.negocioencontrol.subir_productos

import android.content.Context
import android.net.Uri
import android.widget.Toast
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
import org.json.JSONObject

import com.example.negocioencontrol.network.VolleyMultipartRequest
import com.android.volley.Response

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class SubirProductosActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SubirProductoScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun SubirProductoScreen() {

    val context = LocalContext.current

    var producto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // 📸 Selector de imagen
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
        uri?.let {
            val input = context.contentResolver.openInputStream(it)
            bitmap = android.graphics.BitmapFactory.decodeStream(input)
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
            launcher.launch("image/*")
        }) {
            Text("Seleccionar Imagen")
        }

        bitmap?.let {
            Spacer(modifier = Modifier.height(10.dp))
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                subirProducto(
                    context,
                    producto,
                    descripcion,
                    codigo,
                    cantidad,
                    stock,
                    precio,
                    categoria,
                    imageUri
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF27AE60))
        ) {
            Text("Guardar Producto")
        }
    }
}


fun subirProducto(
    context: Context,
    producto: String,
    descripcion: String,
    codigo: String,
    cantidad: String,
    stock: String,
    precio: String,
    categoria: String,
    imageUri: Uri?
) {

    val url = "https://elpollovolantuso.com/negocioencontrol/api/subir-productos.php"

    val request = object : VolleyMultipartRequest(
        Request.Method.POST,
        url,
        Response.Listener { response ->
            val json = JSONObject(String(response.data))
            Toast.makeText(context, json.getString("msg"), Toast.LENGTH_LONG).show()
        },
        Response.ErrorListener { error ->
            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
        }
    ) {

        override fun getParams(): MutableMap<String, String> {
            return hashMapOf(
                "action" to "crear",
                "producto" to producto,
                "descripcion" to descripcion,
                "codigo_barra" to codigo,
                "cantidad" to cantidad,
                "stock_inicial" to stock,
                "precio" to precio,
                "categoria" to categoria
            )
        }

        override fun getByteData(): MutableMap<String, DataPart> {
            val params = HashMap<String, DataPart>()

            imageUri?.let {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream!!.readBytes()

                params["imagen"] = DataPart(
                    "producto.jpg",
                    bytes,
                    "image/jpeg"
                )
            }

            return params
        }
    }

    Volley.newRequestQueue(context).add(request)
}