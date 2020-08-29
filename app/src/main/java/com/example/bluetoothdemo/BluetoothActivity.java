package com.example.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.LogUtils;
import com.example.bluetoothdemo.adapter.MyBluetoothDeviceAdapter;
import com.example.bluetoothdemo.lifecycle.BluetoothLifecycleObserver;
import com.example.bluetoothdemo.viewmodel.BluetoothViewModel;

import java.util.ArrayList;
import java.util.List;

public class BluetoothActivity extends AppCompatActivity implements LifecycleOwner,
        View.OnClickListener {

    // 开启蓝牙请求码
    private final int REQUEST_ENABLE_BT = 1;
    private BluetoothViewModel mViewModel;
    // 扫描按钮
    private Button btnScan;
    // 查询已配对蓝牙设备列表
    private Button btnScanRecord;
    // 当前蓝牙状态
    private TextView tvState;
    // 扫描出的蓝牙设备列表
    private RecyclerView rcDevices;
    private MyBluetoothDeviceAdapter mDeviceAdapter;
    // 已配对过的蓝牙设备列表
    private RecyclerView rvRecord;
    // 蓝牙传感器
    private BluetoothAdapter mBluetoothAdapter;
    // 蓝牙扫描器
    private BluetoothLeScanner mBluetoothScanner;
    private ScanCallback mScanCallback;
    // 蓝牙扫描器是否处于扫描状态
    private boolean isScanning = false;
    // 存放扫描到的蓝牙设备
    private ArrayList<BluetoothDevice> mDevices;
    // 由蓝牙连接到的设备的托管
    private BluetoothGatt mBluetoothGatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        findViewByIds();

        initListeners();

        initRecyclerView();

        // 注册活动生命周期观察者
        registerLifecycleObserver();

        // 检测是否支持蓝牙4.0 BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "设备不支持蓝牙4.0", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // 创建ViewModel观察者
            createViewModelObserver();
        }
    }

    private void findViewByIds() {
        btnScan = findViewById(R.id.btn_scan);
        btnScanRecord = findViewById(R.id.btn_scan_record);
        tvState = findViewById(R.id.tv_state);
        rcDevices = findViewById(R.id.rv_device);
        rvRecord = findViewById(R.id.rv_record);
    }

    private void initListeners() {
        btnScan.setOnClickListener(this);
        btnScanRecord.setOnClickListener(this);
    }

    private void initRecyclerView() {
        if (mDevices == null) {
            mDevices = new ArrayList<>();
        }
        mDeviceAdapter = new MyBluetoothDeviceAdapter(mDevices);
        rcDevices.setLayoutManager(new LinearLayoutManager(this));
        rcDevices.setAdapter(mDeviceAdapter);
    }

    /**
     * 注册活动生命周期观察者
     */
    private void registerLifecycleObserver() {
        getLifecycle().addObserver(new BluetoothLifecycleObserver());
    }

    /**
     * 创建ViewModel观察者
     */
    private void createViewModelObserver() {
        // 获取ViewModel
        ViewModelProvider provider = new ViewModelProvider(this);
        mViewModel = provider.get(BluetoothViewModel.class);

        // 观察数据，更新界面
        // 连接配对过的蓝牙设备
        mViewModel.getRecordLiveData().observe(this, bluetoothDevices -> {

        });
        // 扫描到的蓝牙设备
        mViewModel.getBluetoothDevicesLiveData().observe(this, bluetoothDevices -> {
            for (BluetoothDevice device : bluetoothDevices) {
                LogUtils.d("扫描到的蓝牙设备:" + device.getName());
            }
            mDeviceAdapter.notifyDataSetChanged();
        });
        // 蓝牙状态
        mViewModel.getBluetoothStateLiveData().observe(this, state -> {
            LogUtils.d("蓝牙状态:" + state);
            tvState.setText(state);
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_scan:
                // 蓝牙扫描
                bluetoothScan();
                break;
            case R.id.btn_scan_record:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            mViewModel.getBluetoothStateLiveData().postValue("蓝牙已开启");
            LogUtils.d("onActivityResult:" + mViewModel.getBluetoothStateLiveData().getValue());
        }
    }

    /**
     * 蓝牙扫描
     */
    private void bluetoothScan() {
        if (mBluetoothAdapter == null) {
            BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            mBluetoothAdapter = manager.getAdapter();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            // 去启动蓝牙
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
            return;
        }

        // 蓝牙扫描器
        if (mBluetoothScanner == null) {
            mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }

        // 蓝牙扫描回调
        if (mScanCallback == null) {
            mScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    // 将扫描出的设备结果保存起来
                    BluetoothDevice device = result.getDevice();
                    mDevices.add(device);
                    LogUtils.d("onScanResult:" + device.getName() + ":" + device.getAddress());
                    mViewModel.getBluetoothDevicesLiveData().setValue(mDevices);
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                    LogUtils.d("onBatchScanResults:" + "length is " + results.size());
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    LogUtils.d("onScanFailed:" + errorCode);
                }
            };
        }

        if (isScanning) {
            // 如果正在扫描，则取消蓝牙扫描；注意！需要和开启蓝牙传入的回调是同一个实例
            isScanning = false;
            mBluetoothScanner.stopScan(mScanCallback);
            mViewModel.getBluetoothStateLiveData().setValue("蓝牙扫描已停止");
        } else {
            // 如果没开启扫描，则开启蓝牙扫描
            isScanning = true;
            mBluetoothScanner.startScan(mScanCallback);
            mViewModel.getBluetoothStateLiveData().setValue("蓝牙扫描已开启");
        }
    }

    /**
     * 连接蓝牙设备
     */
    private void connectBluetoothDevice(BluetoothDevice device) {
        mBluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                LogUtils.d("onConnectionStateChange:" + gatt.getDevice().getBondState());
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    mViewModel.getBluetoothStateLiveData().setValue("蓝牙已连接");
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    mViewModel.getBluetoothStateLiveData().setValue("蓝牙未连接");
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                             int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                              int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
            }
        });
    }
}
