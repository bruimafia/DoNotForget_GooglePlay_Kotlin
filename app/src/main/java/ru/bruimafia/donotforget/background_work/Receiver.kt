package ru.bruimafia.donotforget.background_work

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.bruimafia.donotforget.App
import ru.bruimafia.donotforget.util.Constants
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class Receiver : BroadcastReceiver() {

    private var context: Context? = null

    override fun onReceive(cnx: Context?, intent: Intent?) = goAsync {
        context = cnx
        Log.d(Constants.TAG, "BroadcastReceiver: запущен")

        if (intent != null) {
            if (intent.action != null) {

                when (intent.action) {
                    Intent.ACTION_BOOT_COMPLETED -> {
                        Log.d(Constants.TAG, "BroadcastReceiver: ACTION_BOOT_COMPLETED телефон включился")
                        startNotificationsWorker()
                        startRepeating()
                    }

                    Constants.ACTION_CHECK -> {
                        Log.d(Constants.TAG, "BroadcastReceiver: ACTION_CHECK")
                        startNotificationsWorker()
                    }

                    Constants.ACTION_CREATE_OR_UPDATE -> {
                        Log.d(Constants.TAG, "BroadcastReceiver: ACTION_CREATE_OR_UPDATE")
                        val id: Long = intent.getLongExtra("test_id", -1)

                        Notification().createNotification(App.instance.repository.get(id))
                    }
                }
            }
        }
    }

    private fun BroadcastReceiver.goAsync(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ) {
        val pendingResult = goAsync()
        @OptIn(DelicateCoroutinesApi::class) // Must run globally; there's no teardown callback.
        GlobalScope.launch(context) {
            try {
                block()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun startNotificationsWorker() {
        val data = Data.Builder()
            .putString("action", Constants.ACTION_START)
            .build()

//        val workRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
//            .addTag(Constants.WORKER_CHECK_TAG)
//            .setInputData(data)
//            .build()

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .addTag(Constants.WORKER_CHECK_TAG)
            .setInputData(data)
            .build()

        WorkManager.getInstance(App.instance).enqueue(workRequest)
    }

    private fun startRepeating() {
        val intent: Intent = Intent(App.instance, Receiver::class.java).setAction(Constants.ACTION_CHECK)

        val penIntent: PendingIntent? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.getBroadcast(App.instance, Constants.RC_ALARM, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            else
                PendingIntent.getBroadcast(App.instance, Constants.RC_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (penIntent != null) {
            val manager = App.instance.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
            manager.cancel(penIntent)
            manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Constants.REPEAT_INTERVAL.toLong(), penIntent)
        }
        Log.d(Constants.TAG, "SingleActivity: запущен startRepeating")
    }

}