package ru.bruimafia.donotforget.fragment.history

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import ru.bruimafia.donotforget.fragment.tasks.TasksViewModel
import ru.bruimafia.donotforget.repository.Repository
import ru.bruimafia.donotforget.repository.local.Note


class HistoryViewModel(private val repository: Repository) : ViewModel() {

    var notes: ObservableField<List<Note>> = ObservableField<List<Note>>()
    var isLoading = ObservableField(true)
    var isFullVersion = ObservableField(false)

    fun getNotesInHistory(): Flowable<List<Note>> {
        return repository.getHistory()
    }

    fun clear() {
        repository.clearHistory()
    }

    fun recover(id: Long) {
        repository.recover(id)
    }

}

class HistoryViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java))
            return HistoryViewModel(repository) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}