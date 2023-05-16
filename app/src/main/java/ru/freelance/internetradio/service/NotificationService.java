package ru.freelance.internetradio.service;

import static ru.freelance.internetradio.service.Values.ACTION;
import static ru.freelance.internetradio.service.Values.isPlaying;
import static ru.freelance.internetradio.service.Values.radioUrlPath;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.Objects;

import ru.freelance.internetradio.ListenActivity;
import ru.freelance.internetradio.R;

public class NotificationService extends Service {

    public static boolean notificationShowed = false;

    private void showNotify() {
        Intent notificationIntent = new Intent(this, ListenActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder notifyBuilder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifyBuilder = new Notification.Builder(this, FakeContext.getInstance().getPackageName());
            NotificationChannel audioChannel = new NotificationChannel(FakeContext.getInstance().getPackageName(), "audio", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notifyManager.createNotificationChannel(audioChannel);
            notifyBuilder.setChannelId(audioChannel.getId());
        } else notifyBuilder = new Notification.Builder(this);

        notifyBuilder
                .setSmallIcon(R.drawable.ic_radio)
                .setContentIntent(pendingIntent);

        startForeground(Values.FOREGROUND_SERVICE, notifyBuilder.build());
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (Objects.requireNonNull(intent.getAction())) {
            case ACTION.PLAY_PAUSE_ACTION:
                if (!isPlaying) {
                    if (!notificationShowed) {
                        showNotify();
                        notificationShowed = true;
                    }
                    RadioPlayer.start(radioUrlPath, FakeContext.getInstance());
                } else RadioPlayer.pause();
                isPlaying = !isPlaying;
                break;
            case ACTION.STOP_ACTION:
                RadioPlayer.stop();
                stopForeground(STOP_FOREGROUND_REMOVE);
                stopSelf();
                notificationShowed = false;
                break;
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
