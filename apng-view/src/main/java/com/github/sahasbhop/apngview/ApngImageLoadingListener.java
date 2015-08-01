package com.github.sahasbhop.apngview;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.github.sahasbhop.flog.FLog;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.File;

public class ApngImageLoadingListener implements ImageLoadingListener {
	private Context mContext;
	private Uri mUri;
	
	public ApngImageLoadingListener(Context context, Uri uri) {
		mContext = context;
		mUri = uri;
	}
	
	@Override
	public void onLoadingStarted(String imageUri, View view) {
		view.setTag(mUri.toString());
	}
	
	@Override
	public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		Object tag = view.getTag();
		FLog.d("tag: %s", tag);
		
		if (tag != null && tag instanceof String) {
			String actualUri = tag.toString();
			
			File pngFile = ApngHelper.getCopiedFile(mContext, actualUri);
			
			if (pngFile == null) {
				FLog.w("Can't locate the file!!! %s", actualUri);
				
			} else if (pngFile.exists()) {
				boolean isApng = ApngHelper.isApng(pngFile);
				
				if (isApng) {
					FLog.d("Setup apng drawable");
					ApngDrawable drawable = new ApngDrawable(mContext, loadedImage, Uri.fromFile(pngFile));
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
	}
	
	@Override
	public void onLoadingCancelled(String imageUri, View view) {
		Object tag = view.getTag();
		FLog.d("tag: %s", tag);
		
		view.setTag(null);
	}
	
	@Override
	public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
		Object tag = view.getTag();
		FLog.d("tag: %s", tag);
		
		view.setTag(null);
	}
}