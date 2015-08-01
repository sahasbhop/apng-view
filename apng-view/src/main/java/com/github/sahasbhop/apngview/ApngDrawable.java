package com.github.sahasbhop.apngview;

import android.content.Context;
import android.content.res.Resources;
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

// ref: http://www.vogella.com/code/com.vogella.android.drawables.animation/src/com/vogella/android/drawables/animation/ColorAnimationDrawable.html
public class ApngDrawable extends Drawable implements Animatable, Runnable {
	
	private static final boolean VERBOSE = false;
	private static final float DELAY_FACTOR = 1000F;
	
	private ApngCallback mCallback;
	private ArrayList<PngChunkFCTL> mFctlList;
	private Bitmap mBaseBitmap;
	private Bitmap[] mBitmaps;
	private Context mContext;
	private DisplayImageOptions mDisplayImageOptions;
	private ImageLoader mImageLoader;
	private Paint mPaint;
	private String mImagePath;
	private String mWorkingPath;
	
	private boolean mIsPrepared;
	private boolean mIsRunning;
	
	private int mBaseWidth;
	private int mBaseHeight;
	private int mCurrentFrame;
	private int mCurrentLoop;
	private int mNumFrames;
	private int mNumPlays;
	
	private float mScaling;
	
	public ApngDrawable(Context context, Bitmap bitmap, Uri uri) {
		super();
		
		mCurrentFrame = -1;
		mCurrentLoop = 0;
		mScaling = 0F;
		
		mContext = context;
		
		mPaint = new Paint();
	    mPaint.setAntiAlias(true);
		
		mDisplayImageOptions = new DisplayImageOptions.Builder()
			.cacheInMemory(false) // prevent GC_ALLOC, which cause lacking while playing
			.cacheOnDisk(true)
			.build();
		
		File workingDir = ApngHelper.getWorkingDir(mContext);
		
		mWorkingPath = workingDir.getPath();	
		
		if (VERBOSE) FLog.v("uri: %s", uri.toString());
		mImageLoader = ImageLoader.getInstance();
		
		mBaseBitmap = bitmap;
		mBaseWidth = bitmap.getWidth();
		mBaseHeight = bitmap.getHeight();
		
		FLog.d("Bitmap size: %dx%d", mBaseWidth, mBaseHeight);
		
		try {
			if (uri != null) {
				String filename = uri.getLastPathSegment();
				
				File file = new File(mWorkingPath, filename);
				
				if (!file.exists()) {
					if (VERBOSE) FLog.v("Copy file from %s to %s", uri.getPath(), file.getPath());
					FileUtils.copyFile(new File(uri.getPath()), file);
				}
				
				mImagePath = file.getPath();
				
			} // if URI is not null
 		} catch (Exception e) {
			FLog.e("Error: %s", e.toString());
		}
	}
	
	public void setCallback(ApngCallback callback) {
		mCallback = callback;
	}
	
	public int getNumPlays() {
		return mNumPlays;
	}
	
	public void setNumPlays(int numPlays) {
		mNumPlays = numPlays;
	}
	
	public int getNumFrames() {
		return mNumFrames;
	}
	
	public void prepare() {
		if (VERBOSE) FLog.v("Extracting PNGs..");
		ApngExtractFrames.process(new File(mImagePath));
		if (VERBOSE) FLog.v("Extracting complete");
		
		if (VERBOSE) FLog.v("Read APNG information..");
		readApngInformation(mContext.getResources());
		
		mIsPrepared = true;
	}
	
	@Override
	public void start() {
		if (!isRunning()) {
			mIsRunning = true;
			mCurrentFrame = 0;
			
			if (!mIsPrepared) {
				if (VERBOSE) FLog.v("Prepare");
				prepare();
			}
			
			run();
			
			if (mCallback != null) {
				mCallback.onStart();
			}
		}
	}
	
	@Override
	public void stop() {
		if (isRunning()) {
	        mCurrentLoop = 0;
	        
			unscheduleSelf(this);
			mIsRunning = false;
			
			if (mCallback != null) {
				mCallback.onStop();
			}
	    }
	}

	@Override
	public boolean isRunning() {
		return mIsRunning;
	}

	@Override
	public void run() {
		if (mCurrentFrame < 0) {
			mCurrentFrame = 0;
			
		} else if (mCurrentFrame > mFctlList.size() - 1) {
			mCurrentFrame = 0;
		}
		
		PngChunkFCTL pngChunk = mFctlList.get(mCurrentFrame);
		
		int delayNum = pngChunk.getDelayNum();
		int delayDen = pngChunk.getDelayDen();
		int delay = Math.round(delayNum * DELAY_FACTOR / delayDen);
		
		scheduleSelf(this, SystemClock.uptimeMillis() + delay);
		invalidateSelf();
	}
	
