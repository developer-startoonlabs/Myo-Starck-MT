package com.startoonlabs.apps.multiplebleemg.model;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

public class PheezeeServicesListModel {
    BluetoothGattCharacteristic mCustomCharacteristic;

    BluetoothGattDescriptor mCustomCharacteristicDescriptor;

    public PheezeeServicesListModel(BluetoothGattCharacteristic mCustomCharacteristic,
                                    BluetoothGattDescriptor mCustomCharacteristicDescriptor) {
        this.mCustomCharacteristic = mCustomCharacteristic;
        this.mCustomCharacteristicDescriptor = mCustomCharacteristicDescriptor;
    }

    public BluetoothGattCharacteristic getmCustomCharacteristic() {
        return mCustomCharacteristic;
    }

    public void setmCustomCharacteristic(BluetoothGattCharacteristic mCustomCharacteristic) {
        this.mCustomCharacteristic = mCustomCharacteristic;
    }

    public BluetoothGattDescriptor getmCustomCharacteristicDescriptor() {
        return mCustomCharacteristicDescriptor;
    }

    public void setmCustomCharacteristicDescriptor(BluetoothGattDescriptor mCustomCharacteristicDescriptor) {
        this.mCustomCharacteristicDescriptor = mCustomCharacteristicDescriptor;
    }
}
