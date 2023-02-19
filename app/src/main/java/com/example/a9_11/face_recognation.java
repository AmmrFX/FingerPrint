package com.example.a9_11;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.morpho.android.usb.USBManager;
import com.morpho.morphosmart.sdk.CompressionAlgorithm;
import com.morpho.morphosmart.sdk.CustomInteger;
import com.morpho.morphosmart.sdk.DetectionMode;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.LatentDetection;
import com.morpho.morphosmart.sdk.MorphoDevice;
import com.morpho.morphosmart.sdk.MorphoImage;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class face_recognation extends AppCompatActivity {
    EditText LName1;
    EditText ID1;
    ImageView finger;
    Button btn_enumerate;
    Button btn_OpenClose;
    Button btn_acquire;
    Button btn_DataBase;
    MorphoDevice morphoDevice = new MorphoDevice();
    String m_sensor_name;
    EditText birthdate, military_status, marital_status, rank, weapon, national_Id, name, address, religion, gender;
    byte[] test = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognation);
        btn_enumerate = findViewById(R.id.button_enumerate2);
        btn_acquire = findViewById(R.id.button_acquire2);
        btn_OpenClose = findViewById(R.id.button_open_close2);
        btn_DataBase = findViewById(R.id.Verify);
        finger = findViewById(R.id.fingerImageView2);
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

        btn_enumerate.setOnClickListener(view -> {
            int ret = ErrorCodes.MORPHO_OK;
            CustomInteger L_Usb = new CustomInteger();
            ret = morphoDevice.initUsbDevicesNameEnum(L_Usb);
            if (ret == ErrorCodes.MORPHO_OK && L_Usb.getValueOf() > 0) {
                m_sensor_name = morphoDevice.getUsbDeviceName(0);
                btn_OpenClose.setEnabled(true);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(face_recognation.this);
                builder.setTitle("Please Connect To Finger Print Device First")
                        .setMessage("Device not detected")
                        .setCancelable(false)
                        .setPositiveButton("ok", (dialogInterface, i) ->
                                USBManager.getInstance().initialize(face_recognation.this,
                                        "com.morpho.morphosample.USB_ACTION", true));
                builder.create().show();
            }
        });


        btn_OpenClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button button = (Button) view;
                int ret = ErrorCodes.MORPHO_OK;
                if (button.getText() == "فتح") {
                    ret = morphoDevice.openUsbDevice(m_sensor_name, 0);

                    if (ret == ErrorCodes.MORPHO_OK) {
                        btn_acquire.setEnabled(true);
                        button.setText("إغلاق");
                    }
                } else {
                    try {
                        ret = morphoDevice.closeDevice();
                        if (ret == ErrorCodes.MORPHO_OK) {
                            finger.setImageDrawable(null);
                            button.setText("فتح");
                            btn_acquire.setEnabled(false);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        });
        btn_acquire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AcquireImage();
                String connectionUrl = "jdbc:jtds:sqlserver://192.168.1.245:1433;databaseName=kianMorphoTab;user=Sa;password=Moh@123";
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                ResultSet resultSet = null;
                finger = findViewById(R.id.fingerImageView2);
                finger.buildDrawingCache();
                Bitmap bitmap = finger.getDrawingCache();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
                byte[] image = stream.toByteArray();

                String img_str = Base64.encodeToString(image, Base64.DEFAULT);
                Random rand = new Random();
                code = rand.nextInt();
                InsertToDB(img_str);
            }
        });

        btn_DataBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String NatID = GetIdenticalFinger(code);
                getFromCard(NatID);
            }
        });
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

    private String GetIdenticalFinger(int code) {
        DbSetup db = new DbSetup();
        return (db.getFinger(code));
    }

    private void InsertToDB(String img_str) {
        DbSetup dbSetup = new DbSetup();
        try {
            Toast.makeText(face_recognation.this, "ok", Toast.LENGTH_SHORT).show();
            dbSetup.InsertImgValidation(img_str, code);
        } catch (Exception exception) {
            Toast.makeText(face_recognation.this, (CharSequence) exception, Toast.LENGTH_SHORT).show();
        }
    }

    int code = 0;

    public void AcquireImage() {
        finger.setImageDrawable(null);
        MorphoImage morphoImage = new MorphoImage();
        int ret = ErrorCodes.MORPHO_OK;
        ret = morphoDevice.getImage(100, 0, CompressionAlgorithm.MORPHO_NO_COMPRESS, 0,
                DetectionMode.MORPHO_ENROLL_DETECT_MODE.getValue(),
                LatentDetection.LATENT_DETECT_DISABLE, morphoImage, 0, null);
        if (ret == ErrorCodes.MORPHO_OK) {
            int ImageRow = morphoImage.getMorphoImageHeader().getNbRow();
            int ImageColumn = morphoImage.getMorphoImageHeader().getNbColumn();
            Bitmap bitmap = Bitmap.createBitmap(ImageColumn, ImageRow, Bitmap.Config.ALPHA_8);
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(morphoImage.getImage(), 0, morphoImage.getImage().length));
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] image = stream.toByteArray();
//            Bitmap bitmap2=Bitmap.createScaledBitmap(bitmap, 399, 400, true);
            finger.setImageBitmap(bitmap);
//            Bitmap bitmap2 = finger.getDrawingCache();
//            ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
//            bitmap2.compress(Bitmap.CompressFormat.PNG, 90, stream2);


//            Thread.interrupted();
//            return image;
        }

    }

}