	public void recycleBitmaps() {
		if (mBitmaps != null) {
			for (Bitmap bitmap : mBitmaps) {
				bitmap.recycle();
			}
			
			for (int i = 0; i < mBitmaps.length; i++) {
				mBitmaps[i] = null;
			}
		}
	}

	@Override
	public void draw(Canvas canvas) {
		if (VERBOSE) FLog.v("Current frame: %d", mCurrentFrame);
		
		if (mCurrentFrame <= 0) {
			drawBaseBitmap(canvas);
		} else {
			drawApng(mCurrentFrame, canvas);
		}
		
		if (mNumPlays > 0 && mCurrentLoop >= mNumPlays) {
			stop();
		}
		
		if (mNumPlays > 0 && mCurrentFrame == mNumFrames - 1) {
			mCurrentLoop++;
			if (VERBOSE) FLog.v("Loop count: %d/%d", mCurrentLoop, mNumPlays);
		}
		
		mCurrentFrame++;
	}

	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
	
	private void readApngInformation(Resources res) {
		mFctlList = new ArrayList<PngChunkFCTL>();
		
		File baseFile = new File(mImagePath);
		PngReaderApng reader = new PngReaderApng(baseFile);
		reader.end();
		
		List<PngChunk> pngChunks = reader.getChunksList().getChunks();
		PngChunk chunk = null;
		
		for (int i = 0; i < pngChunks.size(); i++) {
			chunk = pngChunks.get(i);
			
			if (chunk instanceof PngChunkACTL) {
				mNumFrames = ((PngChunkACTL) chunk).getNumFrames();
				FLog.d("numFrames: %d", mNumFrames);
				
				if (mNumFrames <= 0) {
					mNumPlays = ((PngChunkACTL) chunk).getNumPlays();
					FLog.d("numPlays: %d (media info)", mNumPlays);
				} else {
					FLog.d("numPlays: %d (user defined)", mNumPlays);
				}
				
			} else if (chunk instanceof PngChunkFCTL) {
				mFctlList.add((PngChunkFCTL) chunk);
			}
		}
		
		mBitmaps = new Bitmap[mFctlList.size()];
	}

	private void drawBaseBitmap(Canvas canvas) {
		if (mScaling == 0F) {
			int width = canvas.getWidth();
			int height = canvas.getHeight();
			
			if (VERBOSE) FLog.v("Canvas: %dx%d", width, height);
			
			float scalingByWidth = ((float) canvas.getWidth())/mBaseWidth;
			if (VERBOSE) FLog.v("scalingByWidth: %.2f", scalingByWidth);
			
			float scalingByHeight = ((float) canvas.getHeight())/mBaseHeight;
			if (VERBOSE) FLog.v("scalingByHeight: %.2f", scalingByHeight);
			
			mScaling = scalingByWidth <= scalingByHeight ? scalingByWidth : scalingByHeight;
			if (VERBOSE) FLog.v("mScaling: %.2f", mScaling);
		}
		
		RectF dst = new RectF(0, 0, mScaling * mBaseWidth, mScaling * mBaseHeight);
		canvas.drawBitmap(mBaseBitmap, null, dst, mPaint);
		
		if (mBitmaps != null) {
			mBitmaps[0] = mBaseBitmap;
		}
	}
	
	private void drawApng(int frameIndex, Canvas canvas) {
		if (mBitmaps != null && mBitmaps.length > frameIndex) {
			Bitmap bitmap = mBitmaps[frameIndex] == null ? createBitmap(frameIndex) : mBitmaps[frameIndex];
			
			if (bitmap != null && mBitmaps[frameIndex] == null) {
				mBitmaps[frameIndex] = bitmap;
			}
			
			RectF dst = new RectF(0, 0, mScaling * bitmap.getWidth(), mScaling * bitmap.getHeight());
			
			canvas.drawBitmap(bitmap, null, dst, mPaint);
		}
	}

