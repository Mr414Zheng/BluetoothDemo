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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: ZhengHuaizhi
 * Date: 2020/8/30
 * Description: 扫描到的蓝牙的RecyclerView适配器
 */
public class MyBluetoothDeviceAdapter extends RecyclerView.Adapter<MyBluetoothDeviceAdapter.BluetoothDeviceViewHolder> {

    // 扫描到的蓝牙列表
    private ArrayList<BluetoothDevice> mDataList;
    // item点击事件
    private OnItemClickListener listener;

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

        holder.itemView.setOnClickListener(v -> listener.onItemClick(device));
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    /**
     * 设置新数据
     */
    public void setDataList(Map<String, BluetoothDevice> dataList) {
        // 之后应显示的mac地址列表
        Set<String> keySet = dataList.keySet();
        // 之前显示的mac地址列表
        List<String> addressList = new ArrayList<>();
        // 新扫描出的蓝牙设备列表
        List<BluetoothDevice> newDevices = new ArrayList<>();

        for (BluetoothDevice device : mDataList) {
            addressList.add(device.getAddress());
        }

        for (String key : keySet) {
            if (!addressList.contains(key)) {
                // 如果为之前未显示的mac地址，添加进新设备列表
                newDevices.add(dataList.get(key));
            }
        }

        mDataList.addAll(newDevices);
        notifyItemRangeInserted(mDataList.size(), newDevices.size());
    }

    /**
     * item点击事件
     */
    public interface OnItemClickListener {
        void onItemClick(BluetoothDevice device);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
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
