package com.example.projet_mobile;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.text.style.ImageSpan;
import android.util.SparseArray;
import android.view.GestureDetector;

import android.graphics.Rect;
import android.os.AsyncTask;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import androidx.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class GalleryView extends View {

    private float mScale = 1f;
    private int mNbColumns = 3;
    private int mScrollOffset;

    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mScrollGestureDetector;

    private static int MAX_PICTURES = 1000;
    private Paint[] mColors;

    private List<String> imagesPath;

    private ImagesCache mCache;




    public GalleryView(Context context) {
        super(context);
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGesture());

        mScrollGestureDetector = new GestureDetector(context, new ScrollGesture());

        mCache = new ImagesCache(50);

        Random r = new Random();
        mColors = new Paint[MAX_PICTURES];
        for (int i = 0; i < mColors.length; i++) {
            mColors[i] = new Paint();
            mColors[i].setColor(Color.argb(255, r.nextInt(256), r.nextInt(256), r.nextInt(256)));
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        System.out.println("DRAW");
        int cHeight = getHeight();
        int cWidth = getWidth();
        //System.out.println(canvas.getHeight() + ":" + canvas.getWidth());

        int imgWidth = cWidth / mNbColumns;
        int imgHeight = imgWidth;


        int firstLineImg = mScrollOffset / imgHeight;
        System.out.println(firstLineImg);

        /* Total number of columns/rows displayed (even truncated rows) */
        int cCol = mNbColumns;
        int cRow = cHeight / imgHeight + 1;


        int xOffset;
        int yOffset;
        int imgOffset;
        for (int j = 0; j < cRow; j++) {
            yOffset = j * imgHeight;
            for (int i = 0; i < cCol; i++) {
                xOffset = i * imgWidth;
                imgOffset = (firstLineImg * mNbColumns) + j * mNbColumns + i;
                if (imgOffset < MAX_PICTURES) {
//                    canvas.drawRect(xOffset,yOffset,xOffset + imgWidth,yOffset + imgHeight, mColors[imgOffset]);
                    Bitmap bitmap = getCompressImage(imgOffset, imgHeight);
                    if (bitmap != null)
                        canvas.drawBitmap(bitmap, xOffset, yOffset, null);

                }
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        mScrollGestureDetector.onTouchEvent(event);

        /*switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_CANCEL:
        }*/
        return true;

    }

    public class ScrollGesture extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mScrollOffset += distanceY;
            if (mScrollOffset < 0) {
                mScrollOffset = 0;
            }
            System.out.println("Scroll offset = " + mScrollOffset);
            invalidate();
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    public class ScaleGesture extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            //System.out.println(detector.getScaleFactor());

            mScale /= detector.getScaleFactor();

            mScale = Math.min(mScale, 7);
            mScale = Math.max(mScale, 1);

            mNbColumns = (int) mScale;
            System.out.println(mNbColumns);

            invalidate();
            return true;
        }
    }

    public void setImages(List<String> imagesPath) {
        this.imagesPath = imagesPath;
    }

    @Nullable
    public Bitmap getCompressImage(int index, int size) {
        if (imagesPath == null || index >= imagesPath.size())
            return null;


        Bitmap bitmap = mCache.getEntry(index);

        String uri = imagesPath.get(index);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri, options);
        float aspectRatio = options.outWidth / (float) options.outHeight;


        if (bitmap == null) {

            options.inJustDecodeBounds = false;
//            options.inSampleSize = calculateInSampleSize(options, size, size);
            options.inSampleSize = options.outWidth / size;

            bitmap = BitmapFactory.decodeFile(uri, options);
            mCache.addEntry(index, bitmap);
        }

        return Bitmap.createScaledBitmap(bitmap, size, (int) (size / aspectRatio), true);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


//    private Bitmap getBitmap(String path) {
//        ContentResolver mContentResolver = getContext().getContentResolver();
//
//        Uri uri = Uri.fromFile(new File(path));
//
//        InputStream in = null;
//        try {
//            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
//            in = mContentResolver.openInputStream(uri);
//
//            // Decode image size
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
//            BitmapFactory.decodeStream(in, null, options);
//            in.close();
//
//
//            int scale = 1;
//            while ((options.outWidth * options.outHeight) * (1 / Math.pow(scale, 2)) >
//                    IMAGE_MAX_SIZE) {
//                scale++;
//            }
//
//            Bitmap resultBitmap = null;
//            in = mContentResolver.openInputStream(uri);
//            if (scale > 1) {
//                scale--;
//                // scale to max possible inSampleSize that still yields an image
//                // larger than target
//                options = new BitmapFactory.Options();
//                options.inSampleSize = scale;
//                resultBitmap = BitmapFactory.decodeStream(in, null, options);
//
//                // resize to desired dimensions
//                int height = resultBitmap.getHeight();
//                int width = resultBitmap.getWidth();
//
//
//                double y = Math.sqrt(IMAGE_MAX_SIZE
//                        / (((double) width) / height));
//                double x = (y / height) * width;
//
//                Bitmap scaledBitmap = Bitmap.createScaledBitmap(resultBitmap, (int) x,
//                        (int) y, true);
//                resultBitmap.recycle();
//                resultBitmap = scaledBitmap;
//
//                System.gc();
//            } else {
//                resultBitmap = BitmapFactory.decodeStream(in);
//            }
//            in.close();
//            return resultBitmap;
//        } catch (IOException e) {
//            return null;
//        }
//    }
}