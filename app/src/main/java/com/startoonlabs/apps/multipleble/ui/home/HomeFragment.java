package com.startoonlabs.apps.multipleble.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.startoonlabs.apps.multipleble.R;
import com.startoonlabs.apps.multipleble.services.PheezeeBleService;
import com.startoonlabs.apps.multipleble.utils.ByteToArrayOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission_group.CAMERA;
import static android.content.Context.BIND_AUTO_CREATE;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.bluetooth_state;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.device_mac_1;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.device_mac_2;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.device_mac_3;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.device_mac_4;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.device_mac_5;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.device_mac_6;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.device_state_1;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.device_state_2;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.device_state_3;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.device_state_4;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.device_state_5;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.device_state_6;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.session_data;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.session_data_1;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.session_data_2;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.session_data_3;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.session_data_4;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.session_data_5;
import static com.startoonlabs.apps.multipleble.services.PheezeeBleService.session_data_6;

public class HomeFragment extends Fragment {
    private static final int REQUEST_FINE_LOCATION = 14;
    boolean isBound = false;
    private  int maxEmgValue = 0;

    private int maxEmgValueTwo = 0;
    PheezeeBleService mCustomService;

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 200;
    private HomeViewModel homeViewModel;
    private boolean mBluetoothState = false;
    TextView tv_emg1, tv_emg2, tv_emg3, tv_emg4, tv_emg5, tv_emg6;
    TextView tv_mac_first, tv_mac_second, tv_mac_third, tv_mac_fourth, tv_mac_fifth, tv_mac_sixth;
    TextView tv_first_device_state, tv_second_device_state,tv_third_device_state, tv_fourth_device_state, tv_fifth_device_state, tv_sixth_device_state;
    LineChart lineChart1, lineChart2, lineChart3, lineChart4, lineChart5, lineChart6;
    LineDataSet lineDataSet1, lineDataSet2, lineDataSet3, lineDataSet4, lineDataSet5, lineDataSet6;
    LineData lineData1, lineData2, lineData3, lineData4, lineData5, lineData6;
    List<Entry> dataPoints1, dataPoints2, dataPoints3, dataPoints4, dataPoints5, dataPoints6;
    LinearLayout add_device_bar;

    ProgressBar progress_bar_one,progress_bar_two;

    TextView tv_max_emg_show_one, tv_max_emg_show_two, max_emg_one, max_emg_two;



    Button btn_start_first, btn_stop_device1;
    int num_of_entries1 = 0, num_of_entries2 = 0, num_of_entries3 = 0, num_of_entries4 = 0, num_of_entries5 = 0, num_of_entries6 = 0;

    long MillisecondTime1, StartTime1, TimeBuff1, UpdateTime1 = 0L;
    long MillisecondTime2, StartTime2, TimeBuff2, UpdateTime2 = 0L;
    Long tsLong1 = 0L, tsLong2 = 0L;

    boolean mSessionStarted = false;

    Handler handler1, handler2;
    @SuppressLint("MissingInflatedId")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.new_device_file,container,false);
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
            }
        });
