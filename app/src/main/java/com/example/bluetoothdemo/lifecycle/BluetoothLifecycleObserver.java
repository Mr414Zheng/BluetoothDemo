package com.example.bluetoothdemo.lifecycle;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;

import java.util.List;

/**
 * Author: ZhengHuaizhi
 * Date: 2020/8/29
 * Description: BluetoothActivity生命周期观察者
 */
public class BluetoothLifecycleObserver implements DefaultLifecycleObserver {

    private final String[] PERMISSIONS = {Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        LogUtils.getConfig().setGlobalTag("ZhengHuaizhi");
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        // 检查蓝牙权限
        if (!PermissionUtils.isGranted(PERMISSIONS)) {
            PermissionUtils.permission(PERMISSIONS).callback(new PermissionUtils.FullCallback() {
                @Override
                public void onGranted(@NonNull List<String> granted) {
                    StringBuilder sb = new StringBuilder("Granted:");
                    for (String str : granted) {
                        sb.append(str).append(";");
                    }
                    LogUtils.d(sb);
                }

                @Override
                public void onDenied(@NonNull List<String> deniedForever,
                                     @NonNull List<String> denied) {
                    StringBuilder sb = new StringBuilder("Granted:");
                    for (String str : denied) {
                        sb.append(str).append(";");
                    }
                    LogUtils.d(sb);
                }
            }).request();
        }
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {

    }
}
