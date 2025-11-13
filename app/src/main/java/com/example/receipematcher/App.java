package com.example.receipematcher;

import android.app.Application;

public class App extends Application {
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static App get() {
        return instance;
    }
}
