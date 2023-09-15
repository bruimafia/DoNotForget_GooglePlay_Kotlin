package ru.bruimafia.donotforget.repository

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.bruimafia.donotforget.App
import ru.bruimafia.donotforget.background_work.Notification
import ru.bruimafia.donotforget.background_work.worker.NotificationsWorker
import ru.bruimafia.donotforget.repository.local.Note
import ru.bruimafia.donotforget.repository.local.NoteDao
import ru.bruimafia.donotforget.util.Constants


class Repository(private val dao: NoteDao) : RepositoryBase {

    override fun get(id: Long): Single<Note> = dao[id]

    override fun getAll(): Flowable<List<Note>> = dao.getAll()

    override fun getAllOrderById(): Flowable<List<Note>> = dao.getAllOrderById()

    override fun getAllOrderByRelevance(): Flowable<List<Note>> = dao.getAllOrderByRelevance()

    override fun getHistory(): Flowable<List<Note>> = dao.getHistory()

    override fun create(note: Note) {
        dao.insert(note)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { id ->
                note.id = id
                startCheckNotificationsWorker(Constants.ACTION_CREATE_OR_UPDATE, id)
//                if (note.isFix)
//                    Notification().createNotification(note)
            }
            .subscribe()
    }

    override fun update(note: Note) {
        dao.update(note)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                startCheckNotificationsWorker(Constants.ACTION_CREATE_OR_UPDATE, note.id)
//                Notification().deleteNotification(note.id)
//                if (note.isFix)
//                    Notification().createNotification(note)
            }
            .subscribe()
    }

    override fun delete(id: Long) {
        dao.delete(id, System.currentTimeMillis())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                startCheckNotificationsWorker(Constants.ACTION_DELETE, id)
//                Notification().deleteNotification(id)
            }
            .subscribe()
    }

    override fun recover(id: Long) {
        dao.recover(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                startCheckNotificationsWorker(Constants.ACTION_CREATE_OR_UPDATE, id)
            }
            .subscribe()
    }

    override fun clear() {
        dao.clear(System.currentTimeMillis())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    override fun clearHistory() {
        dao.clearHistory()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    override fun syncing() {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        TODO("Not yet implemented")
    }

    private fun startCheckNotificationsWorker(action: String, id: Long) {
        val data = Data.Builder()
            .putString("action", action)
            .putLong(Constants.NOTE_ID, id)
            .build()
        val workRequest = OneTimeWorkRequest.Builder(NotificationsWorker::class.java)
            .addTag(Constants.WORKER_CHECK_TAG)
            .setInputData(data)
            //.setInitialDelay(5, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(App.instance).enqueue(workRequest)
    }

}