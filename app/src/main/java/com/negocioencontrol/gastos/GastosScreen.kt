package com.negocioencontrol.gastos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun GastosScreen(
    navController: NavHostController,
    nombreBD: String
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("📊 Gestión de Gastos")

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                navController.navigate("ingresar_gasto_screen")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("➕ Ingresar Gasto")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                navController.navigate("lista_gastos_screen")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📋 Ver Lista de Gastos")
        }
    }
}