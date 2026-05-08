    package com.negocioencontrol.bodega

    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.*
    import androidx.compose.material3.Button
    import androidx.compose.material3.ButtonDefaults
    import androidx.compose.material3.Card
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.unit.dp
    import coil.compose.AsyncImage

    @Composable
    fun BodegaCard(

        producto: ProductoBodega,

        onExpandir: () -> Unit,

        onDetalle: () -> Unit,

        onEliminar: (ProductoBodega) -> Unit,

        onEditar: (ProductoBodega) -> Unit
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(12.dp)
            ) {

                // =========================
                // IMAGEN
                // =========================
                AsyncImage(

                    model = producto.imagen,

                    contentDescription = producto.producto,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clickable {
                            onDetalle()
                        },

                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(10.dp))

                // =========================
                // TITULO
                // =========================
                Text(
                    text = producto.producto,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = producto.descripcion
                )

                Spacer(modifier = Modifier.height(8.dp))

                // =========================
                // PRECIO
                // =========================
                Text(
                    text = "$ ${"%.2f".format(producto.precio)}",
                    color = Color(0xFF16A34A)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // =========================
                // STOCK
                // =========================
                Text(
                    text = "Stock: ${producto.cantidad}"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // =========================
                // BOTON VER DETALLE
                // =========================
                Button(

                    onClick = onExpandir,

                    modifier = Modifier.fillMaxWidth()

                ) {

                    Text(

                        if (producto.expandido)
                            "Ocultar detalle"
                        else
                            "Ver detalle"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // =========================
                // BOTON ELIMINAR
                // =========================
                Button(

                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                        MaterialTheme.colorScheme.error
                    ),

                    onClick = {

                        onEliminar(producto)
                    },

                    modifier = Modifier.fillMaxWidth()

                ) {

                    Text("Eliminar")
                }

                Spacer(modifier = Modifier.height(8.dp))
                // =========================
                // BOTON EDITAR
                // =========================
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    onClick = {
                        onEditar(producto)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("✏ Editar")
                }
                // =========================
                // DETALLE
                // =========================
                if (producto.expandido) {

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Categoría: ${producto.categoria}")

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Código: ${producto.codigoBarra}")

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Stock inicial: ${producto.stockInicial}")

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Fecha: ${producto.fechaRegistro}")
                }
            }
        }
    }