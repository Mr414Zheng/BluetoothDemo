package com.example.bluetoothdemo.viewmodel;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;

/**
 * Author: ZhengHuaizhi
 * Date: 2020/8/30
 * Description: 蓝牙ViewModel，存取与BluetoothActivity相关数据LiveData
 */
public class BluetoothViewModel extends ViewModel {

    // 蓝牙状态
    private MutableLiveData<String> mBluetoothState;

    // 扫描到的蓝牙设备
    private MutableLiveData<HashMap<String, BluetoothDevice>> mBluetoothDevices;

    // 连接配对过的蓝牙设备
    private MutableLiveData<HashMap<String, BluetoothDevice>> mRecord;

    /**
     * 获取蓝牙状态LiveData
     */
    public MutableLiveData<String> getBluetoothStateLiveData() {
        if (mBluetoothState == null) {
            mBluetoothState = new MutableLiveData<>();
        }
        return mBluetoothState;
    }

    //    /**
    //     * 设置蓝牙状态LiveData
    //     */
    //    public void setBluetoothStateLiveData(String state) {
    //        if (mBluetoothState == null) {
    //            mBluetoothState = new MutableLiveData<>();
    //        }
    //        mBluetoothState.setValue(state);
    //    }

    /**
     * 获取扫描到的蓝牙设备LiveData
     */
    public MutableLiveData<HashMap<String, BluetoothDevice>> getBluetoothDevicesLiveData() {
        if (mBluetoothDevices == null) {
            mBluetoothDevices = new MutableLiveData<>();
        }
        return mBluetoothDevices;
    }

    //    /**
    //     * 设置扫描到的蓝牙设备LiveData
    //     */
    //    public void setBluetoothDevicesLiveData(ArrayList<BluetoothDevice> devices) {
    //        if (mBluetoothDevices == null) {
    //            mBluetoothDevices = new MutableLiveData<>();
    //        }
    //        mBluetoothDevices.setValue(devices);
    //    }

    /**
     * 获取连接配对过的蓝牙设备LiveData
     */
    public MutableLiveData<HashMap<String, BluetoothDevice>> getRecordLiveData() {
        if (mRecord == null) {
            mRecord = new MutableLiveData<>();
        }
        return mRecord;
    }

    //    /**
    //     * 设置连接配对过的蓝牙设备LiveData
    //     */
    //    public void setRecordLiveData(ArrayList<BluetoothDevice> devices) {
    //        if (mRecord == null) {
    //            mRecord = new MutableLiveData<>();
    //        }
    //        mRecord.setValue(devices);
    //    }
}
