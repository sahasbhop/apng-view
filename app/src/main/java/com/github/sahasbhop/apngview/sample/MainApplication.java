package com.github.sahasbhop.apngview.sample;

import android.app.Application;

import com.github.sahasbhop.apngview.ApngHelper;
import com.github.sahasbhop.apngview.ApngImageLoader;
import com.github.sahasbhop.flog.FLog;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        ImageLoaderConfiguration configuration =
                new ImageLoaderConfiguration.Builder(getApplicationContext())
                        .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                        .memoryCacheSize(2 * 1024 * 1024)
                        .diskCacheSize(50 * 1024 * 1024)
                        .diskCacheFileCount(100)
                        .build();

        FLog.d("Initialize Universal Image Loader - Common Use");
        ImageLoader.getInstance().init(configuration);

        FLog.d("Initialize Universal Image Loader - APNG");
        ApngHelper.initializeApngImageLoader(getApplicationContext());

        FLog.d("ImageLoader instance: %s", ImageLoader.getInstance());
        FLog.d("ApngImageLoader instance: %s", ApngImageLoader.getInstance());
    };
}
