package com.example.letracker;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.UUID;

public class BleService extends Service {
    private final static String TAG = BleService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private static BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public static  int readRssiFlag = 1;
    public static int connectFlag = 0;
    public  static boolean connectionState;
    public static boolean serviceDiscoveryComplete = false;
    MConnectedActivity mo;


    public final static String ACTION_GATT_CONNECTED = "BleService.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "BleService.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "BleService.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "BleService.ACTION_DATA_AVAILABLE";
    public final static String NEW_DATA_AVAILABLE = "BleService.NEW_DATA_AVAILABLE";
    public final static String EXTRA_UUID = "BleService.EXTRA_UUID";
    public final static String EXTRA_DATA = "BleService.EXTRA_DATA";

    public final static String CUSTOM_SERVICE_UUID =  "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public final static String MY_CHAR_UUID =  "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public final static String HR_CHAR_UUID =  "00002a37-0000-1000-8000-00805f9b34fb";


    public UUID HEART_RATE_SERVICE_UUID = convertFromInteger(0x180D);
    static BluetoothGattService CUSTOM_SERVICE;
    static BluetoothGattService HEART_RATE_SERVICE;
    public String hrm_value = "---";

    private boolean development = false;

    private boolean mScanning;
    final Handler mHandler= new Handler();
    private final long SCAN_PERIOD = 5000;


    String char_value, char_name, csv_row = "--";
    String currentTime;

    /*
     * This function is used to cast given int value to UUID
     */
    public UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {

            Log.d("osama","onConnectionStateChange ="+ newState+" device = "+ gatt.getDevice().getName()+" - "+gatt.getDevice().getAddress());

            if(newState == 2){
                connectionState = true;
            }else{
                connectionState = false;
            }
            //MonitorActivity.adapter.notifyDataSetChanged();
            connectFlag = 0;
            if(connectionState == true)
            {
                if(mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.cancelDiscovery();
                }
                //mBluetoothGatt.disconnect();
                mBluetoothGatt = gatt;
                //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                mBluetoothGatt.discoverServices();
                serviceDiscoveryComplete = false;
            }
            else
            {
                Log.d("osama","onConnectionStateChange (closed) ="+ newState+" device = "+ gatt.getDevice().getName()+" - "+gatt.getDevice().getAddress());
                mBluetoothGatt.close();
                //connect(gatt.getDevice().getAddress());
            }

            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;

                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction, gatt.getDevice().getName(), gatt.getDevice().getAddress());

                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                // Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;

                mConnectionState = STATE_DISCONNECTED;

                Log.i(TAG, "Disconnected from GATT server.");
                connect(gatt.getDevice().getAddress());

                broadcastUpdate(intentAction, gatt.getDevice().getName());
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d("umarsajjad","services discoverd");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                serviceDiscoveryComplete = true;
                mBluetoothGatt = gatt;


