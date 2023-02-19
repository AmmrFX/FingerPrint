package com.example.a9_11;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a9_11.CbmMainActivity;
import com.example.a9_11.CbmProcessObserver;
import com.example.a9_11.R;
import com.morpho.morphosmart.sdk.CallbackMask;
import com.morpho.morphosmart.sdk.Coder;
import com.morpho.morphosmart.sdk.DetectionMode;
import com.morpho.morphosmart.sdk.EnrollmentType;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.LatentDetection;
import com.morpho.morphosmart.sdk.MatchingStrategy;
import com.morpho.morphosmart.sdk.MorphoDevice;
import com.morpho.morphosmart.sdk.ResultMatching;
import com.morpho.morphosmart.sdk.Template;
import com.morpho.morphosmart.sdk.TemplateFVPType;
import com.morpho.morphosmart.sdk.TemplateList;
import com.morpho.morphosmart.sdk.TemplateType;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.morpho.morphosmart.sdk.Coder.MORPHO_MSO_V9_CODER;
import static com.morpho.morphosmart.sdk.FalseAcceptanceRate.MORPHO_FAR_5;

public class CbmVerifyActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView fpImg;
    private Button btnVerify;
    private TextView verifyResult;
    private TextView txtResult;
    private MorphoDevice morphoDevice;
    private boolean verifying = false;
    private ArrayList<File> fileList = new ArrayList<File>();
    private List<String> list = new ArrayList<String>();
    private File root, templateFile;
    private static final String TAG = "CbmVerifyActivity";
    View rootView;
    private Template template;


    private TextView cptrResponse;
    private ProgressBar progressBar;
    private Button btnCapture;

    CbmProcessObserver processObserver;
    private boolean capturing = false;
    private boolean deviceIsSet = false;
    private void getFileList() {
        root = new File("/sdcard/cbm_templates/");
        if (!root.exists()) {
            Toast.makeText(CbmVerifyActivity.this, "Template directory does not exist", Toast.LENGTH_LONG).show();
            return;
        }
        File[] files = root.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                getFileList();
            } else if (file.getName().endsWith(".mtf")) {
                fileList.add(file);
                list.add(file.getName());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cbm_verify_fragment);

        btnVerify = findViewById(R.id.btnVerify);

        btnVerify.setOnClickListener(this);


        verifyResult = (TextView) findViewById(R.id.txtVerify);
        txtResult = (TextView) findViewById(R.id.txtResponse);
        fpImg = (ImageView) findViewById(R.id.fpImgVerify);

    }

    @Override
    public void onClick(View v) {
        if (v == btnVerify) {
            if (v.getId() == R.id.btnVerify){

                if (!verifying) {
                    verifyResult.setText(" ");
                    verifying = true;


                    AlertDialog alertDialog = new AlertDialog.Builder(CbmVerifyActivity.this).create();
                    alertDialog.setCancelable(false);
                    alertDialog.setTitle(R.string.app_name);
                    alertDialog.setMessage("Please select a template to verify");
                    alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Ok", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // Here, this filters only iso-fmr template files
                            selectFile();
                        }
                    });
                    alertDialog.show();
                }
                else {
                    Toast.makeText(CbmVerifyActivity.this, "Please wait until the end of process",Toast.LENGTH_SHORT).show();
                }
        }
    }
    }

    public void morphoDeviceVerify(){

        if (morphoDevice == null){
         //   morphoDevice = CbmVerifyActivity.this.initMorphoDevice();
        }

        /********* VERIFY THREAD *************/
        Thread commandThread = new Thread(new Runnable() {
            @Override
            public void run() {

                String match = "";
                int ret = 0;
                int timeOut = 30;
                int far =  MORPHO_FAR_5;
                Coder coder = MORPHO_MSO_V9_CODER;
                int detectModeChoice = DetectionMode.MORPHO_ENROLL_DETECT_MODE.getValue()
                        | DetectionMode.MORPHO_FORCE_FINGER_ON_TOP_DETECT_MODE.getValue();
                int matchingStrategy = MatchingStrategy.MORPHO_STANDARD_MATCHING_STRATEGY.getValue();
                final TemplateList templateList = new TemplateList();

                int callbackCmd = CallbackMask.MORPHO_CALLBACK_IMAGE_CMD.getValue()
                        | CallbackMask.MORPHO_CALLBACK_COMMAND_CMD.getValue();
                final ResultMatching resultMatching = new ResultMatching();

                // Read the template file provided by the user
                byte[] buffer = readFile();

                if (buffer != null) {
                    template = new Template();
                    template.setData(buffer);
                    template.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                    templateList.putTemplate(template);

                    /********* VERIFY *************/
                    ret = morphoDevice.verify(timeOut, far, coder, detectModeChoice, matchingStrategy,
                            templateList, callbackCmd, processObserver, resultMatching);

                    Log.d("VerifyFragment", "morphoDeviceVerify ret = " + ret);
                    if (ret != ErrorCodes.MORPHO_OK) {
                        String err = "";
                        if (ret == ErrorCodes.MORPHOERR_TIMEOUT) {
                            err = "Verify process failed : timeout";
                        } else if (ret == ErrorCodes.MORPHOERR_CMDE_ABORTED) {
                            err = "Verify process aborted";
                        } else if (ret == ErrorCodes.MORPHOERR_UNAVAILABLE) {
                            err = "Device is not available";
                        } else if (ret == ErrorCodes.MORPHOERR_INVALID_FINGER || ret == ErrorCodes.MORPHOERR_NO_HIT) {
                            err = "Authentication or Identification failed";
                            match = "Template doesn't match";
                        } else {
                            err = "Error code is " + ret;
                        }
                        final String finalErr = err;
                        CbmVerifyActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CbmVerifyActivity.this, finalErr, Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        if (resultMatching != null) {
                            match = "Matching score: " + resultMatching.getMatchingScore();
                        }
                    }
                } else {
                    // Wrong file/template
                    CbmVerifyActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CbmVerifyActivity.this, "Incorrect template", Toast.LENGTH_SHORT).show();
                            Log.d("VerifyFragment", "morphoDeviceVerify : incorrect template");
                        }
                    });

                }

                final String finalMatch = match;
                CbmVerifyActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        verifying = false;


                        txtResult.setText(" ");
                        verifyResult.setText(finalMatch);
                    }
                });
            }
        });
        commandThread.start();
    }

    public void selectFile(){
        // Get internal storage path (/sdcard)
        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());

        list.clear(); // Contains Strings (= filenames)
        fileList.clear(); // Contains File structures

        getfile(root);

        if (list.size() > 0){
            final CharSequence[] cs = list.toArray(new CharSequence[list.size()]);

            final AlertDialog.Builder builder = new AlertDialog.Builder(CbmVerifyActivity.this);
            builder.setTitle("Select Template");
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int pos = ((AlertDialog)dialog).getListView().getCheckedItemPosition();

                    templateFile = fileList.get(pos);

                    morphoDeviceVerify();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    verifying = false;

                }
            });
            builder.setSingleChoiceItems(cs, 0, null);
            builder.show();
        }
        else{
          /*  AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setCancelable(false);
            alertDialog.setTitle(R.string.app_name);
            alertDialog.setMessage("No templates were found!\nPlease capture a fingerprint");
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Ok", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    verifying = false;
                    rootView.setKeepScreenOn(false);
                }
            });
            alertDialog.show();*/
        }
    }

    public ArrayList<File> getfile(File dir) {
        File listFile[] = dir.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (int i = 0; i < listFile.length; i++) {

                if (!listFile[i].isDirectory()) {
                    if (listFile[i].getName().endsWith(".iso-fmr")) {
                        fileList.add(listFile[i]);
                        list.add(listFile[i].getName());
                    }
                }
            }
        }
        return fileList;
    }

    public byte[] readFile() {
        DataInputStream dis;
        byte[] buffer = null;

        try {
            dis = new DataInputStream(new FileInputStream(templateFile));

            int length = dis.available();
            buffer = new byte[length];
            dis.readFully(buffer);
            dis.close();
        }
        catch (Exception e){
            Log.e("VerifyFragment", "ReadFile : " + e.getMessage());
        }

        return buffer;
    }

    @Override
    public void onPause() {
        super.onPause();

     //   morphoDevice = ((CbmMainActivity)getActivity()).closeMorphoDevice(morphoDevice);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}