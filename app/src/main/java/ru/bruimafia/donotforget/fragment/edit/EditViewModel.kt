package ru.bruimafia.donotforget.fragment.edit

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.bruimafia.donotforget.R
import ru.bruimafia.donotforget.repository.Repository
import ru.bruimafia.donotforget.repository.local.Note
import java.util.Calendar

class EditViewModel(private val repository: Repository) : ViewModel() {

    var note: ObservableField<Note> = ObservableField<Note>()
    var isFullVersion = ObservableField(false)
    var colorBackground = ObservableField(com.google.android.material.R.attr.backgroundColor)

    fun setNote(id: Long) {
        if (note.get() == null && id != -1L)
            repository[id]
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { n: Note -> note.set(n) }
                .subscribe()
        if (note.get() == null && id == -1L)
            note.set(Note())
    }

    fun create(note: Note) {
        repository.create(note)
    }

    fun update(note: Note) {
        repository.update(note)
    }

    fun delete(id: Long) {
        repository.delete(id)
    }

    fun recover(id: Long) {
        repository.recover(id)
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

}

class EditViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditViewModel::class.java))
            return EditViewModel(repository) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}