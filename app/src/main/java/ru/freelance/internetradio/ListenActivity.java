package ru.freelance.internetradio;

import static ru.freelance.internetradio.service.Values.isPlaying;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import ru.freelance.internetradio.service.NotificationService;
import ru.freelance.internetradio.service.Values;

public class ListenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen);

        Button startStopListen = findViewById(R.id.startStopRadio);
        // release activities below
        Button toSchedule = findViewById(R.id.toSchedule);
        ((Button) findViewById(R.id.toListenRadio)).setSelected(true);
        Button toContacts = findViewById(R.id.toContacts);

        startStopListen.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, NotificationService.class);
            serviceIntent.setAction(Values.ACTION.PLAY_PAUSE_ACTION);
            startService(serviceIntent);
            startStopListen.setActivated(!isPlaying);
        });
    }
}