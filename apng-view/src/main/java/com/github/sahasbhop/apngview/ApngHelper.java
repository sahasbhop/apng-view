package com.github.sahasbhop.apngview;

import android.content.Context;
import android.net.Uri;

import com.github.sahasbhop.flog.FLog;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.security.MessageDigest;

import ar.com.hjg.pngj.PngReaderApng;

public class ApngHelper {
	
	public static void initializeApngImageLoader(Context context) {
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
		
		ApngImageLoader.getInstance().init(configuration);
	}

	public static File getWorkingDir(Context context) {
		File workingDir = null;
		File cacheDir = context.getExternalCacheDir();
		
		if (cacheDir == null) {
			cacheDir = context.getCacheDir();
		}
		
		if (cacheDir != null) {
			workingDir = new File(String.format("%s/apng/.nomedia/", cacheDir.getPath()));
			
			if (!workingDir.exists()) {
				workingDir.mkdirs();
			}
		}
		
		return workingDir;	
	}
	
	public static File getCopiedFile(Context context, String imageUri) {
		String filename;
		
		try {
			filename = String.format("%s.png", md5(imageUri));
			
		} catch (Exception e) { 
			filename = Uri.parse(imageUri).getLastPathSegment();
		}
		
		File workingDir = getWorkingDir(context);
		File f = null;
		
		if (workingDir != null && workingDir.exists()) {
			f = new File(workingDir, filename);
		}
		
		return f;
	}
	
	public static boolean isApng(File filePng) {
		boolean isApng = false;
		
		try {
			PngReaderApng reader = new PngReaderApng(filePng);
			reader.end();
			
			int apngNumFrames = reader.getApngNumFrames();
			
			isApng = apngNumFrames > 1;
			
		} catch (Exception e) {
			FLog.w("Error: %s", e.toString());
		}
		
		return isApng;
	}

	public static final char[] HEX_ARRAY = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

	public static String md5(String message) throws Exception {
		MessageDigest md = MessageDigest.getInstance("md5");
		return bytesToHex(md.digest(message.getBytes("utf-8")));
	}

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for ( int j = 0; j < bytes.length; j++ ) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}
	
}
