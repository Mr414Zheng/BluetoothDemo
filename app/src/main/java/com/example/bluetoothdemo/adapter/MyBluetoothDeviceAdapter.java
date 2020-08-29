package com.example.bluetoothdemo.adapter;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bluetoothdemo.R;

import java.util.ArrayList;

/**
 * Author: ZhengHuaizhi
 * Date: 2020/8/30
 * Description: 扫描到的蓝牙的RecyclerView适配器
 */
public class MyBluetoothDeviceAdapter extends RecyclerView.Adapter<MyBluetoothDeviceAdapter.BluetoothDeviceViewHolder> {

    // 扫描到的蓝牙列表
    private ArrayList<BluetoothDevice> mDataList;

    public MyBluetoothDeviceAdapter(ArrayList<BluetoothDevice> mDataList) {
        this.mDataList = mDataList;
    }

    @NonNull
    @Override
    public BluetoothDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_bluetooth_device, parent, false);
        return new BluetoothDeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BluetoothDeviceViewHolder holder, int position) {
        BluetoothDevice device = mDataList.get(position);
        holder.tvName.setText(device.getName());
        holder.tvAddress.setText(device.getAddress());
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class BluetoothDeviceViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvAddress;

        BluetoothDeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAddress = itemView.findViewById(R.id.tv_address);
        }
    }
}
