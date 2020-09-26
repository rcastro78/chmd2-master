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
import mx.edu.chmd1.modelos.Padre;

public class PadreAdapter extends BaseAdapter {
    protected Activity activity;
    protected ArrayList<Padre> items;
    Padre padre;
    ViewHolder holder=new ViewHolder();
    String TAG="AlumnosAdapter";
    Typeface tf,tfBold;
    public PadreAdapter(Activity activity, ArrayList<Padre> items) {
        this.activity = activity;
        this.items = items;
        tf = Typeface.createFromAsset(activity.getAssets(),"fonts/GothamRoundedBook_21018.ttf");
        tfBold = Typeface.createFromAsset(activity.getAssets(),"fonts/GothamRoundedBold_21016.ttf");
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

        padre = items.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.celda_padre, null);
            holder = new ViewHolder();
            holder.lblIdPadre = convertView.findViewById(R.id.lblIdPadre);
            holder.lblNombrePadre = convertView.findViewById(R.id.lblNombrePadre);
            holder.lblFamilia = convertView.findViewById(R.id.lblFamilia);
            holder.lblRol = convertView.findViewById(R.id.lblRol);
            holder.lblEmail = convertView.findViewById(R.id.lblEmail);

            holder.lblId = convertView.findViewById(R.id.lblId);
            holder.lblNom = convertView.findViewById(R.id.lblNom);
            holder.lblFam = convertView.findViewById(R.id.lblFam);
            holder.lblR = convertView.findViewById(R.id.lblR);
            holder.lblEml = convertView.findViewById(R.id.lblEml);

            holder.imgPadre = convertView.findViewById(R.id.imgPadre);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.lblIdPadre.setTypeface(tf);
        holder.lblNombrePadre.setTypeface(tfBold);
        holder.lblFamilia.setTypeface(tf);
        holder.lblRol.setTypeface(tf);
        holder.lblNom.setTypeface(tf);
        holder.lblEmail.setTypeface(tf);


        holder.lblIdPadre.setText(String.valueOf(padre.getId()));
        holder.lblNombrePadre.setText(padre.getNombre());
        holder.lblFamilia.setText(padre.getApellidos());
        holder.lblRol.setText(padre.getRol());
        holder.lblEmail.setText(padre.getCorreo());


        return convertView;
    }

    static class ViewHolder {
        TextView lblIdPadre,lblNombrePadre,lblFamilia,lblRol,lblEmail;
        ImageView imgPadre;
        TextView lblId,lblNom,lblFam,lblR,lblEml;

    }
}



