package com.kontakt.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.kontakt.sample.samples.BackgroundScanActivity;
import com.kontakt.sample.samples.BeaconConfigurationActivity;
import com.kontakt.sample.samples.BeaconEddystoneScanActivity;
import com.kontakt.sample.samples.BeaconProScanActivity;
import com.kontakt.sample.samples.KontaktCloudActivity;
import com.kontakt.sample.samples.ScanFiltersActivity;
import com.kontakt.sample.samples.ScanRegionsActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  public static final int REQUEST_CODE_PERMISSIONS = 100;

  private Button beaconsScanningButton;
  private Button beaconsProScanningButton;
  private Button scanRegionsButton;
  private Button scanFiltersButton;
  private Button backgroundScanButton;
  private Button configurationButton;
  private Button kontaktCloudButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    setupButtons();
    checkPermissions();
  }

  //Setting up buttons and listeners.
  private void setupButtons() {
    beaconsScanningButton = (Button) findViewById(R.id.button_scan_beacons);
    beaconsProScanningButton = (Button) findViewById(R.id.button_scan_beacons_pro);
    scanRegionsButton = (Button) findViewById(R.id.button_scan_regions);
    scanFiltersButton = (Button) findViewById(R.id.button_scan_filters);
    backgroundScanButton = (Button) findViewById(R.id.button_scan_background);
    configurationButton = (Button) findViewById(R.id.button_beacon_config);
    kontaktCloudButton = (Button) findViewById(R.id.button_kontakt_cloud);

    beaconsScanningButton.setOnClickListener(this);
    beaconsProScanningButton.setOnClickListener(this);
    scanRegionsButton.setOnClickListener(this);
    scanFiltersButton.setOnClickListener(this);
    backgroundScanButton.setOnClickListener(this);
    configurationButton.setOnClickListener(this);
    kontaktCloudButton.setOnClickListener(this);
  }

  //Since Android Marshmallow starting a Bluetooth Low Energy scan requires permission from location group.
  private void checkPermissions() {
    int checkSelfPermissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
    if (PackageManager.PERMISSION_GRANTED != checkSelfPermissionResult) {
      //Permission not granted so we ask for it. Results are handled in onRequestPermissionsResult() callback.
      ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_CODE_PERMISSIONS);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      if (REQUEST_CODE_PERMISSIONS == requestCode) {
        Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show();
      }
    } else {
      disableButtons();
      Toast.makeText(this, "Location permissions are mandatory to use BLE features on Android 6.0 or higher", Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.button_scan_beacons:
        startActivity(BeaconEddystoneScanActivity.createIntent(this));
        break;
      case R.id.button_scan_beacons_pro:
        startActivity(BeaconProScanActivity.createIntent(this));
        break;
      case R.id.button_scan_filters:
        startActivity(ScanFiltersActivity.createIntent(this));
        break;
      case R.id.button_scan_regions:
        startActivity(ScanRegionsActivity.createIntent(this));
        break;
      case R.id.button_scan_background:
        startActivity(BackgroundScanActivity.createIntent(this));
        break;
      case R.id.button_beacon_config:
        startActivity(BeaconConfigurationActivity.createIntent(this));
        break;
      case R.id.button_kontakt_cloud:
        startActivity(KontaktCloudActivity.createIntent(this));
        break;
    }
  }

  private void disableButtons() {
    beaconsScanningButton.setEnabled(false);
    beaconsProScanningButton.setEnabled(false);
    scanRegionsButton.setEnabled(false);
    scanFiltersButton.setEnabled(false);
    backgroundScanButton.setEnabled(false);
    configurationButton.setEnabled(false);
    kontaktCloudButton.setEnabled(false);
  }
}
