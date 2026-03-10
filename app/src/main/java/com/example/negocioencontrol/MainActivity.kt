package com.example.negocioencontrol

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.negocioencontrol.scanner.ScannerActivity

class MainActivity : ComponentActivity() {

    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("sesion", MODE_PRIVATE)

        setContent {
            MainScreen {
                // Cerrar sesión
                prefs.edit().clear().apply()
                val intent = Intent(this, LoginScreen::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Barra superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D1B2A))
                .padding(16.dp)
        ) {
            Text(
                text = "NEGOCIO EN CONTROL",
                color = Color.White,
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Contenido
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .background(Color(0xFF0D1B2A)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Botón Escanear Producto
            Button(
                onClick = {
                    val intent = Intent(context, ScannerActivity::class.java)
                    intent.putExtra("nombreBD", "el_nombre_de_tu_base") // Cambia según tu BD
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCode,
                    contentDescription = "Scanner"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Escanear Producto")
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón cerrar sesión
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Cerrar Sesión", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}