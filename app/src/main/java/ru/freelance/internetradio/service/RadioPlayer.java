package ru.freelance.internetradio.service;

import static ru.freelance.internetradio.service.Values.exoPlayer;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;

public class RadioPlayer {
    public static void start(String url, Context context) {
        if (exoPlayer != null) exoPlayer.stop();

        exoPlayer = new ExoPlayer.Builder(context).build();

        exoPlayer.addMediaItem(MediaItem.fromUri(url));
        exoPlayer.prepare();    
        exoPlayer.play();

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                @Nullable Throwable cause = error.getCause();
                if (cause instanceof HttpDataSource.HttpDataSourceException) {
                    HttpDataSource.HttpDataSourceException httpError = (HttpDataSource.HttpDataSourceException) cause;
                    if (httpError instanceof HttpDataSource.InvalidResponseCodeException) {
                        Toast.makeText(context, "Произошла сетевая ошибка", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Произошла неизвестная ошибка", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    public static void pause() {
        if (exoPlayer != null) exoPlayer.pause();
    }

    public static void stop() {
        if (exoPlayer != null) exoPlayer.stop();
    }
}
