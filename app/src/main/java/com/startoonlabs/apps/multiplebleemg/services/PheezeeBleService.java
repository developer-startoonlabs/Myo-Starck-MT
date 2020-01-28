package com.startoonlabs.apps.multiplebleemg.services;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.startoonlabs.apps.multiplebleemg.R;
import com.startoonlabs.apps.multiplebleemg.classes.DeviceListClass;
import com.startoonlabs.apps.multiplebleemg.model.PheezeeDevicesGattListModel;
import com.startoonlabs.apps.multiplebleemg.model.PheezeeServicesListModel;
import com.startoonlabs.apps.multiplebleemg.utils.ByteToArrayOperations;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.startoonlabs.apps.multiplebleemg.App.CHANNEL_ID;

public class PheezeeBleService extends Service {
    private byte[] temp_info_packet = null;
    private double latitude=0, longitude=0;
    private long first_scan = 0;
    private int num_of_scan = 0, mDeviceStatus=0;
    private int num_of_device_connected = 0;
    private boolean tooFrequentScan  =false, firmware_error = false;
    SharedPreferences preferences;
    private final String device_connected_notif = "Device Connected";
    private final String device_disconnected_notif = "Device not connected";
    private final String device_charging = "Device Connected, charging";
    private boolean first_packet = true;
    Message mMessage = new Message();

    boolean isConnectCommandGiven = false;
    //Intent Actions
    public static String device_state = "device.state";
    public static String device_state_1 = "device.state.1";
    public static String device_state_2 = "device.state.2";
    public static String device_mac_1 = "device.mac.1";
    public static String device_mac_2 = "device.mac.2";
    public static String bluetooth_state = "ble.state";
    public static String battery_percent = "battery.percent";
    public static String usb_state = "usb.state";
    public static String firmware_version = "firmware.version";
    public static String atiny_version = "atiny.version";
    public static String manufacturer_name = "manufacturer.name";
    public static String hardware_version = "hardware.version";
    public static String serial_id = "serial.id";
    public static String scanned_list = "scanned.list";
    public static String session_data = "session.data";
    public static String session_data_1 = "session.data.1";
    public static String session_data_2 = "session.data.2";
    public static String scan_state = "scan.state";
    public static String scan_too_frequent = "scan.too.frequent";
    public static String firmware_log = "firmware.log";
    public static String health_status = "health.status";
    public static String location_status = "location.status";
    public static String device_details_status = "device.details.status";
    public static String device_details_email = "device.details.email";
    public static String dfu_start_initiated = "dfu.start.initiated";
    public static String df_characteristic_written = "dfu.characteristic.written";
    public static String firmware_update_available = "firmware.update.available";
    public static String device_disconnected_firmware = "device.disconnected.firmware";
    public static String scedule_device_status_service = "scedule.device.status.service";
    public static String deactivate_device = "deactivate.device";


    public static int jobid_firmware_log = 0;
    public static  int jobid_fimware_update = 1;
    public static int jobid_health_data = 2;
    public static int jobid_location_status = 3;
    public static int jobid_device_details_update = 4;
    public static int jobid_user_connected_update = 5;
    public static int jobid_device_status = 6;









    //Service UUIDS
    public static final UUID generic_service_uuid = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    public static final UUID custom_service_uuid = UUID.fromString("909a1400-9693-4920-96e6-893c0157fedd");
    public static final UUID battery_service_uuid = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static final UUID device_info_service_uuid = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID dfu_service_uuid = UUID.fromString("0000fe59-0000-1000-8000-00805f9b34fb");





    //Characteristic
    public static final UUID device_name_characteristic_uuid = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");
    public static final UUID custom_characteristic_uuid = UUID.fromString("909a1401-9693-4920-96e6-893c0157fedd");
    public static final UUID battery_level_characteristic_uuid = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    public static final UUID firmware_version_characteristic_uuid = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID serial_number_characteristic_uuid = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb");
    public static final UUID manufacturer_name_characteristic_uuid = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    public static final UUID hardware_version_characteristic_uuid = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb");
    public static final UUID dfu_characteristic_uuid = UUID.fromString("8ec90003-f315-4f60-9fb8-838830daea50");


    //descriptor
    public static final UUID universal_descriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    //Binder
    IBinder myServiceBinder = new LocalBinder();

