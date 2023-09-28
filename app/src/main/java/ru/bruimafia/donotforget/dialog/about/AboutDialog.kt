package ru.bruimafia.donotforget.dialog.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.fragment.app.DialogFragment
import ru.bruimafia.donotforget.R
import ru.bruimafia.donotforget.BuildConfig
import ru.bruimafia.donotforget.databinding.DialogAboutBinding
import ru.bruimafia.donotforget.util.Constants
import ru.bruimafia.donotforget.util.SharedPreferencesManager


class AboutDialog : DialogFragment(), OnClickMethod {

    private lateinit var binding: DialogAboutBinding
    var isFullVersion = ObservableField(false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_about, container, false)
        binding.view = this

        isFullVersion.set(SharedPreferencesManager.isFullVersion)

        return binding.root
    }

    override fun onVkLink() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("linkedin://profile/%s" + Constants.VK_ID)))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/id" + Constants.VK_ID)))
        }
    }

    override fun onGooglePlayLink() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)))
        }
    }

    override fun onPrivacyPolicyLink() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_link))))
    }

}