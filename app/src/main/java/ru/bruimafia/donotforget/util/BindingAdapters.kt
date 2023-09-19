package ru.bruimafia.donotforget.util

import android.graphics.Color
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.card.MaterialCardView
import ru.bruimafia.donotforget.App
import ru.bruimafia.donotforget.BuildConfig
import ru.bruimafia.donotforget.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BindingAdapters {

    @BindingAdapter("app:setDate")
    fun setDate(view: TextView, milliseconds: Long) {
        if (milliseconds != 0L) view.text = getDateOf(milliseconds) else view.setText(R.string.date)
    }

    private fun getDateOf(milliseconds: Long): String {
        val sf = SimpleDateFormat("E, dd MMMM", Locale.getDefault())
        return sf.format(Date(milliseconds))
    }

    @BindingAdapter("app:setExpandedDate")
    fun setExpandedDate(view: TextView, milliseconds: Long) {
        if (milliseconds != 0L) view.text = getExpandedDateOf(milliseconds)
        else view.setText(R.string.date)
    }

    private fun getExpandedDateOf(milliseconds: Long): String? {
        val sf = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
        return sf.format(Date(milliseconds))
    }

    @BindingAdapter("app:setTime")
    fun setTime(view: TextView, milliseconds: Long) {
        if (milliseconds != 0L) view.text = getTimeOf(milliseconds) else view.setText(R.string.time)
    }

    private fun getTimeOf(milliseconds: Long): String? {
        val sf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sf.format(Date(milliseconds))
    }

    @BindingAdapter("app:backgroundColor")
    fun backgroundColor(view: MaterialCardView, color: Int) {
        if (color != 0) view.setCardBackgroundColor(color)
        else view.setCardBackgroundColor(view.context.resources.getColor(R.color.white))
    }

    @BindingAdapter("app:textColor")
    fun textColor(view: TextView, color: Int) {
        if (isDarkBackground(color) && color != 0) view.setTextColor(view.context.resources.getColor(R.color.colorLightText))
        else view.setTextColor(view.context.resources.getColor(R.color.white))
    }

    @BindingAdapter("app:imageCalendarColor")
    fun imageCalendarColor(view: ImageView, color: Int) {
        if (isDarkBackground(color) && color != 0) view.setImageResource(R.drawable.ic_calendar_light)
        else view.setImageResource(R.drawable.ic_calendar)
    }

    @BindingAdapter("app:imageClockColor")
    fun imageClockColor(view: ImageView, color: Int) {
        if (isDarkBackground(color) && color != 0) view.setImageResource(R.drawable.ic_clock_light)
        else view.setImageResource(R.drawable.ic_clock)
    }

    // определение оттенка цвета фона
    private fun isDarkBackground(color: Int): Boolean {
        // if (darkness > 0.5) -> темный цвет фона
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness > 0.5
    }

    @BindingAdapter("app:setVersion")
    fun setVersion(view: TextView, isFullVersion: Boolean) {
        val version = if (isFullVersion) "pro" else ""
        view.text = java.lang.String.format(
            App.instance.resources.getString(R.string.app_version),
            BuildConfig.VERSION_NAME,
            version,
            BuildConfig.VERSION_CODE
        )
    }

    @BindingAdapter("app:setLastSync")
    fun setLastSync(view: TextView, time: Long) {
        view.text = transformationTimeLastSync(time)
    }

    private fun transformationTimeLastSync(time: Long): String? {
        return if (time == 0L) App.instance.resources
            .getString(R.string.tv_no_sync) else SimpleDateFormat("EEEE, dd MMMM y HH:mm", Locale.getDefault()).format(Date(time))
    }

}