    //Characteristic read list
    ArrayList<BluetoothGattCharacteristic> mCharacteristicReadList;

    private Boolean mDeviceState = false, mBluetoothState = false, mUsbState = false,mDeviceState1 = false, mDeviceState2 = false;
    private int mBatteryPercent = 0;
    private String mFirmwareVersion = "", mSerialId = "", mManufacturerName = "", mAtinyVersion = "", mHardwareVersion="";
    private boolean mScanning = false;
    public String deviceMacc = "";
    ArrayList<DeviceListClass> mScanResults;
    private BtleScanCallback mScanCallback;
    BluetoothLeScanner mBluetoothLeScanner;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice remoteDevice;
    BluetoothGatt bluetoothGatt;
    BluetoothGattCharacteristic mCustomCharacteristic;

    BluetoothGattDescriptor  mCustomCharacteristicDescriptor;


    //For multiple
    List<PheezeeDevicesGattListModel> gattList;


    public PheezeeBleService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return myServiceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showNotification(device_disconnected_notif);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        gattList = new ArrayList<>();
        bluetoothAdapter = bluetoothManager.getAdapter();
        mCharacteristicReadList = new ArrayList<>();
        if(bluetoothAdapter!=null && bluetoothAdapter.isEnabled()){
            mBluetoothState = true;
            bluetoothStateBroadcast();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothProfile.EXTRA_STATE);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(bluetoothReceiver, filter);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(bluetoothGatt!=null){
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
        if(gattList!=null){
            for (int i=0;i<gattList.size();i++){
                gattList.get(i).getmBluetoothGatt().disconnect();
                gattList.get(i).getmBluetoothGatt().close();
            }
        }
        stopScaninBackground();
        disconnectDevice();
        unregisterReceiver(bluetoothReceiver);
        stopSelf();
    }

