package com.example.projet_mobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Random;
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


    public GalleryView(Context context){
        super(context);
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGesture());

        
        mScrollGestureDetector = new GestureDetector(context,new ScrollGesture());


        Random r = new Random();
        mColors = new Paint[MAX_PICTURES];
        for (int i = 0; i < mColors.length; i++){
            mColors[i] = new Paint();
            mColors[i].setColor(Color.argb(255, r.nextInt(256), r.nextInt(256), r.nextInt(256)));
        }
        mNbColumns = 3;
        mNbRows = MAX_PICTURES/mNbColumns;
        mPictureSize = getWidth()/mNbColumns;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int cHeight = getHeight();
        int cWidth = getWidth();
        mPictureSize = cWidth/mNbColumns;
        //System.out.println(canvas.getHeight() + ":" + canvas.getWidth());


        int firstLineImg = mScrollOffset/mPictureSize;
        System.out.println(firstLineImg);

        /* Total number of columns/rows displayed (even truncated rows) */
        int cCol = mNbColumns;
        int cRow = cHeight/mPictureSize + 2;


        int xOffset;
        int yOffset;
        int imgOffset;
        for (int j = 0; j < cRow ; j++){
            yOffset = j * mPictureSize - (mScrollOffset % mPictureSize);
            for (int i  = 0; i < cCol; i++){
                xOffset = i * mPictureSize;
                imgOffset = (firstLineImg*mNbColumns) + j * mNbColumns + i;
                if (imgOffset < MAX_PICTURES) {
                    canvas.drawRect(xOffset,yOffset,xOffset + mPictureSize,yOffset + mPictureSize, mColors[imgOffset]);
                    //canvas.drawBitmap(getCompressImage(imgOffset,imgHeight),xOffset,yOffset,mPaint);
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
            mScale = Math.max(mScale,1);

            mNbColumns = (int) mScale;
            mNbRows = MAX_PICTURES/mNbColumns;

            mPictureSize = getWidth()/mNbColumns;


            invalidate();
            return true;
        }
    }

    public void setImages(List<String> imagesPath) {
        this.imagesPath = imagesPath;
        //MAX_PICTURES = imagesPath.size();
        invalidate();
    }

    @Nullable
    public Bitmap getCompressImage(int index, int size) {
        if (imagesPath == null || imagesPath.size() < index)
            return null;

        String uri = imagesPath.get(index);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri, options);
        int imageWidth = options.outWidth;
//        int imageHeight = options.outHeight;

        BitmapFactory.Options options2 = new BitmapFactory.Options();
        options2.inSampleSize = size / imageWidth;
        Bitmap bitmap =  BitmapFactory.decodeFile(uri, options2);

        return  Bitmap.createScaledBitmap(bitmap, size, size, true);
    }
}
