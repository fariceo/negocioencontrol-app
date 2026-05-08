package com.negocioencontrol.bodega

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun DetalleBodegaDialog(
    producto: ProductoBodega,
    onDismiss: () -> Unit
) {

    AlertDialog(

        onDismissRequest = onDismiss,

        confirmButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("Cerrar")
            }
        },

        title = {
            Text(
                text = producto.producto,
                style = MaterialTheme.typography.titleLarge
            )
        },

        text = {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {

                // =========================
                // IMAGEN
                // =========================
                AsyncImage(
                    model = producto.imagen,
                    contentDescription = producto.producto,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),

                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(14.dp))

                // =========================
                // DESCRIPCION
                // =========================
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text(
                            "Descripción",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(producto.descripcion)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // =========================
                // PRECIO
                // =========================
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text(
                            "Precio",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            "$ ${"%.2f".format(producto.precio)}",
                            color = Color(0xFF16A34A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // =========================
                // STOCK
                // =========================
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text(
                            "Cantidad actual",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("${producto.cantidad}")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // =========================
                // CATEGORIA
                // =========================
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text(
                            "Categoría",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(producto.categoria)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // =========================
                // CODIGO BARRAS
                // =========================
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text(
                            "Código de barras",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(producto.codigoBarra)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // =========================
                // STOCK INICIAL
                // =========================
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text(
                            "Stock inicial",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("${producto.stockInicial}")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // =========================
                // FECHA
                // =========================
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text(
                            "Fecha registro",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(producto.fechaRegistro)
                    }
                }
            }
        }
    )
}