//        checkPermissionsRequired();
        checkLocationEnabled();
        checkAndRequestPermissions();


        add_device_bar = root.findViewById(R.id.add_device_bar);

        progress_bar_one = root.findViewById(R.id.progress_bar_one);
        progress_bar_two = root.findViewById(R.id.progress_bar_two);
        tv_max_emg_show_one = root.findViewById(R.id.tv_max_emg_show_one);
        tv_max_emg_show_two = root.findViewById(R.id.tv_max_emg_show_two);
        max_emg_one = root.findViewById(R.id.max_emg_one);
        max_emg_two = root.findViewById(R.id.max_emg_two);

        tv_first_device_state = root.findViewById(R.id.tv_device_status_1);
        tv_second_device_state = root.findViewById(R.id.tv_device_status_2);

        tv_mac_first = root.findViewById(R.id.tv_mac_first);
        tv_mac_second = root.findViewById(R.id.tv_mac_second);

        btn_start_first = root.findViewById(R.id.btn_start_device1);
        btn_stop_device1 = root.findViewById(R.id.btn_stop_device1);
        handler1 = new Handler();
        handler2 = new Handler();

        Intent mIntent = new Intent(getActivity(), PheezeeBleService.class);
        Objects.requireNonNull(getActivity()).bindService(mIntent,mConnection,BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(device_state_1);
        intentFilter.addAction(device_state_2);
        intentFilter.addAction(device_state_3);
        intentFilter.addAction(device_state_4);
        intentFilter.addAction(device_state_5);
        intentFilter.addAction(device_state_6);

        intentFilter.addAction(device_mac_1);
        intentFilter.addAction(device_mac_2);
        intentFilter.addAction(device_mac_3);
        intentFilter.addAction(device_mac_4);
        intentFilter.addAction(device_mac_5);
        intentFilter.addAction(device_mac_6);

        intentFilter.addAction(bluetooth_state);
        intentFilter.addAction(session_data_1);
        intentFilter.addAction(session_data_2);
        intentFilter.addAction(session_data_3);
        intentFilter.addAction(session_data_4);
        intentFilter.addAction(session_data_5);
        intentFilter.addAction(session_data_6);

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


        add_device_bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionsRequired();
                Navigation.findNavController(view).navigate(R.id.navigation_dashboard);
            }
        });

        btn_start_first.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                startSessions();
                if (btn_start_first.getText().toString().equalsIgnoreCase("start")) {
                        num_of_entries1=0;num_of_entries2=0;num_of_entries3=0;num_of_entries4 = 0;num_of_entries5=0;num_of_entries6=0;
                        btn_start_first.setVisibility(View.GONE);
                        btn_stop_device1.setVisibility(View.VISIBLE);
                        if (mCustomService != null) {
                            mCustomService.startAllNotification();
                        }
                        handler1.postDelayed(runnable1, 0);
                    }

            }



        });

        btn_stop_device1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_start_first.setVisibility(View.VISIBLE);
                btn_stop_device1.setVisibility(View.GONE);
                if (mCustomService != null) {
                    mCustomService.stopAlNotification();
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
        });











        homeViewModel.getEmg1().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer==-1){
                    tv_max_emg_show_one.setText("0".concat("μV"));
                    progress_bar_one.setProgress(0);
                }else {
                    tv_max_emg_show_one.setText(String.valueOf(integer).concat("μV"));
                    Log.e("integer", String.valueOf(integer));
                    progress_bar_one.setProgress(integer);
                }
            }
        });

        homeViewModel.getEmg2().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer==-1){
                    tv_max_emg_show_two.setText("0".concat("μV"));
                    progress_bar_two.setProgress(0);
                }else {
                    tv_max_emg_show_one.setText(String.valueOf(integer).concat("μV"));
                    progress_bar_two.setProgress(integer);
                }
            }
        });

        return root;
    }



    private void startSessions() {

        mSessionStarted = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                mCustomService.sendBodypartDataToDevice("0", 01, "0", 01,
                        01, 01, 01);
            }
        }, 100);

    }




    public Message getSessionData(){
        if(mCustomService!=null) {
            Message msg = mCustomService.getSessionData();
            return msg;
        }
        return null;
    }

    public Message getSessionDataTwo(){
        if(mCustomService!=null) {
            Message msg = mCustomService.getSessionDataTwo();
            return msg;
        }
        return null;
    }



    @SuppressLint("HandlerLeak")
