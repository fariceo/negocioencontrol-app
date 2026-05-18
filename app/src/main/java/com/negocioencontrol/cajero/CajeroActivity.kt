package com.negocioencontrol.cajero

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.negocioencontrol.login.LoginScreen
import com.negocioencontrol.productos.ProductosActivity
import com.negocioencontrol.scanner.ScannerActivity
import com.negocioencontrol.ventas.VentasActivity

class CajeroActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("sesion", MODE_PRIVATE)

        val nombreNegocio =
            prefs.getString(
                "nombre_negocio",
                "NEGOCIO EN CONTROL"
            ) ?: "NEGOCIO EN CONTROL"

        setContent {

            CajeroScreen(
                nombreNegocio = nombreNegocio,

                onProductosClick = {
                    startActivity(
                        Intent(
                            this,
                            ProductosActivity::class.java
                        )
                    )
                },

                onCarritoClick = {
                    startActivity(
                        Intent(
                            this,
                            ScannerActivity::class.java
                        )
                    )
                },

                onVentasClick = {
                    startActivity(
                        Intent(
                            this,
                            VentasActivity::class.java
                        )
                    )
                },

                onLogout = {

                    prefs.edit().clear().apply()

                    val intent = Intent(
                        this,
                        LoginScreen::class.java
                    )

                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}

@Composable
fun CajeroScreen(
    nombreNegocio: String,
    onProductosClick: () -> Unit,
    onCarritoClick: () -> Unit,
    onVentasClick: () -> Unit,
    onLogout: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D1B2A))
                .padding(16.dp)
        ) {

            Text(
                text = nombreNegocio,
                color = Color.White,
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D1B2A))
                .padding(24.dp),

            verticalArrangement = Arrangement.spacedBy(12.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                onClick = onProductosClick,
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = "Productos"
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text("Productos")
            }

            Button(
                onClick = onCarritoClick,
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Carrito"
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text("Ver carrito")
            }

            Button(
                onClick = onVentasClick,
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector = Icons.Default.PointOfSale,
                    contentDescription = "Ventas"
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text("Ventas")
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = onLogout,

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(12.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {

                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Cerrar sesión"
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "Cerrar Sesión",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
    }
}