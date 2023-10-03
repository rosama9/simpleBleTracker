package com.example.letracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BroadcastReceiver_BTLE_GATT extends BroadcastReceiver {

    private boolean mConnected = false;
    private static String last_hrm = "";
    private static String last_rrInt = "";

    //private Activity_BTLE_Services activity;
    private  MConnectedActivity activity;

    //public BroadcastReceiver_BTLE_GATT(Activity_BTLE_Services activity) {
    public BroadcastReceiver_BTLE_GATT(MConnectedActivity activity) {
        this.activity = activity;
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a
    // result of read or notification operations.

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (BleService.ACTION_GATT_CONNECTED.equals(action)) {
            mConnected = true;
            Log.w("TAG", "in new connection");
            activity.device_connection_action(intent.getStringExtra("name"), intent.getStringExtra("mac"));
        }
        else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
            mConnected = false;
            activity.device_disconnection_action(intent.getStringExtra(BleService.EXTRA_DATA));
        }
        else if (BleService.NEW_DATA_AVAILABLE.equals(action)) {
            activity.device_characteristic_update_action(intent.getStringExtra("mac"), intent.getStringExtra("data"), intent.getStringExtra("time"));
        }
        else if (BleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
        }


        return;
    }
}
