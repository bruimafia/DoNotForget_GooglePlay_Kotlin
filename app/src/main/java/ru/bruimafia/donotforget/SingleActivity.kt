package ru.bruimafia.donotforget

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import ru.bruimafia.donotforget.background_work.Notification
import ru.bruimafia.donotforget.background_work.NotificationWorker
import ru.bruimafia.donotforget.background_work.Receiver
import ru.bruimafia.donotforget.repository.remote.FirebaseManager
import ru.bruimafia.donotforget.util.Constants


class SingleActivity : AppCompatActivity() {

    private lateinit var appUpdateManager: AppUpdateManager
    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
        run {
            if (result.resultCode != RESULT_OK)
                Log.d(Constants.TAG, "Update flow failed! Result code: " + result.resultCode)
        }
    }

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
        checkUpdateAvailability()
        requestReview()
        FirebaseManager.init()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isNotificationPermissionGranted() = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PERMISSION_GRANTED

    private fun startNotificationsWorker() {
        val data = Data.Builder()
            .putString("action", Constants.ACTION_CHECK)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .addTag(Constants.WORKER_CHECK)
            .setInputData(data)
            .build()

        WorkManager.getInstance(App.instance).enqueue(workRequest)
    }

    private fun startRepeating() {
        val intent: Intent = Intent(App.instance, Receiver::class.java).setAction(Constants.ACTION_CHECK)
        val penIntent: PendingIntent =
            PendingIntent.getBroadcast(App.instance, Constants.RC_ALARM, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val manager = App.instance.getSystemService(ALARM_SERVICE) as AlarmManager
        manager.cancel(penIntent)
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, penIntent)
        Log.d(Constants.TAG, "From SingleActivity::class -> запущен startRepeating()")
        Log.d(Constants.TAG, "From SingleActivity::class -> ${penIntent.creatorUid}")
    }

    // проверка обновлений
    private fun checkUpdateAvailability() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            }
        }
    }

    // окно выставления оценки и отзыва
    private fun requestReview() {
        val reviewManager = ReviewManagerFactory.create(this)
        val requestReviewFlow = reviewManager.requestReviewFlow()

        requestReviewFlow.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                val reviewInfo = request.result
                val flow = reviewManager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener {
//                    SharedPreferencesManager.isPlayRating = true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        activityResultLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                }
            }
    }

    override fun onDestroy() {
        startRepeating()
        super.onDestroy()
    }

}