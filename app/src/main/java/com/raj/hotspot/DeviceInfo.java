package com.raj.hotspot;

public class DeviceInfo {
    public String name;
    public String ip;
    public String mac;
    public long bytesUsed;
    public long dataLimit;
    public boolean blocked;
    public long connectedAt;

    public DeviceInfo(String name, String ip, String mac) {
        this.name = name;
        this.ip = ip;
        this.mac = mac;
        this.bytesUsed = 0;
        this.dataLimit = 0;
        this.blocked = false;
        this.connectedAt = System.currentTimeMillis();
    }

    public String getFormattedUsage() {
        double mb = bytesUsed / (1024.0 * 1024.0);
        if (mb >= 1024) return String.format("%.2f GB", mb / 1024);
        if (mb < 1) return String.format("%.0f KB", mb * 1024);
        return String.format("%.1f MB", mb);
    }

    public int getProgressPercent() {
        if (dataLimit <= 0) return 0;
        return (int) Math.min(100, (bytesUsed * 100) / dataLimit);
    }
}
