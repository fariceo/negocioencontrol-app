package com.negocioencontrol.bodega

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.platform.LocalContext
import android.graphics.Bitmap

import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import java.io.File



@Composable
fun AgregarEditarProductoDialog(

    producto: ProductoBodega? = null,

    onDismiss: () -> Unit,

    onGuardar: (

        producto: String,
        descripcion: String,
        cantidad: String,
        precio: String,
        categoria: String,
        codigoBarra: String,
        stockInicial: String,
        imagenUri: Uri?

    ) -> Unit
) {

    var nombre by remember {
        mutableStateOf(producto?.producto ?: "")
    }

    var descripcion by remember {
        mutableStateOf(producto?.descripcion ?: "")
    }

    var cantidad by remember {
        mutableStateOf(
            producto?.cantidad?.toString() ?: ""
        )
    }

    var precio by remember {
        mutableStateOf(
            producto?.precio?.toString() ?: ""
        )
    }

    var categoria by remember {
        mutableStateOf(producto?.categoria ?: "")
    }

    var codigoBarra by remember {
        mutableStateOf(producto?.codigoBarra ?: "")
    }

    var stockInicial by remember {
        mutableStateOf(
            producto?.stockInicial?.toString() ?: ""
        )
    }

    var imagenUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var cameraImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val context = LocalContext.current
// =========================
// GALERIA
// =========================
    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract =
            ActivityResultContracts.GetContent()
        ) {

            imagenUri = it
        }

// =========================
// CAMARA
// =========================
    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract =
            ActivityResultContracts.TakePicture()
        ) { success ->

            if (success) {

                imagenUri = cameraImageUri
            }
        }

    AlertDialog(

        onDismissRequest = onDismiss,

        confirmButton = {

            Button(

                onClick = {

                    onGuardar(

                        nombre,
                        descripcion,
                        cantidad,
                        precio,
                        categoria,
                        codigoBarra,
                        stockInicial,
                        imagenUri
                    )
                }
            ) {

                Text("Guardar")
            }
        },

        dismissButton = {

            OutlinedButton(
                onClick = onDismiss
            ) {

                Text("Cancelar")
            }
        },

        title = {

            Text(
                if (producto == null)
                    "Nuevo Producto"
                else
                    "Editar Producto"
            )
        },

        text = {

            Column(

                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState()
                    )

            ) {

                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                    },
                    label = {
                        Text("Producto")
                    },
                    modifier =
                    Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = {
                        descripcion = it
                    },
                    label = {
                        Text("Descripción")
                    },
                    modifier =
                    Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cantidad,
                    onValueChange = {
                        cantidad = it
                    },
                    label = {
                        Text("Cantidad")
                    },
                    modifier =
                    Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = precio,
                    onValueChange = {
                        precio = it
                    },
                    label = {
                        Text("Precio")
                    },
                    modifier =
                    Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = categoria,
                    onValueChange = {
                        categoria = it
                    },
                    label = {
                        Text("Categoría")
                    },
                    modifier =
                    Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = codigoBarra,
                    onValueChange = {
                        codigoBarra = it
                    },
                    label = {
                        Text("Código de barras")
                    },
                    modifier =
                    Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = stockInicial,
                    onValueChange = {
                        stockInicial = it
                    },
                    label = {
                        Text("Stock inicial")
                    },
                    modifier =
                    Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
                ) {

                    // =========================
                    // GALERIA
                    // =========================
                    Button(
                        onClick = {

                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.weight(1f)
                    ) {

                        Text("Galería")
                    }

                    // =========================
                    // CAMARA
                    // =========================
                    Button(
                        onClick = {

                            val file = File(
                                context.cacheDir,
                                "camera_photo.jpg"
                            )

                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )

                            cameraImageUri = uri

                            cameraLauncher.launch(uri)

                        },
                        modifier = Modifier.weight(1f)
                    ) {

                        Text("Cámara")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                when {

                    imagenUri != null -> {

                        Image(
                            painter =
                            rememberAsyncImagePainter(imagenUri),

                            contentDescription = null,

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),

                            contentScale =
                            ContentScale.Crop
                        )
                    }

                    !producto?.imagen.isNullOrBlank() -> {

                        Image(
                            painter =
                            rememberAsyncImagePainter(
                                producto?.imagen
                            ),

                            contentDescription = null,

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),

                            contentScale =
                            ContentScale.Crop
                        )
                    }
                }
            }
        }
    )
}