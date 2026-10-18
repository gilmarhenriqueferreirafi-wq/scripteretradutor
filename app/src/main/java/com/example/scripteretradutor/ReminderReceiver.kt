package com.example.scripteretradutor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(CHANNEL, "Lembretes de escrita", NotificationManager.IMPORTANCE_DEFAULT))
        }
        manager.notify(NOTIFICATION_ID, NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle("Hora de escrever")
            .setContentText("Você ainda não escreveu hoje.")
            .setAutoCancel(true)
            .build())
    }

    companion object { const val CHANNEL = "writing_reminders"; const val NOTIFICATION_ID = 7001 }
}
