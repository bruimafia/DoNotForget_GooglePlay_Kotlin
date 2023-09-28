package ru.bruimafia.donotforget.background_work.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ru.bruimafia.donotforget.App;
import ru.bruimafia.donotforget.background_work.Notification;
import ru.bruimafia.donotforget.background_work.worker.NotificationWorker;
import ru.bruimafia.donotforget.repository.local_store.Note;
import ru.bruimafia.donotforget.repository.local_store.NoteDao;
import ru.bruimafia.donotforget.util.Constants;

public class NotificationService extends Service {

    private final CompositeDisposable disposable = new CompositeDisposable();
    private NoteDao noteDao = App.getInstance().getDatabase().noteDao();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Constants.TAG, "Service: запущен");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(Constants.TAG, "Service: остановлен");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction() != null) {
            Log.d(Constants.TAG, Constants.NOTE_ID + " " + intent.getLongExtra(Constants.NOTE_ID, -1));
            switch (intent.getAction()) {
                case Constants.ACTION_START:
                    Log.d(Constants.TAG, "Service: ACTION_START");
                    checkNotifications();
                    break;
                case Constants.ACTION_CREATE_OR_UPDATE:
                    Log.d(Constants.TAG, "Service: ACTION_CREATE_OR_UPDATE");
                    noteDao.get(intent.getLongExtra(Constants.NOTE_ID, -1))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSuccess(this::create)
                            .subscribe();
                    break;
                case Constants.ACTION_DELETE:
                    Log.d(Constants.TAG, "Service: ACTION_DELETE");
                    noteDao.get(intent.getLongExtra(Constants.NOTE_ID, -1))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSuccess(this::delete)
                            .subscribe();
                    break;
            }
        }

        return START_REDELIVER_INTENT;
    }


    private void checkNotifications() {
        disposable.add(noteDao.getAllOrderById()
                .subscribeOn(Schedulers.io())
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::checkNotes));
    }

    private void checkNotes(List<Note> notes) {
        for (Note note : notes) {
            if (note.isFix() || note.getDate() >= System.currentTimeMillis())
                create(note);
        }
    }

    private void create(Note note) {
        delete(note);

        if (note.isFix() && note.getDate() == 0) {
            new Notification().createNotification(note);
        }

        if (note.getDate() >= dateInMidnight()) {
            Data data = new Data.Builder()
                    .putLong(Constants.NOTE_ID, note.getId())
                    .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                    .addTag(Constants.WORKER_NOTIFICATION_TAG + note.getId())
                    .setInitialDelay(note.getDate() - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .build();
            WorkManager.getInstance(App.getInstance()).enqueue(workRequest);
        }
    }

    private void delete(Note note) {
        WorkManager.getInstance(App.getInstance()).cancelAllWorkByTag(Constants.WORKER_NOTIFICATION_TAG + note.getId());

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(App.getInstance());
        notificationManager.cancel((int) note.getId());
    }

    private long dateInMidnight() {
        long milliseconds = System.currentTimeMillis();
        Calendar rightNow = Calendar.getInstance();
        long offset = rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET);
        long sinceMidnight = (milliseconds + offset) % (24 * 60 * 60 * 1000);
        return milliseconds - sinceMidnight;
    }

    private void generateTestNotes() {
        NoteDao noteDao = App.getInstance().getDatabase().noteDao();
        Random rnd = new Random();
        for (int i = 1; i <= 350; i++) {
            Note note = new Note(generateTitle(rnd.nextInt(100 - 10 + 1)), 0, 0, getRandomBoolean(), false, System.currentTimeMillis(), 0);
            noteDao.insert(note)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }
    }

    public String generateTitle(int len) {
        String chars = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя ";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    public boolean getRandomBoolean() {
        return new Random().nextBoolean();
    }

}
