package com.startoonlabs.apps.multipleble.ui.home;

import android.Manifest;
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
import android.os.SystemClock;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.startoonlabs.apps.multipleble.R;
import com.startoonlabs.apps.multipleble.services.PheezeeBleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.bluetooth_state;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.device_mac_1;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.device_mac_2;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.device_state_1;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.device_state_2;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.session_data_1;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.session_data_2;

public class HomeFragment extends Fragment {
    private static final int REQUEST_FINE_LOCATION = 14;
    boolean isBound = false;
    PheezeeBleService mCustomService;
    private HomeViewModel homeViewModel;
    private boolean mBluetoothState = false;
    TextView tv_first_device_state, tv_second_device_state, tv_mac_first, tv_mac_second, tv_emg1, tv_emg2;

    LineChart lineChart1, lineChart2;
    LineDataSet lineDataSet1, lineDataSet2;
    LineData lineData1, lineData2;
    List<Entry> dataPoints1, dataPoints2;

    Button btn_start_first;
    int num_of_entries1 = 0, num_of_entries2 = 0;

    long MillisecondTime1, StartTime1, TimeBuff1, UpdateTime1 = 0L;
    long MillisecondTime2, StartTime2, TimeBuff2, UpdateTime2 = 0L;
    Long tsLong1 = 0L, tsLong2 = 0L;

    Handler handler1, handler2;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home,container,false);
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
            }
        });
        checkPermissionsRequired();
        checkLocationEnabled();
        startBluetoothService();


        tv_first_device_state = root.findViewById(R.id.tv_device_status_1);
        tv_second_device_state = root.findViewById(R.id.tv_device_status_2);
        tv_mac_first = root.findViewById(R.id.tv_mac_first);
        tv_mac_second = root.findViewById(R.id.tv_mac_second);
        btn_start_first = root.findViewById(R.id.btn_start_device1);
        tv_emg1 = root.findViewById(R.id.tv_emg_data1);
        tv_emg2 = root.findViewById(R.id.tv_emg_data_2);
        lineChart1 = root.findViewById(R.id.chart);
        lineChart2 = root.findViewById(R.id.chart1);
        handler1 = new Handler();
        handler2 = new Handler();

        Intent mIntent = new Intent(getActivity(), PheezeeBleService.class);
        Objects.requireNonNull(getActivity()).bindService(mIntent,mConnection,BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(device_state_1);
        intentFilter.addAction(device_state_2);
        intentFilter.addAction(device_mac_1);
        intentFilter.addAction(device_mac_2);
        intentFilter.addAction(bluetooth_state);
        intentFilter.addAction(session_data_1);
        intentFilter.addAction(session_data_2);
        getActivity().registerReceiver(receiver,intentFilter);

        homeViewModel.getDeviceState1().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    tv_first_device_state.setText("Connected");
                }else {
                    tv_first_device_state.setText("Not Connected");
                }
            }
        });


        homeViewModel.getDeviceState2().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    tv_second_device_state.setText("Connected");
                }else {
                    tv_second_device_state.setText("Not Connected");
                }
            }
        });

        homeViewModel.getDeviceMac1().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tv_mac_first.setText(s);
            }
        });

        homeViewModel.getDeviceMac2().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tv_mac_second.setText(s);
            }
        });

        btn_start_first.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (btn_start_first.getText().toString().equalsIgnoreCase("start")) {
                        if(homeViewModel.getDeviceState1().getValue() && homeViewModel.getDeviceState2().getValue()) {
                        num_of_entries1=0;num_of_entries2=0;
                        creatFirstGraphView();
                        creatSecondGraphView();
                        btn_start_first.setText("STOP");
                        if (mCustomService != null) {
                            mCustomService.startFirstDeviceNotification();
                            mCustomService.startSecondDeviceNotification();
                        }
                        handler1.postDelayed(runnable1, 0);
                        }else {
                            Toast.makeText(getActivity(), "Please Connect Both devices", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        btn_start_first.setText("START");
                        if (mCustomService != null) {
                            mCustomService.stopFirstDeviceNotification();
                            mCustomService.stopSecondDeviceNotification();
                        }
                        TimeBuff1 += MillisecondTime1;
                        handler1.removeCallbacks(runnable1);
                        MillisecondTime1 = 0L;
                        StartTime1 = 0L;
                        num_of_entries1=0;
                        TimeBuff1 = 0L;
                        UpdateTime1 = 0L;
                        tsLong1 = System.currentTimeMillis();
                    }

            }
        });



