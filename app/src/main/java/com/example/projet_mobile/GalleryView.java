package com.example.projet_mobile;

import android.content.Context;
import android.graphics.Canvas;
import android.view.ScaleGestureDetector;
import android.view.View;

public class GalleryView extends View {

    private ScaleGestureDetector mScaleGestureDetector;

    public GalleryView(Context context){
        super(context);
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGesture());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public class ScaleGesture extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            //mScale *= detector.getScaleFactor();
            //mPaint.setTextSize(mScale*mFontSize);
            invalidate();
            return true;
        }
    }
}
