package com.kontakt.sample.samples;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.kontakt.sample.R;
import com.kontakt.sample.utils.SharedPreference;
import com.kontakt.sample.service.BackgroundScanService;
import com.kontakt.sample.service.ForegroundScanService;
import com.kontakt.sample.service.Interfaces.IsRunningResponseListener;
import com.kontakt.sample.service.Interfaces.TestUrlResponseListener;
import com.kontakt.sample.service.api.TestUrlApiService;
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;

import static com.kontakt.sample.utils.config.BASE_URL;
import static com.kontakt.sample.utils.config.KEY_CONFIG;
import static com.kontakt.sample.utils.config.KEY_SAVE_CONFIG_DATA;

public class ForegroundScanActivity extends AppCompatActivity implements View.OnClickListener {

    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, ForegroundScanActivity.class);
    }
    private Intent serviceIntent;
    private TextView statusText;
    private LinearLayout ll_configuracion_enabled;
    private EditText et_configuracion;
    private Button btn_editar_configuracion, btn_cancelar_configuracion, btn_aceptar_configuracion;
    private ImageView img_active_signal;

    private SharedPreference sharedPreference;

    public static final int REQUEST_CODE_PERMISSIONS = 100;
    public static final int PERMISSION_READ_STATE = 101;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foreground_scan);
        checkPermissions();

        bindUI();
        // Init views
        statusText = findViewById(R.id.status_text);

        // Set service intent
        serviceIntent = ForegroundScanService.createIntent(this);

        //Setup Toolbar
        setupToolbar();

        //Setup buttons
        setupButtons();

        try {
            if (sharedPreference.getValue(getApplicationContext(), KEY_SAVE_CONFIG_DATA).equals("true")) {
                et_configuracion.setText(sharedPreference.getValue(getApplicationContext(), KEY_CONFIG));
            }
        } catch (Exception e) {
            sharedPreference.save(getApplicationContext(), "false", KEY_SAVE_CONFIG_DATA);
        }

        btn_editar_configuracion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_editar_configuracion.setVisibility(View.GONE);
                ll_configuracion_enabled.setVisibility(View.VISIBLE);
                et_configuracion.setEnabled(true);
            }
        });

        btn_cancelar_configuracion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_configuracion_enabled.setVisibility(View.GONE);
                btn_editar_configuracion.setVisibility(View.VISIBLE);
                et_configuracion.setEnabled(false);
            }
        });

        btn_aceptar_configuracion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String testUrlString = et_configuracion.getText().toString();
                //String testUrlString = "http://192.168.1.84:45455/api";
                testUrl(testUrlString);
            }
        });

        getIsEnable();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register Broadcast receiver that will accept results from background scanning
        IntentFilter intentFilter = new IntentFilter(ForegroundScanService.ACTION_DEVICE_DISCOVERED);
        registerReceiver(scanningBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(scanningBroadcastReceiver);
        super.onPause();
    }

    private void setupToolbar() {
        final ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupButtons() {
        final Button startScanButton = findViewById(R.id.start_scan_button);
        final Button stopScanButton = findViewById(R.id.stop_scan_button);
        startScanButton.setOnClickListener(this);
        stopScanButton.setOnClickListener(this);
    }

    private void startBackgroundService() {
        if (!sharedPreference.getValue(getApplicationContext(), KEY_CONFIG).isEmpty()){
            BASE_URL = sharedPreference.getValue(getApplicationContext(), KEY_CONFIG);
            statusText.setText("Señal Activa");
            img_active_signal.setImageResource(R.drawable.ic_torre_de_senal_activo);
            startService(serviceIntent);
        } else {
            Toast.makeText(this, "Por favor inserte un URL válido antes continuar", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopBackgroundService() {
        statusText.setText("Señal Inactiva");
        img_active_signal.setImageResource(R.drawable.ic_torre_de_senal_inagtive);
        stopService(serviceIntent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_scan_button:
                startBackgroundService();
                break;
            case R.id.stop_scan_button:
                stopBackgroundService();
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

    private final BroadcastReceiver scanningBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Device discovered!
            int devicesCount = intent.getIntExtra(BackgroundScanService.EXTRA_DEVICES_COUNT, 0);
            RemoteBluetoothDevice device = intent.getParcelableExtra(BackgroundScanService.EXTRA_DEVICE);
            //statusText.setText(String.format("Total discovered devices: %d\n\nLast scanned device:\n%s", devicesCount, device.toString()));
        }
    };

    //Since Android Marshmallow starting a Bluetooth Low Energy scan requires permission from location group.
    private void checkPermissions() {
        int checkSelfPermissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermissionResult && PackageManager.PERMISSION_GRANTED != permissionCheck) {
            //Permission not granted so we ask for it. Results are handled in onRequestPermissionsResult() callback.
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE
            }, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (REQUEST_CODE_PERMISSIONS == requestCode) {
                enableButtons();
                //Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show();
            }
        } else {
            disableButtons();
            Toast.makeText(this, "Location permissions are mandatory to use BLE features on Android 6.0 or higher", Toast.LENGTH_LONG).show();
        }
    }

    private void getIsEnable(){
        ForegroundScanService.getIsRunning(new IsRunningResponseListener() {
            @Override
            public void requestStarted(boolean isEnable) {
                if (isEnable){
                    statusText.setText("Señal Activa");
                    img_active_signal.setImageResource(R.drawable.ic_torre_de_senal_activo);
                } else {
                    statusText.setText("Señal Inactiva");
                    img_active_signal.setImageResource(R.drawable.ic_torre_de_senal_inagtive);
                }
            }
        });
    }

    private void disableButtons() {
        Button start_scan_button = findViewById(R.id.start_scan_button);
        Button stop_scan_button = findViewById(R.id.stop_scan_button);
        start_scan_button.setEnabled(false);
        stop_scan_button.setEnabled(false);
    }

    private void enableButtons(){
        Button start_scan_button = findViewById(R.id.start_scan_button);
        Button stop_scan_button = findViewById(R.id.stop_scan_button);
        start_scan_button.setEnabled(true);
        stop_scan_button.setEnabled(true);
    }

    private void testUrl(String url) {
        final ProgressDialog progressDialog = new ProgressDialog(ForegroundScanActivity.this);
        progressDialog.setCancelable(false); // set cancelable to false
        progressDialog.setMessage("Cargando"); // set message
        progressDialog.show(); // show progress dialog

        TestUrlApiService testUrlApiService = new TestUrlApiService();
        testUrlApiService.test_url(this, url, new TestUrlResponseListener() {
            @Override
            public void requestStarted() {
                progressDialog.show();
            }

            @Override
            public void requestCompleted() {
                progressDialog.dismiss();
                Toast.makeText(ForegroundScanActivity.this, "URL validado correctamente", Toast.LENGTH_SHORT).show();
                stopService(serviceIntent);
                saveDataLogin();
            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                progressDialog.dismiss();
                Toast toast1 = Toast.makeText(ForegroundScanActivity.this, "Error al conectar con el URL ingresado.", Toast.LENGTH_LONG);
                toast1.show();
            }
        });
    }

    private void saveDataLogin() {
        BASE_URL = et_configuracion.getText().toString();
        sharedPreference.save(getApplicationContext(), et_configuracion.getText().toString(), KEY_CONFIG);
        sharedPreference.save(getApplicationContext(), "true", KEY_SAVE_CONFIG_DATA);
        ll_configuracion_enabled.setVisibility(View.GONE);
        btn_editar_configuracion.setVisibility(View.VISIBLE);
        et_configuracion.setEnabled(false);
    }

    private void bindUI() {
        ll_configuracion_enabled = findViewById(R.id.ll_configuracion_enabled);
        et_configuracion = findViewById(R.id.et_configuracion);
        btn_editar_configuracion = findViewById(R.id.btn_editar_configuracion);
        btn_cancelar_configuracion = findViewById(R.id.btn_cancelar_configuracion);
        btn_aceptar_configuracion = findViewById(R.id.btn_aceptar_configuracion);

        img_active_signal = findViewById(R.id.img_active_signal);

        sharedPreference = new SharedPreference();
    }


}
