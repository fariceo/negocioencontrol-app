package com.negocioencontrol.personas.clientes

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.negocioencontrol.productos.ProductosActivity

class ClientesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ClientesScreen(
                onProductosClick = {
                    startActivity(
                        Intent(this, ProductosActivity::class.java)
                    )
                },
                onCarritoClick = {
                    // Aquí luego conectas tu CarritoActivity
                },
                onPedidosClick = {
                    // Aquí luego conectas historial de pedidos
                }
            )
        }
    }
}

@Composable
fun ClientesScreen(
    onProductosClick: () -> Unit,
    onCarritoClick: () -> Unit,
    onPedidosClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Panel de Clientes",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Gestiona tus compras y pedidos",
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onProductosClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Storefront, contentDescription = "Productos")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ver productos")
        }

        Button(
            onClick = onCarritoClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ver carrito")
        }

        Button(
            onClick = onPedidosClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.List, contentDescription = "Pedidos")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Mis pedidos")
        }
    }
}