package ru.freelance.internetradio.service;

import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.SessionToken;
import androidx.media3.ui.PlayerView;

public class Values {
    public static ExoPlayer exoPlayer;
    public static String radioUrlPath = "http://ddproj.ru:8000/live";
    public static String radioCurrentTrack = "http://ddproj.ru:8000/getCurrentLiveTrack";

    public interface ACTION {
        String PLAY_PAUSE_ACTION = "ru.freelance.internetradio.action.play_pause";
        String STOP_ACTION = "ru.freelance.internetradio.action.stop";
    }
    public static boolean isPlaying = false;
    public static int FOREGROUND_SERVICE = 101;
}
