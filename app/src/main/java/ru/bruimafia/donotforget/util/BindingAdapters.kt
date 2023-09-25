package ru.bruimafia.donotforget.util

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout
import ru.bruimafia.donotforget.App
import ru.bruimafia.donotforget.BuildConfig
import ru.bruimafia.donotforget.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class BindingAdapters {

    @BindingAdapter("app:setDate")
    fun setDate(view: TextView, milliseconds: Long) {
        view.text = if (milliseconds > 0L) getDateOf(milliseconds) else view.context.resources.getString(R.string.date)
    }

    private fun getDateOf(milliseconds: Long): String = SimpleDateFormat("E, dd MMMM yyyy", Locale.getDefault()).format(Date(milliseconds))

    @BindingAdapter("app:setExpandedDate")
    fun setExpandedDate(view: TextView, milliseconds: Long) {
        view.text = if (milliseconds > 0L) getExpandedDateOf(milliseconds) else view.context.resources.getString(R.string.date)
    }

    private fun getExpandedDateOf(milliseconds: Long): String = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date(milliseconds))

    @BindingAdapter("app:setTime")
    fun setTime(view: TextView, milliseconds: Long) {
        view.text = if (milliseconds > 0L) getTimeOf(milliseconds) else view.context.resources.getString(R.string.time)
    }

    private fun getTimeOf(milliseconds: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(milliseconds))

    @BindingAdapter("app:noteBackgroundColor")
    fun noteBackgroundColor(view: MaterialCardView, color: Int) {
        val typedValue = TypedValue()
        view.context.theme.resolveAttribute(R.attr.colorTransparencyOut, typedValue, true)

        view.setCardBackgroundColor(
            if (color != 0) color
            else typedValue.data
        )
    }

    @BindingAdapter("app:imageIconTint")
    fun imageIconTint(view: ImageView, color: Int) {
        view.imageTintList =
            if (isDarkBackground(color) && color != 0) ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.lightText_mediumEmphasis))
            else ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.darkText_mediumEmphasis))
    }

    @BindingAdapter("app:textColorDateTime")
    fun textColorDateTime(view: TextView, color: Int) {
        view.setTextColor(
            if (isDarkBackground(color) && color != 0) ContextCompat.getColor(view.context, R.color.lightText_mediumEmphasis)
            else ContextCompat.getColor(view.context, R.color.darkText_mediumEmphasis)
        )
    }

    @BindingAdapter("app:textColorOnNote")
    fun textColorOnNote(view: TextView, color: Int) {
        val typedValue = TypedValue()
        view.context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnBackground, typedValue, true)

        view.setTextColor(
            if (color == 0) typedValue.data
            else if (isDarkBackground(color)) ContextCompat.getColor(view.context, R.color.lightText_highEmphasis)
            else ContextCompat.getColor(view.context, R.color.darkText_highEmphasis)
        )
    }

    // определение оттенка цвета фона
    private fun isDarkBackground(color: Int): Boolean {
        // if (darkness > 0.5) -> темный цвет фона
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness > 0.2
    }

    @BindingAdapter("app:textInputLayoutBoxStrokeColor")
    fun textInputLayoutBoxStrokeColor(view: TextInputLayout, color: Int) {
        val typedValue = TypedValue()
        view.context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnBackground, typedValue, true)
        view.boxStrokeColor = if (color != 0) color else typedValue.data
    }

    @BindingAdapter("app:textInputLayoutHintTextColor")
    fun textInputLayoutHintTextColor(view: TextInputLayout, color: Int) {
        val typedValue = TypedValue()
        view.context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnBackground, typedValue, true)
        view.hintTextColor = if (color != 0) ColorStateList.valueOf(color) else ColorStateList.valueOf(typedValue.data)
    }

    @BindingAdapter("app:btnChooseColorBackgroundTint")
    fun btnChooseColorBackgroundTint(view: MaterialButton, color: Int) {
        val typedValue = TypedValue()
        view.context.theme.resolveAttribute(com.google.android.material.R.attr.backgroundColor, typedValue, true)
        view.backgroundTintList = if (color != 0) ColorStateList.valueOf(color) else ColorStateList.valueOf(typedValue.data)
    }

    @BindingAdapter("app:setAppVersion")
    fun setAppVersion(view: TextView, isFullVersion: Boolean) {
        val version = if (isFullVersion) "pro" else ""
        view.text = String.format(
            App.instance.resources.getString(R.string.app_version),
            BuildConfig.VERSION_NAME,
            version,
            BuildConfig.VERSION_CODE
        )
    }

    @BindingAdapter("app:setLastSync")
    fun setLastSync(view: TextView, time: Long) {
        view.text = formatLastSync(time)
    }

    private fun formatLastSync(time: Long): String = if (time == 0L) App.instance.resources
        .getString(R.string.tv_no_sync) else SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale.getDefault()).format(Date(time))

}