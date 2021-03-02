package com.kontakt.sample.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.kontakt.sample.Class.Registro;
import com.kontakt.sample.R;
import com.kontakt.sample.model.RegistroDB;
import com.kontakt.sample.utils.TrackGPS;
import com.kontakt.sample.utils.config;
import com.kontakt.sample.service.Interfaces.BeaconResponseListener;
import com.kontakt.sample.service.Interfaces.IsRunningResponseListener;
import com.kontakt.sample.service.api.BeaconApiService;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;

import java.util.List;

public class ForegroundScanService extends Service {

  public static final String TAG = ForegroundScanService.class.getSimpleName();

  public static final String ACTION_DEVICE_DISCOVERED = "DEVICE_DISCOVERED_ACTION";
  public static final String EXTRA_DEVICE = "DeviceExtra";
  public static final String EXTRA_DEVICES_COUNT = "DevicesCountExtra";
  public static final String EXTRA_INOUT = "InOutExtra";

  private static final String STOP_SERVICE_ACTION = "STOP_SERVICE_ACTION";

  private static final String NOTIFICATION_CHANEL_NAME = "Kontakt SDK Samples";
  private static final String NOTIFICATION_CHANEL_ID = "scanning_service_channel_id";

  private ProximityManager proximityManager;
  private static boolean isRunning; // Flag indicating if service is already running.
  private int devicesCount; // Total discovered devices count

  private PowerManager.WakeLock mWakeLock;
  private final static String channelId = "AminanoProperty";

  public static Intent createIntent(final Context context) {
    return new Intent(context, ForegroundScanService.class);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    setupProximityManager();
    isRunning = false;
  }

  private void setupProximityManager() {
    PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
    mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PartialWakeLockTag:");
    mWakeLock.acquire();
    // Create proximity manager instance
    proximityManager = ProximityManagerFactory.create(this);

    // Configure proximity manager basic options
    proximityManager.configuration()
        //Using ranging for continuous scanning or MONITORING for scanning with intervals
        .scanPeriod(ScanPeriod.RANGING)
        //Using BALANCED for best performance/battery ratio
        .scanMode(ScanMode.BALANCED);

    // Set up iBeacon listener
    proximityManager.setIBeaconListener(createIBeaconListener());
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
        if (STOP_SERVICE_ACTION.equals(intent.getAction())) {
      stopSelf();
      return START_NOT_STICKY;
    }

    // Check if service is already active
    if (isRunning) {
      Toast.makeText(this, "El servicio ya está funcionando.", Toast.LENGTH_SHORT).show();
      return START_STICKY;
    }

      /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          startMyOwnForeground();
      } else {
          startForeground(210, new Notification());
      }*/
    startInForeground();
    startScanning();
    isRunning = true;
    return START_STICKY;
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onDestroy() {
    mWakeLock.release();
    if (proximityManager != null) {
      proximityManager.disconnect();
      proximityManager = null;
    }
    Toast.makeText(this, "Scanning Detenido.", Toast.LENGTH_SHORT).show();
    super.onDestroy();
  }

