package com.kontakt.sample.samples;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.kontakt.sample.R;
import com.kontakt.sample.utils.TrackGPS;
import com.kontakt.sample.service.Interfaces.BeaconResponseListener;
import com.kontakt.sample.service.api.BeaconApiService;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is a sample of simple iBeacon and Eddystone foreground scanning.
 */
public class BeaconEddystoneScanActivity extends AppCompatActivity implements View.OnClickListener {

    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, BeaconEddystoneScanActivity.class);
    }

    public static final String TAG = "ProximityManager";

    private ProximityManager proximityManager;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_eddystone_scan);
        progressBar = (ProgressBar) findViewById(R.id.scanning_progress);

        //Setup Toolbar
        setupToolbar();

        //Setup buttons
        setupButtons();

        //Initialize and configure proximity manager
        setupProximityManager();
    }

    private void setupToolbar() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupButtons() {
        Button startScanButton = (Button) findViewById(R.id.start_scan_button);
        Button stopScanButton = (Button) findViewById(R.id.stop_scan_button);
        startScanButton.setOnClickListener(this);
        stopScanButton.setOnClickListener(this);
    }

    private void setupProximityManager() {
        proximityManager = ProximityManagerFactory.create(this);

        //Configure proximity manager basic options
        proximityManager.configuration()
                //Using ranging for continuous scanning or MONITORING for scanning with intervals
                .scanPeriod(ScanPeriod.RANGING)
                //Using BALANCED for best performance/battery ratio
                .scanMode(ScanMode.BALANCED)
                //OnDeviceUpdate callback will be received with 5 seconds interval
                .deviceUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(5));

        //Setting up iBeacon and Eddystone listeners
        proximityManager.setIBeaconListener(createIBeaconListener());
        proximityManager.setEddystoneListener(createEddystoneListener());
    }

    private void startScanning() {
        //Connect to scanning service and start scanning when ready
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                //Check if proximity manager is already scanning
                if (proximityManager.isScanning()) {
                    Toast.makeText(BeaconEddystoneScanActivity.this, "Already scanning", Toast.LENGTH_SHORT).show();
                    return;
                }
                proximityManager.startScanning();
                progressBar.setVisibility(View.VISIBLE);
                Toast.makeText(BeaconEddystoneScanActivity.this, "Scanning started", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void stopScanning() {
        //Stop scanning if scanning is in progress
        if (proximityManager.isScanning()) {
            proximityManager.stopScanning();
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Scanning stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private IBeaconListener createIBeaconListener() {
        return new IBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice iBeacon, IBeaconRegion region) {
                Log.i(TAG, "onIBeaconDiscovered: " + iBeacon);
                TrackGPS trackGPS = new TrackGPS(BeaconEddystoneScanActivity.this);
                String lat = String.valueOf(trackGPS.getLatitude());
                String lon = String.valueOf(trackGPS.getLongitude());
                String sim = getSimSerialNumber();

                serviceBeacon(sim, iBeacon.getProximityUUID().toString(), iBeacon.getMajor(), iBeacon.getMinor(), lat, lon, iBeacon.toString(),
                        "Entrada","");
            }

            @Override
            public void onIBeaconsUpdated(List<IBeaconDevice> iBeacons, IBeaconRegion region) {
                Log.i(TAG, "onIBeaconsUpdated: " + iBeacons.size());
            }

            @Override
            public void onIBeaconLost(IBeaconDevice iBeacon, IBeaconRegion region) {
                Log.e(TAG, "onIBeaconLost: " + iBeacon.toString());
                Toast.makeText(BeaconEddystoneScanActivity.this, "BEACON PERDIDO", Toast.LENGTH_SHORT).show();
                TrackGPS trackGPS = new TrackGPS(BeaconEddystoneScanActivity.this);
                String lat = String.valueOf(trackGPS.getLatitude());
                String lon = String.valueOf(trackGPS.getLongitude());
                String sim = getSimSerialNumber();

                serviceBeacon(sim, iBeacon.getProximityUUID().toString(), iBeacon.getMajor(), iBeacon.getMinor(), lat, lon, iBeacon.toString(),
                        "Salida","");
            }
        };
    }

    private EddystoneListener createEddystoneListener() {
        return new EddystoneListener() {
            @Override
            public void onEddystoneDiscovered(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
                Log.i(TAG, "onEddystoneDiscovered: " + eddystone.toString());
            }

            @Override
            public void onEddystonesUpdated(List<IEddystoneDevice> eddystones, IEddystoneNamespace namespace) {
                Log.i(TAG, "onEddystonesUpdated: " + eddystones.size());
            }

            @Override
            public void onEddystoneLost(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
                Log.e(TAG, "onEddystoneLost: " + eddystone.toString());
            }
        };
    }

    private String getSimSerialNumber() {
        String simSerialNo = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {

            SubscriptionManager subsManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            List<SubscriptionInfo> subsList = subsManager.getActiveSubscriptionInfoList();

            if (subsList != null) {
                for (SubscriptionInfo subsInfo : subsList) {
                    if (subsInfo != null) {
                        simSerialNo = subsInfo.getIccId();
                    }
                }
            }
        } else {
            TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            simSerialNo = tMgr.getSimSerialNumber();
        }

        return simSerialNo;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_scan_button:
                startScanning();
                break;
            case R.id.stop_scan_button:
                stopScanning();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        //Stop scanning when leaving screen.
        stopScanning();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //Remember to disconnect when finished.
        proximityManager.disconnect();
        super.onDestroy();
    }

    private void serviceBeacon(String sim, String uuid, int major, int minor, String latitud, String longitud, String dato1, String dato2, String dato3) {
        final ProgressDialog progressDialog = new ProgressDialog(BeaconEddystoneScanActivity.this);
        progressDialog.setCancelable(false); // set cancelable to false
        progressDialog.setMessage("Cargando"); // set message
        progressDialog.show(); // show progress dialog

        BeaconApiService beaconApiService = new BeaconApiService();
        beaconApiService.connect(this, sim, uuid, major, minor,latitud, longitud, dato1, dato2, dato3, new BeaconResponseListener() {
            @Override
            public void requestStarted() {
                progressDialog.show();
            }

            @Override
            public void requestCompleted() {
                progressDialog.dismiss();
                Toast.makeText(BeaconEddystoneScanActivity.this, "BEACON ENCONTRADO", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(BeaconEddystoneScanActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
