package com.example.bluetoothdemo.viewmodel;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

/**
 * Author: ZhengHuaizhi
 * Date: 2020/8/30
 * Description: 蓝牙ViewModel，存取与BluetoothActivity相关数据LiveData
 */
public class BluetoothViewModel extends ViewModel {

    // 蓝牙状态
    private MutableLiveData<String> mBluetoothState;

    // 扫描到的蓝牙设备
    private MutableLiveData<ArrayList<BluetoothDevice>> mBluetoothDevices;

    // 连接配对过的蓝牙设备
    private MutableLiveData<ArrayList<BluetoothDevice>> mRecord;

    /**
     * 获取蓝牙状态LiveData
     */
    public MutableLiveData<String> getBluetoothStateLiveData() {
        return mBluetoothState == null ? new MutableLiveData<>() : null;
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
    public MutableLiveData<ArrayList<BluetoothDevice>> getBluetoothDevicesLiveData() {
        return mBluetoothDevices == null ? new MutableLiveData<>() : null;
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
    public MutableLiveData<ArrayList<BluetoothDevice>> getRecordLiveData() {
        return mRecord == null ? new MutableLiveData<>() : null;
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
