package com.example.projet_mobile;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.SparseArray;

import androidx.annotation.Nullable;

public class ImagesCache {

    private SparseArray<BitmapDrawable> cachedImages = new SparseArray<>();
    private int MAX_CACHE_IMAGES = 100;

    public ImagesCache(int cacheSize) {
        MAX_CACHE_IMAGES = cacheSize;
    }

    public void addEntry(int key, BitmapDrawable bitmap) {
        cachedImages.put(key, bitmap);
        int cacheSize = cachedImages.size();
        if (cacheSize > MAX_CACHE_IMAGES) {
            cachedImages.removeAt(key > cacheSize / 2 ? 0 : cacheSize - 1);
        }
    }

    @Nullable
    public BitmapDrawable getEntry(int key) {
        return cachedImages.get(key);
    }
}
