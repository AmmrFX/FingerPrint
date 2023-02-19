package com.example.a9_11;



import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.morpho.android.usb.USBManager;
import com.morpho.morphosmart.sdk.CallbackMask;
import com.morpho.morphosmart.sdk.Coder;
import com.morpho.morphosmart.sdk.DetectionMode;
import com.morpho.morphosmart.sdk.EnrollmentType;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.LatentDetection;
import com.morpho.morphosmart.sdk.MorphoDevice;
import com.morpho.morphosmart.sdk.Template;
import com.morpho.morphosmart.sdk.TemplateFVPType;
import com.morpho.morphosmart.sdk.TemplateList;
import com.morpho.morphosmart.sdk.TemplateType;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
public class CbmCaptureActivity extends AppCompatActivity {
    private ImageView fpImg;
    private TextView cptrResponse;
    private ProgressBar progressBar;
    private Button btnCapture;
    private MorphoDevice morphoDevice;
    CbmProcessObserver processObserver;
    private boolean capturing = false;
    private boolean deviceIsSet = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cbm_capture);

        btnCapture = findViewById(R.id.btnCapture);
       // .setOnClickListener((OnClickListener) this);

        cptrResponse = (TextView) findViewById(R.id.txtResponse);
        fpImg = (ImageView)findViewById(R.id.fpImgCapture);
        USBManager.getInstance().initialize(this, "com.morpho.morphosample.USB_ACTION", true);

        progressBar = (ProgressBar) findViewById(R.id.vertical_progressbar);
        progressBar.setVisibility(View.INVISIBLE);
        final float[] roundedCorners = new float[]{5, 5, 5, 5, 5, 5, 5, 5};
        ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null, null));
        pgDrawable.getPaint().setColor(Color.blue(0));
        ClipDrawable progress = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
        progressBar.setProgressDrawable(progress);

        capturing = false;
        deviceIsSet = false;



btnCapture.setOnClickListener(new OnClickListener() {
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnCapture){

            if (!capturing){
                capturing = true;

                progressBar.setVisibility(View.VISIBLE);

                morphoDeviceCapture();

                btnCapture.setText(R.string.stop);
            }
            else if (capturing && deviceIsSet) {


                CbmMainActivity CbmMainActivity = new CbmMainActivity();
                morphoDevice = (CbmMainActivity).closeMorphoDevice(morphoDevice);

                btnCapture.setText(R.string.capture);

                capturing = false;
                deviceIsSet = false;
            }
            else{
                Toast.makeText(CbmCaptureActivity.this, "Device is being initialized, please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }
});




    }
    public void morphoDeviceCapture() {

        if (morphoDevice == null){
            CbmMainActivity CbmMainActivity = new CbmMainActivity();
            morphoDevice = ((CbmMainActivity)).initMorphoDevice();
            deviceIsSet = true;
        }

        /********* CAPTURE THREAD *************/
        Thread commandThread = new Thread(new Runnable() {
            @Override
            public void run() {

                int ret = 0;
                int timeout = 30;
                final int acquisitionThreshold = 0;
                int advancedSecurityLevelsRequired = 0;
                int fingerNumber = 1;
                TemplateType templateType = TemplateType.MORPHO_PK_ISO_FMR;
                TemplateFVPType templateFVPType = TemplateFVPType.MORPHO_NO_PK_FVP;
                int maxSizeTemplate = 512;
                EnrollmentType enrollType = EnrollmentType.ONE_ACQUISITIONS;
                LatentDetection latentDetection = LatentDetection.LATENT_DETECT_ENABLE;
                Coder coderChoice = Coder.MORPHO_DEFAULT_CODER;
                int detectModeChoice = DetectionMode.MORPHO_ENROLL_DETECT_MODE.getValue()
                        | DetectionMode.MORPHO_FORCE_FINGER_ON_TOP_DETECT_MODE.getValue();//18;
                TemplateList templateList = new TemplateList();

                // Define the messages sent through the callback
                int callbackCmd = CallbackMask.MORPHO_CALLBACK_COMMAND_CMD.getValue()
                        | CallbackMask.MORPHO_CALLBACK_IMAGE_CMD.getValue()
                        | CallbackMask.MORPHO_CALLBACK_CODEQUALITY.getValue()
                        | CallbackMask.MORPHO_CALLBACK_DETECTQUALITY.getValue();

                /********* CAPTURE *************/
                ret = morphoDevice.capture(timeout, acquisitionThreshold, advancedSecurityLevelsRequired,
                        fingerNumber, templateType, templateFVPType, maxSizeTemplate, enrollType,
                        latentDetection, coderChoice, detectModeChoice, templateList, callbackCmd, processObserver);

                Log.d("CaptureFragment", "morphoDeviceCapture ret = " + ret);
                if(ret != ErrorCodes.MORPHO_OK) {
                    String err = "";
                    if ( ret == ErrorCodes.MORPHOERR_TIMEOUT ){
                        err = "Capture failed : timeout";
                    }
                    else if (ret == ErrorCodes.MORPHOERR_CMDE_ABORTED ){
                        err = "Capture aborted";
                    }
                    else if (ret == ErrorCodes.MORPHOERR_UNAVAILABLE) {
                        err = "Device is not available";
                    }
                    else{
                        err = "Error code is " + ret;
                    }

                    final String finalErr = err;
                    CbmCaptureActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CbmCaptureActivity.this, finalErr, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    // Here fingerNumber = 1, so we will get only one template
                    int nbTemplate = templateList.getNbTemplate();
                    Log.d("CaptureFragment", "morphoDeviceCapture nbTemplate = " + nbTemplate);

                    String msg = "";

                    if (nbTemplate == 1) {

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                        String currentDateandTime = sdf.format(new Date());
                        String file_name = "sdcard/TemplateFP_" + currentDateandTime + "_" + System.currentTimeMillis();
                        file_name += templateType.getExtension();

                        msg += "Template successfully captured!\nThis template was saved as [" + file_name + "]";

                        final String alertMessage = msg;
                        final String filename = file_name;
                        final Template t = templateList.getTemplate(0);

                        // Dialog window to save the template
                        CbmCaptureActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog alertDialog = new AlertDialog.Builder(CbmCaptureActivity.this).create();
                                alertDialog.setTitle(R.string.app_name);
                                alertDialog.setCancelable(false);
                                alertDialog.setMessage(alertMessage);
                                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        FileOutputStream fos = null;
                                        try {

                                            fos = new FileOutputStream(filename);
                                            fos.write(t.getData());
                                            fos.close();

                                        } catch (Exception e) {
                                            Log.e("CaptureFragment", "FileOutputStream : " + e.getMessage());
                                        }
                                    }
                                });
                                alertDialog.show();
                            }
                        });

                    }
                }

                CbmCaptureActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                        progressBar.setProgress(0);

                        cptrResponse.setText("");

                        capturing = false;


                        btnCapture.setText(R.string.capture);
                    }
                });
            }
        });
        commandThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        CbmMainActivity CbmMainActivity = new CbmMainActivity();
        morphoDevice = ((CbmMainActivity)).closeMorphoDevice(morphoDevice);
        deviceIsSet = false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}






/////////////////////////////////////////////





