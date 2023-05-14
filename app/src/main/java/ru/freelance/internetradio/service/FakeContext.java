package ru.freelance.internetradio.service;

import android.app.Application;

public class FakeContext extends Application {

    private static FakeContext instance;

    public FakeContext() {
        instance = this;
    }

    public static FakeContext getInstance() {
        return instance;
    }

}