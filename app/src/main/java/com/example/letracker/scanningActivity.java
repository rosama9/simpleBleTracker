package com.example.letracker;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class scanningActivity extends AppCompatActivity {
    private int REQUEST_ENABLE_BT = 99; // Any positive integer should work.
    private BluetoothAdapter mBluetoothAdapter;
    final Handler mHandler= new Handler();
    private final long SCAN_PERIOD = 10000;

    private String tagName = "Osama";

    List<String> scanned =new ArrayList<>();
    List<String> maintitle =new ArrayList<>();
    List<String> subtitle =new ArrayList<>();
    List<Integer> imgid =new ArrayList<>();
    List<Integer> selected =new ArrayList<>();

    ListView list;
    private Button scan, connect;
    public MyListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        scan= (Button) findViewById(R.id.startScan);
        connect= (Button) findViewById(R.id.connect);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if( !mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }


        adapter=new MyListAdapter(this, maintitle, subtitle,imgid);
        list=(ListView)findViewById(R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!selected.contains(position)){
//                  Removes the previously selected device if any device is selected
//                    if(selected.size() > 0) {
//                        list.getChildAt(selected.get(0)).setBackgroundColor(0x00000000);
//                        selected.clear();
//                    }

//                  Highlight currently selected device
                    selected.add(position);
                    list.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.teal_700));
                    connect.setVisibility(View.VISIBLE);
                }else{
                    removeSelectedDevice(position);
                }

            }
        });


        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tagName, "scan requested");
                scan.setVisibility(View.GONE);
                scanDevices(true);
            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToDevice();
            }
        });

    }

    public void connectToDevice(){
        String mac = "";
        String Name = "";
        for (int i=0;i<selected.size();i++){
            if(i != 0){
                mac = mac +",";
                Name = Name +",";
            }
            mac = mac + adapter.getMac(selected.get(i));
            Name = Name + adapter.getName(selected.get(i));
        }
        Toast.makeText(this,"macs:"+mac+" -- Names:"+Name, Toast.LENGTH_LONG).show();
//        Intent i = new Intent(scaningActivity.this, MonitorActivity.class);
//        i.putExtra("mac",mac);
//        i.putExtra("device",Name);
//        startActivity(i);
    }

    private void removeSelectedDevice(int position){
        selected.remove(selected.indexOf(position));
        list.getChildAt(position).setBackgroundColor(0x00000000);
        if(selected.isEmpty()){
            connect.setVisibility(View.GONE);
        }
    }

    public void scanDevices(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCall);
                    scan.setVisibility(View.VISIBLE);
                }
            }, SCAN_PERIOD);

            Log.d(tagName, "Ble scan started");

            ScanFilter.Builder builder = new ScanFilter.Builder();
            ScanFilter filter = builder.build();

            ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

            mBluetoothAdapter.getBluetoothLeScanner().startScan(Collections.singletonList(filter), scanSettings, mScanCall);

            scan.setVisibility(View.GONE);
        } else {
            //mScanning = false;
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCall);
            scan.setVisibility(View.VISIBLE);
        }
    }


    private void onDeviceDetectedFunction(BluetoothDevice device, int mrssi){
        if (!scanned.contains(device.getAddress())) {
            Log.w(tagName, "Name:"+device.getName()+"\r\nMAC:"+device.getAddress());
            if (!TextUtils.isEmpty(device.getName())) {
                Log.w(tagName, "new device added to found list");
//                subtitle.add(device.getAddress());
//                maintitle.add(device.getName());
                scanned.add(device.getAddress());
                adapter.addElement(device.getName(), device.getAddress());
            }
        }
    }

    private ScanCallback mScanCall = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            int rssi = result.getRssi();
            Log.d(tagName, "new method: found device = " + device.getName());
            Log.d(tagName, "new method: device rssi = " + rssi);
            onDeviceDetectedFunction(device, rssi);
        }
    };
}