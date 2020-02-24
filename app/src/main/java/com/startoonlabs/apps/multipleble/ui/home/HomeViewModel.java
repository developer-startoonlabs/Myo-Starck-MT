package com.startoonlabs.apps.multipleble.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<Boolean> deviceState1, deviceState2, deviceState3, deviceState4, deviceState5, deviceState6;
    private MutableLiveData<String> deviceMac1, deviceMac2, deviceMac3, deviceMac4, deviceMac5, deviceMac6;
    private MutableLiveData<Integer> emg1, emg2, emg3, emg4, emg5, emg6;
    public HomeViewModel() {
        mText = new MutableLiveData<>();
        deviceState1 = new MutableLiveData<>();
        deviceState2 = new MutableLiveData<>();
        deviceState3 = new MutableLiveData<>();
        deviceState4 = new MutableLiveData<>();
        deviceState5 = new MutableLiveData<>();
        deviceState6 = new MutableLiveData<>();


        deviceMac1 = new MutableLiveData<>();
        deviceMac2 = new MutableLiveData<>();
        deviceMac3 = new MutableLiveData<>();
        deviceMac4 = new MutableLiveData<>();
        deviceMac5 = new MutableLiveData<>();
        deviceMac6 = new MutableLiveData<>();


        emg1 = new MutableLiveData<>();
        emg2 = new MutableLiveData<>();
        emg3 = new MutableLiveData<>();
        emg4 = new MutableLiveData<>();
        emg5 = new MutableLiveData<>();
        emg6 = new MutableLiveData<>();

        mText.setValue("This is home fragment");

        deviceMac1.setValue("");
        deviceMac2.setValue("");
        deviceMac3.setValue("");
        deviceMac4.setValue("");
        deviceMac5.setValue("");
        deviceMac6.setValue("");


        emg1.setValue(-1);
        emg2.setValue(-1);
        emg3.setValue(-1);
        emg4.setValue(-1);
        emg5.setValue(-1);
        emg6.setValue(-1);

        deviceState1.setValue(false);
        deviceState2.setValue(false);
        deviceState3.setValue(false);
        deviceState4.setValue(false);
        deviceState5.setValue(false);
        deviceState6.setValue(false);
    }


    public MutableLiveData<Boolean> getDeviceState3() {
        return deviceState3;
    }

    public MutableLiveData<Boolean> getDeviceState4() {
        return deviceState4;
    }

    public MutableLiveData<Boolean> getDeviceState5() {
        return deviceState5;
    }

    public MutableLiveData<Boolean> getDeviceState6() {
        return deviceState6;
    }

    public MutableLiveData<String> getDeviceMac3() {
        return deviceMac3;
    }

    public MutableLiveData<String> getDeviceMac4() {
        return deviceMac4;
    }

    public MutableLiveData<String> getDeviceMac5() {
        return deviceMac5;
    }

    public MutableLiveData<String> getDeviceMac6() {
        return deviceMac6;
    }

    public MutableLiveData<Integer> getEmg3() {
        return emg3;
    }

    public MutableLiveData<Integer> getEmg4() {
        return emg4;
    }

    public MutableLiveData<Integer> getEmg5() {
        return emg5;
    }

    public MutableLiveData<Integer> getEmg6() {
        return emg6;
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