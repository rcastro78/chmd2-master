package mx.edu.chmd1.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import mx.edu.chmd1.R;


public class CHMDSpinnerAdapter extends BaseAdapter {
    protected Activity activity;
    protected ArrayList<String> items;
    String item;
    ViewHolder holder=new ViewHolder();
    String TAG="AlumnosAdapter";
    Typeface tf,tfBold;
    public CHMDSpinnerAdapter(Activity activity, ArrayList<String> items) {
        this.activity = activity;
        this.items = items;
        tf = Typeface.createFromAsset(activity.getAssets(),"fonts/GothamRoundedBook_21018.ttf");

    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        item = items.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_spinner, null);
            holder = new ViewHolder();
            holder.lblItem = convertView.findViewById(R.id.itemSpinner);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.lblItem.setTypeface(tf);
        holder.lblItem.setText(item);
        return convertView;
    }

    static class ViewHolder {
        TextView lblItem;

    }
}


