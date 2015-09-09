package com.github.sahasbhop.apngview.assist;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.github.sahasbhop.apngview.ApngDrawable;
import com.github.sahasbhop.apngview.ApngImageLoader;
import com.github.sahasbhop.flog.FLog;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.File;

public class ApngImageLoadingListener implements ImageLoadingListener {
    private Context context;
    private ImageLoadingListener imageLoadingListener;
    private Uri uri;

    public ApngImageLoadingListener(Context context, Uri uri) {
        this(context, uri, null);
    }

    public ApngImageLoadingListener(Context context, Uri uri, ImageLoadingListener imageLoadingListener) {
        this.context = context;
        this.uri = uri;
        this.imageLoadingListener = imageLoadingListener;
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {
        view.setTag(uri.toString());
        if (imageLoadingListener != null) imageLoadingListener.onLoadingStarted(imageUri, view);
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        Object tag = view.getTag();
        FLog.d("tag: %s", tag);

        if (tag != null && tag instanceof String) {
            String actualUri = tag.toString();

            File pngFile = AssistUtil.getCopiedFile(context, actualUri);

            if (pngFile == null) {
                FLog.w("Can't locate the file!!! %s", actualUri);

            } else if (pngFile.exists()) {
                boolean isApng = AssistUtil.isApng(pngFile);

                if (isApng) {
                    FLog.d("Setup apng drawable");
                    ApngDrawable drawable = new ApngDrawable(context, loadedImage, Uri.fromFile(pngFile));
                    ((ImageView) view).setImageDrawable(drawable);
                } else {
                    ((ImageView) view).setImageBitmap(loadedImage);
                }

            } else {
                FLog.d("Clear cache and reload");
                MemoryCacheUtils.removeFromCache(actualUri, ApngImageLoader.getInstance().getMemoryCache());
                DiskCacheUtils.removeFromCache(actualUri, ApngImageLoader.getInstance().getDiskCache());

                ApngImageLoader.getInstance().displayImage(actualUri, (ImageView) view, this);
            }
        }

        if (imageLoadingListener != null) imageLoadingListener.onLoadingComplete(imageUri, view, loadedImage);
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {
        Object tag = view.getTag();
        FLog.d("tag: %s", tag);

        view.setTag(null);

        if (imageLoadingListener != null) imageLoadingListener.onLoadingCancelled(imageUri, view);
    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        Object tag = view.getTag();
        FLog.d("tag: %s", tag);

        view.setTag(null);

        if (imageLoadingListener != null) imageLoadingListener.onLoadingFailed(imageUri, view, failReason);
    }
}