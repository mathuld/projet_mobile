package com.example.projet_mobile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
        System.out.println(canvas.getHeight() + ":" + canvas.getWidth());

        canvas.drawRect(0,0,120,120, new Paint(Color.BLACK));
    }

    public class ScaleGesture extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            System.out.println(detector.getScaleFactor());
            invalidate();
            return true;
        }
    }
}
