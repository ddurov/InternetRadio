package ru.radio.poems;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ru.radio.poems.service.Values;

public class ScheduleActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        findViewById(R.id.toSchedule).setActivated(true);
        Button toListen = findViewById(R.id.toListenRadio);
        Button toContacts = findViewById(R.id.toContacts);
        TextView scheduleTextView = findViewById(R.id.scheduleText);

        scheduleTextView.setText(Values.scheduleText == null ? "Расписания нет :(" : Values.scheduleText);

        toListen.setOnClickListener(v -> {
            Intent intent = new Intent(ScheduleActivity.this, ListenActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        toContacts.setOnClickListener(v -> {
            Intent intent = new Intent(ScheduleActivity.this, ContactsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }
}