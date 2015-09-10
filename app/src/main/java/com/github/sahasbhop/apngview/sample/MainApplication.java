package com.github.sahasbhop.apngview.sample;

import android.app.Application;

import com.github.sahasbhop.apngview.ApngImageLoader;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ApngImageLoader apngImageLoader = ApngImageLoader.getInstance();
        apngImageLoader.setEnableDebugLog(false);
        apngImageLoader.setEnableVerboseLog(false);

        apngImageLoader.init(getApplicationContext());

    }
}
