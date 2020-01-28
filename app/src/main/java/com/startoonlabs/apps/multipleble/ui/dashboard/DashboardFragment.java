package com.startoonlabs.apps.multipleble.ui.dashboard;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.startoonlabs.apps.multipleble.R;
import com.startoonlabs.apps.multipleble.adapter.DeviceListArrayAdapter;
import com.startoonlabs.apps.multipleble.classes.DeviceListClass;
import com.startoonlabs.apps.multipleble.services.PheezeeBleService;
import com.startoonlabs.apps.multipleble.utils.RegexOperations;

import java.util.ArrayList;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.BLUETOOTH_SERVICE;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.bluetooth_state;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.device_state;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.scan_state;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.scan_too_frequent;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.scanned_list;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private boolean tooFrequentScan = false;
    AlertDialog tooFrequentDialog;
    ListView lv_scandevices;
    SwipeRefreshLayout swipeRefreshLayout;
    BluetoothAdapter madapter_scandevices;
    private boolean mBluetoothState = true;
    private static final int REQUEST_FINE_LOCATION = 1;
    ArrayList<DeviceListClass> mScanResults;
    DeviceListArrayAdapter deviceListArrayAdapter;
    boolean isBound = false;
    private static final int REQUEST_ENABLE_BT = 1;


    PheezeeBleService mCustomService;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        dashboardViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        checkPermissionsRequired();
        checkLocationEnabled();
        startBluetoothService();

        lv_scandevices =root.findViewById(R.id.lv_deviceList);
        swipeRefreshLayout = root.findViewById(R.id.scandevices_swiperefresh);
        mScanResults = new ArrayList<>();
        deviceListArrayAdapter = new DeviceListArrayAdapter(getActivity(), mScanResults);
        deviceListArrayAdapter.setOnDeviceConnectPressed(new DeviceListArrayAdapter.onDeviceConnectPressed() {
            @Override
            public void onDeviceConnectPressed(String macAddress) {
                if(mBluetoothState) {
                    Intent intent = new Intent();
                    if (RegexOperations.validate(macAddress)) {
                        if(mCustomService!=null){
                            mCustomService.connectDevice(macAddress);
                        }
                    } else {
                    }
                }else {
                    startBleRequest();
                }
            }
        });

        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(BLUETOOTH_SERVICE);
        madapter_scandevices = bluetoothManager.getAdapter();
        if(madapter_scandevices == null || !madapter_scandevices.isEnabled()){
            mBluetoothState = false;
            startBleRequest();
        }


        swipeRefreshLayout.setColorSchemeResources(R.color.good_green,R.color.pale_good_green);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCustomService.stopScaninBackground();
                mCustomService.startScanInBackground();
                if(tooFrequentScan) {
                    swipeRefreshLayout.setRefreshing(false);
                }else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, 2000);
                }
            }
        });


        lv_scandevices.setAdapter(deviceListArrayAdapter);

//        start_scan_handler.run();

        Intent mIntent = new Intent(getActivity(), PheezeeBleService.class);
        getActivity().bindService(mIntent,mConnection,BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(device_state);
        intentFilter.addAction(bluetooth_state);
        intentFilter.addAction(scanned_list);
        intentFilter.addAction(scan_state);
        intentFilter.addAction(scan_too_frequent);
        getActivity().registerReceiver(receiver,intentFilter);

        return root;
    }

    private void checkPermissionsRequired() {
        //external storage permission
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        hasPermissions();
    }


    private void startBluetoothService() {
        ContextCompat.startForegroundService(getActivity(),new Intent(getActivity(), PheezeeBleService.class));
    }

    private boolean checkLocationEnabled(){
        LocationManager lm = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        if(!gps_enabled){
            AlertDialog mDialog = new AlertDialog.Builder(getActivity())
                    .setTitle("Location is turned of")
                    .setMessage("Please turn on location to scan and connect Pheezee")
                    .setCancelable(false)
                    .setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            dialog.dismiss();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
            mDialog.show();

        }
        return gps_enabled;
    }

    private void startBleRequest(){
        Intent enable_bluetooth  = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enable_bluetooth, REQUEST_ENABLE_BT);
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            isBound = true;
            PheezeeBleService.LocalBinder mLocalBinder = (PheezeeBleService.LocalBinder)service;
            mCustomService = mLocalBinder.getServiceInstance();
            if(mBluetoothState) {
                mCustomService.stopScaninBackground();
                mCustomService.startScanInBackground();
                updateList(mCustomService.getScannedList());
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mCustomService.stopScaninBackground();
            mCustomService = null;
            isBound = false;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isBound){
            this.getActivity().unbindService(mConnection);
        }
        this.getActivity().unregisterReceiver(receiver);
    }

    private boolean hasPermissions() {
        if (!hasLocationPermissions()) {
            requestLocationPermission();
            return false;
        }
        return true;
    }

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }


    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }


    private void updateList(ArrayList<DeviceListClass> listClasses){
        if(listClasses!=null && listClasses.size()>0) {
            mScanResults = listClasses;
            deviceListArrayAdapter.updateList(mScanResults);
        }else {
            if(listClasses!=null){
                mScanResults = listClasses;
                deviceListArrayAdapter.updateList(mScanResults);
            }
        }
    }



    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equalsIgnoreCase(device_state)){
                boolean device_status = intent.getBooleanExtra(device_state,false);
                if(device_status){
                    showToast("Device Connected!");
                }else {
                }
            }else if(action.equalsIgnoreCase(bluetooth_state)){
                boolean ble_state = intent.getBooleanExtra(bluetooth_state,false);
                if(ble_state){
                    mBluetoothState = true;
                    mCustomService.stopScaninBackground();
                    mCustomService.startScanInBackground();
                }else {
                    startBleRequest();
                    mBluetoothState = false;
//                    mCustomService.stopScaninBackground();
                }
            }else if(action.equalsIgnoreCase(scanned_list)){
                if(mCustomService!=null){
                    mScanResults =  mCustomService.getScannedList();
                    updateList(mScanResults);
                }

            }else if(action.equalsIgnoreCase(scan_state)){
                boolean scanning_state = intent.getBooleanExtra(scan_state,false);
                if(scanning_state){
                }else {
                }
            }else if(action.equalsIgnoreCase(scan_too_frequent)){
                boolean scanning_frequence = intent.getBooleanExtra(scan_too_frequent,false);
                if(scanning_frequence){
                }else {
                }
            }
        }
    };
    public void showToast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

}