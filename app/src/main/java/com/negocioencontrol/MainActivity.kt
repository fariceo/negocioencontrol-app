package com.negocioencontrol

import android.app.Activity
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


class MainActivity : ComponentActivity() {

    lateinit var prefs: SharedPreferences

    private lateinit var appUpdateManager: AppUpdateManager
    private val UPDATE_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("sesion", MODE_PRIVATE)

        val nombreNegocio =
            prefs.getString("nombre_negocio", "NEGOCIO EN CONTROL") ?: "NEGOCIO EN CONTROL"

        // 🔥 UPDATE MANAGER
        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForAppUpdate()

        // 🔥 FIREBASE TOKEN
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("FCM", "Error obteniendo token", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                val usuario = prefs.getString("correo_usuario", "") ?: ""
                val negocio = prefs.getString("nombre_negocio", "") ?: ""

                guardarTokenFCM(token, usuario, negocio, this)
            }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

        setContent {
            MainScreen(
                nombreNegocio = nombreNegocio,
                onScanClick = {
                    startActivity(Intent(this, ScannerActivity::class.java))
                },
                onProductosClick = {
                    startActivity(
                        Intent(this, com.negocioencontrol.productos.ProductosActivity::class.java)
                    )
                },
                onVentasClick = {
                    startActivity(
                        Intent(this, com.negocioencontrol.ventas.VentasActivity::class.java)
                    )
                },
                onReportesClick = {
                    startActivity(
                        Intent(this, com.negocioencontrol.gastos.GastosActivity::class.java)
                    )
                },
                onBodegaClick = {
                    startActivity(
                        Intent(this, com.negocioencontrol.bodega.BodegaActivity::class.java)
                    )
                },
                onLogout = {
                    prefs.edit().clear().apply()

                    val intent = Intent(this, LoginScreen::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            )
        }
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
                Text("Productos")
            }
            Button(
                onClick = onScanClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver carrito y finalizar pedido")
            }

            Button(
                onClick = onVentasClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver ventas")
            }

            Button(
                onClick = onReportesClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reportes")
            }

            Button(
                onClick = onBodegaClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Bodega")
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