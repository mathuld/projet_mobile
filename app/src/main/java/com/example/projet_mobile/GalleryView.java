/**
 * GalleryView.java
 * Université du Québec à Chicoutimi - Automne 2019
 * Programmation Mobile
 * TP
 * Romain GUILLOT - Tanguy SAUTON - Mathieu VINCENT
 */
package com.example.projet_mobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

import androidx.annotation.Nullable;

import java.util.List;

public class GalleryView extends View {

    private float mScale;
    private int mNbColumns;
    private int mNbRows;
    private int mScrollOffset;
    private int mPictureSize;

    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mScrollGestureDetector;



    /* Liste des images à afficher, par défaut, liste vide */
    private List<String> mImagesPath = new ArrayList<>();


    /* Système de cache */
    //private ImagesCache mCache;
    private BitmapCache mBitmapCache;


    public GalleryView(Context context) {
        super(context);
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGesture());

        mScrollGestureDetector = new GestureDetector(context, new ScrollGesture());

        /* Initialize cache */
        //mCache = new ImagesCache(50);
        mBitmapCache = new BitmapCache();

        /* Default zoom is set to 3 pictures by line */
        mScale = 3f;
        /* Nombre de colonnes*/
        mNbColumns = (int) mScale;
        /* Nombre de lignes au total */
        mNbRows = mImagesPath.size()/mNbColumns;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);;
        int cHeight = getHeight();
        int cWidth = getWidth();

        /* Picture size (square, height = width = mPictureSize) */
        mPictureSize = cWidth/mNbColumns;

        /* Index of the first image to be displayed */
        int firstLineImg = mScrollOffset/mPictureSize;

        /* Total number of columns/rows displayed (even truncated rows) */
        int cCol = mNbColumns;
        int cRow = cHeight/mPictureSize + 2;

        /* Offset and index of the bitmap to display */
        int xOffset, yOffset, imgOffset;

        for (int j = 0; j < cRow; j++) {
            yOffset = j * mPictureSize - (mScrollOffset % mPictureSize);
            for (int i = 0; i < cCol; i++) {
                xOffset = i * mPictureSize;
                imgOffset = (firstLineImg * mNbColumns) + j * mNbColumns + i;
                if (imgOffset < mImagesPath.size()) {

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
                /*Check if we can scroll down */
                if (mNbRows != 0){
                    /* Tricky, they can be cases, either the last line of pictures is full or either it's not */
                    if (mImagesPath.size()%mNbRows == 0){ /* Last line is full */
                        if (mScrollOffset > (mNbRows * mPictureSize - getHeight())){
                            mScrollOffset = (mNbRows * mPictureSize - getHeight());
                        }
                    } else { /* Last line is not full*/
                        if (mScrollOffset > ((mNbRows + 1) * mPictureSize - getHeight())){
                            mScrollOffset = ((mNbRows + 1) * mPictureSize - getHeight());
                        }
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
            float scale = mScale / detector.getScaleFactor();

            scale = Math.min(scale, 7);
            scale = Math.max(scale, 1);

            if (scale != mScale)
                mBitmapCache.clearCache();

            mScale = scale;

            mNbColumns = (int) mScale;
            mNbRows = mImagesPath.size()/mNbColumns;

            mPictureSize = getWidth()/mNbColumns;

            invalidate();
            return true;
        }
    }

    public void setImages(List<String> imagesPath) {
        this.mImagesPath = imagesPath;
        invalidate();
    }

    @Nullable
    public void loadCompressImage(int index, int size) {
        if (index < 0  || index >= mImagesPath.size())
            return;


        //if (bitmap == null) {
        Thread tx = new Thread(() -> {
                String uri = mImagesPath.get(index);
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
}