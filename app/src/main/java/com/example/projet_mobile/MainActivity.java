package com.example.projet_mobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int READ_PERMISSION_REQUEST = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(new GalleryView(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},READ_PERMISSION_REQUEST);
        } else {
            getImages();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case READ_PERMISSION_REQUEST:
                break;
        }

        if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getImages();
        }
    }

    private List<String> getImages(){
        List<String> imagesPath = new ArrayList<>();

        ContentResolver cr = getContentResolver();
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor data = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projection,null,null,null);

        for (data.moveToLast(); !data.isBeforeFirst(); data.moveToPrevious()){
            String photoPath = data.getString(0);
            imagesPath.add(photoPath);
        }
        return imagesPath;
    }
}
