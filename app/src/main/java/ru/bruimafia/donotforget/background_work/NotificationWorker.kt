package ru.bruimafia.donotforget.background_work

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ru.bruimafia.donotforget.App
import ru.bruimafia.donotforget.repository.local.Note
import ru.bruimafia.donotforget.util.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class NotificationWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val action = inputData.getString("action")
        val noteID = inputData.getLong(Constants.NOTE_ID, -1)
        Log.d(Constants.TAG, "From NotificationWorker doWork(): action -> $action; noteID -> $noteID")

        when (action) {
            Constants.ACTION_CHECK -> {
                Log.d(Constants.TAG, "From NotificationWorker doWork(): ACTION_CHECK")
                App.instance.repository.getAllOrderById().collect { list -> checkNotes(list, action) }
            }

            Constants.ACTION_CREATE_OR_UPDATE -> {
                Log.d(Constants.TAG, "From NotificationWorker doWork(): ACTION_CREATE_OR_UPDATE")
                createNotification(App.instance.repository.get(noteID))
            }

            Constants.ACTION_DELETE -> {
                Log.d(Constants.TAG, "From NotificationWorker doWork(): ACTION_DELETE")
                deleteNotification(App.instance.repository.get(noteID))
            }
        }

        return Result.success()
    }

    private fun checkNotes(notes: List<Note>, action: String) {
        Log.d(Constants.TAG, "From NotificationWorker checkNotes(): START")

        for (note in notes) {
            Log.d(Constants.TAG, "From NotificationWorker checkNotes(): note.id -> ${note.id}")
            if (note.isFix || note.date >= System.currentTimeMillis()) {
                if (!existsNotification(note.id))
                    createNotification(note)
            }
        }
    }

    private fun existsNotification(id: Long): Boolean {
        val notificationManager = App.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifications: Array<StatusBarNotification> = notificationManager.activeNotifications
        for (notification in notifications) {
            if (notification.id == id.toInt())
                return true
        }
        return false
    }

    private fun createNotification(note: Note) {
        Log.d(Constants.TAG, "dateInMidnight: ${SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale.getDefault()).format(Date(dateInMidnight()))}")
        Log.d(Constants.TAG, "note.date: ${SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale.getDefault()).format(Date(note.date))}")

        deleteNotification(note)
        if (note.isFix && note.date == 0L) Notification().createNotification(note)
        if (note.date <= System.currentTimeMillis() && note.date != 0L) Notification().createNotification(note)
        if (note.date >= dateInMidnight()) createPendingNotification(note)
    }

    private fun deleteNotification(note: Note) {
        Notification().deleteNotification(note.id)
        deletePendingNotification(note)
    }

    private fun createPendingNotification(note: Note) {
        val intent: Intent = Intent(App.instance, Receiver::class.java).setAction(Constants.ACTION_CREATE_OR_UPDATE).putExtra(Constants.NOTE_ID, note.id)

        val penIntent: PendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.getBroadcast(App.instance, note.id.toInt(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            else
                PendingIntent.getBroadcast(App.instance, note.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val manager = App.instance.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.cancel(penIntent)
        manager[AlarmManager.RTC_WAKEUP, note.date] = penIntent
    }

    private fun deletePendingNotification(note: Note) {
        val intent: Intent = Intent(App.instance, Receiver::class.java).setAction(Constants.ACTION_CREATE_OR_UPDATE).putExtra(Constants.NOTE_ID, note.id)

        val penIntent: PendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.getBroadcast(App.instance, note.id.toInt(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            else
                PendingIntent.getBroadcast(App.instance, note.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val manager = App.instance.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.cancel(penIntent)
    }

    private fun dateInMidnight(): Long {
        val milliseconds = System.currentTimeMillis()
        val rightNow: Calendar = Calendar.getInstance()
        val offset: Int = rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET)
        val sinceMidnight = (milliseconds + offset) % (24 * 60 * 60 * 1000)
        return milliseconds - sinceMidnight
    }
}
