package com.example.a9_11;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

public class Entry extends AppCompatActivity {
    Button Zoz;
    EditText FName1;
    EditText LName1;
    EditText ID1;
    ImageView finger;
    Button btn_enumerate;
    Button btn_OpenClose;
    Button btn_acquire;
    Button btn_DataBase;
    MorphoDevice morphoDevice = new MorphoDevice();
    String m_sensor_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        btn_enumerate = findViewById(R.id.button_enumerate2);
        btn_acquire = findViewById(R.id.button_acquire2);
        btn_OpenClose = findViewById(R.id.button_open_close2);
        btn_DataBase = findViewById(R.id.button_DataBase2);
        FName1 = findViewById(R.id.First);
        LName1 = findViewById(R.id.Last);
        ID1 = findViewById(R.id.Id);
        String Id = ID1.getText().toString();
        String F = FName1.getText().toString();
        String L = LName1.getText().toString();
        finger = findViewById(R.id.fingerImageView2);
        btn_enumerate.setOnClickListener(view -> {
            int ret = ErrorCodes.MORPHO_OK;
            CustomInteger L_Usb = new CustomInteger();
            ret = morphoDevice.initUsbDevicesNameEnum(L_Usb);
            if (ret == ErrorCodes.MORPHO_OK && L_Usb.getValueOf() > 0) {
                m_sensor_name = morphoDevice.getUsbDeviceName(0);
                btn_OpenClose.setEnabled(true);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(Entry.this);
                builder.setTitle("Amr")
                        .setMessage("Device not detected")
                        .setCancelable(false)
                        .setPositiveButton("ok", (dialogInterface, i) ->
                                USBManager.getInstance().initialize(Entry.this,
                                        "com.morpho.morphosample.USB_ACTION", true));
                builder.create().show();
            }
        });


        btn_OpenClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button button = (Button) view;
                int ret = ErrorCodes.MORPHO_OK;
                if (button.getText() == "Open") {
                    ret = morphoDevice.openUsbDevice(m_sensor_name, 0);

                    if (ret == ErrorCodes.MORPHO_OK) {
                        btn_acquire.setEnabled(true);
                        button.setText("Close");
                    }
                } else {
                    try {
                        ret = morphoDevice.closeDevice();
                        if (ret == ErrorCodes.MORPHO_OK) {
                            finger.setImageDrawable(null);
                            button.setText("Open");
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
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
                    byte[] image = stream.toByteArray();

                    String connectionUrl = "jdbc:jtds:sqlserver://192.168.1.229:1433;databaseName=Morpho;user=sa;password=Moh@123";
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    ResultSet resultSet = null;


                    /*String encoded = Base64.encodeToString(i);*/

                    String img_str = Base64.encodeToString(image, Base64.DEFAULT);


                    if (FName1.getText().toString().isEmpty()  || LName1.getText().toString().isEmpty()) {

                        Toast.makeText(Entry.this, "Please Insert your credentials correctly", Toast.LENGTH_SHORT).show();
                    }else {
                        String insertSql="";
                        try {
                            Toast.makeText(Entry.this, "ok", Toast.LENGTH_SHORT).show();
                             insertSql = "INSERT INTO Morpho (Finger,FName,LName) VALUES " + "('" + img_str + "','" + FName1.getText() + "','" + LName1.getText() + "');";

                        }catch (Exception exception){
                            Toast.makeText(Entry.this, (CharSequence) exception, Toast.LENGTH_SHORT).show();
                        }

                        try {
                            Class.forName("net.sourceforge.jtds.jdbc.Driver");
                            Connection connection = DriverManager.getConnection(connectionUrl);
                            PreparedStatement prepsInsertProduct = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                            prepsInsertProduct.execute();
                            // Retrieve the generated key from the insert.
                        } catch (SQLException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    finger.setImageBitmap(bitmap);


                    Thread.interrupted();
                }
            }
        });

        btn_DataBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String connectionUrl = "jdbc:jtds:sqlserver://192.168.1.152:1433;databaseName=Test;user=Sa;password=Moh@123";
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
                if (FName1.getText().toString().isEmpty() || ID1.getText().toString().isEmpty() || LName1.getText().toString().isEmpty()) {

                    Toast.makeText(Entry.this, "Please Insert your credentials correctly", Toast.LENGTH_SHORT).show();
                }else {
                    String insertSql="";
                    try {
                        Toast.makeText(Entry.this, "ok", Toast.LENGTH_SHORT).show();
                        insertSql = "INSERT INTO Morpho (Finger,FName,LName) VALUES " + "('" + img_str + "','" + FName1.getText() + "','" + LName1.getText() + "');";

                    }catch (Exception exception){
                        Toast.makeText(Entry.this, (CharSequence) exception, Toast.LENGTH_SHORT).show();
                    }

                    try {
                        Class.forName("net.sourceforge.jtds.jdbc.Driver");
                        Connection connection = DriverManager.getConnection(connectionUrl);
                        PreparedStatement prepsInsertProduct = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                        prepsInsertProduct.execute();
                        // Retrieve the generated key from the insert.
                    } catch (SQLException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                       //  String img_str = Base64.encodeToString(image, 0);
                }
            }
        });
    }
}