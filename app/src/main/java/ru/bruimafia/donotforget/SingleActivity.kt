package ru.bruimafia.donotforget

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import ru.bruimafia.donotforget.background_work.Notification
import ru.bruimafia.donotforget.background_work.Receiver
import ru.bruimafia.donotforget.background_work.worker.NotificationsWorker
import ru.bruimafia.donotforget.util.Constants


class SingleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_DoNotForget)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single)

        Notification().createNotificationChannel()

        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted)
                Notification().createNotificationChannel()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!isNotificationPermissionGranted())
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        startNotificationsWorker()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isNotificationPermissionGranted() = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PERMISSION_GRANTED

    private fun startNotificationsWorker() {
        val data = Data.Builder()
            .putString("action", Constants.ACTION_START)
            .build()
        val workRequest = OneTimeWorkRequest.Builder(NotificationsWorker::class.java)
            .addTag(Constants.WORKER_CHECK_TAG)
            .setInputData(data)
            .build()
        WorkManager.getInstance(App.instance).enqueue(workRequest)
    }

    override fun onDestroy() {
        startRepeating()
        super.onDestroy()
    }

    private fun startRepeating() {
        val intent: Intent = Intent(App.instance, Receiver::class.java).setAction(Constants.ACTION_CHECK)

        val penIntent: PendingIntent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.getBroadcast(
            App.instance,
            Constants.RC_ALARM,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        ) else PendingIntent.getBroadcast(App.instance, Constants.RC_ALARM, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        if (penIntent != null) {
            val manager = App.instance.getSystemService(ALARM_SERVICE) as AlarmManager
            manager.cancel(penIntent)
            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Constants.REPEAT_INTERVAL.toLong(), penIntent)
        }
        Log.d(Constants.TAG, "SingleActivity: запущен startRepeating")
    }
}