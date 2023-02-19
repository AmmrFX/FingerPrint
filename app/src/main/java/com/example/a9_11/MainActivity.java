package com.example.a9_11;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.renderscript.Matrix3f;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.morpho.android.usb.USBManager;
import com.morpho.morphosmart.sdk.CompressionAlgorithm;
import com.morpho.morphosmart.sdk.CustomInteger;
import com.morpho.morphosmart.sdk.DetectionMode;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.LatentDetection;
import com.morpho.morphosmart.sdk.MorphoDatabase;
import com.morpho.morphosmart.sdk.MorphoDevice;
import com.morpho.morphosmart.sdk.MorphoImage;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button btn_enumerate;
    Button btn_OpenClose;
    Button btn_acquire;
    Button btn_DataBase;
    TextView Name;
    ImageView finger;
    Button Enroll;

    DbSetup dbSetup = new DbSetup();
    static MorphoDatabase morphoDatabase = null;
   // static List<DataBaseItem> databaseItems = null;
    private byte[] PrivacyKey = null;
    private ListView databaseListView;
    private int currentNumberOfRecordValue = 0;

    private MorphoDevice.MorphoDevicePrivacyModeStatus PrivacyModeStatus = MorphoDevice.MorphoDevicePrivacyModeStatus.PRIVACY_MODE_DISABLED;

    MorphoDevice morphoDevice = new MorphoDevice();

    String m_sensor_name;
    private int privacy_ret = 0;
    EditText Base64btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Name = findViewById(R.id.serialnumber);
        finger = findViewById(R.id.fingerImageView);
        btn_enumerate = findViewById(R.id.button_enumerate);
        btn_acquire = findViewById(R.id.button_acquire);
        btn_OpenClose = findViewById(R.id.button_open_close);
        btn_DataBase = findViewById(R.id.button_DataBase);
        Enroll = findViewById(R.id.Enroll);

        btn_acquire.setEnabled(false);
        btn_OpenClose.setEnabled(false);
        btn_DataBase.setEnabled(true);

        USBManager.getInstance().initialize(this, "com.morpho.morphosample.USB_ACTION", true);


        btn_enumerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int ret = ErrorCodes.MORPHO_OK;
                CustomInteger L_Usb = new CustomInteger();
                ret = morphoDevice.initUsbDevicesNameEnum(L_Usb);
                if (ret == ErrorCodes.MORPHO_OK) {
                    if ((L_Usb.getValueOf() > 0)) {
                        m_sensor_name = morphoDevice.getUsbDeviceName(0);
                        Name.setText(m_sensor_name.toString());
                        btn_OpenClose.setEnabled(true);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Amr").setMessage("Device not detected").setCancelable(false)
                                .setPositiveButton("ok", (new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        USBManager.getInstance().initialize(MainActivity.this,
                                                "com.morpho.morphosample.USB_ACTION", true);
                                    }
                                }));
                        AlertDialog builder1 = builder.create();
                        builder1.show();
                    }
                }
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
                    finger.setImageBitmap(bitmap);

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

                    String amr= Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
                    Thread.interrupted();
                }
            }
        });

        btn_DataBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                finger.buildDrawingCache();
                Bitmap bitmap = finger.getDrawingCache();
                ByteArrayOutputStream stream=new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
                byte[] image=stream.toByteArray();

                String img_str = Base64.encodeToString(image, Base64.DEFAULT);

            }
        });

        Enroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CbmMainActivity.class));
            }
        });
    }

    class ExampleThread extends Thread {
        String data;

        ExampleThread(String data) {
            this.data = data;
        }

        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                Log.d(TAG, "onClick: " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void setCurrentNumberOfRecordValue(int currentNumberOfRecordValue) {
        this.currentNumberOfRecordValue = currentNumberOfRecordValue;
    }

    protected void alert(int codeError, int internalError, String title, String message) {
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        String msg;
        if (codeError == 0) {
            msg = "good";
        } else {
            msg = "bad" + "\n" + ErrorCodes.getError(codeError, internalError);
        }
        msg += ((message.equalsIgnoreCase("")) ? "" : "\n" + message);
        alertDialog.setMessage(msg);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }





}