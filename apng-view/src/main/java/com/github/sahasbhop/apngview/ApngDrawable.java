package com.github.sahasbhop.apngview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.SystemClock;

import com.github.sahasbhop.flog.FLog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ar.com.hjg.pngj.PngReaderApng;
import ar.com.hjg.pngj.chunks.PngChunk;
import ar.com.hjg.pngj.chunks.PngChunkACTL;
import ar.com.hjg.pngj.chunks.PngChunkFCTL;

/**
 * Reference: http://www.vogella.com/code/com.vogella.android.drawables.animation/src/com/vogella/android/drawables/animation/ColorAnimationDrawable.html
 */
public class ApngDrawable extends Drawable implements Animatable, Runnable {
	
	private static final boolean VERBOSE = false;
	private static final float DELAY_FACTOR = 1000F;
	
	private ApngCallback apngCallback;
	private ArrayList<PngChunkFCTL> fctlArrayList;
	private Bitmap baseBitmap;
	private Bitmap[] bitmapArray;
	private DisplayImageOptions displayImageOptions;
	private ImageLoader imageLoader;
	private Paint paint;
	private String imagePath;
	private String workingPath;
	
	private boolean isPrepared;
	private boolean isRunning;
	
	private int baseWidth;
	private int baseHeight;
	private int currentFrame;
	private int currentLoop;
	private int numFrames;
	private int numPlays;
	
	private float mScaling;
	
	public ApngDrawable(Context context, Bitmap bitmap, Uri uri) {
		super();
		
		currentFrame = -1;
		currentLoop = 0;
		mScaling = 0F;

		paint = new Paint();
	    paint.setAntiAlias(true);
		
		displayImageOptions = new DisplayImageOptions.Builder()
			.cacheInMemory(false) // prevent GC_ALLOC, which cause lacking while playing
			.cacheOnDisk(true)
			.build();
		
		File workingDir = ApngHelper.getWorkingDir(context);
		
		workingPath = workingDir.getPath();
		
		if (VERBOSE) FLog.v("uri: %s", uri.toString());
		imageLoader = ImageLoader.getInstance();
		
		baseBitmap = bitmap;
		baseWidth = bitmap.getWidth();
		baseHeight = bitmap.getHeight();
		
		FLog.d("Bitmap size: %dx%d", baseWidth, baseHeight);
		
		try {
			if (uri != null) {
				String filename = uri.getLastPathSegment();
				
				File file = new File(workingPath, filename);
				
				if (!file.exists()) {
					if (VERBOSE) FLog.v("Copy file from %s to %s", uri.getPath(), file.getPath());
					FileUtils.copyFile(new File(uri.getPath()), file);
				}
				
				imagePath = file.getPath();
				
			} // if URI is not null
 		} catch (Exception e) {
			FLog.e("Error: %s", e.toString());
		}
	}

	@SuppressWarnings("unused")
	public void setCallback(ApngCallback callback) {
		apngCallback = callback;
	}

	@SuppressWarnings("unused")
	public int getNumPlays() {
		return numPlays;
	}
	
	public void setNumPlays(int numPlays) {
		this.numPlays = numPlays;
	}

	@SuppressWarnings("unused")
	public int getNumFrames() {
		return numFrames;
	}
	
	public void prepare() {
		if (VERBOSE) FLog.v("Extracting PNGs..");
		ApngExtractFrames.process(new File(imagePath));
		if (VERBOSE) FLog.v("Extracting complete");
		
		if (VERBOSE) FLog.v("Read APNG information..");
		readApngInformation();
		
		isPrepared = true;
	}
	
	@Override
	public void start() {
		if (!isRunning()) {
			isRunning = true;
			currentFrame = 0;
			
			if (!isPrepared) {
				if (VERBOSE) FLog.v("Prepare");
				prepare();
			}
			
			run();
			
			if (apngCallback != null) {
				apngCallback.onStart();
			}
		}
	}
	
