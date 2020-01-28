package com.startoonlabs.apps.multipleble.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<Boolean> deviceState1, deviceState2;
    private MutableLiveData<String> deviceMac1, deviceMac2;
    private MutableLiveData<Integer> emg1, emg2;
    public HomeViewModel() {
        mText = new MutableLiveData<>();
        deviceState1 = new MutableLiveData<>();
        deviceState2 = new MutableLiveData<>();
        deviceMac1 = new MutableLiveData<>();
        deviceMac2 = new MutableLiveData<>();
        emg1 = new MutableLiveData<>();
        emg2 = new MutableLiveData<>();
        mText.setValue("This is home fragment");

        deviceMac1.setValue("");
        deviceMac2.setValue("");
        emg1.setValue(-1);
        emg2.setValue(-1);
        deviceState1.setValue(false);
        deviceState2.setValue(false);
    }

    public MutableLiveData<Integer> getEmg1() {
        return emg1;
    }

    public MutableLiveData<Integer> getEmg2() {
        return emg2;
    }

    public LiveData<String> getText() {
        return mText;
    }

    public MutableLiveData<Boolean> getDeviceState1() {
        return deviceState1;
    }

    public MutableLiveData<Boolean> getDeviceState2() {
        return deviceState2;
    }

    public MutableLiveData<String> getDeviceMac1() {
        return deviceMac1;
    }

    public MutableLiveData<String> getDeviceMac2() {
        return deviceMac2;
    }
}