                if(development){
                    HEART_RATE_SERVICE = mBluetoothGatt.getService(HEART_RATE_SERVICE_UUID);
                    get_hrsChar(mBluetoothGatt);
                }else{
                    CUSTOM_SERVICE = mBluetoothGatt.getService(UUID.fromString(CUSTOM_SERVICE_UUID));
                    get_custom_char(mBluetoothGatt);
                }

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
            broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //flag = 1;
            mBluetoothGatt = gatt;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "char uuid: " + characteristic.getUuid().toString());
                if(characteristic.getUuid().toString().equals("00002a19-0000-1000-8000-00805f9b34fb")){
                    get_hrsChar(mBluetoothGatt);
                }else{
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    Log.w(TAG, "hrs read alue: " + characteristic.getValue().toString());
                }
            }
            readRssiFlag = 1;
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            mBluetoothGatt = gatt;
            if(development){
                if(characteristic.getUuid().toString().equals(HR_CHAR_UUID)) {
                    char_value = hexToString(characteristic.getValue());
                    char_name = gatt.getDevice().getName();
                    long seconds = System.currentTimeMillis() / 1000;
                    currentTime = String.valueOf(seconds);
                    broadcastUpdate(NEW_DATA_AVAILABLE, char_value, gatt.getDevice().getAddress(), currentTime);
                    Log.w(TAG, "new value: " + char_value + "  ---  time: " + currentTime);
                }
            }else{
                if(characteristic.getUuid().toString().equals(MY_CHAR_UUID)) {
                    char_value = hexToString(characteristic.getValue());
                    char_name = gatt.getDevice().getName();
                    long seconds = System.currentTimeMillis() / 1000;
                    currentTime = String.valueOf(seconds);
                    broadcastUpdate(NEW_DATA_AVAILABLE, char_value, gatt.getDevice().getAddress(), currentTime);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        }

    };

    /*
     * Following three functions are used send msgs across application. msgs from these broadcast are received in
     * broadcast receiver and corresponding actions.
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {

        final Intent intent = new Intent(action);

        intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());

        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();

        if (data != null && data.length > 0) {

            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + hexToString(data));
        } else {
            intent.putExtra(EXTRA_DATA, "0");
        }

        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final String data) {

        final Intent intent = new Intent(action);

        intent.putExtra(EXTRA_DATA, data);

        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final String deviceName, final String deviceMac) {

        final Intent intent = new Intent(action);

        intent.putExtra("name", deviceName);
        intent.putExtra("mac", deviceMac);

        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final String data, final String mac, final String time) {

        final Intent intent = new Intent(action);

        intent.putExtra("data", data);
        intent.putExtra("mac", mac);
        intent.putExtra("time", time);

        sendBroadcast(intent);
    }



    public static String hexToString(byte[] data) {
        final StringBuilder sb = new StringBuilder(data.length);

        for(byte byteChar : data) {
            sb.append(String.format("%02X ", byteChar));
        }

        return sb.toString();
    }

    public class BTLeServiceBinder extends Binder {

        BleService getService() {
            return BleService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {

    }

//    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new BTLeServiceBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e("Rana Osama", "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e("Rana Osama", "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        mo = new MConnectedActivity();
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    //public boolean connect(final String address) {
    public BluetoothGatt connect(final String address) {
        Log.d("Rana Osama","we are in connect mac = "+ address);
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        if (device == null) {
            Log.w("Rana Osama", "Device not found. Unable to connect.");
            //return false;
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        if(mBluetoothAdapter.isDiscovering()){
            //mBluetoothAdapter.startDiscovery();
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);

        Log.d("Rana Osama", "Trying to create a new connection.");
        mConnectionState = STATE_CONNECTING;

        return mBluetoothGatt;
        // return  true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w("Rana Osama", "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.disconnect();
    }

    protected static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    /**
     * This function is called after service discovery from connected device is complete this function is responsible to discover
     * heart rate value characteristic and set up notifications fro service once notifications are enabled the updates for
     * heart rate value are received in onCharecteristic changed function of ble callback.
     * */
    public static void get_hrsChar(BluetoothGatt gatt)
    {
        if(serviceDiscoveryComplete) {
            Log.d("osama", "setting notifications");

            BluetoothGattCharacteristic hrs_char = HEART_RATE_SERVICE.getCharacteristic(UUID.fromString(HR_CHAR_UUID));//(HEART_RATE_MEASUREMENT_CHAR_UUID);
            mBluetoothGatt.setCharacteristicNotification(hrs_char, true);
            BluetoothGattDescriptor descriptor = hrs_char.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
            descriptor.setValue(true ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[]{0x00, 0x00});
            mBluetoothGatt.writeDescriptor(descriptor);
            Log.d("osama", "setting notifications successful");

        }
        else
        {
            readRssiFlag = 1;
        }
    }

    public static void get_custom_char(BluetoothGatt gatt)
    {
        if(serviceDiscoveryComplete) {
            Log.d("Osama", "setting notifications");

            BluetoothGattCharacteristic my_char = CUSTOM_SERVICE.getCharacteristic(UUID.fromString(MY_CHAR_UUID));
            mBluetoothGatt.setCharacteristicNotification(my_char, true);
            BluetoothGattDescriptor descriptor = my_char.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
            descriptor.setValue(true ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[]{0x00, 0x00});
            mBluetoothGatt.writeDescriptor(descriptor);
            Log.d("Osama", "setting notifications successful");

        }
        else
        {
            readRssiFlag = 1;
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

                    Log.d("Rana Osama","found device = "+ device.getName().toString());
                }
            };


    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {

        if (mBluetoothGatt == null) {
            return;
        }

        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
}