	@Override
	public void stop() {
		if (isRunning()) {
	        currentLoop = 0;
	        
			unscheduleSelf(this);
			isRunning = false;
			
			if (apngCallback != null) {
				apngCallback.onStop();
			}
	    }
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public void run() {
		if (currentFrame < 0) {
			currentFrame = 0;
			
		} else if (currentFrame > fctlArrayList.size() - 1) {
			currentFrame = 0;
		}
		
		PngChunkFCTL pngChunk = fctlArrayList.get(currentFrame);
		
		int delayNum = pngChunk.getDelayNum();
		int delayDen = pngChunk.getDelayDen();
		int delay = Math.round(delayNum * DELAY_FACTOR / delayDen);
		
		scheduleSelf(this, SystemClock.uptimeMillis() + delay);
		invalidateSelf();
	}

	@SuppressWarnings("unused")
	public void recycleBitmaps() {
		if (bitmapArray != null) {
			for (Bitmap bitmap : bitmapArray) {
				bitmap.recycle();
			}
			
			for (int i = 0; i < bitmapArray.length; i++) {
				bitmapArray[i] = null;
			}
		}
	}

	@Override
	public void draw(Canvas canvas) {
		if (VERBOSE) FLog.v("Current frame: %d", currentFrame);
		
		if (currentFrame <= 0) {
			drawBaseBitmap(canvas);
		} else {
			drawApng(currentFrame, canvas);
		}
		
		if (numPlays > 0 && currentLoop >= numPlays) {
			stop();
		}
		
		if (numPlays > 0 && currentFrame == numFrames - 1) {
			currentLoop++;
			if (VERBOSE) FLog.v("Loop count: %d/%d", currentLoop, numPlays);
		}
		
		currentFrame++;
	}

	@Override
	public void setAlpha(int alpha) {
		paint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		paint.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
	
	private void readApngInformation() {
		fctlArrayList = new ArrayList<>();
		
		File baseFile = new File(imagePath);
		PngReaderApng reader = new PngReaderApng(baseFile);
		reader.end();
		
		List<PngChunk> pngChunks = reader.getChunksList().getChunks();
		PngChunk chunk;
		
		for (int i = 0; i < pngChunks.size(); i++) {
			chunk = pngChunks.get(i);
			
			if (chunk instanceof PngChunkACTL) {
				numFrames = ((PngChunkACTL) chunk).getNumFrames();
				FLog.d("numFrames: %d", numFrames);
				
				if (numFrames <= 0) {
					numPlays = ((PngChunkACTL) chunk).getNumPlays();
					FLog.d("numPlays: %d (media info)", numPlays);
				} else {
					FLog.d("numPlays: %d (user defined)", numPlays);
				}
				
			} else if (chunk instanceof PngChunkFCTL) {
				fctlArrayList.add((PngChunkFCTL) chunk);
			}
		}
		
		bitmapArray = new Bitmap[fctlArrayList.size()];
	}

	private void drawBaseBitmap(Canvas canvas) {
		if (mScaling == 0F) {
			int width = canvas.getWidth();
			int height = canvas.getHeight();
			
			if (VERBOSE) FLog.v("Canvas: %dx%d", width, height);
			
			float scalingByWidth = ((float) canvas.getWidth())/ baseWidth;
			if (VERBOSE) FLog.v("scalingByWidth: %.2f", scalingByWidth);
			
			float scalingByHeight = ((float) canvas.getHeight())/ baseHeight;
			if (VERBOSE) FLog.v("scalingByHeight: %.2f", scalingByHeight);
			
			mScaling = scalingByWidth <= scalingByHeight ? scalingByWidth : scalingByHeight;
			if (VERBOSE) FLog.v("mScaling: %.2f", mScaling);
		}
		
		RectF dst = new RectF(0, 0, mScaling * baseWidth, mScaling * baseHeight);
		canvas.drawBitmap(baseBitmap, null, dst, paint);
		
		if (bitmapArray != null) {
			bitmapArray[0] = baseBitmap;
		}
	}
	
	private void drawApng(int frameIndex, Canvas canvas) {
		if (bitmapArray != null && bitmapArray.length > frameIndex) {
			Bitmap bitmap = bitmapArray[frameIndex] == null ? createBitmap(frameIndex) : bitmapArray[frameIndex];
			if (bitmap == null) return;

			if (bitmapArray[frameIndex] == null) {
				bitmapArray[frameIndex] = bitmap;
			}
			
			RectF dst = new RectF(0, 0, mScaling * bitmap.getWidth(), mScaling * bitmap.getHeight());
			
			canvas.drawBitmap(bitmap, null, dst, paint);
		}
	}

	private Bitmap createBitmap(int frameIndex) {
		if (VERBOSE) FLog.v("ENTER");
		File baseFile = new File(imagePath);
		
		Bitmap baseBitmap = null;
		
		PngChunkFCTL previousChunk = frameIndex > 0 ? fctlArrayList.get(frameIndex - 1) : null;
		
		if (previousChunk != null) {
			byte disposeOp = previousChunk.getDisposeOp();
			int offsetX = previousChunk.getxOff();
			int offsetY = previousChunk.getyOff();
			
			Bitmap tempBitmap;
			Canvas tempCanvas;
			String tempPath;
			Bitmap tempFrameBitmap;
			
			switch (disposeOp) {
			case PngChunkFCTL.APNG_DISPOSE_OP_NONE:
				// Get bitmap from the previous frame
				baseBitmap = frameIndex > 0 ? bitmapArray[frameIndex - 1] : null;
				break;
				
			case PngChunkFCTL.APNG_DISPOSE_OP_BACKGROUND:
				// Get bitmap from the previous frame but the drawing region is needed to be cleared
				baseBitmap = frameIndex > 0 ? bitmapArray[frameIndex - 1] : null;
				if (baseBitmap == null) break;
				
				tempPath = new File(workingPath, ApngExtractFrames.getFileName(baseFile, frameIndex - 1)).getPath();
				tempFrameBitmap = imageLoader.loadImageSync(Uri.fromFile(new File(tempPath)).toString(), displayImageOptions);
				
				tempBitmap = Bitmap.createBitmap(baseWidth, baseHeight, Bitmap.Config.ARGB_8888);
				tempCanvas = new Canvas(tempBitmap);
				tempCanvas.drawBitmap(baseBitmap, 0, 0, null);
				
				tempCanvas.clipRect(offsetX, offsetY, offsetX + tempFrameBitmap.getWidth(), offsetY + tempFrameBitmap.getHeight());
				tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
				tempCanvas.clipRect(0, 0, baseWidth, baseHeight);
				
				baseBitmap = tempBitmap;
				break;
				
			case PngChunkFCTL.APNG_DISPOSE_OP_PREVIOUS:
				if (frameIndex > 1) {
					PngChunkFCTL tempPngChunk;
					
					for (int i = frameIndex - 2; i >= 0; i--) {
						tempPngChunk = fctlArrayList.get(i);
						int tempDisposeOp = tempPngChunk.getDisposeOp();
						int tempOffsetX = tempPngChunk.getxOff();
						int tempOffsetY = tempPngChunk.getyOff();
						
						tempPath = new File(workingPath, ApngExtractFrames.getFileName(baseFile, i)).getPath();
						tempFrameBitmap = imageLoader.loadImageSync(Uri.fromFile(new File(tempPath)).toString(), displayImageOptions);
						
						if (tempDisposeOp != PngChunkFCTL.APNG_DISPOSE_OP_PREVIOUS) {
							
							if (tempDisposeOp == PngChunkFCTL.APNG_DISPOSE_OP_NONE) {
								baseBitmap = bitmapArray[i];
								
							} else if (tempDisposeOp == PngChunkFCTL.APNG_DISPOSE_OP_BACKGROUND) {
								tempBitmap = Bitmap.createBitmap(baseWidth, baseHeight, Bitmap.Config.ARGB_8888);
								tempCanvas = new Canvas(tempBitmap);
								tempCanvas.drawBitmap(bitmapArray[i], 0, 0, null);
								
								tempCanvas.clipRect(tempOffsetX, tempOffsetY, tempOffsetX + tempFrameBitmap.getWidth(), tempOffsetY + tempFrameBitmap.getHeight());
								tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
								tempCanvas.clipRect(0, 0, baseWidth, baseHeight);
								
								baseBitmap = tempBitmap;
							}
							break;
						}
					}
				}
				break;
			}			
		}
		
		String path = new File(workingPath, ApngExtractFrames.getFileName(baseFile, frameIndex)).getPath();
		Bitmap currentFrameBitmap = imageLoader.loadImageSync(Uri.fromFile(new File(path)).toString(), displayImageOptions);
		
		Bitmap redrawnBitmap;
		
		PngChunkFCTL chunk = fctlArrayList.get(frameIndex);
		
		byte blendOp = chunk.getBlendOp();
		int offsetX = chunk.getxOff();
		int offsetY = chunk.getyOff();
		
		redrawnBitmap = redrawPng(baseWidth, baseHeight, offsetX, offsetY, blendOp, currentFrameBitmap, baseBitmap);
		
		if (VERBOSE) FLog.v("EXIT");
		return redrawnBitmap;
	}

	private Bitmap redrawPng(
			int width, int height, 
			int offsetX, int offsetY, byte blendOp, 
			Bitmap frameBitmap, Bitmap baseBitmap) {
		
		Bitmap redrawnBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		
		Canvas canvas = new Canvas(redrawnBitmap);
		
		if (baseBitmap != null) {
			canvas.drawBitmap(baseBitmap, 0, 0, null);
			
			if (blendOp == PngChunkFCTL.APNG_BLEND_OP_SOURCE) {
				canvas.clipRect(offsetX, offsetY, offsetX + frameBitmap.getWidth(), offsetY + frameBitmap.getHeight());
				canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
				canvas.clipRect(0, 0, width, height);
			}
		}
		
		canvas.drawBitmap(frameBitmap, offsetX, offsetY, null);
		
		return redrawnBitmap;
	}
	
	public interface ApngCallback {
		void onStart();
		void onStop();
	}
	
}
