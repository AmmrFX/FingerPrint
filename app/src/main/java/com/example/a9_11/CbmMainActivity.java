package com.example.a9_11;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.morpho.android.usb.USBManager;
import com.morpho.morphosmart.sdk.CustomInteger;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.MorphoDevice;

public class CbmMainActivity extends Activity {

    private boolean isActive;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cbm_main);

        setTitle("Fingerprint Sensor");
        PackageManager packageManager = getPackageManager();
      //  getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg_gradient));

       /* ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        bar.addTab(bar
                .newTab()
                .setText("Capture")
                .setTag("Capture")
                .setTabListener((ActionBar.TabListener) new TabListener<CbmCaptureFragment>(this, "Capture", CbmCaptureFragment.class)));
        bar.addTab(bar
                .newTab()
                .setText("Verify")
                .setTag("Verify")
                .setTabListener((ActionBar.TabListener) new TabListener<CbmVerifyFragment>(this, "Verify", CbmVerifyFragment.class))*/;

       /* if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }
*/
        Button b1 = findViewById(R.id.one);
        Button b2 = findViewById(R.id.two);
/*        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new <CbmVerifyFragment>(this, "Verify", CbmVerifyFragment.class))
            }
        });*/
b1.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        startActivity(new Intent(CbmMainActivity.this, CbmVerifyActivity.class));

    }
});b2.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        startActivity(new Intent(CbmMainActivity.this, CbmCaptureActivity.class));

    }
});

/*        bar.addTab(bar
                .newTab()
                .setText("Capture")
                .setTag("Capture")
                .setTabListener(new SimpleTabListener<CbmCaptureFragment>(this, "Capture", CbmCaptureFragment.class)));
        bar.addTab(bar
                .newTab()
                .setText("Verify")
                .setTag("Verify")
                .setTabListener(new SimpleTabListener<CbmVerifyFragment>(this, "Verify", CbmVerifyFragment.class)));
        Button btnCapture = findViewById(R.id.one);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container1, new CbmCaptureFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });*/

/*        Button verifyButton = findViewById(R.id.two);
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new CbmVerifyFragment());
                fragmentTransaction.commit();
            }
        });*/


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Initialize and open USB devise
    public MorphoDevice initMorphoDevice() {
        int ret = 0;
        Log.d("MainActivity", "initMorphoDevice");

        // On Morphotablet, 3rd parameter (enableWakeLock) must always be true
        USBManager.getInstance().initialize(CbmMainActivity.this, "com.morpho.morphosample.USB_ACTION", true);
        MorphoDevice md = new MorphoDevice();
        CustomInteger nbUsbDevice = new CustomInteger(); // Do not use Integer.valueOf
        ret = md.initUsbDevicesNameEnum(nbUsbDevice);

        if (ret == ErrorCodes.MORPHO_OK) {
            if (nbUsbDevice.getValueOf() != 1) {

                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication(), "Please check your USB MorphoSmart™ device!\nIs it powered?" +
                                "\nDid you plug more than one USB MorphoSmart™ device?", Toast.LENGTH_LONG).show();
                    }
                });
                finish();

            } else {
                String sensorName = md.getUsbDeviceName(0); // We use first CBM found
                ret = md.openUsbDevice(sensorName, 0);
                if (ret != ErrorCodes.MORPHO_OK){
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplication(), "Error opening USB device", Toast.LENGTH_SHORT).show();
                        }
                    });

                    finish();
                }
            }
        }
        else{
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplication(), "Error initializing USB device", Toast.LENGTH_SHORT).show();
                }
            });
            finish();
        }

        return md;
    }

    // Close the USB device
    public MorphoDevice closeMorphoDevice(MorphoDevice md){
        if(md != null) {
            Log.d("MainActivity", "closeMorphoDevice");
            try {
                md.cancelLiveAcquisition();
                md.closeDevice();
                md = null;

            } catch (Exception e) {
                Log.e("MainActivity", "closeMorphoDevice : " + e.getMessage());
            }
        }
        return md;
    }


    /**Tab listener class**/
    private class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private final Bundle mArgs;
        private Fragment mFragment;

        public TabListener(Activity activity, String tag, Class<T> clz) {
            this(activity, tag, clz, null);
        }

        public TabListener(Activity activity, String tag, Class<T> clz, Bundle args) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            mArgs = args;

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
        }

        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            Toast.makeText(mActivity, "Reselected!", Toast.LENGTH_SHORT).show();
        }
    }
}
