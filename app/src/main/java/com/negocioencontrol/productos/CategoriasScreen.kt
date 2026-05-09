package com.negocioencontrol.productos

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

@Composable
fun CategoriasScreen(
    nombreBD: String,
    onCategoriaClick: (String) -> Unit
) {

    val context = LocalContext.current
    val categorias = remember { mutableStateListOf<Categoria>() }

    var loading by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    fun cargarCategorias() {

        loading = true

        val url = "https://elpollovolantuso.com/negocioencontrol/api/categorias_api.php"

        val request = object : StringRequest(
            Request.Method.POST,
            url,
            { response ->

                try {
                    val json = JSONObject(response)
                    val array = json.getJSONArray("categorias")

                    categorias.clear()

                    for (i in 0 until array.length()) {

                        val obj = array.getJSONObject(i)
                        val imgs = obj.getJSONArray("imagenes")

                        val lista = mutableListOf<String>()
                        for (j in 0 until imgs.length()) {
                            lista.add(imgs.getString(j))
                        }

                        categorias.add(
                            Categoria(
                                nombre = obj.getString("nombre"),
                                imagenes = lista
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
        cargarCategorias()
    }

    val categoriasFiltradas = categorias.filter {
        it.nombre.contains(searchText, ignoreCase = true)
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        Text("📂 Categorías", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(10.dp))

        // 🔍 BUSCADOR
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar categoría") },
            singleLine = true
        )

        Spacer(Modifier.height(10.dp))

        if (loading) {
            Text("Cargando...")
        }

        // GRID
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp)
        ) {

            items(categoriasFiltradas) { cat ->

                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            onCategoriaClick(cat.nombre.trim())
                        }
                ) {

                    Column(Modifier.padding(8.dp)) {

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            cat.imagenes.take(3).forEach { img ->

                                AsyncImage(
                                    model = img,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .padding(2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(cat.nombre)
                    }
                }
            }
        }
    }
}