package com.github.sahasbhop.apngview;

import com.nostra13.universalimageloader.core.ImageLoader;

public class ApngImageLoader extends ImageLoader {

	private static ApngImageLoader sInstance;

	public static ApngImageLoader getInstance() {
		if (sInstance == null) {
			synchronized (ApngImageLoader.class) {
				if (sInstance == null) {
					sInstance = new ApngImageLoader();
				}
			}
		}
		return sInstance;
	}

	protected ApngImageLoader() { /* Singleton */ }
	
}
