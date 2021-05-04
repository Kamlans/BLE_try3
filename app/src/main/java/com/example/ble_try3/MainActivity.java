package com.example.ble_try3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.DialogTitle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<Model> scanResultList = new ArrayList<>();
    private TextView textView;
    private boolean scanning = false;
    private static final String TAG = "kamlans";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int ACCESS_COARSE_LOCATION_REQUEST = 2;
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
    private RecAdapter recAdapter;

    private ScanSettings scanSettings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .setReportDelay(0L)
            .build();


    private ScanCallback scanCallback = new ScanCallback() {


        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(TAG, "onScanResult: ");
            Log.d(TAG, "onScanResult: intype :" + callbackType);
            BluetoothDevice device = result.getDevice();
            scanResultList.add(new Model(device.getName().toString()));
            recAdapter.notifyDataSetChanged();
            Log.d(TAG, "onScanResult: " + device);

        }


        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(TAG, "onBatchScanResults: " + results.toArray().length);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "onScanFailed: " + errorCode);
        }
    };
    private static final long SCAN_PERIOD = 10000;

    private void scanLeDevice() {

        Handler handler = new Handler(Looper.getMainLooper());

        if (scanner != null) {
            if (!scanning) {
                // Stops scanning after a pre-defined scan period.
                handler.postDelayed(() -> {

                    try {
                        Log.d(TAG, "run: ");
                        scanning = false;
                        scanner.stopScan(scanCallback);
                    } catch (Exception e) {
                        Log.d(TAG, "run: error : " + e);
                    }
                }, SCAN_PERIOD);

                try {
                    scanning = true;
                    Log.d(TAG, "scanLeDevice: scaning true");
                    scanner.startScan(scanCallback);
                } catch (Exception e) {
                    Log.d(TAG, "run: error : " + e);
                }

            } else {

                try {
                    scanning = false;
                    scanner.stopScan(scanCallback);
                } catch (Exception e) {
                    Log.d(TAG, "run: error : " + e);
                }

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Use this check to determine whether BLE is supported on the device. Then
// you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble_not_supported", Toast.LENGTH_SHORT).show();

        }

        hasPermissions();

        RecyclerView recyclerView = findViewById(R.id.recView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recAdapter = new RecAdapter(getApplicationContext(), scanResultList);
        recyclerView.setAdapter(recAdapter);


        Button button = findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (scanner != null) {
//                    try{
//
//                    //scanner.startScan(scanCallback);
//
//                        Log.d(TAG, "scan started");
//                        scanLeDevice();
//                    }catch (Exception e){
//                        Log.d(TAG, "onClick: error in starting scan"+e);
//                    }
//
//                }  else {
//                    Log.e(TAG, "could not get scanner object");
//                }
//




                if(hasBlePermissions() && areLocationServicesEnabled(getApplicationContext())) {
                    final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                    adapter = bluetoothManager.getAdapter();

                    if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                        Toast.makeText(getApplicationContext(), "Bluetooth low energy is not supported", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    // Ensures Bluetooth is available on the device and it is enabled. If not,
                    // displays a dialog requesting user permission to enable Bluetooth.
                    if (adapter == null || !adapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    } else {
                        scanLeDevice(true);
                    }
                }



            }
        });


    }

    private boolean hasPermissions() {

        if (adapter != null && !adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_COARSE_LOCATION_REQUEST);
                return false;
            }
        }
        return true;
    }

    public boolean hasBlePermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public void requestBlePermissions(final Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
    }

    public boolean checkGrantResults(String[] permissions, int[] grantResults) {
        int granted = 0;

        if (grantResults.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) || permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        granted++;
                    }
                }
            }
        } else { // if cancelled
            return false;
        }

        return granted == 2;
    }

    public boolean areLocationServicesEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        try {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("kamlans", "new device :" + device.getName());
                        }
                    });
                }
            };

    private void scanLeDevice(final boolean enable) {
        boolean mScanning;
        if (enable) {


            mScanning = true;
            adapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            adapter.stopLeScan(mLeScanCallback);
        }
    }

}