package ru.freelance.internetradio;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.freelance.internetradio.service.Values;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    SplashScreen splashScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        new OkHttpClient().newCall(
                new Request.Builder().url(Values.scheduleTextPage).build()
        ).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("radio", Objects.requireNonNull(e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String page = response.body().string();
                final String regex = "\\\"desc\\\":\\\"««(.*)»»\\\"";
                final Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
                final Matcher matcher = pattern.matcher(page);
                if (matcher.find()) {
                    Values.scheduleText = Objects.requireNonNull(matcher.group(1))
                            .replaceAll("\\\\n", "\n")
                            .replaceAll("&quot;", "\"");
                }
            }
        });

        splashScreen.setKeepOnScreenCondition(() -> {
            if (Values.scheduleText != null) {
                startActivity(new Intent(SplashScreenActivity.this, ListenActivity.class));
                finish();
                return false;
            } else return true;
        });
    }
}