package ru.freelance.internetradio.service;

import static ru.freelance.internetradio.service.Values.exoPlayer;
import static ru.freelance.internetradio.service.Values.radioUrlPath;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import ru.freelance.internetradio.ListenActivity;
import ru.freelance.internetradio.R;

public class RadioService extends Service {
    private final String NOTIFICATION_DEFAULT_CHANNEL_ID = "default_channel";

    private final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

    private final PlaybackStateCompat.Builder stateBuilder =
            new PlaybackStateCompat.Builder().setActions(
                    PlaybackStateCompat.ACTION_STOP | PlaybackStateCompat.ACTION_PLAY_PAUSE
            );

    private MediaSessionCompat mediaSession;

    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private boolean audioFocusRequested = false;

    WifiManager.WifiLock wifiLock = null;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_DEFAULT_CHANNEL_ID, "Radio Controls", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .setAcceptsDelayedFocusGain(false)
                    .setWillPauseWhenDucked(true)
                    .setAudioAttributes(audioAttributes)
                    .build();
        }

        WifiManager wifiManager = (WifiManager) FakeContext.getInstance().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager != null)
            wifiLock = wifiManager.createWifiLock(
                    WifiManager.WIFI_MODE_FULL, "wifiLocker"
            );

        wifiLock.setReferenceCounted(false);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mediaSession = new MediaSessionCompat(this, "RadioService");
        mediaSession.setCallback(mediaSessionCallback);

        Context appContext = getApplicationContext();

        Intent activityIntent = new Intent(appContext, ListenActivity.class);
        mediaSession.setSessionActivity(PendingIntent.getActivity(appContext, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null, appContext, MediaButtonReceiver.class);
        mediaSession.setMediaButtonReceiver(PendingIntent.getBroadcast(appContext, 0, mediaButtonIntent, PendingIntent.FLAG_IMMUTABLE));

        exoPlayer = new ExoPlayer.Builder(appContext).build();

        mediaSession.setMetadata(metadataBuilder.build());
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onMediaMetadataChanged(@NonNull MediaMetadata mediaMetadata) {
                Player.Listener.super.onMediaMetadataChanged(mediaMetadata);
                if (mediaMetadata.title != null) {
                    String[] trackInfo = ((String) mediaMetadata.title).split(" - ");
                    metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, trackInfo[0]);
                    metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, trackInfo[1]);
                    mediaSession.setMetadata(metadataBuilder.build());
                    NotificationManager notificationManager = (NotificationManager) FakeContext.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(1, getNotification(trackInfo[1], trackInfo[0]));
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSession.release();
        exoPlayer.release();
    }

    private final MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            if (!exoPlayer.getPlayWhenReady()) {
                startService(new Intent(getApplicationContext(), RadioService.class));

                exoPlayer.setMediaItem(MediaItem.fromUri(radioUrlPath));
                exoPlayer.prepare();

                if (!audioFocusRequested) {
                    audioFocusRequested = true;

                    int audioFocusResult;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        audioFocusResult = audioManager.requestAudioFocus(audioFocusRequest);
                    } else {
                        audioFocusResult = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                    }
                    if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return;
                }

                mediaSession.setActive(true);

                registerReceiver(becomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

                exoPlayer.setPlayWhenReady(true);

                if (!wifiLock.isHeld()) wifiLock.acquire();
            }

            mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());

            startForeground(1, getNotification("Loading...", "Please, wait"));
        }

        @Override
        public void onPause() {
            if (exoPlayer.getPlayWhenReady()) {
                exoPlayer.setPlayWhenReady(false);
                unregisterReceiver(becomingNoisyReceiver);
            }

            mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
        }

        @Override
        public void onStop() {
            if (exoPlayer.getPlayWhenReady()) {
                exoPlayer.setPlayWhenReady(false);
                unregisterReceiver(becomingNoisyReceiver);
            }

            if (audioFocusRequested) {
                audioFocusRequested = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    audioManager.abandonAudioFocusRequest(audioFocusRequest);
                } else {
                    audioManager.abandonAudioFocus(audioFocusChangeListener);
                }
            }

            mediaSession.setActive(false);

            mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());

            if (wifiLock != null && wifiLock.isHeld()) wifiLock.release();

            stopForeground(true);

            stopSelf();
        }
    };

    private final AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = focusChange -> {
        exoPlayer.setPlayWhenReady(focusChange == AudioManager.AUDIOFOCUS_GAIN);
    };

    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                exoPlayer.setPlayWhenReady(false);
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new RadioServiceBinder();
    }

    public class RadioServiceBinder extends Binder {
        public MediaSessionCompat.Token getMediaSessionToken() {
            return mediaSession.getSessionToken();
        }
    }

    private Notification getNotification(String nameSong, String artistSong) {
        NotificationCompat.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(FakeContext.getInstance().getApplicationContext(), FakeContext.getInstance().getPackageName());
            NotificationChannel audioChannel = new NotificationChannel(FakeContext.getInstance().getPackageName(), "audio", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notifyManager = (NotificationManager) FakeContext.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
            notifyManager.createNotificationChannel(audioChannel);
            builder.setChannelId(audioChannel.getId());
        } else builder = new NotificationCompat.Builder(FakeContext.getInstance().getApplicationContext());

        builder.addAction(
                new NotificationCompat.Action(
                        R.drawable.ic_notification_button_stop_radio,
                        "Остановить",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP)
                )
        );
        builder.addAction(
                new NotificationCompat.Action(
                        R.drawable.ic_notification_button_start_pause_radio,
                        "Продолжить/Пауза",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)
                )
        );

        builder.setStyle(
                new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(1)
                        .setMediaSession(mediaSession.getSessionToken())
        );
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.notification_icon));
        builder.setSmallIcon(R.drawable.ic_radio);
        builder.setContentTitle(nameSong);
        builder.setContentText(artistSong);
        builder.setShowWhen(false);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setOnlyAlertOnce(true);
        builder.setChannelId(NOTIFICATION_DEFAULT_CHANNEL_ID);

        return builder.build();
    }
}
