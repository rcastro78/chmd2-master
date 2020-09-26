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
import mx.edu.chmd1.modelos.Alumno;

public class AlumnosAdapter extends BaseAdapter {
    protected Activity activity;
    protected ArrayList<Alumno> items;
    Alumno alumno;
    ViewHolder holder=new ViewHolder();
    String TAG="AlumnosAdapter";
    Typeface tf,tfBold;
    public AlumnosAdapter(Activity activity, ArrayList<Alumno> items) {
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

        alumno = items.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.celda_alumno, null);
            holder = new ViewHolder();
            holder.lblIdAlumno = convertView.findViewById(R.id.lblIdAlumno);
            holder.lblNomApeAlumno = convertView.findViewById(R.id.lblNomApeAlumno);
            holder.lblGradoAlumno = convertView.findViewById(R.id.lblGradoAlumno);

            holder.lblId = convertView.findViewById(R.id.lblId);
            holder.lblNom = convertView.findViewById(R.id.lblNom);
            holder.lblGrad = convertView.findViewById(R.id.lblGrad);

            holder.imgAlumno = convertView.findViewById(R.id.imgAlumno);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.lblIdAlumno.setTypeface(tf);
        holder.lblNomApeAlumno.setTypeface(tfBold);
        holder.lblGradoAlumno.setTypeface(tf);
        holder.lblIdAlumno.setTypeface(tf);
        holder.lblNom.setTypeface(tf);
        holder.lblGrad.setTypeface(tf);


        holder.lblIdAlumno.setText(String.valueOf(alumno.getIdAlumno()));
        holder.lblNomApeAlumno.setText(alumno.getNombre()+" "+alumno.getFamilia());
        holder.lblGradoAlumno.setText(alumno.getGrado());


        return convertView;
    }

    static class ViewHolder {
        TextView lblIdAlumno,lblNomApeAlumno,lblGradoAlumno;
        ImageView imgAlumno;
        TextView lblId,lblNom,lblGrad;

    }
}