//        btn_start_second.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                    if (btn_start_second.getText().toString().equalsIgnoreCase("start")) {
//                        if(homeViewModel.getDeviceState2().getValue()) {
//                            num_of_entries2=0;
//                            creatSecondGraphView();
//                            btn_start_second.setText("STOP");
//                            if (mCustomService != null) {
//                                mCustomService.startSecondDeviceNotification();
//                            }
//                        }else {
//                            Toast.makeText(getActivity(), "Please Connect device", Toast.LENGTH_SHORT).show();
//                        }
//                        handler2.postDelayed(runnable2, 0);
//                    } else {
//                        btn_start_second.setText("START");
//                        if (mCustomService != null) {
//                            mCustomService.stopSecondDeviceNotification();
//                        }
//                        TimeBuff2 += MillisecondTime2;
//                        handler2.removeCallbacks(runnable2);
//                        MillisecondTime2 = 0L;
//                        StartTime2 = 0L;
//                        TimeBuff2 = 0L;
//                        UpdateTime2 = 0L;
//                        num_of_entries2=0;
//                        tsLong2 = System.currentTimeMillis();
//                    }
//
//            }
//        });


        homeViewModel.getEmg1().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer==-1){
                    tv_emg1.setText("NULL");
                }else {
                    tv_emg1.setText(String.valueOf(integer));
                }
            }
        });

        homeViewModel.getEmg2().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer==-1){
                    tv_emg2.setText("NULL");
                }else {
                    tv_emg2.setText(String.valueOf(integer));
                }
            }
        });
        creatFirstGraphView();
        creatSecondGraphView();

        return root;
    }

    private void checkPermissionsRequired() {
        //external storage permission
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        hasPermissions();
    }

    private boolean hasPermissions() {
        if (!hasLocationPermissions()) {
            requestLocationPermission();
            return false;
        }
        return true;
    }

    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
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

    public Runnable runnable1 = new Runnable() {
        public void run() {
            MillisecondTime1 = SystemClock.uptimeMillis() - StartTime1;
            UpdateTime1 = TimeBuff1 + MillisecondTime1;
            int Seconds = (int) (UpdateTime1 / 1000);
            int Minutes = Seconds / 60;
            Seconds = Seconds % 60;
            handler1.postDelayed(runnable1, 0);
        }
    };

    public Runnable runnable2 = new Runnable() {
        public void run() {
            MillisecondTime2 = SystemClock.uptimeMillis() - StartTime2;
            UpdateTime2 = TimeBuff2 + MillisecondTime2;
            int Seconds = (int) (UpdateTime2 / 1000);
            int Minutes = Seconds / 60;
            Seconds = Seconds % 60;
            handler2.postDelayed(runnable2, 0);
        }
    };

    private void creatFirstGraphView() {
        lineChart1.setHardwareAccelerationEnabled(true);
        dataPoints1 = new ArrayList<>();
        dataPoints1.add(new Entry(0, 0));
        lineDataSet1 = new LineDataSet(dataPoints1, "Emg Graph");
        lineDataSet1.setDrawCircles(false);
        lineDataSet1.setValueTextSize(0);
        lineDataSet1.setDrawValues(false);
        lineDataSet1.setColor(getResources().getColor(R.color.pitch_black));
        lineData1 = new LineData(lineDataSet1);
        lineChart1.getXAxis();
        lineChart1.getXAxis().setAxisMinimum(0f);
        lineChart1.getAxisLeft().setSpaceTop(60f);
        lineChart1.getAxisRight().setSpaceTop(60f);
        lineChart1.getAxisRight().setDrawLabels(false);
        lineChart1.getAxisLeft().setStartAtZero(false);
        lineChart1.getAxisLeft().setDrawGridLines(false);
        lineChart1.getXAxis().setDrawGridLines(false);
        lineChart1.getXAxis().setDrawAxisLine(false);
        lineChart1.setHorizontalScrollBarEnabled(true);
        lineChart1.getDescription().setEnabled(false);
        lineChart1.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart1.setScaleXEnabled(true);
        lineChart1.fitScreen();
        lineChart1.setData(lineData1);
    }

    private void creatSecondGraphView() {
        lineChart2.setHardwareAccelerationEnabled(true);
        dataPoints2 = new ArrayList<>();
        dataPoints2.add(new Entry(0, 0));
        lineDataSet2 = new LineDataSet(dataPoints2, "Emg Graph");
        lineDataSet2.setDrawCircles(false);
        lineDataSet2.setValueTextSize(0);
        lineDataSet2.setDrawValues(false);
        lineDataSet2.setColor(getResources().getColor(R.color.pitch_black));
        lineData2 = new LineData(lineDataSet2);
        lineChart2.getXAxis();
        lineChart2.getXAxis().setAxisMinimum(0f);
        lineChart2.getAxisLeft().setSpaceTop(60f);
        lineChart2.getAxisRight().setSpaceTop(60f);
        lineChart2.getAxisRight().setDrawLabels(false);
        lineChart2.getAxisLeft().setStartAtZero(false);
        lineChart2.getAxisLeft().setDrawGridLines(false);
        lineChart2.getXAxis().setDrawGridLines(false);
        lineChart2.getXAxis().setDrawAxisLine(false);
        lineChart2.setHorizontalScrollBarEnabled(true);
        lineChart2.getDescription().setEnabled(false);
        lineChart2.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart2.setScaleXEnabled(true);
        lineChart2.fitScreen();
        lineChart2.setData(lineData2);
    }


    public void updateFirstChart(int value){
        ++num_of_entries1;
        lineData1.addEntry(new Entry((float) num_of_entries1 / 50, value), 0);
        lineChart1.notifyDataSetChanged();
        lineChart1.invalidate();
        lineChart1.getXAxis();
        lineChart1.getAxisLeft();
        lineChart1.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return (int) value + getResources().getString(R.string.emg_unit);
            }
        });
        if (UpdateTime1 / 1000 > 3)
            lineChart1.setVisibleXRangeMaximum(5f);
        lineChart1.moveViewToX((float) num_of_entries1 / 50);
    }

    public void updateSecondChart(int value){
        ++num_of_entries2;
        lineData2.addEntry(new Entry((float) num_of_entries2 / 50, value), 0);
        lineChart2.notifyDataSetChanged();
        lineChart2.invalidate();
        lineChart2.getXAxis();
        lineChart2.getAxisLeft();
        lineChart2.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return (int) value + getResources().getString(R.string.emg_unit);
            }
        });
        if (UpdateTime1 / 1000 > 3)
            lineChart2.setVisibleXRangeMaximum(5f);
        lineChart2.moveViewToX((float) num_of_entries2 / 50);
    }



    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            isBound = true;
            PheezeeBleService.LocalBinder mLocalBinder = (PheezeeBleService.LocalBinder)service;
            mCustomService = mLocalBinder.getServiceInstance();
            if(mCustomService!=null){
                mCustomService.getDefaultDetails();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mCustomService.stopFirstDeviceNotification();
            mCustomService.stopSecondDeviceNotification();
            mCustomService = null;
            isBound = false;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if(isBound){
            this.getActivity().unbindService(mConnection);
        }
        this.getActivity().unregisterReceiver(receiver);
        super.onDestroy();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equalsIgnoreCase(device_state_1)){
                boolean device_status = intent.getBooleanExtra(device_state_1,false);
                if(device_status){
                    homeViewModel.getDeviceState1().setValue(true);
                }else {
                    homeViewModel.getDeviceState1().setValue(false);
                }
            }else if(action.equalsIgnoreCase(device_state_2)){
                boolean device_status = intent.getBooleanExtra(device_state_2,false);
                if(device_status){
                    homeViewModel.getDeviceState2().setValue(true);
                }else {
                    homeViewModel.getDeviceState2().setValue(false);
                }
            }else if(action.equalsIgnoreCase(device_mac_1)){
                String device_mac = intent.getStringExtra(device_mac_1);
                homeViewModel.getDeviceMac1().setValue(device_mac);
            }else if(action.equalsIgnoreCase(device_mac_2)){
                String device_mac = intent.getStringExtra(device_mac_2);
                homeViewModel.getDeviceMac2().setValue(device_mac);
            }else if(action.equalsIgnoreCase(session_data_1)){
                int emg = intent.getIntExtra(session_data_1,0);
                homeViewModel.getEmg1().setValue(emg);
                updateFirstChart(emg);
            } else if(action.equalsIgnoreCase(session_data_2)){
                int emg = intent.getIntExtra(session_data_2,0);
                homeViewModel.getEmg2().setValue(emg);
                updateSecondChart(emg);
            }else if(action.equalsIgnoreCase(bluetooth_state)){
                boolean ble_state = intent.getBooleanExtra(bluetooth_state,false);
                if(ble_state){
                    mBluetoothState = true;
                }else {
                    mBluetoothState = false;
                }
            }
        }
    };
}