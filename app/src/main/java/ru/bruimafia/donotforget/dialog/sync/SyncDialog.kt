package ru.bruimafia.donotforget.dialog.sync

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.core.graphics.component1
import androidx.core.graphics.component2
import androidx.core.graphics.component3
import androidx.core.graphics.component4
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import ru.bruimafia.donotforget.App
import ru.bruimafia.donotforget.R
import ru.bruimafia.donotforget.databinding.DialogSyncBinding
import ru.bruimafia.donotforget.repository.Repository
import ru.bruimafia.donotforget.util.SharedPreferencesManager

class SyncDialog: DialogFragment(), OnClickMethod {

    private lateinit var binding: DialogSyncBinding
    var isLogin = ObservableField(false)
    var lastSync = ObservableField(0L)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_sync, container, false)
        binding.view = this

        return binding.root
    }

    override fun onSignIn() {
        binding.btnId.revertAnimation{
            binding.btnId.text = "ok"
        }
    }

    override fun onSync() {
        binding.btnId.startAnimation()
    }

}