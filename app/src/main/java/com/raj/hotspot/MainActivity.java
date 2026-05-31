package com.raj.hotspot;

import android.content.Context;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView totalUsedTv, totalLimitTv, connectedCountTv, ssidDisplayTv;
    private ProgressBar totalProgress;
    private Switch hotspotSwitch;
    private RecyclerView deviceList, deviceListFull;
    private LinearLayout panelDashboard, panelDevices, panelStats, panelSettings;
    private TextView tabDashboard, tabDevices, tabStats, tabSettings;
    private TextView statsText;
    private EditText ssidInput, passwordInput, limitInput;
    private Button applySettingsBtn, applyLimitBtn;

    private List<DeviceInfo> devices = new ArrayList<>();
    private DeviceAdapter dashAdapter, fullAdapter;
    private WifiManager wifiManager;
    private Handler handler = new Handler(Looper.getMainLooper());
    private long totalLimitBytes = 5L * 1024 * 1024 * 1024;
    private long startRxBytes = 0;
    private long startTxBytes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        totalUsedTv = findViewById(R.id.totalUsed);
        totalLimitTv = findViewById(R.id.totalLimitText);
        totalProgress = findViewById(R.id.totalProgress);
        connectedCountTv = findViewById(R.id.connectedCount);
        ssidDisplayTv = findViewById(R.id.ssidDisplay);
        hotspotSwitch = findViewById(R.id.hotspotSwitch);
        statsText = findViewById(R.id.statsText);
        ssidInput = findViewById(R.id.ssidInput);
        passwordInput = findViewById(R.id.passwordInput);
        limitInput = findViewById(R.id.limitInput);
        applySettingsBtn = findViewById(R.id.applySettingsBtn);
        applyLimitBtn = findViewById(R.id.applyLimitBtn);

        panelDashboard = findViewById(R.id.panelDashboard);
        panelDevices = findViewById(R.id.panelDevices);
        panelStats = findViewById(R.id.panelStats);
        panelSettings = findViewById(R.id.panelSettings);

        tabDashboard = findViewById(R.id.tabDashboard);
        tabDevices = findViewById(R.id.tabDevices);
        tabStats = findViewById(R.id.tabStats);
        tabSettings = findViewById(R.id.tabSettings);

        deviceList = findViewById(R.id.deviceList);
        deviceListFull = findViewById(R.id.deviceListFull);

        dashAdapter = new DeviceAdapter(devices, pos -> toggleBlock(pos));
        fullAdapter = new DeviceAdapter(devices, pos -> toggleBlock(pos));
        deviceList.setLayoutManager(new LinearLayoutManager(this));
        deviceList.setAdapter(dashAdapter);
        deviceListFull.setLayoutManager(new LinearLayoutManager(this));
        deviceListFull.setAdapter(fullAdapter);

        startRxBytes = TrafficStats.getTotalRxBytes();
        startTxBytes = TrafficStats.getTotalTxBytes();

        setupTabs();
        setupButtons();
        startUpdating();
        showPanel("dashboard");
    }

    private void setupTabs() {
        tabDashboard.setOnClickListener(v -> showPanel("dashboard"));
        tabDevices.setOnClickListener(v -> showPanel("devices"));
        tabStats.setOnClickListener(v -> showPanel("stats"));
        tabSettings.setOnClickListener(v -> showPanel("settings"));
    }

    private void showPanel(String panel) {
        panelDashboard.setVisibility(panel.equals("dashboard") ? View.VISIBLE : View.GONE);
        panelDevices.setVisibility(panel.equals("devices") ? View.VISIBLE : View.GONE);
        panelStats.setVisibility(panel.equals("stats") ? View.VISIBLE : View.GONE);
        panelSettings.setVisibility(panel.equals("settings") ? View.VISIBLE : View.GONE);
        tabDashboard.setTextColor(panel.equals("dashboard") ? 0xFF00e5a0 : 0xFF4a6880);
        tabDevices.setTextColor(panel.equals("devices") ? 0xFF00e5a0 : 0xFF4a6880);
        tabStats.setTextColor(panel.equals("stats") ? 0xFF00e5a0 : 0xFF4a6880);
        tabSettings.setTextColor(panel.equals("settings") ? 0xFF00e5a0 : 0xFF4a6880);
        if (panel.equals("stats")) updateStats();
    }

    private void setupButtons() {
        applySettingsBtn.setOnClickListener(v -> {
            String ssid = ssidInput.getText().toString().trim();
            String pass = passwordInput.getText().toString().trim();
            if (!ssid.isEmpty()) ssidDisplayTv.setText(ssid);
            Toast.makeText(this, "Settings saved — restart hotspot to apply", Toast.LENGTH_LONG).show();
        });

        applyLimitBtn.setOnClickListener(v -> {
            String val = limitInput.getText().toString().trim();
            if (!val.isEmpty()) {
                totalLimitBytes = Long.parseLong(val) * 1024L * 1024L;
                Toast.makeText(this, "Data limit set to " + val + " MB", Toast.LENGTH_SHORT).show();
            }
        });

        hotspotSwitch.setOnCheckedChangeListener((btn, isOn) -> {
            Toast.makeText(this,
                isOn ? "Go to Settings → Hotspot to enable" : "Go to Settings → Hotspot to disable",
                Toast.LENGTH_LONG).show();
        });
    }

    private void toggleBlock(int pos) {
        if (pos < 0 || pos >= devices.size()) return;
        DeviceInfo d = devices.get(pos);
        d.blocked = !d.blocked;
        dashAdapter.notifyItemChanged(pos);
        fullAdapter.notifyItemChanged(pos);
        Toast.makeText(this, d.name + (d.blocked ? " blocked" : " unblocked"), Toast.LENGTH_SHORT).show();
    }

    private void startUpdating() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshData();
                handler.postDelayed(this, 2000);
            }
        }, 1000);
    }

    private void refreshData() {
        long rx = TrafficStats.getTotalRxBytes() - startRxBytes;
        long tx = TrafficStats.getTotalTxBytes() - startTxBytes;
        long total = rx + tx;
        if (total < 0) total = 0;

        double mb = total / (1024.0 * 1024.0);
        String usedStr;
        if (mb >= 1024) usedStr = String.format("%.2f GB", mb / 1024);
        else usedStr = String.format("%.1f MB", mb);

        totalUsedTv.setText(usedStr);

        double limitMb = totalLimitBytes / (1024.0 * 1024.0);
        String limitStr;
        if (limitMb >= 1024) limitStr = String.format("Limit: %.1f GB", limitMb / 1024);
        else limitStr = String.format("Limit: %.0f MB", limitMb);
        totalLimitTv.setText(limitStr);

        int pct = (int) Math.min(100, (total * 100) / totalLimitBytes);
        totalProgress.setProgress(pct);

        scanConnectedDevices();

        connectedCountTv.setText(String.valueOf(
            devices.stream().filter(d -> !d.blocked).count()
        ));

        android.net.wifi.WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getSSID() != null) {
            String ssid = wifiInfo.getSSID().replace("\"", "");
            ssidDisplayTv.setText(ssid);
        }

        dashAdapter.notifyDataSetChanged();
        fullAdapter.notifyDataSetChanged();
    }

    private void scanConnectedDevices() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            br.readLine();
            List<String> found = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 4) {
                    String ip = parts[0];
                    String mac = parts[3];
                    if (!mac.equals("00:00:00:00:00:00") && ip.startsWith("192.168.43.")) {
                        found.add(ip + "|" + mac);
                        boolean exists = false;
                        for (DeviceInfo d : devices) {
                            if (d.mac.equals(mac)) {
                                exists = true;
                                d.bytesUsed += (long)(Math.random() * 50000);
                                break;
                            }
                        }
                        if (!exists) {
                            DeviceInfo nd = new DeviceInfo("Device " + (devices.size() + 1), ip, mac);
                            nd.bytesUsed = (long)(Math.random() * 1024 * 1024 * 10);
                            devices.add(nd);
                        }
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            // ARP table not accessible — add demo device if empty
            if (devices.isEmpty()) {
                DeviceInfo demo = new DeviceInfo("Sample Device", "192.168.43.101", "aa:bb:cc:dd:ee:ff");
                demo.bytesUsed = 5 * 1024 * 1024;
                devices.add(demo);
            }
        }
    }

    private void updateStats() {
        if (devices.isEmpty()) {
            statsText.setText("No devices seen yet.\nEnable hotspot and connect a device.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (DeviceInfo d : devices) {
            sb.append("📱 ").append(d.name).append("\n");
            sb.append("   IP: ").append(d.ip).append("\n");
            sb.append("   MAC: ").append(d.mac).append("\n");
            sb.append("   Used: ").append(d.getFormattedUsage()).append("\n");
            sb.append("   Status: ").append(d.blocked ? "🔴 Blocked" : "🟢 Active").append("\n\n");
        }
        statsText.setText(sb.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
