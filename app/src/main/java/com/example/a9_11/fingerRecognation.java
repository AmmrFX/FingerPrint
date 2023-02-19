package com.example.a9_11;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class fingerRecognation extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private int photoCount = 0;
    Button capture;
    TextView count;
    EditText NatId;
    String NationalID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_recognation);
        capture = findViewById(R.id.button_open_close2);
        count = findViewById(R.id.count);
        NatId = findViewById(R.id.NatId);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NationalID = NatId.getText().toString();
                capturePhotos();
            }
        });
    }

    // Call this method to start capturing photos
    private void capturePhotos() {
//        for (int i = 0; i < 3; i++) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
//        }
    }

    // This method will be called when the camera app returns a result

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Encode the photo to Base64
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            String encodedImage = encodeToBase64(imageBitmap);

            DbSetup db = new DbSetup();
            // If we've captured all 6 photos, do something with them
            if (photoCount == 5) {
                db.savePhotoToDatabase(encodedImage,NationalID, true); // Mark the last photo as "true"
            } else {
                db.savePhotoToDatabase(encodedImage,NationalID ,false);
            }
            photoCount++;
            count.setText((""+ photoCount));
        }
    }

    private String encodeToBase64(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // This method saves a photo to a file
    private void savePhotoToFile(Bitmap imageBitmap) {
        // Add your own code to save the photo to a file
        // For example:
        File file = new File(getExternalFilesDir(null), "photo_" + photoCount + ".jpg");
        try (OutputStream outputStream = new FileOutputStream(file)) {
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // This method is called when all 6 photos have been captured
    private void doSomethingWithPhotos() {
        // Add your own code to do something with the photos
        // For example, display them in an image gallery:
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + getExternalFilesDir(null)), "image/*");
        startActivity(intent);
    }

}