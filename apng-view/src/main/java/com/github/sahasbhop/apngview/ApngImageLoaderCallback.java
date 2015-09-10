package com.github.sahasbhop.apngview;

import android.view.View;

/**
 * Interface for listening whether an APNG image is finished loading.
 * The caller may consider playing an animation immediately or knowing that it is ready for playing
 */
public interface ApngImageLoaderCallback {
    void onLoadComplete(String imageUri, View view);
    void onLoadFailed(String imageUri, View view);
}
