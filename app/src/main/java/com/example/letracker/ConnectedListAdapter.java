package com.example.letracker;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ConnectedListAdapter extends ArrayAdapter<Sensor> {

    private final Activity context;

    private  List<Sensor> sensors = new ArrayList<>();

    public ConnectedListAdapter(Activity context, List<Sensor> mySensors) {
        super(context, R.layout.connectedlist, mySensors);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.sensors = mySensors;
    }

    public boolean checkMac(String Mac){
        boolean check = false;
        for (int i = 0; i < sensors.size(); i++) {
            if(sensors.get(i).getMac().equals(Mac)){
                check = true;
                break;
            }
        }
        return check;
    }

    public String getDeviceNameFromMac(String Mac){
        String name = "--";
        for (int i = 0; i < sensors.size(); i++) {
            if(sensors.get(i).getMac().equals(Mac)){
                name = sensors.get(i).getName();
                break;
            }
        }
        return name;
    }

    public int getDevicePosition(String Mac){
        int check = -1;
        for (int i = 0; i < sensors.size(); i++) {
            if(sensors.get(i).getMac().equals(Mac)){
                check = i;
                break;
            }
        }
        return check;
    }
    public void addElement(Sensor device, String newVal, String upTime){
        int position = getDevicePosition(device.getMac());
        if(position == -1){
            this.sensors.add(device);
            position = this.sensors.size()-1;
        }
//        this.sensors.get(position).newReading(newVal, upTime);
    }

    public void addElement(Sensor device){
        if(!checkMac(device.getMac())){
            this.sensors.add(device);
        }
        this.notifyDataSetChanged();
    }

    public boolean updateValues(String Mac, String val, String timeSt){
        int position = getDevicePosition(Mac);
//        boolean write = this.sensors.get(position).newReading(val, timeSt);

        this.notifyDataSetChanged();
        return true;
    }

    public boolean updateValues(String Mac, String val, String timeSt, String fileName){
        int position = getDevicePosition(Mac);
//        boolean write = this.sensors.get(position).newReading(val, timeSt, fileName);

        this.notifyDataSetChanged();
        return true;
    }

    public int getSize(){
        return this.sensors.size();
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.connectedlist, null,true);
        TextView sensorName = (TextView) rowView.findViewById(R.id.sensor);
        TextView sensorMac = (TextView) rowView.findViewById(R.id.mac);
        TextView sensorVal = (TextView) rowView.findViewById(R.id.val);
        TextView sensorUpTime = (TextView) rowView.findViewById(R.id.time);

        sensorName.setText(sensors.get(position).getName());
        sensorMac.setText(sensors.get(position).getMac());
        sensorVal.setText(sensors.get(position).getLatestReading());
        sensorUpTime.setText(sensors.get(position).getUpdatedAt());

        return rowView;

    };
}
