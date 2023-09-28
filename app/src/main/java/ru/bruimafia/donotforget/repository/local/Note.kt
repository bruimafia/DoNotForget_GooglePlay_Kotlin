package ru.bruimafia.donotforget.repository.local

import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.bruimafia.donotforget.BR


@Entity(tableName = "notes")
@Keep
class Note(
    id: Long = 0,
    title: String = "",
    date: Long = 0,
    color: Int = 0,
    isFix: Boolean = true,
    inHistory: Boolean = false,
    dateCreate: Long = System.currentTimeMillis(),
    dateDelete: Long = 0,
    dateUpdate: Long = System.currentTimeMillis()
) : BaseObservable() {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @get:Bindable
    var id: Long = id
        set(value) {
            field = value
            notifyPropertyChanged(BR.id)
        }

    @ColumnInfo(name = "title")
    @get:Bindable
    var title: String = title
        set(value) {
            field = value
            notifyPropertyChanged(BR.title)
        }

    @ColumnInfo(name = "date")
    @get:Bindable
    var date: Long = date
        set(value) {
            field = value
            notifyPropertyChanged(BR.date)
        }

    @ColumnInfo(name = "color")
    @get:Bindable
    var color: Int = color
        set(value) {
            field = value
            notifyPropertyChanged(BR.color)
        }

    @ColumnInfo(name = "is_fix")
    @get:Bindable
    var isFix: Boolean = isFix
        set(value) {
            field = value
            notifyPropertyChanged(BR.fix)
        }

    @ColumnInfo(name = "in_history")
    @get:Bindable
    var inHistory: Boolean = inHistory
        set(value) {
            field = value
            notifyPropertyChanged(BR.inHistory)
        }

    @ColumnInfo(name = "date_create")
    @get:Bindable
    var dateCreate: Long = dateCreate
        set(value) {
            field = value
            notifyPropertyChanged(BR.dateCreate)
        }

    @ColumnInfo(name = "date_delete")
    @get:Bindable
    var dateDelete: Long = dateDelete
        set(value) {
            field = value
            notifyPropertyChanged(BR.dateDelete)
        }

    @ColumnInfo(name = "date_update")
    @get:Bindable
    var dateUpdate: Long = dateUpdate
        set(value) {
            field = value
            notifyPropertyChanged(BR.dateUpdate)
        }

    override fun equals(other: Any?): Boolean {
        val note: Note = other as Note
        if (id != note.id) return false
        return hashCode() == note.hashCode()
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + color
        result = 31 * result + isFix.hashCode()
        result = 31 * result + inHistory.hashCode()
        result = 31 * result + dateCreate.hashCode()
        result = 31 * result + dateDelete.hashCode()
        result = 31 * result + dateUpdate.hashCode()
        return result
    }

    override fun toString(): String {
        return "Note(hashcode=${hashCode()}, id=$id, title='$title', date=$date, color=$color, isFix=$isFix, inHistory=$inHistory, dateCreate=$dateCreate, dateDelete=$dateDelete, dateUpdate=$dateUpdate)"
    }

}