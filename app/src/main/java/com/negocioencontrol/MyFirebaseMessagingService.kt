package com.negocioencontrol // <-- NUEVO PAQUETE

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.negocioencontrol.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val prefs = getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val usuario = prefs.getString("correo_usuario", "") ?: ""
        val negocio = prefs.getString("nombre_bd", "") ?: ""

        if (usuario.isNotEmpty() && negocio.isNotEmpty()) {
            guardarTokenFCM(token, usuario, negocio, applicationContext)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val titulo = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Nueva notificación"

        val mensaje = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Tienes un nuevo mensaje"

        crearCanalNotificacion()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, "canal_ventas")

            .setSmallIcon(R.mipmap.negocioencontrol_ic)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        NotificationManagerCompat.from(this).notify(
            System.currentTimeMillis().toInt(),
            builder.build()
        )
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "canal_ventas",
                "Notificaciones de ventas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para notificaciones de ventas"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}