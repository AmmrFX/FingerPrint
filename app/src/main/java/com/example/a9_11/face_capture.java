package com.example.a9_11;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class face_capture extends AppCompatActivity {
    EditText birthdate, military_status, marital_status, rank, weapon, national_Id, name,address,religion,gender;
    Button btn_enumerate;
    Button btn_OpenClose,btn_DataBase;
    Button btn_acquire;
    Button validate;
    int code = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private int photoCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_capture);
        national_Id = findViewById(R.id.NatId);
        btn_OpenClose = findViewById(R.id.button_open_close2);

        btn_enumerate = findViewById(R.id.button_enumerate2);
        //btn_acquire = findViewById(R.id.button_acquire2);
        btn_OpenClose = findViewById(R.id.button_open_close2);
        btn_DataBase = findViewById(R.id.Verify);
       // finger = findViewById(R.id.fingerImageView2);
        birthdate = findViewById(R.id.birthdate);
        military_status = findViewById(R.id.MilitaryStatus);
        marital_status = findViewById(R.id.martialStatus);
        rank = findViewById(R.id.Rank);
        weapon = findViewById(R.id.Weapon);
        national_Id = findViewById(R.id.NatId);
        name = findViewById(R.id.Name);
        address = findViewById(R.id.Address);
        religion = findViewById(R.id.Religion);
        gender = findViewById(R.id.Gender);


        btn_OpenClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( national_Id.getText().toString().isEmpty()){
                    Random rand = new Random();
                    code = rand.nextInt();
                    capturePhotos();
                }


            }
        });
        btn_DataBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String NatID = GetIdenticalFace(code);
                getFromCard(NatID);
            }
        });

    }

    private String GetIdenticalFace(int code) {
        DbSetup db = new DbSetup();
        return db.checkFaceRecognation(code);
    }

    private void getFromCard(String NatID) {
        try {
            if (! NatID.equals("00")) {

                DbSetup db = new DbSetup();
                Visitor visitor = db.GetCardHolderByNatID(NatID);
                // EditText birthdate, military_status, marital_status, rank, weapon, national_Id, name, address, religion, gender;
                birthdate.setText(visitor.getDate());
                military_status.setText(visitor.getMilitaryStatus());
                marital_status.setText(visitor.getDescription());
                rank.setText(visitor.getRank());
                weapon.setText(visitor.getWeapon());
                national_Id.setText(visitor.getNationalId());
                name.setText(visitor.getName());
                address.setText(visitor.getAddress());
                religion.setText(visitor.getReligion());
                gender.setText(visitor.getUnit());
            }
            else
            {
                Toast.makeText(this, "Not Detected", Toast.LENGTH_SHORT).show();
            }            }catch (Exception exception){

        }


    }
    private void capturePhotos() {

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

        }
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

                db.InsertToFaceSearch(encodedImage,code);

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
}