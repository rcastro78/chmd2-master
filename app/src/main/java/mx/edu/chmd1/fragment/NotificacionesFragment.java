package mx.edu.chmd1.fragment;
import org.apache.http.HttpEntity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mx.edu.chmd1.AppCHMD;
import mx.edu.chmd1.CircularActivity;
import mx.edu.chmd1.CircularDetalleActivity;
import mx.edu.chmd1.R;
import mx.edu.chmd1.adapter.CircularesAdapter;
import mx.edu.chmd1.adapter.NotificacionAdapter;
import mx.edu.chmd1.modelos.Circular;
import mx.edu.chmd1.modelosDB.DBCircular;
import mx.edu.chmd1.modelosDB.DBNotificacion;
import mx.edu.chmd1.networking.APIUtils;
import mx.edu.chmd1.networking.ICircularesCHMD;
import retrofit2.Call;
import retrofit2.Callback;

public class NotificacionesFragment extends Fragment {
    public ListView lstCirculares;
    public ArrayList<Circular> circulares = new ArrayList<>();
    public ArrayList<Circular> circulares2 = new ArrayList<>();
    ICircularesCHMD iCircularesCHMD;
    public NotificacionAdapter adapter = null;
    ArrayList<String> seleccionados = new ArrayList<String>();
    ArrayList<String> idsSeleccionados = new ArrayList<String>();

    static String METODO="getNotificaciones_iOS.php";

    ImageView imgEliminarSeleccionados;
    static String BASE_URL;
    static String RUTA;

