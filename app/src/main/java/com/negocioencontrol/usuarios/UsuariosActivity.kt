package com.negocioencontrol.usuarios

import android.content.Intent
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class UsuariosActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UsuariosMenuScreen()
        }
    }
}

// =========================
// MENÚ PRINCIPAL
// =========================
@Composable
fun UsuariosMenuScreen() {

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .padding(16.dp)
    ) {

        Text(
            text = "Gestión de Usuarios",
            color = Color.White,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // =========================
        // VER USUARIOS
        // =========================
        /*Button(
            onClick = {
                context.startActivity(
                    Intent(context, ListaUsuariosActivity::class.java)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver Usuarios")
        }*/

        Spacer(modifier = Modifier.height(12.dp))

        // =========================
        // ROLES
        // =========================
        Button(
            onClick = {
                context.startActivity(
                    Intent(context, RolesActivity::class.java)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gestionar Roles")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // =========================
        // FUTURO (ejemplo)
        // =========================
        /*Button(
            onClick = {
                context.startActivity(
                    Intent(context, HistorialUsuariosActivity::class.java)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Historial de Usuarios")
        }*/
    }
}