package ru.bruimafia.donotforget.util

import androidx.databinding.DataBindingComponent

class AppDataBindingComponent : DataBindingComponent {
    private val adapter = BindingAdapters()
    override fun getBindingAdapters() = adapter
}