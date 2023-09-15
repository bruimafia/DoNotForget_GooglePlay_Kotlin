package ru.bruimafia.donotforget

import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.databinding.DataBindingUtil
import ru.bruimafia.donotforget.repository.Repository
import ru.bruimafia.donotforget.repository.local.LocalDatabase
import ru.bruimafia.donotforget.util.AppDataBindingComponent
import ru.bruimafia.donotforget.util.SharedPreferencesManager

class App : Application() {

    private val database by lazy { LocalDatabase.getDatabase(this) }
    val repository by lazy { Repository(database.noteDao()) }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.setDefaultComponent(AppDataBindingComponent())
        SharedPreferencesManager.init(this)
        instance = this
        checkCurrentTheme()
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