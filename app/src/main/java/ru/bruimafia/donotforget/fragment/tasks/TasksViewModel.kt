package ru.bruimafia.donotforget.fragment.tasks

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import kotlinx.coroutines.launch
import ru.bruimafia.donotforget.App
import ru.bruimafia.donotforget.background_work.Notification
import ru.bruimafia.donotforget.background_work.NotificationWorker
import ru.bruimafia.donotforget.repository.Repository
import ru.bruimafia.donotforget.repository.local.Note
import ru.bruimafia.donotforget.util.Constants


class TasksViewModel(private val repository: Repository) : ViewModel() {

    var notesForScreen = ObservableField<List<Note>>()
    var isOrderById = ObservableField(true)
    var isLoading = ObservableField(true)
    var isFullVersion = ObservableField(false)

    fun getNotes() = liveData {
        if (isOrderById.get() == true)
            repository.getAllActualOrderById().collect {
                emit(it)
            }
        else
            repository.getAllActualOrderByRelevance().collect {
                emit(it)
            }
    }

    fun delete(id: Long) = viewModelScope.launch {
        repository.delete(id)
        startCheckNotificationsWorker(id)
    }

    private fun startCheckNotificationsWorker(id: Long) {
        val data = Data.Builder()
            .putString("action", Constants.ACTION_DELETE)
            .putLong(Constants.NOTE_ID, id)
            .build()
        val workRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .addTag(Constants.WORKER_CHECK)
            .setInputData(data)
            .build()
        WorkManager.getInstance(App.instance).enqueue(workRequest)
    }

}

class TasksViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java))
            return TasksViewModel(repository) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}