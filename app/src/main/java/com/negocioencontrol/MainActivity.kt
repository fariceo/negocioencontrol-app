package com.negocioencontrol

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.negocioencontrol.scanner.ScannerActivity
import com.negocioencontrol.login.LoginScreen
import com.negocioencontrol.gastos.GastosActivity
import com.negocioencontrol.productos.ProductosActivity
import com.negocioencontrol.usuarios.UsuariosActivity
import com.negocioencontrol.cajero.CajeroActivity
import com.negocioencontrol.personas.clientes.ClientesActivity
import com.google.firebase.messaging.FirebaseMessaging

import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Request

import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.InstallStateUpdatedListener

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import com.negocioencontrol.cobros.CobrosPagosActivity


class MainActivity : ComponentActivity() {

    lateinit var prefs: SharedPreferences

    private lateinit var appUpdateManager: AppUpdateManager
    private val UPDATE_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("sesion", MODE_PRIVATE)

        val nombreNegocio =
            prefs.getString("nombre_negocio", "NEGOCIO EN CONTROL") ?: "NEGOCIO EN CONTROL"

        val rol = prefs.getString("rol", "") ?: ""

        if (rol != "admin") {

            when (rol) {
                "cajero" -> startActivity(Intent(this, CajeroActivity::class.java))
                "cliente" -> startActivity(Intent(this, ClientesActivity::class.java))
                else -> startActivity(Intent(this, LoginScreen::class.java))
            }

            finish()
            return
        }

        setContent {

            MainScreen(
                nombreNegocio = nombreNegocio,
                onScanClick = { startActivity(Intent(this, ScannerActivity::class.java)) },
                onProductosClick = { startActivity(Intent(this, ProductosActivity::class.java)) },
                onVentasClick = { startActivity(Intent(this, com.negocioencontrol.ventas.VentasActivity::class.java)) },
                onReportesClick = { startActivity(Intent(this, GastosActivity::class.java)) },
                onBodegaClick = { startActivity(Intent(this, com.negocioencontrol.bodega.BodegaActivity::class.java)) },
                onUsuariosClick = { startActivity(Intent(this, UsuariosActivity::class.java)) },
                onCobrosPagosClick = {
                    startActivity(Intent(this, CobrosPagosActivity::class.java))
                },
                onLogout = {
                    prefs.edit().clear().apply()
                    startActivity(Intent(this, LoginScreen::class.java))
                    finish()
                }
            )
        }

        // 🔥 DESPUÉS DEL UI (NO BLOQUEA)
        initBackgroundTasks()
    }

    private fun initBackgroundTasks() {

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    val usuario = prefs.getString("correo_usuario", "") ?: ""
                    val negocio = prefs.getString("nombre_negocio", "") ?: ""

                    guardarTokenFCM(token, usuario, negocio, this)
                }
            }

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForAppUpdate()
    }

    // =========================
    // 🔥 UPDATE "PRO" (FLEXIBLE + FALLBACK)
    // =========================
    private fun checkForAppUpdate() {

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { info ->

            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {

                try {
                    // 🔵 FLEXIBLE UPDATE (tipo WhatsApp / Play Store)
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        AppUpdateType.FLEXIBLE,
                        this,
                        UPDATE_REQUEST_CODE
                    )

                    listenToUpdateProgress()

                } catch (e: Exception) {
                    Log.e("UPDATE", "Flexible falló, usando IMMEDIATE", e)

                    // 🔴 FALLBACK
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        AppUpdateType.IMMEDIATE,
                        this,
                        UPDATE_REQUEST_CODE
                    )
                }
            }
        }
    }

    // 🔥 progreso de descarga
    private fun listenToUpdateProgress() {

        val listener = InstallStateUpdatedListener { state ->

            if (state.installStatus() == InstallStatus.DOWNLOADING) {

                val progress = (state.bytesDownloaded().toFloat() /
                        state.totalBytesToDownload().toFloat()) * 100

                Log.d("UPDATE", "Descargando: ${progress.toInt()}%")
            }

            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                Log.d("UPDATE", "Listo para instalar")

                appUpdateManager.completeUpdate()
            }
        }

        appUpdateManager.registerListener(listener)
    }
}

// =========================
// 🔥 UI (SIN CAMBIOS)
// =========================
@Composable
fun MainScreen(
    nombreNegocio: String,
    onProductosClick: () -> Unit,
    onScanClick: () -> Unit,
    onVentasClick: () -> Unit,
    onReportesClick: () -> Unit,
    onBodegaClick: () -> Unit,
    onUsuariosClick: () -> Unit,
    onCobrosPagosClick: () -> Unit,
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

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

                Spacer(modifier = Modifier.width(8.dp))

                Text("Productos")
            }
            Button(
                onClick = onScanClick,
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Carrito"
                )

                Spacer(modifier = Modifier.width(8.dp))

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

                Spacer(modifier = Modifier.width(8.dp))

                Text("Ver ventas")
            }

            Button(
                onClick = onReportesClick,
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Reportes"
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Reportes")
            }

            Button(
                onClick = onBodegaClick,
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "Bodega"
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Bodega")
            }

            Button(
                onClick = onUsuariosClick,
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Usuarios"
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Usuarios")

            }

            Button(
                onClick = onCobrosPagosClick,
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = "Cobros y Pagos"
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Cobros y Pagos")
            }


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

// =========================
// 🔥 FCM (SIN CAMBIOS)
// =========================
fun guardarTokenFCM(
    token: String,
    usuario: String,
    negocio: String,
    context: Context
) {
    val url = "https://elpollovolantuso.com/negocioencontrol/api/guardar_token.php"
    val queue = Volley.newRequestQueue(context)

    val request = object : StringRequest(
        Request.Method.POST,
        url,
        { response ->
            Log.d("FCM_SAVE", "OK: $response")
        },
        { error ->
            Log.e("FCM_SAVE", "ERROR", error)
        }
    ) {
        override fun getParams(): MutableMap<String, String> {
            return hashMapOf(
                "token" to token,
                "usuario" to usuario,
                "negocio" to negocio,
                "dispositivo" to "android"
            )
        }
    }

    queue.add(request)
}