    String idUsuarioCredencial;
    SharedPreferences sharedPreferences;
    int totalCirculares=0;
    int idUsuario=0;
    @Override
    public void onPause() {
        super.onPause();
        circulares.clear();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(hayConexion()){
            getCirculares(idUsuario);
        }
        else{
            leeCirculares(idUsuario);
            Toast.makeText(getActivity(),"Se muestran las circulares almacenadas en el dispositivo",Toast.LENGTH_LONG).show();
            Toast.makeText(getActivity().getApplicationContext(),"No hay conexión a Internet",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        BASE_URL = this.getString(R.string.BASE_URL);
        RUTA = this.getString(R.string.PATH);
        sharedPreferences = getActivity().getSharedPreferences(this.getString(R.string.SHARED_PREF), 0);
        idUsuarioCredencial = sharedPreferences.getString("idUsuarioCredencial","0");
        idUsuario = Integer.parseInt(idUsuarioCredencial);
        iCircularesCHMD = APIUtils.getCircularesService();
        View v = inflater.inflate(R.layout.fragment_notificaciones, container, false);
        lstCirculares = v.findViewById(R.id.lstCirculares);
        imgEliminarSeleccionados = v.findViewById(R.id.imgEliminarSeleccionados);


        final SwipeRefreshLayout pullToRefresh = v.findViewById(R.id.swiperefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                circulares.clear();
                getCirculares(idUsuario);// your code
                pullToRefresh.setRefreshing(false);
            }
        });

        lstCirculares.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    pullToRefresh.setEnabled(true);
                } else pullToRefresh.setEnabled(false);
            }
        });


        imgEliminarSeleccionados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hayConexion()) {
                    seleccionados = adapter.getSeleccionados();
                    if (seleccionados.size() > 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("¡Advertencia!");
                        builder.setMessage("¿Estás seguro que quieres eliminar estas notificaciones?");
                        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (int i = 0; i < seleccionados.size(); i++) {
                                    Circular c = (Circular) adapter.getItem(Integer.parseInt(seleccionados.get(i)));
                                    idsSeleccionados.add(c.getIdCircular());
                                }

                                //new EliminaAsyncTask(idsSeleccionados, idUsuarioCredencial).execute();
                                borrarCircular(idsSeleccionados,idUsuarioCredencial);
                                try {
                                    Thread.sleep(500);
                                }catch (Exception ex){

                                }
                                leeCirculares(idUsuario);

                            }
                        });
                        builder.setNegativeButton("Cancelar", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        Toast.makeText(getActivity(), "Debes seleccionar al menos una notificación para utilizar esta opción", Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(getActivity().getApplicationContext(),"Esta función sólo está disponible con una conexión a Internet",Toast.LENGTH_LONG).show();
                }

            }
        });


        //Esto permite mover la lista hacia arriba a pesar de tener pullToRefresh
        lstCirculares.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    pullToRefresh.setEnabled(true);
                } else pullToRefresh.setEnabled(false);
            }
        });






        lstCirculares.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lstCirculares.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Se desplegará la circular

                Circular circular = (Circular)lstCirculares.getItemAtPosition(position);
                String idCircular = circular.getIdCircular();
                Intent intent = new Intent(getActivity(), CircularDetalleActivity.class);
                intent.putExtra("idCircular",idCircular);
                intent.putExtra("tipo",5);
                intent.putExtra("tituloCircular",circular.getNombre());
                intent.putExtra("contenidoCircular",circular.getContenido());
                intent.putExtra("fechaCircular",circular.getFecha2());
                intent.putExtra("viaNotif",0);
                intent.putExtra("temaIcs",circular.getTemaIcs());
                intent.putExtra("fechaIcs",circular.getFechaIcs());
                intent.putExtra("ubicaIcs",circular.getUbicacionIcs());
                intent.putExtra("horaInicioIcs",circular.getHoraInicialIcs());
                intent.putExtra("horaFinIcs",circular.getHoraFinalIcs());
                intent.putExtra("adjunto",circular.getAdjunto());
                intent.putExtra("nivel",circular.getPara());
                getActivity().startActivity(intent);
                getActivity().finish();

            }
        });

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        circulares.clear();
    }


    private void borrarCircular(ArrayList<String> idCirculares, String  idUsuarioCredencial) {
        for(String idCircular:idCirculares) {
            iCircularesCHMD.eliminarCircular(idCircular, idUsuarioCredencial)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                            if (response.isSuccessful()) {
                                Log.d("LEER", "Se marcó como no leída");
                                //Intent intent = new Intent(getActivity(),CircularActivity.class);
                                //startActivity(intent);
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Log.d("LEER", t.getMessage());
                        }
                    });

            new Update(DBNotificacion.class)
                    .set("leida=0 , favorita=0 , eliminada=1")
                    .where("idCircular=?",idCircular)
                    .execute();

        }
        //Intent intent = new Intent(getActivity(),CircularActivity.class);
        //startActivity(intent);

    }


    public void leeCirculares(int idUsuario){
        circulares.clear();
        seleccionados.clear();
        idsSeleccionados.clear();
        ArrayList<DBNotificacion> dbCirculares = new ArrayList<>();
        List<DBNotificacion> list = new Select().from(DBNotificacion.class).where("idUsuario=? AND eliminada=0",idUsuario).execute();
        dbCirculares.addAll(list);
        //llenar el adapter
        for(int i=0; i<dbCirculares.size(); i++){
            String idCircular = dbCirculares.get(i).idCircular;
            String nombre = dbCirculares.get(i).nombre;
            String fecha1 =dbCirculares.get(i).created_at;
            String fecha2 = dbCirculares.get(i).updated_at;


            String estado = "0";
            String favorito =  String.valueOf(dbCirculares.get(i).favorita);
            String leido = String.valueOf(dbCirculares.get(i).leida);
            String contenido = String.valueOf(dbCirculares.get(i).contenido);
            String fechaIcs = dbCirculares.get(i).fecha_ics;
            String para = dbCirculares.get(i).para;
            //Toast.makeText(getActivity(),contenido,Toast.LENGTH_LONG).show();

            circulares.add(new Circular(idCircular,
                    "Circular No. "+idCircular,
                    nombre,"",
                    fecha1,
                    fecha2,
                    estado,
                    Integer.parseInt(leido),
                    Integer.parseInt(favorito),
                    contenido,
                    fechaIcs,
                    para));



        } //fin del for
         adapter = new NotificacionAdapter(getActivity(),circulares);
        lstCirculares.setAdapter(adapter);

    }


    public void getCirculares(int usuario_id){

        final SimpleDateFormat formatoInicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final SimpleDateFormat formatoDestino = new SimpleDateFormat("HH:mm:ss");
        final SimpleDateFormat formatoDestino2 = new SimpleDateFormat("dd/MM/yyyy");
        circulares.clear();
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+RUTA+METODO+"?usuario_id="+usuario_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for(int i=0; i<response.length(); i++){
                                totalCirculares = response.length();
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                String idCircular = jsonObject.getString("id");
                                String nombre = jsonObject.getString("titulo");
                                String fecha1 = jsonObject.getString("fecha");
                                String fecha2 = jsonObject.getString("fecha");
                                Date date1=new Date(),date2=new Date();
                                try{
                                    date1 = formatoInicio.parse(fecha1);
                                    date2 = formatoInicio.parse(fecha2);
                                }catch (ParseException ex){

                                }
                                String strFecha1 = formatoDestino.format(date1);
                                String strFecha2 = formatoDestino2.format(date2);
                                String estado = jsonObject.getString("estatus");
                                String favorito = jsonObject.getString("favorito");
                                String leido = jsonObject.getString("leido");
                                String contenido = jsonObject.getString("contenido");
                                String eliminada = jsonObject.getString("eliminado");
                                String enviaTodos = jsonObject.getString("envia_todos");
                                String temaIcs = jsonObject.getString("tema_ics");
                                String fechaIcs = jsonObject.getString("fecha_ics");
                                String horaInicialIcs = jsonObject.getString("hora_inicial_ics");
                                String horaFinalIcs = jsonObject.getString("hora_final_ics");
                                String ubicacionIcs = jsonObject.getString("ubicacion_ics");
                                String adjunto = jsonObject.getString("adjunto");

                                String grados  = "";
                                try{
                                    grados = jsonObject.getString("grados");
                                }catch (Exception ex){

                                }
                                String adm = "";
                                try {
                                    adm = jsonObject.getString("adm");
                                    if (adm.equalsIgnoreCase("admin")) {
                                        adm = "";
                                    }
                                }catch (Exception ex){}
                                String rts ="";
                                String para="";
                                try{
                                    rts =  jsonObject.getString("rts");
                                    if(rts.equalsIgnoreCase("rutas")){rts="";}
                                }catch (Exception ex){}
                                String espec = jsonObject.getString("espec");


                                String nivel = "";
                                try{
                                    nivel=jsonObject.getString("nivel");
                                }catch (Exception ex){
                                    nivel="";
                                }

                                if(nivel.length()>0) {nivel="nivel: "+nivel+"/";}
                                if(grados.length()>0) {grados=" para: "+grados+"/";}
                                if(espec.length()>0) {espec=espec+"/";}
                                if(adm.length()>0) {adm=adm+"/";}
                                if(rts.length()>0) {espec=rts+"/";}

                                para = nivel+grados+espec+adm+rts;

                                try {
                                    para = para.substring(0, para.length() - 1);
                                }catch (Exception ex){
                                    Log.d("PARA",ex.getMessage());
                                }

                                if(enviaTodos.equalsIgnoreCase("0") && adm.equalsIgnoreCase("")
                                        && rts.equalsIgnoreCase("") && espec.equalsIgnoreCase("")
                                        && grados.equalsIgnoreCase("") & nivel.equalsIgnoreCase("")){
                                    para = "Personal";
                                }

                                if(enviaTodos.equalsIgnoreCase("1")){para="Todos";}


                                //No mostrar las eliminadas
                                if(Integer.parseInt(eliminada)==0) {
                                    circulares.add(new Circular(idCircular,
                                            "Circular No. " + idCircular,
                                            nombre, "",
                                            strFecha1,
                                            strFecha2,
                                            estado,
                                            Integer.parseInt(leido),
                                            Integer.parseInt(favorito),
                                            contenido,
                                            temaIcs,
                                            fechaIcs,
                                            horaInicialIcs,
                                            horaFinalIcs,
                                            ubicacionIcs,
                                            Integer.parseInt(adjunto),
                                            nivel,
                                            para));

                                }
                                    circulares2.add(new Circular(idCircular,
                                            "Circular No. " + idCircular,
                                            nombre, "",
                                            strFecha1,
                                            strFecha2,
                                            estado,
                                            Integer.parseInt(leido),
                                            Integer.parseInt(favorito),
                                            contenido,
                                            Integer.parseInt(eliminada),
                                            para,
                                            fechaIcs));


                                //String idCircular, String encabezado, String nombre,
                                //                    String textoCircular, String fecha1, String fecha2, String estado

                            }


                        }catch (JSONException e)
                        {
                            e.printStackTrace();

                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Error",
                                    Toast.LENGTH_LONG).show();
                        }
                        //TODO: Cambiarlo cuando pase a prueba en MX
                        // if (existe.equalsIgnoreCase("1")) {
                        //llenado de datos
                        //eliminar circulares y guardar las primeras 10 del registro
                        //Borra toda la tabla
                        new Delete().from(DBNotificacion.class).execute();
                        int maxRecuento = totalCirculares;

                        for(int i=0; i<maxRecuento; i++){
                            DBNotificacion dbCircular = new DBNotificacion();
                            dbCircular.idCircular = circulares2.get(i).getIdCircular();
                            dbCircular.leida = circulares2.get(i).getLeida();
                            if (circulares2.get(i).getLeida()==1){
                                dbCircular.no_leida = 0;
                            }
                            if (circulares2.get(i).getLeida()==0){
                                dbCircular.no_leida = 1;
                            }
                            dbCircular.idUsuario = idUsuario;
                            dbCircular.favorita = circulares2.get(i).getFavorita();
                            //dbCircular.compartida = circulares.get(i).getCompartida();
                            dbCircular.eliminada = circulares2.get(i).getEliminada();
                            dbCircular.nombre = circulares2.get(i).getNombre();
                            dbCircular.fecha_ics = circulares2.get(i).getFechaIcs();
                            dbCircular.contenido = circulares2.get(i).getContenido();
                            dbCircular.created_at = circulares2.get(i).getFecha1();
                            dbCircular.updated_at = circulares2.get(i).getFecha2();
                            dbCircular.para = circulares2.get(i).getPara();


                            Log.w("GUARDANDO",""+dbCircular.save());
                        }
                        try{
                            if(circulares.size()>0) {
                                adapter = new NotificacionAdapter(getActivity(), circulares);
                                lstCirculares.setAdapter(adapter);
                            }
                        }catch(Exception ex){
                            Toast.makeText(getActivity().getApplicationContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
                        }



                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                VolleyLog.d("ERROR", "Error: " + error.getMessage());


            }
        });

        // Adding request to request queue
        AppCHMD.getInstance().addToRequestQueue(req);
    }



    public boolean hayConexion() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();

    }






}