	private Bitmap createBitmap(int frameIndex) {
		if (VERBOSE) FLog.v("ENTER");
		File baseFile = new File(mImagePath);
		
		Bitmap baseBitmap = null;
		
		PngChunkFCTL previousChunk = frameIndex > 0 ? mFctlList.get(frameIndex - 1) : null;
		
		if (previousChunk != null) {
			byte disposeOp = previousChunk.getDisposeOp();
			int offsetX = previousChunk.getxOff();
			int offsetY = previousChunk.getyOff();
			
			Bitmap tempBitmap = null;
			Canvas tempCanvas = null;
			String tempPath = null;
			Bitmap tempFrameBitmap = null;
			
			switch (disposeOp) {
			case PngChunkFCTL.APNG_DISPOSE_OP_NONE:
				// Get bitmap from the previous frame
				baseBitmap = frameIndex > 0 ? mBitmaps[frameIndex - 1] : null;
				break;
				
			case PngChunkFCTL.APNG_DISPOSE_OP_BACKGROUND:
				// Get bitmap from the previous frame but the drawing region is needed to be cleared
				baseBitmap = frameIndex > 0 ? mBitmaps[frameIndex - 1] : null;
				
				tempPath = new File(mWorkingPath, ApngExtractFrames.getFileName(baseFile, frameIndex - 1)).getPath();
				tempFrameBitmap = mImageLoader.loadImageSync(Uri.fromFile(new File(tempPath)).toString(), mDisplayImageOptions);
				
				tempBitmap = Bitmap.createBitmap(mBaseWidth, mBaseHeight, Bitmap.Config.ARGB_8888);
				tempCanvas = new Canvas(tempBitmap);
				tempCanvas.drawBitmap(baseBitmap, 0, 0, null);
				
				tempCanvas.clipRect(offsetX, offsetY, offsetX + tempFrameBitmap.getWidth(), offsetY + tempFrameBitmap.getHeight());
				tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
				tempCanvas.clipRect(0, 0, mBaseWidth, mBaseHeight);
				
				baseBitmap = tempBitmap;
				break;
				
			case PngChunkFCTL.APNG_DISPOSE_OP_PREVIOUS:
				if (frameIndex > 1) {
					PngChunkFCTL tempPngChunk = null;
					
					for (int i = frameIndex - 2; i >= 0; i--) {
						tempPngChunk = mFctlList.get(i);
						int tempDisposeOp = tempPngChunk.getDisposeOp();
						int tempOffsetX = tempPngChunk.getxOff();
						int tempOffsetY = tempPngChunk.getyOff();
						
						tempPath = new File(mWorkingPath, ApngExtractFrames.getFileName(baseFile, i)).getPath();
						tempFrameBitmap = mImageLoader.loadImageSync(Uri.fromFile(new File(tempPath)).toString(), mDisplayImageOptions);
						
						if (tempDisposeOp != PngChunkFCTL.APNG_DISPOSE_OP_PREVIOUS) {
							
							if (tempDisposeOp == PngChunkFCTL.APNG_DISPOSE_OP_NONE) {
								baseBitmap = mBitmaps[i];
								
							} else if (tempDisposeOp == PngChunkFCTL.APNG_DISPOSE_OP_BACKGROUND) {
								tempBitmap = Bitmap.createBitmap(mBaseWidth, mBaseHeight, Bitmap.Config.ARGB_8888);
								tempCanvas = new Canvas(tempBitmap);
								tempCanvas.drawBitmap(mBitmaps[i], 0, 0, null);
								
								tempCanvas.clipRect(tempOffsetX, tempOffsetY, tempOffsetX + tempFrameBitmap.getWidth(), tempOffsetY + tempFrameBitmap.getHeight());
								tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
								tempCanvas.clipRect(0, 0, mBaseWidth, mBaseHeight);
								
								baseBitmap = tempBitmap;
							}
							break;
						}
					}
				}
				break;
			}			
		}
		
		String path = new File(mWorkingPath, ApngExtractFrames.getFileName(baseFile, frameIndex)).getPath();
		Bitmap currentFrameBitmap = mImageLoader.loadImageSync(Uri.fromFile(new File(path)).toString(), mDisplayImageOptions);
		
		Bitmap redrawnBitmap = null;
		
		PngChunkFCTL chunk = mFctlList.get(frameIndex);
		
		byte blendOp = ((PngChunkFCTL) chunk).getBlendOp();
		int offsetX = ((PngChunkFCTL) chunk).getxOff();
		int offsetY = ((PngChunkFCTL) chunk).getyOff();
		
		redrawnBitmap = redrawPng(mBaseWidth, mBaseHeight, offsetX, offsetY, blendOp, currentFrameBitmap, baseBitmap);
		
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
	
	public static interface ApngCallback {
		public void onStart();
		public void onStop();
	}
	
}
