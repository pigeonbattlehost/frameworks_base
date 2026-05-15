package com.android.server.net;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.net.wifi.WifiManager;

import com.android.server.SystemService;

public class DataLimitService extends SystemService {

    private Context mContext;
    private Handler mHandler;

    private boolean mobileDisabled = false;
    private boolean wifiDisabled = false;

    private long lastMobileRx = 0;
    private long lastMobileTx = 0;

    public DataLimitService(Context context) {
        super(context);
        mContext = context;

        HandlerThread thread = new HandlerThread("DataLimitService");
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    @Override
    public void onStart() {
        // SystemService requires this even if it's empty, bruh
    }

    @Override
    public void onBootPhase(int phase) {
        if (phase == PHASE_SYSTEM_SERVICES_READY) {
            scheduleLoop();
        }
    }

    private void scheduleLoop() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkLimits();
                mHandler.postDelayed(this, 10_000);
            }
        }, 10_000);
    }

    private void checkLimits() {

        long mobileLimit = Settings.Global.getLong(
                mContext.getContentResolver(),
                "mobile_data_limit_bytes",
                -1
        );

        long wifiLimit = Settings.Global.getLong(
                mContext.getContentResolver(),
                "wifi_data_limit_bytes",
                -1
        );

        long mobileUsage = getMobileUsage();
        long wifiUsage = getWifiUsage();

        if (mobileLimit > 0 && mobileUsage > mobileLimit && !mobileDisabled) {
            disableMobileData();
            mobileDisabled = true;
        }

        if (wifiLimit > 0 && wifiUsage > wifiLimit && !wifiDisabled) {
            disableWifi();
            wifiDisabled = true;
        }
    }

    // mobile usage
    private long getMobileUsage() {
        long rx = TrafficStats.getMobileRxBytes();
        long tx = TrafficStats.getMobileTxBytes();
        return rx + tx;
    }

    // wifi usage
    private long getWifiUsage() {
        long total = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
        long mobile = getMobileUsage();
        return Math.max(0, total - mobile);
    }

    private void disableMobileData() {
        try {
            TelephonyManager tm =
                    mContext.getSystemService(TelephonyManager.class);

            if (tm != null) {
                tm.setDataEnabled(false);
            }
        } catch (Exception ignored) {}
    }

    private void disableWifi() {
        try {
            WifiManager wm =
                    mContext.getSystemService(WifiManager.class);

            if (wm != null) {
                wm.setWifiEnabled(false);
            }
        } catch (Exception ignored) {}
    }
}