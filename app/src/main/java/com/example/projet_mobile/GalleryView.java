package com.example.projet_mobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class GalleryView extends View {

    private float mScale = 1f;
    private int mNbColumns = 3;

    private ScaleGestureDetector mScaleGestureDetector;

    private List<String> imagesPath;

    private static int NB_COMPRESSOR = 5;
    private Compressor[] mCompressors;


    public GalleryView(Context context){
        super(context);
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGesture());

        mCompressors = new Compressor[NB_COMPRESSOR];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        canvas.drawBitmap(getCompressImage(0, 500), null, new Rect(0,0,500,500), null);
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

    public void setImages(List<String> imagesPath) {
        this.imagesPath = imagesPath;
        invalidate();
    }

    @Nullable
    public Bitmap getCompressImage(int index, int size) {
        if (imagesPath == null || imagesPath.size() < index)
            return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeFile(imagesPath.get(index), options);

        return  Bitmap.createScaledBitmap(bitmap, size, size, true);
    }


}
