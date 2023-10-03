package com.example.letracker;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Sensor {
    private String name;
    private String mac;
    private String lastReading;
    private String latestReading;
    private String updatedAt;

    public Sensor(String sensor, String address){
        this.name = sensor;
        this.mac = address;
        this.latestReading = "";
        this.updatedAt = "";
    }

    public Sensor(String sensor, String address, String reading, String upTime){
        this.name = sensor;
        this.mac = address;
        this.latestReading = reading;
        this.updatedAt = upTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getLatestReading() {
        if(latestReading.isEmpty()){
            return  "--";
        }
        return latestReading;
    }

    public void setLatestReading(String latestReading) {
        this.latestReading = latestReading;
    }

    public String getUpdatedAt() {
        if(updatedAt.isEmpty()){
            return  "--";
        }
        return updatedAt;
    }

    /*
    * This takes epoch time stamp as input and sets the date time to updated at*/
    public void setUpdatedAt(String updatedAt) {
        //this.updatedAt = updatedAt;
        long dv = Long.valueOf(updatedAt)*1000;// its need to be in milisecond
        Date df = new java.util.Date(dv);
        String vv = new SimpleDateFormat("MM dd, yyyy hh:mm:ss").format(df);
        this.updatedAt = vv;
    }

    private static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    private String getFileData(File path){
        String ret = "";
        try {
            FileInputStream fin = new FileInputStream(path);
            ret = convertStreamToString(fin);
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Make sure you close all streams.
        return ret;
    }

    public void generateNoteOnSD(String sFileName, String sBody) {
        sFileName = sFileName+".csv";
        try {
            File docs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            Log.d("File Path", "generateNoteOnSD: " +docs.getAbsolutePath());
            File root = new File(docs, "Antti");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            String temp = "";
            if(gpxfile.exists()){
                temp = getFileData(gpxfile);
            }else{
                gpxfile.createNewFile();
            }
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(temp);
            writer.append(sBody);
            writer.flush();
            writer.close();
            //Toast.makeText(context, "Saved"+gpxfile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            Log.d("File path", "File path for new file: "+gpxfile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
