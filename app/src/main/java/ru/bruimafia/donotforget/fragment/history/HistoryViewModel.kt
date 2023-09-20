package ru.bruimafia.donotforget.fragment.history

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
import ru.bruimafia.donotforget.background_work.NotificationWorker
import ru.bruimafia.donotforget.repository.Repository
import ru.bruimafia.donotforget.repository.local.Note
import ru.bruimafia.donotforget.util.Constants


class HistoryViewModel(private val repository: Repository) : ViewModel() {

    var notes: ObservableField<List<Note>> = ObservableField<List<Note>>()
    var isLoading = ObservableField(true)
    var isFullVersion = ObservableField(false)

    fun getNotesInHistory() = liveData {
        repository.getHistory().collect {
            emit(it)
        }
    }

    fun clearHistory() = viewModelScope.launch {
        repository.clearHistory()
    }

    fun recover(id: Long) = viewModelScope.launch {
        repository.recover(id)

        startCheckNotificationsWorker(Constants.ACTION_CREATE_OR_UPDATE, id)
    }

    private fun startCheckNotificationsWorker(action: String, id: Long) {
        val data = Data.Builder()
            .putString("action", action)
            .putLong(Constants.NOTE_ID, id)
            .build()
        val workRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .addTag(Constants.WORKER_CHECK_TAG)
            .setInputData(data)
            //.setInitialDelay(5, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(App.instance).enqueue(workRequest)
    }

}

class HistoryViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java))
            return HistoryViewModel(repository) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}