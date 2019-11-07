package com.example.projet_mobile;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.style.ImageSpan;
import android.util.SparseArray;
import android.view.GestureDetector;
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

import java.util.List;

public class GalleryView extends View {

    private float mScale = 3f;
    private int mNbColumns = (int) mScale;
    private int mNbRows;
    private int mScrollOffset;
    private int mPictureSize;

    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mScrollGestureDetector;

    private static int MAX_PICTURES = 1000;
    private Paint[] mColors;

    private List<String> imagesPath;

    private Paint mPaint = new Paint(Color.BLACK);
    private ImagesCache mCache;
    private BitmapCache mBitmapCache;


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
        mNbColumns = 3;
        mNbRows = MAX_PICTURES/mNbColumns;
        mPictureSize = getWidth()/mNbColumns;
        mBitmapCache = new BitmapCache();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        System.out.println("DRAW");
        int cHeight = getHeight();
        int cWidth = getWidth();
        mPictureSize = cWidth/mNbColumns;
        //System.out.println(canvas.getHeight() + ":" + canvas.getWidth());


        int firstLineImg = mScrollOffset/mPictureSize;
        int imgWidth = cWidth / mNbColumns;
        int imgHeight = imgWidth;

        System.out.println(firstLineImg);

        /* Total number of columns/rows displayed (even truncated rows) */
        int cCol = mNbColumns;
        int cRow = cHeight/mPictureSize + 2;



        int xOffset;
        int yOffset;
        int imgOffset;

        for (int j = 0; j < cRow; j++) {
            yOffset = j * mPictureSize - (mScrollOffset % mPictureSize);
            for (int i = 0; i < cCol; i++) {
                xOffset = i * mPictureSize;
                imgOffset = (firstLineImg * mNbColumns) + j * mNbColumns + i;
                if (imgOffset < MAX_PICTURES) {
                    //canvas.drawRect(xOffset,yOffset,xOffset + mPictureSize,yOffset + mPictureSize, mColors[imgOffset]);
                    //BitmapDrawable bitmap = mCache.getEntry(imgOffset);
                    //if (bitmap == null){
                    //    loadCompressImage(imgOffset, mPictureSize);
                    //} else {
                    //    bitmap.setBounds(xOffset,yOffset,xOffset+mPictureSize,yOffset+mPictureSize);
                    //    bitmap.draw(canvas);
                    //}

                    Bitmap bitmap = mBitmapCache.getBitmapFromMemCache(imgOffset);
                    if (bitmap == null){
                        loadCompressImage(imgOffset,mPictureSize);
                    } else {
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(),bitmap);
                        bitmapDrawable.setBounds(xOffset,yOffset,xOffset+mPictureSize,yOffset+mPictureSize);
                        bitmapDrawable.draw(canvas);
                    }
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
            /* Stop scrolling before first picture and after last picture */
            if (mScrollOffset < 0) {
                mScrollOffset = 0;
            } else {
                /* Tricky, they can be cases, either the last line of pictures is full or either it's not */
                if (MAX_PICTURES%mNbRows == 0){ /* Last line is full */
                    if (mScrollOffset > (mNbRows * mPictureSize - getHeight())){
                        mScrollOffset = (mNbRows * mPictureSize - getHeight());
                    }
                } else { /* Last line is not full*/
                    if (mScrollOffset > ((mNbRows + 1) * mPictureSize - getHeight())){
                        mScrollOffset = ((mNbRows + 1) * mPictureSize - getHeight());
                    }
                }

            }
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
            mScale /= detector.getScaleFactor();

            mScale = Math.min(mScale, 7);
            mScale = Math.max(mScale, 1);

            mNbColumns = (int) mScale;
            mNbRows = MAX_PICTURES/mNbColumns;

            mPictureSize = getWidth()/mNbColumns;


            invalidate();
            return true;
        }
    }

    public void setImages(List<String> imagesPath) {
        this.imagesPath = imagesPath;
        MAX_PICTURES = imagesPath.size();
        invalidate();
    }

    @Nullable
    public void loadCompressImage(int index, int size) {
        if (imagesPath == null || index >= imagesPath.size())
            return;


        //if (bitmap == null) {
        Thread tx = new Thread(() -> {
                String uri = imagesPath.get(index);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(uri, options);

                float aspectRatio = options.outWidth / (float) options.outHeight;

                options.inJustDecodeBounds = false;

                options.inSampleSize = calculateInSampleSize(options, size, size);
                //options.inSampleSize = options.outWidth / size;
                Bitmap tmpBitmap = BitmapFactory.decodeFile(uri, options);

                Bitmap finalBitmap = Bitmap.createScaledBitmap(tmpBitmap, size, (int) (size / aspectRatio), true);
                //mCache.addEntry(index, new BitmapDrawable(getResources(),tmpBitmap));

                mBitmapCache.addBitmapToMemoryCache(index,finalBitmap);
                invalidate();
            }
        );
        tx.start();
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