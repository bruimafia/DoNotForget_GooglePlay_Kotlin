package ru.bruimafia.donotforget.fragment.edit

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.launch
import ru.bruimafia.donotforget.App
import ru.bruimafia.donotforget.background_work.NotificationWorker
import ru.bruimafia.donotforget.repository.Repository
import ru.bruimafia.donotforget.repository.local.Note
import ru.bruimafia.donotforget.util.Constants
import java.util.Calendar

class EditViewModel(private val repository: Repository) : ViewModel() {

    var note: ObservableField<Note> = ObservableField<Note>()
    var isFullVersion = ObservableField(false)

    fun setNote(id: Long)= viewModelScope.launch {
        if (note.get() == null && id != -1L)
            note.set(repository.get(id))
        if (note.get() == null && id == -1L)
            note.set(Note())
    }

    fun create(note: Note) = viewModelScope.launch {
        val id = repository.create(note)
        startNotificationsWorker(Constants.ACTION_CREATE_OR_UPDATE, id)
    }

    fun update(note: Note) = viewModelScope.launch {
        repository.update(note)
        startNotificationsWorker(Constants.ACTION_CREATE_OR_UPDATE, note.id)
    }

    fun delete(id: Long) = viewModelScope.launch {
        repository.delete(id)
        startNotificationsWorker(Constants.ACTION_DELETE, id)
    }

    fun recover(id: Long) = viewModelScope.launch {
        repository.recover(id)
        startNotificationsWorker(Constants.ACTION_CREATE_OR_UPDATE, id)
    }

    fun setDate(milliseconds: Long) {
        note.get()?.date = dateInMidnight(milliseconds)
    }

    fun setTime(milliseconds: Long) {
        note.get()?.date = checkCurrentDate() + timeSinceMidnight(milliseconds)
    }

    private fun dateInMidnight(milliseconds: Long): Long {
        val rightNow: Calendar = Calendar.getInstance()
        val offset: Long = (rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET)).toLong()
        val sinceMidnight = (milliseconds + offset) % (24 * 60 * 60 * 1000)
        return milliseconds - sinceMidnight
    }

    private fun timeSinceMidnight(milliseconds: Long): Long {
        val rightNow: Calendar = Calendar.getInstance()
        val offset: Long = (rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET)).toLong()
        return (milliseconds + offset) % (24 * 60 * 60 * 1000)
    }

    private fun checkCurrentDate(): Long {
        return if (note.get()?.date == 0L) dateInMidnight(System.currentTimeMillis()) else dateInMidnight(note.get()!!.date)
    }

    private fun startNotificationsWorker(action: String, id: Long) {
        val data = Data.Builder()
            .putString("action", action)
            .putLong(Constants.NOTE_ID, id)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .addTag(Constants.WORKER_CHECK)
            .setInputData(data)
            .build()

        WorkManager.getInstance(App.instance).enqueue(workRequest)
    }

}

class EditViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditViewModel::class.java))
            return EditViewModel(repository) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}