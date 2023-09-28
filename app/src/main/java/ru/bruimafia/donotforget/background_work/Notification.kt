package ru.bruimafia.donotforget.background_work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ru.bruimafia.donotforget.App
import ru.bruimafia.donotforget.BuildConfig
import ru.bruimafia.donotforget.R
import ru.bruimafia.donotforget.SingleActivity
import ru.bruimafia.donotforget.repository.local.Note
import ru.bruimafia.donotforget.util.Constants


class Notification {

    fun createNotificationChannel() {
        val notificationManager = App.instance.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(Constants.CHANNEL_ID, App.instance.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
            //val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + App.instance.packageName + "/raw/notification2")
            val att = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            channel.setSound(soundUri, att)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 1500, 500, 1500)
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.description = "Notes Notification"
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification(note: Note) {
        val view = RemoteViews(BuildConfig.APPLICATION_ID, R.layout.notification)
        view.setTextViewText(R.id.tv_noteTitle, note.title)

        val intent: Intent = Intent(App.instance, SingleActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .putExtra(Constants.NOTE_ID, note.id)

        Log.d(Constants.TAG, "From Notification::class -> id = ${note.id}")

        val requestCode = (System.currentTimeMillis() and 0xfffffffL).toInt()
        val pendingIntent = PendingIntent.getActivity(App.instance, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(App.instance, Constants.CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(view)
            .setSmallIcon(R.drawable.ic_statusbar)
            .setAutoCancel(false)
            .setOngoing(note.isFix) // закрепить ли уведомление
            .setContentTitle(note.title)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(App.instance)) {
            if (ActivityCompat.checkSelfPermission(App.instance, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                return
            notify(note.id.toInt(), notification.build())
        }
    }

    fun deleteNotification(id: Long) {
        NotificationManagerCompat.from(App.instance).cancel(id.toInt())
    }

}