package com.github.sahasbhop.apngview.assist;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;

import com.github.sahasbhop.flog.FLog;
import com.nostra13.universalimageloader.core.assist.ContentLengthInputStream;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.github.sahasbhop.apngview.ApngImageLoader.enableDebugLog;
import static com.github.sahasbhop.apngview.ApngImageLoader.enableVerboseLog;

/**
 * Responsible for making a copy of any PNG files being loaded by the library.
 * When image loading process is finished,
 * {@link ApngImageLoadingListener#onLoadingComplete(String, View, Bitmap)} will be triggered
 * and a copied PNG will be processed there.
 */
public class ApngImageDownloader extends BaseImageDownloader {

	private static final int BUFFER_SIZE = 32 * 1024; // 32 Kb

	private Context mContext;
	private ExecutorService mExecutor;
	
	public ApngImageDownloader(Context context) {
		super(context);
		mContext = context;
		mExecutor = Executors.newSingleThreadExecutor();
	}
	
	@Override
	protected InputStream getStreamFromFile(final String imageUri, Object extra) throws IOException {
		final InputStream imageStream = super.getStreamFromFile(imageUri, extra);
		
		Future<InputStream> future = mExecutor.submit(new Callable<InputStream>() {
			@Override
			public InputStream call() throws Exception {
				return processImage(imageUri, imageStream);
			}
		});
		
		InputStream result = null;
		
		try {
			result = future.get();
		} catch (Exception e) {
			if (enableDebugLog) FLog.w("Error: %s", e.toString());
		}
		
		return result;
	}
	
	@Override
	protected InputStream getStreamFromAssets(final String imageUri, Object extra) throws IOException {
		final InputStream imageStream = super.getStreamFromAssets(imageUri, extra);
		
		Future<InputStream> future = mExecutor.submit(new Callable<InputStream>() {
			@Override
			public InputStream call() throws Exception {
				return processImage(imageUri, imageStream);
			}
		});
		
		InputStream result = null;
		
		try {
			result = future.get();
		} catch (Exception e) {
			if (enableDebugLog) FLog.w("Error: %s", e.toString());
		}
		
		return result;
	}
	
	@Override
	protected InputStream getStreamFromNetwork(final String imageUri, Object extra) throws IOException {
		final InputStream imageStream = super.getStreamFromNetwork(imageUri, extra);
		
		Future<InputStream> future = mExecutor.submit(new Callable<InputStream>() {
			@Override
			public InputStream call() throws Exception {
				return processImage(imageUri, imageStream);
			}
		});
		
		InputStream result = null;
		
		try {
			result = future.get();
		} catch (Exception e) {
			if (enableDebugLog) FLog.w("Error: %s", e.toString());
		}
		
		return result;
	}
	
	private InputStream processImage(String imageUri, InputStream imageStream) {
		if (imageUri == null || imageStream == null) {
			return imageStream;
		}
		
		boolean isPng = false;
		
		try {
			Uri url = Uri.parse(imageUri);
			String path = url.getPath();
			
			isPng = path != null && path.endsWith(".png");
			
		} catch (Exception e) { /* ignored */ }

		if (!isPng) return imageStream;

		File cacheDir = AssistUtil.getWorkingDir(mContext);
		AssistUtil.checkCahceSize(cacheDir, 0);

		File targetFile = AssistUtil.getCopiedFile(mContext, imageUri);

		if (targetFile == null) {
			if (enableDebugLog) FLog.w("Can't copy a file!!! %s", imageUri);

		} else if (!targetFile.exists()) {
			if (enableVerboseLog) FLog.v("Copy\nfrom: %s\nto: %s", imageUri, targetFile.getPath());

			try {
				try {
					URL url = new URL(imageUri);
					FileUtils.copyURLToFile(url, targetFile);

				} catch (MalformedURLException e) {
					FileUtils.copyInputStreamToFile(imageStream, targetFile);
				}

				if (enableVerboseLog) FLog.v("Copy finished");

				FileInputStream input = new FileInputStream(targetFile);
				imageStream = new ContentLengthInputStream(new BufferedInputStream(input, BUFFER_SIZE), input.available());

			} catch (Exception e) {
				if (enableDebugLog) FLog.w("Error: %s", e.toString());
			}
		}

		return imageStream;
	}

}
