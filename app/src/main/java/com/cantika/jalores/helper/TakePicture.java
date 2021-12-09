package com.cantika.jalores.helper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TakePicture extends AppCompatActivity {

    public final int REQUEST_CAMERA = 7777;
    Uri fileUri;
    String mCurrentPhotoPath;
    Bitmap bmp;
    private boolean cek = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            fileUri = FileProvider.getUriForFile( TakePicture.this,
                    getApplicationContext().getPackageName() + ".provider", createImageFile());

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            cek= true;
            startActivityForResult(intent, REQUEST_CAMERA);
        } catch (Exception e) {

            cek= false;
            startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CAMERA);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            try {
                if(cek){
                    Uri imageUri = Uri.parse(mCurrentPhotoPath);
                    File file = new File(imageUri.getPath());

                    InputStream ims = null;
                    try {
                        ims = new FileInputStream(file);

                        bmp = BitmapFactory.decodeStream(ims);
                        bmp= getResizedBitmap(bmp, 300);

                        Intent intent = new Intent("bitmap");
                        intent.putExtra("valueBitmap", bmp);
                        LocalBroadcastManager.getInstance(TakePicture.this).sendBroadcast(intent);
                        finish();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else{
                    bmp = (Bitmap) data.getExtras().get("data");
                    bmp= getResizedBitmap(bmp, 300);

                    Intent intent = new Intent("bitmap");
                    intent.putExtra("valueBitmap", bmp);
                    LocalBroadcastManager.getInstance(TakePicture.this).sendBroadcast(intent);
                    finish();
                }
            } catch (Exception e) {
                e.getStackTrace();
            }
        } else {
            finish();
        }
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}