//    public final Handler myHandler = new Handler() {
//        public void handleMessage(Message message) {
//
//            if (mSessionStarted) {
//
//                int angleDetected = 0, num_of_reps = 0, hold_time_minutes, hold_time_seconds, active_time_minutes, active_time_seconds, hold_angle = 0;
//                int emg_data, error_device = 0;
//                byte[] sub_byte;
//                sub_byte = (byte[]) message.obj;
//                Log.e("Kranthi_status", String.valueOf(sub_byte));
//                if (sub_byte != null) {
//
//                    error_device = sub_byte[10] & 0xFF;
//                    if (error_device == 0) {
//                        emg_data = ByteToArrayOperations.getAngleFromData(sub_byte[0], sub_byte[1]);
//                        Log.e("emg_data_testing", String.valueOf(emg_data));
//                        progress_bar_one.setProgress(emg_data);
//                        maxEmgValue = maxEmgValue < emg_data ? emg_data : maxEmgValue;
//                        max_emg_one.setText(String.valueOf(maxEmgValue)+"μV");
//                        tv_max_emg_show_one.setText(String.valueOf(emg_data).concat("μV"));
////                        tv_max_emg_show.setText(emg_data+"μV");
////                        progress_bar.setProgress(emg_data);
////                        maxEmgValue = maxEmgValue < emg_data ? emg_data : maxEmgValue;
////                        tv_max_emg_show_tgt.setText(String.valueOf(maxEmgValue)+"μV");
//////                        int pg = emg_data * 2 ;
////                        if(emg_data <= 100){
////                            progress_bar.setMax(100);
////                        }else{
////                            progress_bar.setMax(pg);
////                        }
//
//
//                        angleDetected = ByteToArrayOperations.getAngleFromData(sub_byte[2], sub_byte[3]);
//
//                        num_of_reps = ByteToArrayOperations.getNumberOfReps(sub_byte[4], sub_byte[5]);
//                        if (sub_byte.length > 11) {
//                            hold_angle = ByteToArrayOperations.getAngleFromData(sub_byte[11], sub_byte[12]);
//                        }
//
//
//                        hold_time_minutes = sub_byte[6];
//                        hold_time_seconds = sub_byte[7];
//                        active_time_minutes = sub_byte[8];
//                        active_time_seconds = sub_byte[9];
//
//
//                        String repetitionValue = "" + num_of_reps;
//
//
//                        String minutesValue = "" + hold_time_minutes, secondsValue = "" + hold_time_seconds;
//                        if (hold_time_minutes < 10)
//                            minutesValue = "0" + hold_time_minutes;
//                        if (hold_time_seconds < 10)
//                            secondsValue = "0" + hold_time_seconds;
//
//
//                    }
//                }
//            }
//        }
//
//
//    };


    private  boolean checkAndRequestPermissions() {


        int locationPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionStorage = ContextCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE);
        int permissionCamera = ContextCompat.checkSelfPermission(getContext(), CAMERA);
        int phonePermission = ContextCompat.checkSelfPermission(getContext(),READ_PHONE_STATE);
        int bluetooth_permission = ContextCompat.checkSelfPermission(getContext(),BLUETOOTH_CONNECT);
        int bluetooth_permission_2 = ContextCompat.checkSelfPermission(getContext(),BLUETOOTH_SCAN);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if(phonePermission != PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(READ_PHONE_STATE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(bluetooth_permission_2 != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(BLUETOOTH_SCAN);
            }
            if(bluetooth_permission != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(BLUETOOTH_CONNECT);
            }

        }




        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(getActivity(), listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
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

//    private void creatFirstGraphView() {
//        lineChart1.setHardwareAccelerationEnabled(true);
//        dataPoints1 = new ArrayList<>();
//        dataPoints1.add(new Entry(0, 0));
//        lineDataSet1 = new LineDataSet(dataPoints1, "Emg Graph");
//        lineDataSet1.setDrawCircles(false);
//        lineDataSet1.setValueTextSize(0);
//        lineDataSet1.setDrawValues(false);
//        lineDataSet1.setColor(getResources().getColor(R.color.pitch_black));
//        lineData1 = new LineData(lineDataSet1);
//        lineChart1.getXAxis();
//        lineChart1.getXAxis().setAxisMinimum(0f);
//        lineChart1.getAxisLeft().setSpaceTop(60f);
//        lineChart1.getAxisRight().setSpaceTop(60f);
//        lineChart1.getAxisRight().setDrawLabels(false);
//        lineChart1.getAxisLeft().setStartAtZero(false);
//        lineChart1.getAxisLeft().setDrawGridLines(false);
//        lineChart1.getXAxis().setDrawGridLines(false);
//        lineChart1.getXAxis().setDrawAxisLine(false);
//        lineChart1.setHorizontalScrollBarEnabled(true);
//        lineChart1.getDescription().setEnabled(false);
//        lineChart1.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
//        lineChart1.setScaleXEnabled(true);
//        lineChart1.fitScreen();
//        lineChart1.setData(lineData1);
//    }
//
//    private void creatSecondGraphView() {
//        lineChart2.setHardwareAccelerationEnabled(true);
//        dataPoints2 = new ArrayList<>();
//        dataPoints2.add(new Entry(0, 0));
//        lineDataSet2 = new LineDataSet(dataPoints2, "Emg Graph");
//        lineDataSet2.setDrawCircles(false);
//        lineDataSet2.setValueTextSize(0);
//        lineDataSet2.setDrawValues(false);
//        lineDataSet2.setColor(getResources().getColor(R.color.pitch_black));
//        lineData2 = new LineData(lineDataSet2);
//        lineChart2.getXAxis();
//        lineChart2.getXAxis().setAxisMinimum(0f);
//        lineChart2.getAxisLeft().setSpaceTop(60f);
//        lineChart2.getAxisRight().setSpaceTop(60f);
//        lineChart2.getAxisRight().setDrawLabels(false);
//        lineChart2.getAxisLeft().setStartAtZero(false);
//        lineChart2.getAxisLeft().setDrawGridLines(false);
//        lineChart2.getXAxis().setDrawGridLines(false);
//        lineChart2.getXAxis().setDrawAxisLine(false);
//        lineChart2.setHorizontalScrollBarEnabled(true);
//        lineChart2.getDescription().setEnabled(false);
//        lineChart2.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
//        lineChart2.setScaleXEnabled(true);
//        lineChart2.fitScreen();
//        lineChart2.setData(lineData2);
//    }
//
//    private void creatThirdGraphView() {
//        lineChart3.setHardwareAccelerationEnabled(true);
//        dataPoints3 = new ArrayList<>();
//        dataPoints3.add(new Entry(0, 0));
//        lineDataSet3 = new LineDataSet(dataPoints3, "Emg Graph");
//        lineDataSet3.setDrawCircles(false);
//        lineDataSet3.setValueTextSize(0);
//        lineDataSet3.setDrawValues(false);
//        lineDataSet3.setColor(getResources().getColor(R.color.pitch_black));
//        lineData3 = new LineData(lineDataSet3);
//        lineChart3.getXAxis();
//        lineChart3.getXAxis().setAxisMinimum(0f);
//        lineChart3.getAxisLeft().setSpaceTop(60f);
//        lineChart3.getAxisRight().setSpaceTop(60f);
//        lineChart3.getAxisRight().setDrawLabels(false);
//        lineChart3.getAxisLeft().setStartAtZero(false);
//        lineChart3.getAxisLeft().setDrawGridLines(false);
//        lineChart3.getXAxis().setDrawGridLines(false);
//        lineChart3.getXAxis().setDrawAxisLine(false);
//        lineChart3.setHorizontalScrollBarEnabled(true);
//        lineChart3.getDescription().setEnabled(false);
//        lineChart3.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
//        lineChart3.setScaleXEnabled(true);
//        lineChart3.fitScreen();
//        lineChart3.setData(lineData3);
//    }
//
//    private void creatFourthGraphView() {
//        lineChart4.setHardwareAccelerationEnabled(true);
//        dataPoints4 = new ArrayList<>();
//        dataPoints4.add(new Entry(0, 0));
//        lineDataSet4 = new LineDataSet(dataPoints4, "Emg Graph");
//        lineDataSet4.setDrawCircles(false);
//        lineDataSet4.setValueTextSize(0);
//        lineDataSet4.setDrawValues(false);
//        lineDataSet4.setColor(getResources().getColor(R.color.pitch_black));
//        lineData4 = new LineData(lineDataSet4);
//        lineChart4.getXAxis();
//        lineChart4.getXAxis().setAxisMinimum(0f);
//        lineChart4.getAxisLeft().setSpaceTop(60f);
//        lineChart4.getAxisRight().setSpaceTop(60f);
//        lineChart4.getAxisRight().setDrawLabels(false);
//        lineChart4.getAxisLeft().setStartAtZero(false);
//        lineChart4.getAxisLeft().setDrawGridLines(false);
//        lineChart4.getXAxis().setDrawGridLines(false);
//        lineChart4.getXAxis().setDrawAxisLine(false);
//        lineChart4.setHorizontalScrollBarEnabled(true);
//        lineChart4.getDescription().setEnabled(false);
//        lineChart4.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
//        lineChart4.setScaleXEnabled(true);
//        lineChart4.fitScreen();
//        lineChart4.setData(lineData4);
//    }
//
//    private void creatFifthGraphView() {
//        lineChart5.setHardwareAccelerationEnabled(true);
//        dataPoints5 = new ArrayList<>();
//        dataPoints5.add(new Entry(0, 0));
//        lineDataSet5 = new LineDataSet(dataPoints5, "Emg Graph");
//        lineDataSet5.setDrawCircles(false);
//        lineDataSet5.setValueTextSize(0);
//        lineDataSet5.setDrawValues(false);
//        lineDataSet5.setColor(getResources().getColor(R.color.pitch_black));
//        lineData5 = new LineData(lineDataSet5);
//        lineChart5.getXAxis();
//        lineChart5.getXAxis().setAxisMinimum(0f);
//        lineChart5.getAxisLeft().setSpaceTop(60f);
//        lineChart5.getAxisRight().setSpaceTop(60f);
//        lineChart5.getAxisRight().setDrawLabels(false);
//        lineChart5.getAxisLeft().setStartAtZero(false);
//        lineChart5.getAxisLeft().setDrawGridLines(false);
//        lineChart5.getXAxis().setDrawGridLines(false);
//        lineChart5.getXAxis().setDrawAxisLine(false);
//        lineChart5.setHorizontalScrollBarEnabled(true);
//        lineChart5.getDescription().setEnabled(false);
//        lineChart5.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
//        lineChart5.setScaleXEnabled(true);
//        lineChart5.fitScreen();
//        lineChart5.setData(lineData5);
//    }
//
//    private void creatSixthGraphView() {
//        lineChart6.setHardwareAccelerationEnabled(true);
//        dataPoints6 = new ArrayList<>();
//        dataPoints6.add(new Entry(0, 0));
//        lineDataSet6 = new LineDataSet(dataPoints6, "Emg Graph");
//        lineDataSet6.setDrawCircles(false);
//        lineDataSet6.setValueTextSize(0);
//        lineDataSet6.setDrawValues(false);
//        lineDataSet6.setColor(getResources().getColor(R.color.pitch_black));
//        lineData6 = new LineData(lineDataSet6);
//        lineChart6.getXAxis();
//        lineChart6.getXAxis().setAxisMinimum(0f);
//        lineChart6.getAxisLeft().setSpaceTop(60f);
//        lineChart6.getAxisRight().setSpaceTop(60f);
//        lineChart6.getAxisRight().setDrawLabels(false);
//        lineChart6.getAxisLeft().setStartAtZero(false);
//        lineChart6.getAxisLeft().setDrawGridLines(false);
//        lineChart6.getXAxis().setDrawGridLines(false);
//        lineChart6.getXAxis().setDrawAxisLine(false);
//        lineChart6.setHorizontalScrollBarEnabled(true);
//        lineChart6.getDescription().setEnabled(false);
//        lineChart6.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
//        lineChart6.setScaleXEnabled(true);
//        lineChart6.fitScreen();
//        lineChart6.setData(lineData6);
//    }
//
//
//
//    public void updateFirstChart(int value){
//        ++num_of_entries1;
//        lineData1.addEntry(new Entry((float) num_of_entries1 / 50, value), 0);
//        lineChart1.notifyDataSetChanged();
//        lineChart1.invalidate();
//        lineChart1.getXAxis();
//        lineChart1.getAxisLeft();
//        lineChart1.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return (int) value + getResources().getString(R.string.emg_unit);
//            }
//        });
//        if (UpdateTime1 / 1000 > 3)
//            lineChart1.setVisibleXRangeMaximum(5f);
//        lineChart1.moveViewToX((float) num_of_entries1 / 50);
//    }
//
//    public void updateSecondChart(int value){
//        ++num_of_entries2;
//        lineData2.addEntry(new Entry((float) num_of_entries2 / 50, value), 0);
//        lineChart2.notifyDataSetChanged();
//        lineChart2.invalidate();
//        lineChart2.getXAxis();
//        lineChart2.getAxisLeft();
//        lineChart2.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return (int) value + getResources().getString(R.string.emg_unit);
//            }
//        });
//        if (UpdateTime1 / 1000 > 3)
//            lineChart2.setVisibleXRangeMaximum(5f);
//        lineChart2.moveViewToX((float) num_of_entries2 / 50);
//    }
//
//    public void updateThirdChart(int value){
//        ++num_of_entries3;
//        lineData3.addEntry(new Entry((float) num_of_entries3 / 50, value), 0);
//        lineChart3.notifyDataSetChanged();
//        lineChart3.invalidate();
//        lineChart3.getXAxis();
//        lineChart3.getAxisLeft();
//        lineChart3.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return (int) value + getResources().getString(R.string.emg_unit);
//            }
//        });
//        if (UpdateTime1 / 1000 > 3)
//            lineChart3.setVisibleXRangeMaximum(5f);
//        lineChart3.moveViewToX((float) num_of_entries3 / 50);
//    }
//
//    public void updateFourthChart(int value){
//        ++num_of_entries4;
//        lineData4.addEntry(new Entry((float) num_of_entries4 / 50, value), 0);
//        lineChart4.notifyDataSetChanged();
//        lineChart4.invalidate();
//        lineChart4.getXAxis();
//        lineChart4.getAxisLeft();
//        lineChart4.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return (int) value + getResources().getString(R.string.emg_unit);
//            }
//        });
//        if (UpdateTime1 / 1000 > 3)
//            lineChart4.setVisibleXRangeMaximum(5f);
//        lineChart4.moveViewToX((float) num_of_entries4 / 50);
//    }
//
//    public void updateFifthChart(int value){
//        ++num_of_entries5;
//        lineData5.addEntry(new Entry((float) num_of_entries5 / 50, value), 0);
//        lineChart5.notifyDataSetChanged();
//        lineChart5.invalidate();
//        lineChart5.getXAxis();
//        lineChart5.getAxisLeft();
//        lineChart5.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return (int) value + getResources().getString(R.string.emg_unit);
//            }
//        });
//        if (UpdateTime1 / 1000 > 3)
//            lineChart5.setVisibleXRangeMaximum(5f);
//        lineChart5.moveViewToX((float) num_of_entries5 / 50);
//    }
//
//    public void updateSixthChart(int value){
//        ++num_of_entries6;
//        lineData6.addEntry(new Entry((float) num_of_entries6 / 50, value), 0);
//        lineChart6.notifyDataSetChanged();
//        lineChart6.invalidate();
//        lineChart6.getXAxis();
//        lineChart6.getAxisLeft();
//        lineChart6.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return (int) value + getResources().getString(R.string.emg_unit);
//            }
//        });
//        if (UpdateTime1 / 1000 > 3)
//            lineChart6.setVisibleXRangeMaximum(5f);
//        lineChart6.moveViewToX((float) num_of_entries6 / 50);
//    }



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
            mCustomService.stopAlNotification();
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
                    Log.i("Device State", String.valueOf(true));
                }else {
                    homeViewModel.getDeviceState1().setValue(false);
                    Log.i("Device State", String.valueOf(false));
                }
            }else if(action.equalsIgnoreCase(device_state_2)){
                boolean device_status = intent.getBooleanExtra(device_state_2,false);
                if(device_status){
                    homeViewModel.getDeviceState2().setValue(true);
                }else {
                    homeViewModel.getDeviceState2().setValue(false);
                }
            }else if(action.equalsIgnoreCase(device_state_3)){
                boolean device_status = intent.getBooleanExtra(device_state_3,false);
                if(device_status){
                    homeViewModel.getDeviceState3().setValue(true);
                }else {
                    homeViewModel.getDeviceState3().setValue(false);
                }
            }else if(action.equalsIgnoreCase(device_state_4)){
                boolean device_status = intent.getBooleanExtra(device_state_4,false);
                if(device_status){
                    homeViewModel.getDeviceState4().setValue(true);
                }else {
                    homeViewModel.getDeviceState4().setValue(false);
                }
            }else if(action.equalsIgnoreCase(device_state_5)){
                boolean device_status = intent.getBooleanExtra(device_state_5,false);
                if(device_status){
                    homeViewModel.getDeviceState5().setValue(true);
                }else {
                    homeViewModel.getDeviceState5().setValue(false);
                }
            }else if(action.equalsIgnoreCase(device_state_6)){
                boolean device_status = intent.getBooleanExtra(device_state_6,false);
                if(device_status){
                    homeViewModel.getDeviceState6().setValue(true);
                }else {
                    homeViewModel.getDeviceState6().setValue(false);
                }
            }


            else if(action.equalsIgnoreCase(device_mac_1)){
                String device_mac = intent.getStringExtra(device_mac_1);
                homeViewModel.getDeviceMac1().setValue(device_mac);
            }else if(action.equalsIgnoreCase(device_mac_2)){
                String device_mac = intent.getStringExtra(device_mac_2);
                homeViewModel.getDeviceMac2().setValue(device_mac);
            }else if(action.equalsIgnoreCase(device_mac_3)){
                String device_mac = intent.getStringExtra(device_mac_3);
                homeViewModel.getDeviceMac3().setValue(device_mac);
            }else if(action.equalsIgnoreCase(device_mac_4)){
                String device_mac = intent.getStringExtra(device_mac_4);
                homeViewModel.getDeviceMac4().setValue(device_mac);
            }else if(action.equalsIgnoreCase(device_mac_5)){
                String device_mac = intent.getStringExtra(device_mac_5);
                homeViewModel.getDeviceMac5().setValue(device_mac);
            }else if(action.equalsIgnoreCase(device_mac_6)){
                String device_mac = intent.getStringExtra(device_mac_6);
                homeViewModel.getDeviceMac6().setValue(device_mac);
            }


            else if(action.equalsIgnoreCase(session_data_1)){
                int emg_data, error_device = 0;
                byte[] sub_byte;
                sub_byte = (byte[]) getSessionData().obj;
                Log.e("Kranthi_status", String.valueOf(sub_byte));
                emg_data = ByteToArrayOperations.getAngleFromData(sub_byte[0], sub_byte[1]);
                Log.e("emg_data_testing", String.valueOf(emg_data));
                progress_bar_one.setProgress(emg_data);
                maxEmgValue = maxEmgValue < emg_data ? emg_data : maxEmgValue;
                max_emg_one.setText(String.valueOf(maxEmgValue)+"μV");
                tv_max_emg_show_one.setText(String.valueOf(emg_data).concat("μV"));
            } else if(action.equalsIgnoreCase(session_data_2)){
                int emg_data, error_device = 0;
                byte[] sub_byte;
                sub_byte = (byte[]) getSessionDataTwo().obj;
                Log.e("Kranthi_status", String.valueOf(sub_byte));
                emg_data = ByteToArrayOperations.getAngleFromData(sub_byte[0], sub_byte[1]);
                Log.e("emg_data_testing", String.valueOf(emg_data));
                progress_bar_two.setProgress(emg_data);
                maxEmgValueTwo = maxEmgValueTwo < emg_data ? emg_data : maxEmgValueTwo;
                max_emg_two.setText(String.valueOf(maxEmgValueTwo)+"μV");
                tv_max_emg_show_two.setText(String.valueOf(emg_data).concat("μV"));

//                int emg = intent.getIntExtra(session_data_2,0);
//                Log.e("Kranthi_testtttttwo", String.valueOf(emg));
////                homeViewModel.getEmg2().setValue(emg);
////                updateSecondChart(emg);
            }else if(action.equalsIgnoreCase(session_data_3)){
                int emg = intent.getIntExtra(session_data_3,0);
//                homeViewModel.getEmg3().setValue(emg);
//                updateThirdChart(emg);
            } else if(action.equalsIgnoreCase(session_data_4)){
                int emg = intent.getIntExtra(session_data_4,0);
//                homeViewModel.getEmg4().setValue(emg);
//                updateFourthChart(emg);
            }else if(action.equalsIgnoreCase(session_data_5)){
                int emg = intent.getIntExtra(session_data_5,0);
//                homeViewModel.getEmg5().setValue(emg);
//                updateFifthChart(emg);
            } else if(action.equalsIgnoreCase(session_data_6)){
                int emg = intent.getIntExtra(session_data_6,0);
//                homeViewModel.getEmg6().setValue(emg);
//                updateSixthChart(emg);
            }

            else if(action.equalsIgnoreCase(bluetooth_state)){
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