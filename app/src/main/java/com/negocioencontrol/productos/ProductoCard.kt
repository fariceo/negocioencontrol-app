package com.negocioencontrol.productos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.negocioencontrol.Producto
@Composable
fun ProductoCard(
    producto: `Producto`,
    onAgregar: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // ================= IMAGEN =================
            AsyncImage(
                model = producto.imagen,
                contentDescription = producto.producto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ================= NOMBRE =================
            Text(
                text = producto.producto,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // ================= DESCRIPCIÓN =================
            Text(
                text = producto.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // ================= STOCK =================
            Text(
                text = "Stock: ${producto.stock}",
                style = MaterialTheme.typography.labelSmall,
                color = if (producto.stock > 0) Color(0xFF16A34A) else Color.Red
            )

            // ================= PRECIO =================
            Text(
                text = "$${"%.2f".format(producto.precio)}",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF059669)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ================= BOTÓN =================
            Button(
                onClick = onAgregar,
                modifier = Modifier.fillMaxWidth(),
                enabled = producto.stock > 0
            ) {

                Text(
                    if (producto.stock > 0)
                        "Agregar"
                    else
                        "Sin stock"
                )
            }
        }
    }
}