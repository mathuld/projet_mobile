package com.example.projet_mobile;

import android.content.Context;
import android.graphics.Canvas;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.Random;

public class GalleryView extends View {

    private float mScale = 1f;
    private int mNbColumns = 3;
    private int mScrollOffset;

    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mScrollGestureDetector;

    private static int MAX_PICTURES = 1000;
    private Paint[] mColors;

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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int cHeight = getHeight();
        int cWidth = getWidth();
        //System.out.println(canvas.getHeight() + ":" + canvas.getWidth());

        int imgWidth = cWidth/mNbColumns;
        int imgHeight = imgWidth;


        int firstLineImg = mScrollOffset/imgHeight;
        System.out.println(firstLineImg);

        /* Total number of columns/rows displayed (even truncated rows) */
        int cCol = mNbColumns;
        int cRow = cHeight/imgHeight + 1;


        int xOffset;
        int yOffset;
        int imgOffset;
        for (int j = 0; j < cRow ; j++){
            yOffset = j * imgHeight;
            for (int i  = 0; i < cCol; i++){
                xOffset = i * imgWidth;
                imgOffset = (firstLineImg*mNbColumns) + j * mNbColumns + i;
                if (imgOffset < MAX_PICTURES) {
                    canvas.drawRect(xOffset,yOffset,xOffset + imgWidth,yOffset + imgHeight, mColors[imgOffset]);
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
            if (mScrollOffset < 0) { mScrollOffset = 0;}
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
            mScale = Math.max(mScale,1);

            mNbColumns = (int) mScale;
            System.out.println(mNbColumns);

            invalidate();
            return true;
        }
    }
}
