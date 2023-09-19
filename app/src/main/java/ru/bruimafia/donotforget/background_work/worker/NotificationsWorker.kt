package ru.bruimafia.donotforget.background_work.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.bruimafia.donotforget.App
import ru.bruimafia.donotforget.R
import ru.bruimafia.donotforget.background_work.Notification
import ru.bruimafia.donotforget.background_work.Receiver
import ru.bruimafia.donotforget.repository.local.Note
import ru.bruimafia.donotforget.util.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class NotificationsWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val disposable = CompositeDisposable()

    override fun doWork(): Result {
        val action = inputData.getString("action")
        val noteID = inputData.getLong(Constants.NOTE_ID, -1)
        Log.d(Constants.TAG, String.format("action -> %s; noteID -> %d", action, noteID))

        if (action != null) {
            when (action) {
                Constants.ACTION_START -> {
                    Log.d(Constants.TAG, "NotificationsWorker: ACTION_START")
                    checkNotifications()
                }

                Constants.ACTION_CREATE_OR_UPDATE -> {
                    Log.d(Constants.TAG, "NotificationsWorker: ACTION_CREATE_OR_UPDATE")
                    App.instance.repository[noteID]
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSuccess(this::create)
                        .subscribe()
                }

                Constants.ACTION_DELETE -> {
                    Log.d(Constants.TAG, "NotificationsWorker: ACTION_DELETE")
                    App.instance.repository[noteID]
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSuccess(this::delete)
                        .subscribe()
                }
            }
        }

        return Result.success()
    }

    private fun transformationTimeCheckNotifications(time: Long): String? {
        return if (time == 0L) App.instance.resources.getString(R.string.tv_no_sync)
        else SimpleDateFormat("EEEE, dd MMMM y HH:mm", Locale.getDefault()).format(Date(time))
    }

    private fun checkNotifications() {
        disposable.add(App.instance.repository.getAllOrderById()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { notes: List<Note> -> checkNotes(notes) })
    }

    private fun checkNotes(notes: List<Note>) {
        for (note in notes) {
            if (note.isFix || note.date >= System.currentTimeMillis())
                create(note)
        }
    }

    private fun create(note: Note) {
        delete(note)
        if (note.isFix && note.date == 0L) Notification().createNotification(note)
        if (note.date >= dateInMidnight()) createNotificationWithAlarmManager(note)
    }

    private fun createNotificationWithAlarmManager(note: Note) {
        val intent: Intent = Intent(App.instance, Receiver::class.java).setAction(Constants.ACTION_CREATE_OR_UPDATE).putExtra("test_id", note.id)
        val penIntent: PendingIntent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.getBroadcast(
            App.instance,
            note.id.toInt(),
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        ) else PendingIntent.getBroadcast(
            App.instance, note.id.toInt(), intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (penIntent != null) {
            val manager = App.instance.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            manager.cancel(penIntent)
            manager[AlarmManager.RTC_WAKEUP, note.date] = penIntent
        }
    }

    private fun deleteNotificationWithAlarmManager(note: Note) {
        val intent: Intent = Intent(App.instance, Receiver::class.java).setAction(Constants.ACTION_CREATE_OR_UPDATE).putExtra("test_id", note.id)
        val penIntent: PendingIntent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.getBroadcast(
            App.instance,
            note.id.toInt(),
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        ) else PendingIntent.getBroadcast(App.instance, note.id.toInt(), intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        if (penIntent != null) {
            val manager = App.instance.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            manager.cancel(penIntent)
        }
    }

    private fun delete(note: Note) {
        deleteNotificationWithAlarmManager(note)
        Notification().deleteNotification(note.id)
    }

    private fun dateInMidnight(): Long {
        val milliseconds = System.currentTimeMillis()
        val rightNow: Calendar = Calendar.getInstance()
        val offset: Int = rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET)
        val sinceMidnight = (milliseconds + offset) % (24 * 60 * 60 * 1000)
        return milliseconds - sinceMidnight
    }

}