package ru.bruimafia.donotforget

import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.databinding.DataBindingUtil
import com.google.firebase.FirebaseApp
import com.onesignal.OneSignal
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import ru.bruimafia.donotforget.repository.Repository
import ru.bruimafia.donotforget.repository.local.LocalDatabase
import ru.bruimafia.donotforget.repository.remote.FirebaseManager
import ru.bruimafia.donotforget.util.AppDataBindingComponent
import ru.bruimafia.donotforget.util.SharedPreferencesManager


class App : Application() {

    private val database by lazy { LocalDatabase.getDatabase(this) }
    val repository by lazy {
        FirebaseManager.init()
        Repository(database.noteDao())
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.setDefaultComponent(AppDataBindingComponent())
        SharedPreferencesManager.init(this)
        instance = this
        checkCurrentTheme()

        // Init FirebaseApp for all processes
        FirebaseApp.initializeApp(this)

        // Init the AppMetrica SDK
        val config = AppMetricaConfig.newConfigBuilder(resources.getString(R.string.appMetrica_api_key)).build()
        AppMetrica.activate(this, config)

        // OneSignal Init
        OneSignal.initWithContext(this, resources.getString(R.string.onesignal_app_id))
    }

    fun checkCurrentTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (SharedPreferencesManager.isLightTheme)
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
            else
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        }
    }

    companion object {
        lateinit var instance: App
            private set
    }

}