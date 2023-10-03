package com.example.letracker;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class MyListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final List<String> maintitle;
    private final List<String> subtitle;
    private final List<Integer> imgid;

    public MyListAdapter(Activity context, List<String> maintitle,List<String> subtitle, List<Integer> imgid) {
        super(context, R.layout.mylist, maintitle);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.maintitle=maintitle;
        this.subtitle=subtitle;
        this.imgid=imgid;
    }

    public boolean checkName(String Name){
        boolean check = this.maintitle.contains(Name);
        return check;
    }

    public boolean checkMac(String Mac){
        boolean check = this.subtitle.contains(Mac);
        return check;
    }

    public void addElement(String DeviceName, String MAC){
        this.maintitle.add(DeviceName);
        this.subtitle.add(MAC);
        this.imgid.add(R.drawable.ble);
        this.notifyDataSetChanged();
    }

    public String getMac(int position){
        return subtitle.get(position);
    }

    public String getName(int position){
        return maintitle.get(position);
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.mylist, null,true);

        TextView titleText = (TextView) rowView.findViewById(R.id.title);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView subtitleText = (TextView) rowView.findViewById(R.id.subtitle);

        titleText.setText(maintitle.get(position));
//        imageView.setImageResource(imgid.get(position));
        imageView.setImageResource(R.drawable.ble);
        subtitleText.setText(subtitle.get(position));

        return rowView;

    };
}
