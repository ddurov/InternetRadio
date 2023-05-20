package ru.freelance.internetradio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

public class ContactsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        ImageButton toVKontakte = findViewById(R.id.VKontakteButton);
        Button toSchedule = findViewById(R.id.toSchedule);
        Button toListen = findViewById(R.id.toListenRadio);
        findViewById(R.id.toContacts).setActivated(true);

        toVKontakte.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/platformfree"));
            startActivity(intent);
        });

        toSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(ContactsActivity.this, ScheduleActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        toListen.setOnClickListener(v -> {
            Intent intent = new Intent(ContactsActivity.this, ListenActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }
}