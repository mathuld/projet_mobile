/**
 * MainActivity.java
 * Université du Québec à Chicoutimi - Automne 2019
 * Programmation Mobile
 * TP
 * Romain GUILLOT - Tanguy SAUTON - Mathieu VINCENT
 */
package com.example.projet_mobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activité principale
 * Chargée d'instancier la GalleryView, de demander les permissions
 * d'accès à la mémoire de stockage et d'obtenir la liste de toutes
 * les photos de l'appareil
 */
public class MainActivity extends AppCompatActivity {

    private static final int READ_PERMISSION_REQUEST = 0x01;

    private GalleryView mGalleryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGalleryView = new GalleryView(this);
        setContentView(mGalleryView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        /* Obtention des permissions d'accès à la mémoire de stockage */
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            /* Demande de permission en runtime */
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},READ_PERMISSION_REQUEST);
        } else {
            /* Permission déjà accordée */
            initImages();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case READ_PERMISSION_REQUEST:
                /* Vérification du résultat de la demande de permission */
                if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    /* Permission accordée, accès à la mémoire de stockage */
                    initImages();
                } else {
                    /* Permission accordée, fermeture de l'activité */
                    finish();
                }
                break;
        }
    }

    /**
     * Initialise les images de la GalleryView
     */
    private void initImages() {
        mGalleryView.setImages(getImages());
    }

    /**
     * Récupère les chemins des fichiers photos présents sur l'appareil
     * @return Liste des chemins vers les fichiers images
     */
    private List<String> getImages(){
        List<String> imagesPath = new ArrayList<>();

        ContentResolver cr = getContentResolver();
        /* Query au mediastore et récupération en particulier du champ DATA ()*/
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor data = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projection,null,null,null);

        /* Parcours des données et ajout à la list des fichiers images */
        for (data.moveToLast(); !data.isBeforeFirst(); data.moveToPrevious()){
            String photoPath = data.getString(0);
            imagesPath.add(photoPath);
        }

        data.close();

        return imagesPath;
    }
}
