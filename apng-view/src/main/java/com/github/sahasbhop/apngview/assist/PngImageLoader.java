package com.github.sahasbhop.apngview.assist;

import com.github.sahasbhop.apngview.ApngImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PngImageLoader extends ImageLoader {
    private static PngImageLoader singleton;

    public static PngImageLoader getInstance() {
        if (singleton == null) {
            synchronized (ApngImageLoader.class) {
                if (singleton == null) {
                    singleton = new PngImageLoader();
                }
            }
        }
        return singleton;
    }

    protected PngImageLoader() { /*Singleton*/ }
}
