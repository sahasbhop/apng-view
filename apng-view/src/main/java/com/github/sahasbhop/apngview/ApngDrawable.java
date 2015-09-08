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
    private static final boolean DEBUG = true;
	private static final float DELAY_FACTOR = 1000F;
    private final Uri sourceUri;

    private ApngCallback apngCallback;
	private ArrayList<PngChunkFCTL> fctlArrayList;
	private Bitmap baseBitmap;
	private Bitmap[] bitmapArray;
	private DisplayImageOptions displayImageOptions;
	private ImageLoader imageLoader;
	private Paint paint;
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
    private File baseFile;

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
        sourceUri = uri;

		imageLoader = ImageLoader.getInstance();
		
		baseBitmap = bitmap;
		baseWidth = bitmap.getWidth();
		baseHeight = bitmap.getHeight();

        if (DEBUG) FLog.d("Uri: %s", sourceUri);
        if (DEBUG) FLog.d("Bitmap size: %dx%d", baseWidth, baseHeight);
	}

	@SuppressWarnings("unused")
	public void setCallback(ApngCallback callback) {
		apngCallback = callback;
	}

	@SuppressWarnings("unused")
	public int getNumPlays() {
		return numPlays;
	}

    @SuppressWarnings("unused")
	public void setNumPlays(int numPlays) {
		this.numPlays = numPlays;
	}

	@SuppressWarnings("unused")
	public int getNumFrames() {
		return numFrames;
	}
	
	public void prepare() {
        String imagePath = getImagePathFromUri();
        if (imagePath == null) return;

        baseFile = new File(imagePath);

		if (DEBUG) FLog.d("Extracting PNGs..");
        ApngExtractFrames.process(baseFile);
		if (DEBUG) FLog.d("Extracting complete");
		
		if (DEBUG) FLog.d("Read APNG information..");
		readApngInformation(baseFile);

        isPrepared = true;
	}

    private String getImagePathFromUri() {
        if (sourceUri == null) return null;

        String imagePath = null;

        try {
            String filename = sourceUri.getLastPathSegment();

            File file = new File(workingPath, filename);

            if (!file.exists()) {
                if (VERBOSE) FLog.v("Copy file from %s to %s", sourceUri.getPath(), file.getPath());
                FileUtils.copyFile(new File(sourceUri.getPath()), file);
            }

            imagePath = file.getPath();

        } catch (Exception e) {
            FLog.e("Error: %s", e.toString());
        }

        return imagePath;
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

        if (apngCallback != null) {
            apngCallback.onStart();
        }
	}

	public void recycleBitmaps() {
		if (bitmapArray == null) return;

        for (int i = 1; i < bitmapArray.length; i++) {
            if (bitmapArray[i] == null) continue;
            bitmapArray[i].recycle();
            bitmapArray[i] = null;
        }
	}

	@Override
	public void draw(Canvas canvas) {
		if (VERBOSE) FLog.v("Current frame: %d", currentFrame);
		
		if (currentFrame <= 0) {
			drawBaseBitmap(canvas);
		} else {
			drawAnimateBitmap(canvas, currentFrame);
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
	
	private void readApngInformation(File baseFile) {
		fctlArrayList = new ArrayList<>();
		
		PngReaderApng reader = new PngReaderApng(baseFile);
		reader.end();
		
		List<PngChunk> pngChunks = reader.getChunksList().getChunks();
		PngChunk chunk;
		
		for (int i = 0; i < pngChunks.size(); i++) {
			chunk = pngChunks.get(i);
			
			if (chunk instanceof PngChunkACTL) {
				numFrames = ((PngChunkACTL) chunk).getNumFrames();
				if (DEBUG) FLog.d("numFrames: %d", numFrames);
				
				if (numPlays > 0) {
                    if (DEBUG) FLog.d("numPlays: %d (user defined)", numPlays);
				} else {
                    numPlays = ((PngChunkACTL) chunk).getNumPlays();
                    if (DEBUG) FLog.d("numPlays: %d (media info)", numPlays);
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
	
	private void drawAnimateBitmap(Canvas canvas, int frameIndex) {
		if (bitmapArray != null && bitmapArray.length > frameIndex) {
			if (bitmapArray[frameIndex] == null) {
				bitmapArray[frameIndex] = createAnimateBitmap(frameIndex);
			}

			if (bitmapArray[frameIndex] == null) return;

			RectF dst = new RectF(
					0, 0,
					mScaling * bitmapArray[frameIndex].getWidth(),
					mScaling * bitmapArray[frameIndex].getHeight());

			canvas.drawBitmap(bitmapArray[frameIndex], null, dst, paint);
		}
	}

	private Bitmap createAnimateBitmap(int frameIndex) {
		if (VERBOSE) FLog.v("ENTER");
        Bitmap bitmap = null;

		PngChunkFCTL previousChunk = frameIndex > 0 ? fctlArrayList.get(frameIndex - 1) : null;
		
		if (previousChunk != null) {
            bitmap = handleDisposeOperation(frameIndex, baseFile, previousChunk);
		}
		
		String path = new File(workingPath, ApngExtractFrames.getFileName(baseFile, frameIndex)).getPath();
        Bitmap frameBitmap = imageLoader.loadImageSync(Uri.fromFile(new File(path)).toString(), displayImageOptions);
		
		Bitmap redrawnBitmap;
		
		PngChunkFCTL chunk = fctlArrayList.get(frameIndex);
		
		byte blendOp = chunk.getBlendOp();
		int offsetX = chunk.getxOff();
		int offsetY = chunk.getyOff();
		
		redrawnBitmap = handleBlendingOperation(offsetX, offsetY, blendOp, frameBitmap, bitmap);
		
		if (VERBOSE) FLog.v("EXIT");
		return redrawnBitmap;
	}

    private Bitmap handleDisposeOperation(int frameIndex, File baseFile, PngChunkFCTL previousChunk) {
        Bitmap bitmap = null;

        byte disposeOp = previousChunk.getDisposeOp();
        int offsetX = previousChunk.getxOff();
        int offsetY = previousChunk.getyOff();

        Canvas tempCanvas;
        Bitmap frameBitmap;
        Bitmap tempBitmap;
        String tempPath;

        switch (disposeOp) {
        case PngChunkFCTL.APNG_DISPOSE_OP_NONE:
            // Get bitmap from the previous frame
            bitmap = frameIndex > 0 ? bitmapArray[frameIndex - 1] : null;
            break;

        case PngChunkFCTL.APNG_DISPOSE_OP_BACKGROUND:
            // Get bitmap from the previous frame but the drawing region is needed to be cleared
            bitmap = frameIndex > 0 ? bitmapArray[frameIndex - 1] : null;
            if (bitmap == null) break;

            tempPath = new File(workingPath, ApngExtractFrames.getFileName(baseFile, frameIndex - 1)).getPath();
            frameBitmap = imageLoader.loadImageSync(Uri.fromFile(new File(tempPath)).toString(), displayImageOptions);

            if (VERBOSE) FLog.v("Create a new bitmap");
            tempBitmap = Bitmap.createBitmap(baseWidth, baseHeight, Bitmap.Config.ARGB_8888);
            tempCanvas = new Canvas(tempBitmap);
            tempCanvas.drawBitmap(bitmap, 0, 0, null);

            tempCanvas.clipRect(offsetX, offsetY, offsetX + frameBitmap.getWidth(), offsetY + frameBitmap.getHeight());
            tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            tempCanvas.clipRect(0, 0, baseWidth, baseHeight);

            bitmap = tempBitmap;
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
                    frameBitmap = imageLoader.loadImageSync(Uri.fromFile(new File(tempPath)).toString(), displayImageOptions);

                    if (tempDisposeOp != PngChunkFCTL.APNG_DISPOSE_OP_PREVIOUS) {

                        if (tempDisposeOp == PngChunkFCTL.APNG_DISPOSE_OP_NONE) {
                            bitmap = bitmapArray[i];

                        } else if (tempDisposeOp == PngChunkFCTL.APNG_DISPOSE_OP_BACKGROUND) {
                            if (VERBOSE) FLog.v("Create a new bitmap");
                            tempBitmap = Bitmap.createBitmap(baseWidth, baseHeight, Bitmap.Config.ARGB_8888);
                            tempCanvas = new Canvas(tempBitmap);
                            tempCanvas.drawBitmap(bitmapArray[i], 0, 0, null);

                            tempCanvas.clipRect(tempOffsetX, tempOffsetY, tempOffsetX + frameBitmap.getWidth(), tempOffsetY + frameBitmap.getHeight());
                            tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                            tempCanvas.clipRect(0, 0, baseWidth, baseHeight);

                            bitmap = tempBitmap;
                        }
                        break;
                    }
                }
            }
            break;
        }
        return bitmap;
    }

    /**
     * Process Blending operation, and handle a final draw for this frame
     */
	private Bitmap handleBlendingOperation(
            int offsetX, int offsetY, byte blendOp,
            Bitmap frameBitmap, Bitmap baseBitmap) {

        if (VERBOSE) FLog.v("Create a new bitmap");
		Bitmap redrawnBitmap = Bitmap.createBitmap(baseWidth, baseHeight, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(redrawnBitmap);

		if (baseBitmap != null) {
			canvas.drawBitmap(baseBitmap, 0, 0, null);
			
			if (blendOp == PngChunkFCTL.APNG_BLEND_OP_SOURCE) {
				canvas.clipRect(offsetX, offsetY, offsetX + frameBitmap.getWidth(), offsetY + frameBitmap.getHeight());
				canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
				canvas.clipRect(0, 0, baseWidth, baseHeight);
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
