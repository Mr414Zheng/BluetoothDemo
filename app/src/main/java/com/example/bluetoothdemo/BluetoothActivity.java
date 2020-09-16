package com.example.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.example.bluetoothdemo.adapter.MyBluetoothDeviceAdapter;
import com.example.bluetoothdemo.constant.AppKey;
import com.example.bluetoothdemo.lifecycle.BluetoothLifecycleObserver;
import com.example.bluetoothdemo.viewmodel.BluetoothViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;

public class BluetoothActivity extends AppCompatActivity implements LifecycleOwner,
        View.OnClickListener {

    // 开启蓝牙请求码
    private final int REQUEST_ENABLE_BT = 1;
    private BluetoothViewModel mViewModel;
    // 扫描按钮
    private Button btnScan;
    // 查询已配对蓝牙设备列表
    private Button btnScanRecord;
    // 重连设备
    private Button btnReconnect;
    // 当前蓝牙状态
    private TextView tvState;
    // 扫描出的蓝牙设备列表
    private RecyclerView rcDevices;
    private MyBluetoothDeviceAdapter mDeviceAdapter;
    // 已配对过的蓝牙设备列表
    private RecyclerView rvRecord;
    private MyBluetoothDeviceAdapter mRecordAdapter;
    // 蓝牙传感器
    private BluetoothAdapter mBluetoothAdapter;
    // 蓝牙扫描器
    private BluetoothLeScanner mBluetoothScanner;
    private ScanCallback mScanCallback;
    // 蓝牙扫描器是否处于扫描状态
    private boolean isScanning = false;
    // 存放扫描到的蓝牙设备
    private HashMap<String, BluetoothDevice> mDevices;
    // 经典蓝牙广播接收器
    private BroadcastReceiver mOldBluetoothReceiver;
    // 存放以前绑定过的蓝牙设备
    private HashMap<String, BluetoothDevice> mRecords;
    // 由蓝牙连接到的设备的托管
    private BluetoothGatt mBluetoothGatt;
    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        LogUtils.getConfig().setGlobalTag(AppKey.AUTHOR);

        findViewByIds();

        initListeners();

        initRecyclerView();

        // 注册活动生命周期观察者
        registerLifecycleObserver();

        // 创建ViewModel观察者
        createViewModelObserver();
    }

    private void findViewByIds() {
        btnScan = findViewById(R.id.btn_scan);
        btnScanRecord = findViewById(R.id.btn_scan_record);
        btnReconnect = findViewById(R.id.btn_reconnect);
        tvState = findViewById(R.id.tv_state);
        rcDevices = findViewById(R.id.rv_device);
        rvRecord = findViewById(R.id.rv_record);
    }

    private void initListeners() {
        btnScan.setOnClickListener(this);
        btnScanRecord.setOnClickListener(this);
        btnReconnect.setOnClickListener(this);
    }

    private void initRecyclerView() {
        if (mDevices == null) {
            mDevices = new HashMap<>();
        }
        mDeviceAdapter = new MyBluetoothDeviceAdapter(new ArrayList<>());
        mDeviceAdapter.setOnItemClickListener(this::bluetoothConnect);
        rcDevices.setLayoutManager(new LinearLayoutManager(this));
        rcDevices.setAdapter(mDeviceAdapter);

        if (mRecords == null) {
            mRecords = new HashMap<>();
        }
        mRecordAdapter = new MyBluetoothDeviceAdapter(new ArrayList<>());
        rvRecord.setLayoutManager(new LinearLayoutManager(this));
        rvRecord.setAdapter(mRecordAdapter);
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
            for (int i = 0; i < bluetoothDevices.size(); i++) {
                LogUtils.d("绑定过的蓝牙设备:" + bluetoothDevices.keySet());
            }
            mRecordAdapter.setDataList(bluetoothDevices);
        });

        // 扫描到的蓝牙设备
        mViewModel.getBluetoothDevicesLiveData().observe(this, bluetoothDevices -> {
            for (int i = 0; i < bluetoothDevices.size(); i++) {
                LogUtils.d("扫描到的蓝牙设备:" + bluetoothDevices.keySet());
            }
            mDeviceAdapter.setDataList(bluetoothDevices);
        });

        // 蓝牙状态
        mViewModel.getBluetoothStateLiveData().observe(this, state -> {
            LogUtils.d("蓝牙状态:" + state);
            tvState.setText(state);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            mViewModel.getBluetoothStateLiveData().postValue("蓝牙已开启");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_scan:
                // 检测是否支持蓝牙4.0 BLE
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                    LogUtils.d("设备不支持蓝牙4.0");
                    // 经典蓝牙
                    oldBluetoothScan();
                } else {
                    // 蓝牙4.0扫描
                    bluetoothScan();
                }
                break;
            case R.id.btn_scan_record:
                // 已配对过的蓝牙设备记录
                bluetoothRecord();
                break;
            case R.id.btn_reconnect:
                // 重连设备
                String address = SPUtils.getInstance().getString(AppKey.BLUETOOTH_MAC);
                if (mBluetoothAdapter == null) {
                    BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
                    mBluetoothAdapter = manager.getAdapter();
                }

                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getAddress().equals(address)) {
                        bluetoothConnect(device);
                    }
                }
                break;
        }
    }

    /**
     * 已配对过的蓝牙设备记录
     */
    private void bluetoothRecord() {
        if (mBluetoothAdapter == null) {
            BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            mBluetoothAdapter = manager.getAdapter();
        }
        // 系统记录的已配对设备集合
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        HashMap<String, BluetoothDevice> hashMap = new HashMap<>();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                hashMap.put(deviceHardwareAddress, device);
            }
        }

        mViewModel.getRecordLiveData().setValue(hashMap);
    }

    /**
     * 经典蓝牙扫描
     */
    private void oldBluetoothScan() {
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

        if (mOldBluetoothReceiver == null) {
            mOldBluetoothReceiver = new OldBluetoothReceiver();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mOldBluetoothReceiver, filter);
        }
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * 蓝牙4.0扫描
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
            createScanCallback();
        }

        if (isScanning) {
            // 如果正在扫描，则取消蓝牙扫描；注意！需要和开启蓝牙传入的回调是同一个实例
            stopScan();
        } else {
            // 如果没开启扫描，则开启蓝牙扫描
            startScan();
        }
    }

    /**
     * 创建蓝牙扫描回调实例
     */
    private void createScanCallback() {
        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
                String address = device.getAddress();

                if (!mDevices.containsKey(address)) {
                    // 将初次扫描出的设备结果保存起来
                    mDevices.put(address, device);
                    mViewModel.getBluetoothDevicesLiveData().setValue(mDevices);
                }

                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    if (!mRecords.containsKey(address)) {
                        // 将已配对过的设备保存起来
                        mRecords.put(address, device);
                        mViewModel.getRecordLiveData().setValue(mRecords);
                    } else {
                        // 已配对且在附近的蓝牙设备，直接连接
                        bluetoothConnect(device);
                    }
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
    }

    /**
     * 如果正在扫描，则取消蓝牙扫描；注意！需要和开启蓝牙传入的回调是同一个实例
     */
    private void stopScan() {
        isScanning = false;
        // 解除定时任务
        disposables.clear();
        mBluetoothScanner.stopScan(mScanCallback);
        btnScan.setText("开启扫描");
        mViewModel.getBluetoothStateLiveData().setValue("蓝牙扫描已停止");
    }

    /**
     * 如果没开启扫描，则开启蓝牙扫描
     */
    private void startScan() {
        isScanning = true;

        // 设置扫描定时
        Disposable disposable = Observable.interval(12, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    // 12s后停止扫描
                    stopScan();
                });
        disposables.add(disposable);

        mBluetoothScanner.startScan(mScanCallback);
        btnScan.setText("停止扫描");
        mViewModel.getBluetoothStateLiveData().setValue("蓝牙扫描已开启");
    }

    /**
     * 连接蓝牙设备
     */
    private void bluetoothConnect(BluetoothDevice device) {
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            mBluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() {

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                    LogUtils.d("onConnectionStateChange:" + gatt.getDevice().getBondState(),
                            "status:" + status, "newState:" + newState);
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        SPUtils.getInstance().put(AppKey.BLUETOOTH_MAC, device.getAddress());
                        device.createBond();
                        mViewModel.getBluetoothStateLiveData().postValue("蓝牙已连接");
                        // 启动服务发现
                        mBluetoothGatt.discoverServices();
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        mViewModel.getBluetoothStateLiveData().postValue("蓝牙未连接");
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                    // 当您的 Android 应用成功连接到 GATT 服务器并发现服务后，应用便可在支持的位置读取和写入属性。
                    LogUtils.d("onServicesDiscovered", "status:" + status);
                    if (status == GATT_SUCCESS) {
                        ArrayList<String> uuidList = new ArrayList<>();
                        for (BluetoothGattService service : gatt.getServices()) {
                            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                                // 拿一个特征值来写操作，实际上仅仅只有支持写操作的特征值才能正常写入
                                byte[] bytes = new byte[4];
                                bytes[0] = (byte) 0x21;
                                bytes[1] = (byte) 0xFF;
                                bytes[2] = (byte) 0x00;
                                bytes[3] = (byte) 0x00;
                                characteristic.setWriteType(WRITE_TYPE_NO_RESPONSE);
                                characteristic.setValue(bytes);
                                boolean result = gatt.writeCharacteristic(characteristic);
                                if (result) {
                                    LogUtils.d("writeCharacteristic success:" + characteristic.getUuid());
                                }
                                uuidList.add(characteristic.getUuid().toString());
                            }
                        }
                        LogUtils.d("uuidList:" + uuidList.toString());
                    }
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    super.onCharacteristicRead(gatt, characteristic, status);
                    LogUtils.d("onCharacteristicRead", "status:" + status,
                            ConvertUtils.bytes2HexString(characteristic.getValue()));
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                                  int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    LogUtils.d("onCharacteristicWrite", "status:" + status,
                            ConvertUtils.bytes2HexString(characteristic.getValue()));
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                }
            });
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    protected void onStop() {
        stopScan();
        if (mOldBluetoothReceiver != null) {
            unregisterReceiver(mOldBluetoothReceiver);
        }
        super.onStop();
    }

    /**
     * 经典蓝牙广播接收器
     */
    private class OldBluetoothReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 发现设备，从Intent获取设备信息
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 设备名和设备Mac地址
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                if (!mDevices.containsKey(deviceHardwareAddress)) {
                    // 将初次扫描出的设备结果保存起来
                    mDevices.put(deviceHardwareAddress, device);
                    mViewModel.getBluetoothDevicesLiveData().setValue(mDevices);
                }
            }
        }
    }

}
