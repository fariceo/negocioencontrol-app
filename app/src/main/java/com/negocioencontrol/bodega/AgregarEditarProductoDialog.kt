// AgregarEditarProductoDialog.kt

package com.negocioencontrol.bodega

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

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
        imageBitmap: Bitmap?,
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

    var imageBitmap by remember {
        mutableStateOf<Bitmap?>(null)
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
            imageBitmap = null
        }

    // =========================
    // CAMARA
    // =========================
    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->

            bitmap?.let {

                imageBitmap = it
                imagenUri = null
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
                        imageBitmap,
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

                    Button(
                        onClick = {

                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.weight(1f)
                    ) {

                        Text("Galería")
                    }

                    Button(
                        onClick = {

                            cameraLauncher.launch(null)
                        },
                        modifier = Modifier.weight(1f)
                    ) {

                        Text("Cámara")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                when {

                    imageBitmap != null -> {

                        Image(
                            bitmap =
                            imageBitmap!!.asImageBitmap(),

                            contentDescription = null,

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),

                            contentScale =
                            ContentScale.Crop
                        )
                    }

                    imagenUri != null -> {

                        val bitmap = remember(imagenUri) {

                            context.contentResolver
                                .openInputStream(imagenUri!!)
                                ?.use {
                                    BitmapFactory.decodeStream(it)
                                }
                        }

                        bitmap?.let {

                            Image(
                                bitmap =
                                it.asImageBitmap(),

                                contentDescription = null,

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),

                                contentScale =
                                ContentScale.Crop
                            )
                        }
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