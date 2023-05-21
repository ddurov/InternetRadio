package ru.radio.poems;

import static ru.radio.poems.service.Values.isPlaying;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import ru.radio.poems.service.RadioService;

public class ListenActivity extends AppCompatActivity {

    RadioService.RadioServiceBinder radioServiceBinder;
    MediaControllerCompat mediaControllerCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen);

        Button startStopListen = findViewById(R.id.startStopRadio);
        Button toSchedule = findViewById(R.id.toSchedule);
        findViewById(R.id.toListenRadio).setActivated(true);
        Button toContacts = findViewById(R.id.toContacts);

        bindService(new Intent(this, RadioService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                radioServiceBinder = (RadioService.RadioServiceBinder) service;
                mediaControllerCompat = new MediaControllerCompat(
                        ListenActivity.this,
                        radioServiceBinder.getMediaSessionToken()
                );
                mediaControllerCompat.registerCallback(new MediaControllerCompat.Callback() {
                    @Override
                    public void onPlaybackStateChanged(PlaybackStateCompat state) {
                        super.onPlaybackStateChanged(state);
                        if (state == null) return;
                        isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;
                        startStopListen.setActivated(isPlaying);
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                radioServiceBinder = null;
                mediaControllerCompat = null;
            }
        }, BIND_AUTO_CREATE);

        startStopListen.setOnClickListener(v -> {
            if (mediaControllerCompat != null) {
                if (isPlaying) {
                    mediaControllerCompat.getTransportControls().pause();
                } else {
                    mediaControllerCompat.getTransportControls().play();
                }
                isPlaying = !isPlaying;
            }
        });

        toSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(ListenActivity.this, ScheduleActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        toContacts.setOnClickListener(v -> {
            Intent intent = new Intent(ListenActivity.this, ContactsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }
}