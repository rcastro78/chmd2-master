package mx.edu.chmd1.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import mx.edu.chmd1.R;
import mx.edu.chmd1.modelos.Menu;

public class MenuAdapter extends BaseAdapter {
    protected Activity activity;
    protected ArrayList<Menu> items;
    Menu m;
    ViewHolder holder=new ViewHolder();
    String TAG="MenuAdapter";

    public MenuAdapter(Activity activity, ArrayList<Menu> items) {
        this.activity = activity;
        this.items = items;
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

        m = items.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.celda_menu, null);
            holder = new ViewHolder();
            //holder.lblNomMenu = convertView.findViewById(R.id.lblNomMenu);
            holder.imgMenu = convertView.findViewById(R.id.imgMenu);
            Typeface tf1 = Typeface.createFromAsset(activity.getAssets(),"fonts/GothamRoundedBold_21016.ttf");
            //holder.lblNomMenu.setTypeface(tf1);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        //holder.lblNomMenu.setText(m.getNombreMenu());
        holder.imgMenu.setImageResource(m.getIdImagen());


        return convertView;
    }

    static class ViewHolder {
        //TextView lblNomMenu;
        ImageView imgMenu;


    }
}

