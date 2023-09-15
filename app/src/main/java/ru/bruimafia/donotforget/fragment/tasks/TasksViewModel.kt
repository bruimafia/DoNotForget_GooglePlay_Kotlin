package ru.bruimafia.donotforget.fragment.tasks

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import ru.bruimafia.donotforget.repository.Repository
import ru.bruimafia.donotforget.repository.local.Note

class TasksViewModel(private val repository: Repository) : ViewModel() {

    private lateinit var notes: Flowable<List<Note>>
    var notesForScreen = ObservableField<List<Note>>()
    var isOrderById = ObservableField(true)
    var isLoading = ObservableField(true)
    var isFullVersion = ObservableField(false)

    fun getNotes(): Flowable<List<Note>> {
        return if (isOrderById.get() == true)
            repository.getAllOrderById()
        else
            repository.getAllOrderByRelevance()
    }

    fun delete(id: Long) {
        repository.delete(id)
    }

}

class TasksViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java))
            return TasksViewModel(repository) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}