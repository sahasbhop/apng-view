package com.github.sahasbhop.apngview;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.github.sahasbhop.apngview.assist.ApngImageDownloader;
import com.github.sahasbhop.apngview.assist.ApngImageLoadingListener;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

/**
 * Single
 */
public class ApngImageLoader extends ImageLoader {

    private static ApngImageLoader singleton;
    private Context context;

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
        this.context = context.getApplicationContext();

        DisplayImageOptions defaultDisplayImageOptions =
                new DisplayImageOptions.Builder()
                        .cacheInMemory(false)
                        .cacheOnDisk(true)
                        .build();

        ImageLoaderConfiguration configuration =
                new ImageLoaderConfiguration.Builder(context)
                        .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                        .memoryCacheSize(2 * 1024 * 1024)
                        .diskCacheSize(50 * 1024 * 1024)
                        .diskCacheFileCount(100)
                        .imageDownloader(new ApngImageDownloader(context))
                        .defaultDisplayImageOptions(defaultDisplayImageOptions)
                        .build();

        super.init(configuration);
    }

    @Override
    public void displayImage(String uri, ImageAware imageAware) {
        super.displayImage(uri, imageAware, new ApngImageLoadingListener(context, Uri.parse(uri)));
    }

    @Override
    public void displayImage(String uri, ImageAware imageAware, ImageLoadingListener listener) {
        super.displayImage(uri, imageAware, new ApngImageLoadingListener(context, Uri.parse(uri), listener));
    }

    @Override
    public void displayImage(String uri, ImageAware imageAware, DisplayImageOptions options) {
        super.displayImage(uri, imageAware, options, new ApngImageLoadingListener(context, Uri.parse(uri)));
    }

    @Override
    public void displayImage(String uri, ImageAware imageAware, DisplayImageOptions options, ImageLoadingListener listener) {
        super.displayImage(uri, imageAware, options, new ApngImageLoadingListener(context, Uri.parse(uri), listener));
    }

    @Override
    public void displayImage(String uri, ImageAware imageAware, DisplayImageOptions options, ImageLoadingListener listener, ImageLoadingProgressListener progressListener) {
        super.displayImage(uri, imageAware, options, new ApngImageLoadingListener(context, Uri.parse(uri), listener), progressListener);
    }

    @Override
    public void displayImage(String uri, ImageView imageView) {
        super.displayImage(uri, imageView, new ApngImageLoadingListener(context, Uri.parse(uri)));
    }

    @Override
    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options) {
        super.displayImage(uri, imageView, options, new ApngImageLoadingListener(context, Uri.parse(uri)));
    }

    @Override
    public void displayImage(String uri, ImageView imageView, ImageLoadingListener listener) {
        super.displayImage(uri, imageView, new ApngImageLoadingListener(context, Uri.parse(uri), listener));
    }

    @Override
    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options, ImageLoadingListener listener) {
        super.displayImage(uri, imageView, options, new ApngImageLoadingListener(context, Uri.parse(uri), listener));
    }

    @Override
    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options, ImageLoadingListener listener, ImageLoadingProgressListener progressListener) {
        super.displayImage(uri, imageView, options, new ApngImageLoadingListener(context, Uri.parse(uri), listener), progressListener);
    }
}
