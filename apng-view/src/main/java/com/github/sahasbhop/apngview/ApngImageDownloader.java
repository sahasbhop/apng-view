package com.github.sahasbhop.apngview;

import android.content.Context;
import android.net.Uri;

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
			FLog.w("Error: %s", e.toString());
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
			FLog.w("Error: %s", e.toString());
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
			FLog.w("Error: %s", e.toString());
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
		
		if (isPng) {
			File cacheDir = ApngHelper.getWorkingDir(mContext);
			CacheManager.checkCahceSize(cacheDir, 0);
			
			File targetFile = ApngHelper.getCopiedFile(mContext, imageUri);
			
			if (targetFile == null) {
				FLog.w("Can't copy a file!!! %s", imageUri);
				
			} else if (!targetFile.exists()) {
				FLog.d("Copy\nfrom: %s\nto: %s", imageUri, targetFile.getPath());
				
				try {
					try {
						URL url = new URL(imageUri);
						FileUtils.copyURLToFile(url, targetFile);
						
					} catch (MalformedURLException e) {
						FileUtils.copyInputStreamToFile(imageStream, targetFile);
					}
					
					FLog.d("Copy finished");
					
					FileInputStream input = new FileInputStream(targetFile);
					imageStream = new ContentLengthInputStream(new BufferedInputStream(input, BUFFER_SIZE), input.available());
					
				} catch (Exception e) {
					FLog.w("Error: %s", e.toString());
				}
			}
		} 
		
		return imageStream;
	}

}
