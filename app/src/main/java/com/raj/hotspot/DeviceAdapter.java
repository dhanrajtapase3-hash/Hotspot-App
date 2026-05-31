package com.raj.hotspot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    public interface OnDeviceAction {
        void onBlock(int position);
    }

    private List<DeviceInfo> devices;
    private OnDeviceAction listener;

    public DeviceAdapter(List<DeviceInfo> devices, OnDeviceAction listener) {
        this.devices = devices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        DeviceInfo d = devices.get(position);
        h.name.setText(d.name);
        h.ip.setText(d.ip + " · " + d.mac);
        h.usage.setText(d.getFormattedUsage() + " used");
        h.progress.setProgress(d.getProgressPercent());
        h.blockBtn.setText(d.blocked ? "Unblock" : "Block");
        h.blockBtn.setAlpha(d.blocked ? 0.5f : 1f);
        h.blockBtn.setOnClickListener(v -> listener.onBlock(h.getAdapterPosition()));
        h.itemView.setAlpha(d.blocked ? 0.5f : 1f);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, ip, usage, deviceIcon;
        ProgressBar progress;
        Button blockBtn;

        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.deviceName);
            ip = v.findViewById(R.id.deviceIp);
            usage = v.findViewById(R.id.deviceUsage);
            deviceIcon = v.findViewById(R.id.deviceIcon);
            progress = v.findViewById(R.id.deviceProgress);
            blockBtn = v.findViewById(R.id.blockBtn);
        }
    }
}