    public void startScanInBackground(){
        if(!tooFrequentScan) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    startScan();
                }
            });
        }else {
            sendTooFrequentScanBroadCast();
        }
    }

    public void stopScaninBackground(){
        if(!tooFrequentScan) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    long current_scan_time = Calendar.getInstance().getTimeInMillis();
                    int scan_difference = 0;
                    if (first_scan == 0) {
                        first_scan = Calendar.getInstance().getTimeInMillis();
                    } else {
                        scan_difference = (int) ((current_scan_time - first_scan) / 1000);
                    }
                    if (num_of_scan > 3 && scan_difference != 0 && scan_difference <= 30) {
                        tooFrequentScan = true;
                        sendTooFrequentScanBroadCast();
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                first_scan = 0;
                                num_of_scan = 0;
                                tooFrequentScan = false;
                                sendTooFrequentScanBroadCast();
                            }
                        }, 30000);
                    } else {
                        if(scan_difference>30){
                            scan_difference = 0;
                            first_scan = 0;
                            num_of_scan = 0;
                            tooFrequentScan = false;
                        }
                        stopScan();
                        num_of_scan++;
                    }
                }
            });
        }else {
            sendTooFrequentScanBroadCast();
        }
    }

    public void showNotification(String deviceState){
        Notification builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("Pheezee")
                .setContentText(deviceState)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();
        startForeground(1,builder);
    }


    private void startScan() {
        List<ScanFilter> filters = new ArrayList<>();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        if(!deviceMacc.equalsIgnoreCase("")){
            ScanFilter mFilter = new ScanFilter.Builder().setDeviceAddress(deviceMacc).build();
            filters.add(mFilter);
        }

        if(!mScanning && mBluetoothState) {
            mScanResults = new ArrayList<>();
            mScanCallback = new BtleScanCallback(mScanResults);
            mBluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
            mScanning = true;
            sendScanStateBroadcast();
        }
    }

    public void setLatitudeAndLongitude(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean isScanning(){
        return mScanning;
    }

    //Body part selection
    public boolean getDeviceState() {
        return mDeviceState;
    }

    public int getDeviceBatteryLevel(){
        return mBatteryPercent;
    }

    public boolean getUsbState(){
        return mUsbState;
    }

    public void sendTooFrequentScanBroadCast(){
        Intent i = new Intent(scan_too_frequent);
        i.putExtra(scan_too_frequent,tooFrequentScan);
        sendBroadcast(i);
    }

    public void sendDfuStartInitiated(boolean flag){
        Intent i = new Intent(dfu_start_initiated);
        i.putExtra(dfu_start_initiated,flag);
        sendBroadcast(i);
    }

    public void deviceStateBroadcast(){
        Intent i = new Intent(device_state);
        i.putExtra(device_state,mDeviceState);
        sendBroadcast(i);
    }

    public void deviceStateBroadcastFirst(){
        Intent i = new Intent(device_state_1);
        i.putExtra(device_state_1,mDeviceState1);

        sendBroadcast(i);
    }

    public void deviceMacBroadcastFirst(){
        if(mDeviceState1 && gattList.size()>0) {
            Intent i = new Intent(device_mac_1);
            i.putExtra(device_mac_1, gattList.get(0).getmBluetoothGatt().getDevice().getAddress());
            Log.i("DEVICE MAC123", gattList.get(0).getmBluetoothGatt().getDevice().getAddress());
            sendBroadcast(i);
        }
    }

    public void deviceMacBradcastSecond(){
        if(mDeviceState2 && gattList.size()>1) {
            Intent i = new Intent(device_mac_2);
            i.putExtra(device_mac_2, gattList.get(1).getmBluetoothGatt().getDevice().getAddress());
            sendBroadcast(i);
        }
    }

    public void deviceStateBroadcastSecond(){
        Intent i = new Intent(device_state_2);
        i.putExtra(device_state_2,mDeviceState2);
        sendBroadcast(i);
    }

    public void updatePheezeeMac(String macaddress){
        this.deviceMacc = macaddress;
        if(mBluetoothState)
            startScanInBackground();
    }

    public void forgetPheezee(){
        this.deviceMacc = "";
        isConnectCommandGiven = false;
    }

    public void bluetoothStateBroadcast(){
        Intent i = new Intent(bluetooth_state);
        i.putExtra(bluetooth_state,mBluetoothState);
        sendBroadcast(i);
    }

    public void sendScanStateBroadcast(){
        Intent i = new Intent(scan_state);
        i.putExtra(scan_state,mScanning);
        sendBroadcast(i);
    }

    public void sendBatteryLevelBroadCast(){
        Intent i = new Intent(battery_percent);
        i.putExtra(battery_percent,String.valueOf(mBatteryPercent));
        sendBroadcast(i);
    }

    public void sendUsbStateBroadcast(){
        Intent i = new Intent(usb_state);
        i.putExtra(usb_state,mUsbState);
        sendBroadcast(i);
    }

    public void sendDeviceDisconnectedBroadcast(){
        if(mDeviceStatus==1) {
            Intent i = new Intent(device_disconnected_firmware);
            i.putExtra(device_disconnected_firmware, true);
            sendBroadcast(i);
        }else {
            Intent i = new Intent(device_disconnected_firmware);
            i.putExtra(device_disconnected_firmware, false);
            sendBroadcast(i);
        }
    }

    public void sendFirmwareVersion(){
        Intent i = new Intent(firmware_version);
        i.putExtra(firmware_version,mFirmwareVersion);
        i.putExtra(atiny_version,mAtinyVersion);
        sendBroadcast(i);
    }

    public void sendSerialNumberBroadcast(){
        Intent i = new Intent(serial_id);
        i.putExtra(serial_id,mSerialId);
        sendBroadcast(i);
    }

    public void sendManufacturerName(){
        Intent i = new Intent(manufacturer_name);
        i.putExtra(manufacturer_name,mManufacturerName);
        sendBroadcast(i);
    }

    public void sendHardwareVersion(){
        Intent i = new Intent(hardware_version);
        i.putExtra(hardware_version,mHardwareVersion);
        sendBroadcast(i);
    }

    public void sendScannedListBroadcast(){
        Intent i = new Intent(scanned_list);
        i.putExtra(scanned_list,"");
        sendBroadcast(i);
    }

    public void sendSessionDataBroadcast(){
        Intent i = new Intent(session_data);
        i.putExtra(session_data,"");
        sendBroadcast(i);
    }

    public Message getSessionData(){
        return mMessage;
    }

    public ArrayList<DeviceListClass> getScannedList(){
        return mScanResults;
    }



    public void stopScan(){
        if (mScanning && bluetoothAdapter != null && bluetoothAdapter.isEnabled() && mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
        }
        mScanCallback = null;
        mScanning = false;
        sendScanStateBroadcast();
    }


    public void gerDeviceInfo(){
        bluetoothStateBroadcast();
        deviceStateBroadcast();
        sendBatteryLevelBroadCast();
        sendFirmwareVersion();
        deviceStateBroadcast();
        sendSerialNumberBroadcast();
        sendManufacturerName();
        sendHardwareVersion();
        sendDeviceDisconnectedBroadcast();
    }

    public void gerDeviceBasicInfo(){
        bluetoothStateBroadcast();
        deviceStateBroadcast();
        sendBatteryLevelBroadCast();
        sendFirmwareVersion();
        deviceStateBroadcast();
        sendSerialNumberBroadcast();
        sendManufacturerName();
        sendHardwareVersion();
    }

    public void increaseGain(){
        byte[] b = ByteToArrayOperations.hexStringToByteArray("AD01");
        writeCharacteristic(mCustomCharacteristic,b, "AD01");
    }

    public void decreaseGain(){
        byte[] b = ByteToArrayOperations.hexStringToByteArray("AD02");
        writeCharacteristic(mCustomCharacteristic,b,"AD02");
    }

//    public void sendBodypartDataToDevice(String exerciseType, int body_orientation, String patientName, int exercise_position,
//                                         int muscle_position, int bodypart_position){
//        String session_performing_notif = "Device Connected, Session is going on ";
//        showNotification(session_performing_notif +patientName);
//        writeCharacteristic(mCustomCharacteristic, ValueBasedColorOperations.getParticularDataToPheeze(body_orientation, muscle_position, exercise_position, bodypart_position),"AE");
//    }

    public void disableNotificationOfSession(){
        showNotification(device_connected_notif+"("+num_of_device_connected+")");
        if(bluetoothGatt!=null && mCustomCharacteristicDescriptor!=null && mCustomCharacteristic!=null){
            bluetoothGatt.setCharacteristicNotification(mCustomCharacteristic,false);
            mCustomCharacteristicDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(mCustomCharacteristicDescriptor);
            bluetoothGatt.writeCharacteristic(mCustomCharacteristic);
        }
    }

    public void connectDevice(String deviceMacc){
            final BluetoothManager mbluetoothManager=(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = mbluetoothManager.getAdapter();
            final BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(deviceMacc);
            this.remoteDevice = remoteDevice;
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    bluetoothGatt = remoteDevice.connectGatt(getApplicationContext(), false, callback);
                    PheezeeDevicesGattListModel listModel = new PheezeeDevicesGattListModel(bluetoothGatt);
                    boolean flag = false;
                    for (int i=0;i<gattList.size();i++){
                        if(gattList.get(i).getDeviceStatus()==false){
                            gattList.set(i,listModel);
                            flag = true;
                        }
                    }
                    if(!flag){
                        gattList.add(listModel);
                    }
                    refreshDeviceCache(bluetoothGatt);
                }
            });
    }

    public void disconnectDevice() {
        if(bluetoothGatt==null){
            return;
        }
        bluetoothGatt.disconnect();
        bluetoothGatt.close();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    class BtleScanCallback extends ScanCallback {
        private ArrayList<DeviceListClass> mScanResults;

        BtleScanCallback(ArrayList<DeviceListClass> mScanResults) {
            this.mScanResults = mScanResults;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
//                Log.i("device0",results.get(0).getDevice().getAddress());
                    addScanResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            //Log.e(TAG, "BLE Scan Failed with code " + errorCode);
        }

        private void addScanResult(ScanResult result) {
            String setDeviceBondState;
            BluetoothDevice device = result.getDevice();
            String deviceAddress = device.getAddress();
            String deviceName = device.getName();

            if(deviceName==null)
                deviceName = "UNKNOWN DEVICE";

            long timeStamp = System.currentTimeMillis() -
                    SystemClock.elapsedRealtime() +
                    (result.getTimestampNanos() / 1000000);
            int deviceRssi = result.getRssi();
            int deviceBondState = device.getBondState();
            //Just to update the bondstate if needed to
            if(deviceBondState == 0)
                setDeviceBondState = "BONDED";
            else
                setDeviceBondState = "NOT BONDED";

            //

            boolean flag = false, toBeUpdated = false;
            ArrayList<Integer> list = new ArrayList();
            for (int i = 0; i < mScanResults.size(); i++) {
                if (mScanResults.get(i).getDeviceMacAddress().equals(deviceAddress)) {
                    if(!Objects.equals(mScanResults.get(i).getDeviceBondState(), setDeviceBondState)){
                        mScanResults.get(i).setDeviceBondState(setDeviceBondState);
                    }

                    if (Integer.parseInt(mScanResults.get(i).getDeviceRssi())!= deviceRssi){
                        mScanResults.get(i).setDeviceRssi(""+deviceRssi);
                    }

                    long temp = timeStamp - mScanResults.get(i).getTimeStampNano();
                    mScanResults.get(i).setTimeStampNano(timeStamp);
                    flag = true;
                }else {
                    long currentTimeStamp = System.currentTimeMillis();
                    long temp = currentTimeStamp - mScanResults.get(i).getTimeStampNano();
                    if(temp>2000){
                        list.add(i);
                        toBeUpdated = true;
                    }
                }
            }



            if (!flag) {
                if(mDeviceState){
                    stopScaninBackground();
                }
                String str = "pheezee";
                if(deviceName.toLowerCase().contains(str)) {
                    DeviceListClass deviceListClass = new DeviceListClass();
                    deviceListClass.setDeviceName(deviceName);
                    deviceListClass.setDeviceMacAddress(deviceAddress);
                    deviceListClass.setDeviceRssi("" + deviceRssi);
                    deviceListClass.setTimeStampNano(timeStamp);
                    if (deviceBondState == 0)
                        deviceListClass.setDeviceBondState("BONDED");
                    else
                        deviceListClass.setDeviceBondState("NOT BONDED");

                    mScanResults.add(deviceListClass);

                    sendScannedListBroadcast();
                }
            }

            for (int i=0;i<list.size();i++){
                int a = list.get(i);
                try {
                    mScanResults.remove(a);
                }catch (ArrayIndexOutOfBoundsException e){
                    e.printStackTrace();
                }catch (IndexOutOfBoundsException e){
                    e.printStackTrace();
                }
            }
            if(toBeUpdated){
                sendScannedListBroadcast();
            }
        }
    }


    public void getDefaultDetails(){
        Log.i("HERE","HEREGETDETAILS");
        deviceStateBroadcastFirst();
        deviceStateBroadcastSecond();
        deviceMacBroadcastFirst();
        deviceMacBradcastSecond();
    }


    public void startFirstDeviceNotification(){
        if(gattList.size()>0){
            if(gattList.get(0).getDeviceStatus()){
                if(gattList.get(0).getmServicesList().getmCustomCharacteristic()!=null && gattList.get(0).getmBluetoothGatt()!=null){
                    gattList.get(0).getmBluetoothGatt().setCharacteristicNotification(gattList.get(0).getmServicesList().getmCustomCharacteristic(),true);
                    gattList.get(0).getmServicesList().getmCustomCharacteristicDescriptor().setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gattList.get(0).getmBluetoothGatt().writeDescriptor(gattList.get(0).getmServicesList().getmCustomCharacteristicDescriptor());
                }
            }else {
                Toast.makeText(this, "Device Not Connected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void stopFirstDeviceNotification(){
        if(gattList.size()>0){
            if(gattList.get(0).getDeviceStatus()){
                if(gattList.get(0).getmServicesList().getmCustomCharacteristic()!=null && gattList.get(0).getmBluetoothGatt()!=null){
                    gattList.get(0).getmBluetoothGatt().setCharacteristicNotification(gattList.get(0).getmServicesList().getmCustomCharacteristic(),false);
                    gattList.get(0).getmServicesList().getmCustomCharacteristicDescriptor().setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    gattList.get(0).getmBluetoothGatt().writeDescriptor(gattList.get(0).getmServicesList().getmCustomCharacteristicDescriptor());
                }
            }else {
                Toast.makeText(this, "Device Not Connected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startSecondDeviceNotification(){
        if(gattList.size()>1){
            if(gattList.get(1).getDeviceStatus()){
                if(gattList.get(1).getmServicesList().getmCustomCharacteristic()!=null && gattList.get(1).getmBluetoothGatt()!=null){
                    gattList.get(1).getmBluetoothGatt().setCharacteristicNotification(gattList.get(1).getmServicesList().getmCustomCharacteristic(),true);
                    gattList.get(1).getmServicesList().getmCustomCharacteristicDescriptor().setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gattList.get(1).getmBluetoothGatt().writeDescriptor(gattList.get(1).getmServicesList().getmCustomCharacteristicDescriptor());
                }
            }else {
                Toast.makeText(this, "Device Not Connected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void stopSecondDeviceNotification(){
        if(gattList.size()>1){
            if(gattList.get(1).getDeviceStatus()){
                if(gattList.get(1).getmServicesList().getmCustomCharacteristic()!=null && gattList.get(1).getmBluetoothGatt()!=null){
                    gattList.get(1).getmBluetoothGatt().setCharacteristicNotification(gattList.get(1).getmServicesList().getmCustomCharacteristic(),false);
                    gattList.get(1).getmServicesList().getmCustomCharacteristicDescriptor().setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    gattList.get(1).getmBluetoothGatt().writeDescriptor(gattList.get(1).getmServicesList().getmCustomCharacteristicDescriptor());
                }
            }else {
                Toast.makeText(this, "Device Not Connected", Toast.LENGTH_SHORT).show();
            }
        }
    }



    public BluetoothGattCallback callback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("STATE",status+","+newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    mCharacteristicReadList = new ArrayList<>();
                    mDeviceState = true;
                    gatt.discoverServices();
                    deviceStateBroadcast();
                    num_of_device_connected++;
                    showNotification(device_connected_notif+"("+num_of_device_connected+")");
                    if(gattList.size()>1){
                        if(gatt.getDevice()==gattList.get(0).getmBluetoothGatt().getDevice()){
                            mDeviceState1 = true;
                            gattList.get(0).setDeviceStatus(true);
                            Log.i("FIRST DEVICE","FIRST DEVICE");
                            deviceStateBroadcastFirst();
                            deviceMacBroadcastFirst();
                        }else {
                            Log.i("SECOND DEVICE","SECOND DEVICE");
                            mDeviceState2 = true;
                            gattList.get(1).setDeviceStatus(true);
                            deviceStateBroadcastSecond();
                            deviceMacBradcastSecond();
                        }
                    }else {
                        mDeviceState1 = true;
                        gattList.get(0).setDeviceStatus(true);
                        Log.i("FIRST DEVICE","FIRST DEVICE");
                        deviceStateBroadcastFirst();
                        deviceMacBroadcastFirst();
                    }
                }
            }
            if(status == BluetoothGatt.GATT_FAILURE){
                Message msg = Message.obtain();
                msg.obj = "N/C";
                showNotification(device_disconnected_notif);
            }
            if(status==BluetoothGatt.GATT_SERVER){
                if(newState==BluetoothGatt.STATE_DISCONNECTED){
                    num_of_device_connected--;
                    showNotification(device_connected_notif+"("+num_of_device_connected+")");
                    if(gattList.size()>1){
                        if(gatt.getDevice()==gattList.get(0).getmBluetoothGatt().getDevice()){
                            mDeviceState1 = false;
                            gattList.get(0).setDeviceStatus(false);
                            Log.i("FIRST DEVICE","FIRST DEVICE");
                            deviceStateBroadcastFirst();
                        }else {
                            Log.i("SECOND DEVICE","SECOND DEVICE");
                            mDeviceState2 = false;
                            gattList.get(1).setDeviceStatus(false);
                            deviceStateBroadcastSecond();
                            gattList.remove(1);
                        }
                    }else {
                        mDeviceState1 = false;
                        gattList.get(0).setDeviceStatus(false);
                        Log.i("FIRST DEVICE","FIRST DEVICE");
                        deviceStateBroadcastFirst();
                        if(gattList.size()>0)
                            gattList.remove(0);
                    }
                }
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status){
            mCustomCharacteristic = gatt.getService(custom_service_uuid).getCharacteristic(custom_characteristic_uuid);
            mCustomCharacteristicDescriptor = mCustomCharacteristic.getDescriptor(universal_descriptor);
            for (int i=0;i<gattList.size();i++){
                if(gattList.get(i).getmBluetoothGatt().getDevice()==gatt.getDevice()){
                    PheezeeServicesListModel model = new PheezeeServicesListModel(mCustomCharacteristic,mCustomCharacteristicDescriptor);
                    gattList.get(i).setmServicesList(model);
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i("Descriptor Written","ABCASDASD");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i("here","here");
            byte[] temp_byte;
            int value = 0;
            temp_byte = characteristic.getValue();
            byte header_main = temp_byte[0];
            //session related
            if (ByteToArrayOperations.byteToStringHexadecimal(header_main).equals("A1"))  {
                    value = ByteToArrayOperations.getAngleFromData(temp_byte[1],temp_byte[2]);
                    Log.i("Value", String.valueOf(value));
            }
            if(gattList.size()>=2){
                if(gatt.getDevice()==gattList.get(0).getmBluetoothGatt().getDevice()){
                    sendSessionDataBroadcastForFirstDevice(value);
                }else {
                    sendSessionDataBroadcastForSecondDevice(value);
                }
            }else {
                sendSessionDataBroadcastForFirstDevice(value);
            }
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        }
    };

    private void checkDeviceMacSavedOrNot() {
        if(preferences.getString("deviceMacaddress","").equalsIgnoreCase("")){
            if(remoteDevice!=null){
                if(!remoteDevice.getAddress().equalsIgnoreCase("")){
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("deviceMacaddress",remoteDevice.getAddress());
                    editor.commit();
                }
            }
        }
    }

    private void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] b, String value) {
        if(bluetoothGatt!=null && characteristic!=null) {
            characteristic.setValue(b);
            bluetoothGatt.writeCharacteristic(characteristic);
        }
    }


    private void sendSessionDataBroadcastForFirstDevice(int value){
        Intent i = new Intent(session_data_1);
        i.putExtra(session_data_1,value);
        sendBroadcast(i);
    }

    private void sendSessionDataBroadcastForSecondDevice(int value){
        Intent i = new Intent(session_data_2);
        i.putExtra(session_data_2,value);
        sendBroadcast(i);
    }


    private boolean refreshDeviceCache(BluetoothGatt gatt){
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                return (Boolean) localMethod.invoke(localBluetoothGatt, new Object[0]);
            }
        }
        catch (Exception localException) {
            localException.printStackTrace();
        }
        return false;
    }



    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
//            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
//                isConnectCommandGiven=false;
//                mDeviceState = false;mFirmwareVersion="Null"; mSerialId="NULL";mBatteryPercent = 0;mManufacturerName="Null";mHardwareVersion="Null";
//                mAtinyVersion = "Null";
//                mDeviceStatus=0;
//                if(bluetoothGatt!=null) {
//                    bluetoothGatt.disconnect();
//                    bluetoothGatt.close();
//                }
//                showNotification(device_disconnected_notif);
//                gerDeviceInfo();
//                if(!deviceMacc.equalsIgnoreCase(""))
//                    startScanInBackground();
//            }
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                    isConnectCommandGiven = false;
                    mBluetoothState = true;
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            if(!deviceMacc.equalsIgnoreCase(""))
                                startScanInBackground();
                            bluetoothStateBroadcast();
                        }
                    });
                }
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                    isConnectCommandGiven = false;
                    mDeviceStatus=0;
                    mBluetoothState = false;mDeviceState = false;mFirmwareVersion="Null"; mSerialId="NULL";mBatteryPercent = 0;mManufacturerName="Null";
                    mHardwareVersion="Null";
                    mAtinyVersion = "Null";
                    showNotification(device_disconnected_notif);
                    gerDeviceInfo();
                }
            }

        }
    };


    public class LocalBinder extends Binder {
        public PheezeeBleService getServiceInstance(){
            return PheezeeBleService.this;
        }
    }

    private void sendDfuCharacteristicWritten(){
        Intent i = new Intent(df_characteristic_written);
        i.putExtra(df_characteristic_written,"");
        sendBroadcast(i);
    }

    public String getMacAddress(){
        return remoteDevice.getAddress();
    }

    public String getDeviceName(){
        return remoteDevice.getName();
    }

    public int getDeviceDeactivationStatus(){
        return mDeviceStatus;
    }
    public void deactivateDevice(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                byte[] b = ByteToArrayOperations.hexStringToByteArray("D1");
                writeCharacteristic(mCustomCharacteristic,b,"D1");
            }
        },2000);

    }

    public void reactivateDevice(){
        byte[] b = ByteToArrayOperations.hexStringToByteArray("D2");
        writeCharacteristic(mCustomCharacteristic,b,"D2");
    }

    public byte[] getInfoPacket(){
        return temp_info_packet;
    }
}
