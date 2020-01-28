package com.startoonlabs.apps.multipleble.model;

import android.bluetooth.BluetoothGatt;

public class PheezeeDevicesGattListModel {
    BluetoothGatt mBluetoothGatt;
    PheezeeServicesListModel mServicesList;
    Boolean deviceStatus;

    public PheezeeDevicesGattListModel(BluetoothGatt mBluetoothGatt) {
        this.mBluetoothGatt = mBluetoothGatt;
        this.mServicesList = null;
        this.deviceStatus = false;
    }

    public BluetoothGatt getmBluetoothGatt() {
        return mBluetoothGatt;
    }

    public void setmBluetoothGatt(BluetoothGatt mBluetoothGatt) {
        this.mBluetoothGatt = mBluetoothGatt;
    }

    public PheezeeServicesListModel getmServicesList() {
        return mServicesList;
    }

    public void setmServicesList(PheezeeServicesListModel mServicesList) {
        this.mServicesList = mServicesList;
    }

    public Boolean getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(Boolean deviceStatus) {
        this.deviceStatus = deviceStatus;
    }
}
