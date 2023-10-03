package com.example.letracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

//activity_mconnected
public class MConnectedActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private String to_connect;
    private TextView Connection;

    ListView list;
    public ConnectedListAdapter adapter;

    public boolean recording = false;


    List<String> device_name =new ArrayList<>();
    List<String> device_mac =new ArrayList<>();
    List<Sensor> device =new ArrayList<>();



    private BleService mBLE_Service;
    private BroadcastReceiver_BTLE_GATT mGattUpdateReceiver;
    private boolean mBLE_Service_Bound;

    private Intent mBTLE_Service_Intent;

    ServiceConnection mBTLE_ServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BleService.BTLeServiceBinder binder = (BleService.BTLeServiceBinder) service;
            mBLE_Service = binder.getService();
            mBLE_Service_Bound = true;

            if (!mBLE_Service.initialize()) {
                Log.d("osama_antti", "Unable to initialize Bluetooth");
//                    finish();
            }
            else
            {
                Log.d("osama_antti", "initialized Bluetooth successfully");
                for (int i=0;i<device_mac.size();i++){
                    mBLE_Service.connect(device_mac.get(i));
                }
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBLE_Service = null;
            mBLE_Service_Bound = false;
        }
    };

    public void device_connection_action(String name, String mac)
    {
        Connection.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);
        Log.w("TAG", String.valueOf(adapter.getSize()));
        adapter.addElement(new Sensor(name, mac));
        Log.w("TAG", "new device added");


    }
    public void device_disconnection_action(String name)
    {
        //Code for any changes when a sensor is disconnected
        for (int i=0; i<device_name.size();i++){
            if(name.equals(device_name.get(i))){
                list.getChildAt(i).setBackgroundColor(Color.parseColor("#c2bbbb"));
            }
        }
    }

    boolean updateWriteTime;
    public void device_characteristic_update_action(String mac, String newVal, String upTime){
        if(recording){
            updateWriteTime = adapter.updateValues(mac, newVal, upTime);
            Log.w("Antti", "new record added to collection");
        }
//        if(updateWriteTime){
//
//        }
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mconnected);

        //Enable bluetooth if not enabled already
        int REQUEST_ENABLE_BT = 1;
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Intent intent = getIntent();
        to_connect = intent.getStringExtra("mac");
        device_mac = Arrays.asList(to_connect.split(",").clone());
        device_name = Arrays.asList(intent.getStringExtra("device").split(",").clone());

        for(int loop=0; loop<device_name.size(); loop++) {
            device.add(new Sensor(device_name.get(loop), device_mac.get(loop)));
        }
        Log.d("Osama", "name:"+ to_connect+" -- device:"+ intent.getStringExtra("device"));

        adapter=new ConnectedListAdapter(this, device);
        list=(ListView)findViewById(R.id.list);
        Connection=(TextView)findViewById(R.id.load);

        list.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();

        //Registering broadcast receiver for to receive latest heart rate values from our BleService
        mGattUpdateReceiver = new BroadcastReceiver_BTLE_GATT(this);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        //Register and start Ble service to connect with chest strap and receive updates from it whenever new heart rate value is received
        //BleService will broadcast latest value which is received in broadcast receiver.
        mBTLE_Service_Intent = new Intent(this, BleService.class);
        bindService(mBTLE_Service_Intent, mBTLE_ServiceConnection, Context.BIND_AUTO_CREATE);
        startService(mBTLE_Service_Intent);
    }

    public static IntentFilter makeGattUpdateIntentFilter() {

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleService.NEW_DATA_AVAILABLE);


        return intentFilter;
    }
}