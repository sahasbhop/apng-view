package com.github.sahasbhop.apngview;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.github.sahasbhop.apngview.assist.ApngImageDownloader;
import com.github.sahasbhop.apngview.assist.ApngImageLoadingListener;
import com.github.sahasbhop.apngview.assist.PngImageLoader;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

import java.lang.ref.WeakReference;

/**
 * Main class for APNG image loading that inherited from UIL ImageLoader.
 * Same as its parent, the init() method must be called before any other methods.
 */
public class ApngImageLoader extends ImageLoader {
    public static boolean enableVerboseLog = false;
    public static boolean enableDebugLog = false;

    private static ApngImageLoader singleton;

    private Context context;
    private WeakReference<ApngImageLoaderCallback> callback;

    public static ApngImageLoader getInstance() {
        if (singleton == null) {
            synchronized (ApngImageLoader.class) {
                if (singleton == null) {
                    singleton = new ApngImageLoader();
                }
            }
        }
        return singleton;
    }

    protected ApngImageLoader() { /*Singleton*/ }

    public void init(Context context) {
        this.init(context, null, null);
    }

    public void init(Context context,
                     ImageLoaderConfiguration commonImageLoaderConfiguration,
                     ImageLoaderConfiguration apngComponentImageLoaderConfiguration) {

        this.context = context.getApplicationContext();

        if (commonImageLoaderConfiguration == null) {
            commonImageLoaderConfiguration = getDefaultCommonImageLoaderConfiguration();
        }

        if (apngComponentImageLoaderConfiguration == null) {
            apngComponentImageLoaderConfiguration = getDefaultApngComponentImageLoaderConfiguration(this.context);
        }

        // Initialize UIL for loading plain PNG files
        PngImageLoader.getInstance().init(commonImageLoaderConfiguration);

        // Initialize UIL for loading APNG component files
        super.init(apngComponentImageLoaderConfiguration);
    }

    public ApngImageLoaderCallback getCallback() {
        return callback == null ? null : callback.get();
    }

    public void setCallback(ApngImageLoaderCallback callback) {
        this.callback = new WeakReference<>(callback);
    }

    public void setEnableVerboseLog(boolean enableVerboseLog) {
        ApngImageLoader.enableVerboseLog = enableVerboseLog;
    }

    public void setEnableDebugLog(boolean enableDebugLog) {
        ApngImageLoader.enableDebugLog = enableDebugLog;
    }

    @Override
    public void displayImage(String uri, ImageAware imageAware) {
        super.displayImage(uri, imageAware, new ApngImageLoadingListener(context, Uri.parse(uri), getCallback()));
    }

    @Override
    public void displayImage(String uri, ImageAware imageAware, DisplayImageOptions options) {
        super.displayImage(uri, imageAware, options, new ApngImageLoadingListener(context, Uri.parse(uri), getCallback()));
    }

    @Override
    public void displayImage(String uri, ImageView imageView) {
        super.displayImage(uri, imageView, new ApngImageLoadingListener(context, Uri.parse(uri), getCallback()));
    }

    @Override
    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options) {
        super.displayImage(uri, imageView, options, new ApngImageLoadingListener(context, Uri.parse(uri), getCallback()));
    }

    private ImageLoaderConfiguration getDefaultApngComponentImageLoaderConfiguration(Context context) {
        DisplayImageOptions defaultDisplayImageOptions =
                new DisplayImageOptions.Builder()
                        .cacheInMemory(false)
                        .cacheOnDisk(true)
                        .build();

        return new ImageLoaderConfiguration.Builder(context)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .imageDownloader(new ApngImageDownloader(context))
                .defaultDisplayImageOptions(defaultDisplayImageOptions)
                .build();
    }

    private ImageLoaderConfiguration getDefaultCommonImageLoaderConfiguration() {
        return new ImageLoaderConfiguration.Builder(this.context)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .build();
    }
}