    private void startMyOwnForeground() {
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel =
                new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_NONE);
        manager.createNotificationChannel(channel);
        Notification notification = new NotificationCompat.Builder(this, channelId).build();
        startForeground(219, notification);
    }

  private void startInForeground() {
    // Create notification intent
    final Intent notificationIntent = new Intent();
    final PendingIntent pendingIntent = PendingIntent.getActivity(
        this,
        0,
        notificationIntent,
        0
    );

    // Create stop intent with action
    final Intent intent = ForegroundScanService.createIntent(this);
    intent.setAction(STOP_SERVICE_ACTION);
    final PendingIntent stopIntent = PendingIntent.getService(
        this,
        0,
        intent,
        PendingIntent.FLAG_CANCEL_CURRENT
    );

    // Create notification channel
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      createNotificationChannel();
    }

    // Build notification
    final NotificationCompat.Action action = new NotificationCompat.Action(0, "Stop", stopIntent);
    final Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANEL_ID)
        .setContentTitle("SGA_Bea Se en cuentra en ejecución")
        .setContentText("El escaneo de Beacons está activo")
        .addAction(action)
        .setSmallIcon(R.mipmap.ic_launcher_bcnn)
        .setContentIntent(pendingIntent)
        .build();

    // Start foreground service
    startForeground(1, notification);
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private void createNotificationChannel() {
    final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    if (notificationManager == null) return;

    final NotificationChannel channel = new NotificationChannel(
        NOTIFICATION_CHANEL_ID,
        NOTIFICATION_CHANEL_NAME,
        NotificationManager.IMPORTANCE_DEFAULT
    );
    notificationManager.createNotificationChannel(channel);
  }

  private void startScanning() {
    proximityManager.connect(new OnServiceReadyListener() {
      @Override
      public void onServiceReady() {
        proximityManager.startScanning();
        devicesCount = 0;
        Toast.makeText(ForegroundScanService.this, "Scanning Iniciado.", Toast.LENGTH_SHORT).show();
      }
    });
  }

  private IBeaconListener createIBeaconListener() {
    return new SimpleIBeaconListener() {
      @Override
      public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
        onDeviceDiscovered(ibeacon, "Entrada");
        //Log.i(TAG, "onIBeaconDiscovered: " + ibeacon.toString());
        Log.i(TAG, "onIBeaconDiscovered: ENCONTRADO");
        TrackGPS trackGPS = new TrackGPS(ForegroundScanService.this);
        String lat = String.valueOf(trackGPS.getLatitude());
        String lon = String.valueOf(trackGPS.getLongitude());
        String sim = getSimSerialNumber();

        serviceBeacon(sim, ibeacon.getProximityUUID().toString(), ibeacon.getMajor(), ibeacon.getMinor(), lat, lon,
                ibeacon.toString(),"Entrada","");
      }

      @Override
      public void onIBeaconLost(IBeaconDevice ibeacon, IBeaconRegion region) {
        super.onIBeaconLost(ibeacon, region);
        //onDeviceDiscovered(ibeacon, "Salida");
        //Log.e(TAG, "onIBeaconLost: " + ibeacon.toString());
        Log.e(TAG, "onIBeaconDiscovered: PERDIDO");

        TrackGPS trackGPS = new TrackGPS(ForegroundScanService.this);
        String lat = String.valueOf(trackGPS.getLatitude());
        String lon = String.valueOf(trackGPS.getLongitude());
        String sim = getSimSerialNumber();

        serviceBeacon(sim, ibeacon.getProximityUUID().toString(), ibeacon.getMajor(), ibeacon.getMinor(), lat, lon,
                ibeacon.toString(),"Salida","");
      }
    };
  }

  private void onDeviceDiscovered(final RemoteBluetoothDevice device, String inOut) {
    devicesCount++;
    //Send a broadcast with discovered device
    Intent intent = new Intent();
    intent.setAction(ACTION_DEVICE_DISCOVERED);
    intent.putExtra(EXTRA_DEVICE, device);
    intent.putExtra(EXTRA_DEVICES_COUNT, devicesCount);
    intent.putExtra(EXTRA_INOUT, inOut);
    sendBroadcast(intent);
  }

  private void serviceBeacon(String sim, String uuid, int major, int minor, String latitud, String longitud, String dato1, final String dato2, String dato3) {
    RegistroDB db = new RegistroDB(this);
    db.open();
    db.onCreate();

    if (isOnlineNet()){
      Registro nuevo_registro = new Registro(sim,uuid,major,minor,latitud,longitud,dato1,dato2,dato3);
      db.Insert(nuevo_registro);
      Cursor registros = db.showList();
      if (registros.moveToFirst()){
        do {
          Registro registro = new Registro();
          registro.setSim(registros.getString(registros.getColumnIndex(db.keySim)));
          registro.setDeviceId(registros.getString(registros.getColumnIndex(db.keyDeviceId)));
          registro.setMajor(registros.getInt(registros.getColumnIndex(db.keyMajor)));
          registro.setMinor(registros.getInt(registros.getColumnIndex(db.keyMinor)));
          registro.setLatitud(registros.getString(registros.getColumnIndex(db.keyLatitud)));
          registro.setLongitud(registros.getString(registros.getColumnIndex(db.keyLongitud)));
          registro.setDato1(registros.getString(registros.getColumnIndex(db.keyDato1)));
          registro.setDato2(registros.getString(registros.getColumnIndex(db.keyDato2)));
          registro.setDato3(registros.getString(registros.getColumnIndex(db.keyDato3)));

          uploadData(registro.getSim(), registro.getDeviceId(), registro.getMajor(), registro.getMinor(),registro.getLatitud(),
                  registro.getLongitud(), registro.getDato1(), registro.getDato2(), registro.getDato3());

        } while (registros.moveToNext());

        db.onDrop();
        db.onCreate();
      } else {
        uploadData(sim, uuid, major, minor,latitud, longitud, dato1, dato2, dato3);
      }
    } else {
      Registro nuevo_registro = new Registro(sim,uuid,major,minor,latitud,longitud,dato1,dato2,dato3);
      db.Insert(nuevo_registro);
    }
    db.close();
  }

  private void uploadData(String sim, String uuid, int major, int minor, String latitud, String longitud, String dato1, final String dato2, String dato3){
    BeaconApiService beaconApiService = new BeaconApiService();
    beaconApiService.connect(this, sim, uuid, major, minor,latitud, longitud, dato1, dato2, dato3, new BeaconResponseListener() {
      @Override
      public void requestStarted() {
      }

      @Override
      public void requestCompleted() {
        Log.i(TAG, "requestCompleted: " + dato2);
      }

      @Override
      public void requestEndedWithError(VolleyError error) {
        Log.i(TAG, "onIBeaconDiscovered: " + error);
      }
    });
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

  public static void getIsRunning(IsRunningResponseListener mlistener) {
    IsRunningResponseListener mListener = mlistener;
    mListener.requestStarted(isRunning);

    config.SIGNAL_ACTIVATED = isRunning;
  }

  public Boolean isOnlineNet() {

    try {
      Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.cl");

      int val           = p.waitFor();
      boolean reachable = (val == 0);
      return reachable;

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return false;
  }
}
