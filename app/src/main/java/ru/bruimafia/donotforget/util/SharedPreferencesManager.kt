package ru.bruimafia.donotforget.util

import android.content.Context
import android.content.SharedPreferences


object SharedPreferencesManager {

    private const val NAME = "com.bruimafia.donotforget"
    private const val IS_FULL_VERSION = "FULL_VERSION" // версия приложения
    private const val IS_FIRST_LAUNCH = "FIRST_LAUNCH" // первый запуск
    private const val IS_LIGHT_THEME = "LIGHT_THEME" // тема приложения
    private const val IS_ORDER_BY_ID = "ORDER_BY_ID" // стандартная сортировка
    private const val LAST_SYNC = "LAST_SYNC" // последняя синхронизация

    private lateinit var sPref: SharedPreferences

    fun init(context: Context) {
        sPref = context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    var isFullVersion: Boolean
        get() = sPref.getBoolean(IS_FULL_VERSION, false)
        set(value) {
            sPref.edit().putBoolean(IS_FULL_VERSION, value).apply()
        }

    var isFirstLaunch: Boolean
        get() = sPref.getBoolean(IS_FIRST_LAUNCH, true)
        set(value) {
            sPref.edit().putBoolean(IS_FIRST_LAUNCH, value).apply()
        }

    var isLightTheme: Boolean
        get() = sPref.getBoolean(IS_LIGHT_THEME, true)
        set(value) {
            sPref.edit().putBoolean(IS_LIGHT_THEME, value).apply()
        }

    var isOrderById: Boolean
        get() = sPref.getBoolean(IS_ORDER_BY_ID, true)
        set(value) {
            sPref.edit().putBoolean(IS_ORDER_BY_ID, value).apply()
        }

    var lastSync: Long
        get() = sPref.getLong(LAST_SYNC, 0L)
        set(value) {
            sPref.edit().putLong(LAST_SYNC, value).apply()
        }

}