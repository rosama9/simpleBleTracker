package com.example.letracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static int Spalsh_Time = 1000;
    private static final int REQUEST_ENABLE_BT = 1;
    private boolean location_coarse_permision = false;
    private boolean location_fine_permision = false;
    private boolean bluetooth_permision = false;
    private boolean storage_permision = false;

    private String tagName = "Osama";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkPermisions();
            }
        }, Spalsh_Time);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkPermisions();

        if (requestCode == 100) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                location_fine_permision = false;
                Log.d(tagName, "Please grant location permission");
            }
        }

    }


    private boolean checkBlutoothPermision(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            Log.d(tagName, "bluetooth permission required");
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { android.Manifest.permission.BLUETOOTH_ADMIN, android.Manifest.permission.BLUETOOTH }, 102);

        }
        else
        {
            Log.d(tagName, "bluetooth permission granted");
        }

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        return true;
    }

    private boolean checkFineLocationPermision(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            Log.d(tagName, "LOCATION permissions Required");

            ActivityCompat.requestPermissions(MainActivity.this, new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION }, 100);
        }else{
            Log.d(tagName, "FINE_LOCATION permission available");
        }
        return true;
    }

    private boolean checkStoragePermision(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            Log.d(tagName, "Storage permissions Required");

            ActivityCompat.requestPermissions(MainActivity.this, new String[] { android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE }, 103);
        }else{
            Log.d(tagName, "Storage permission available");
        }
        return true;
    }


    private void checkPermisions(){
        if(!location_fine_permision){
            location_fine_permision = true;
            checkFineLocationPermision();
        }

        if(!bluetooth_permision){
            bluetooth_permision = true;
            checkBlutoothPermision();
        }

        if(!storage_permision){
            storage_permision = true;
            checkStoragePermision();
        }

        if(location_fine_permision && bluetooth_permision && storage_permision){
            Intent i = new Intent(MainActivity.this, scanningActivity.class);
            startActivity(i);
        }
    }
}