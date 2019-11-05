package com.example.projet_mobile;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class GalleryView extends View {

    private float mScale = 1f;
    private int mNbColumns = 3;

    private ScaleGestureDetector mScaleGestureDetector;

    public GalleryView(Context context){
        super(context);
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGesture());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_MOVE:

                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:

                invalidate();
                break;
        }
        return true;
    }

    public class ScaleGesture extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScale *= detector.getScaleFactor();

            mScale = Math.max(mScale, 7);


            System.out.println(mNbColumns);

            invalidate();
            return true;
        }
    }


}
