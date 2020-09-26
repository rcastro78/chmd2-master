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

public class NotificacionesFragment extends Fragment {
    public ListView lstCirculares;
    public ArrayList<Circular> circulares = new ArrayList<>();
    public ArrayList<Circular> circulares2 = new ArrayList<>();
    public NotificacionAdapter adapter = null;
    ArrayList<String> seleccionados = new ArrayList<String>();
    ArrayList<String> idsSeleccionados = new ArrayList<String>();
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    static String METODO="getNotificaciones_iOS.php";

    ImageView imgMoverFavSeleccionados,imgMoverLeidos,imgEliminarSeleccionados,imgMoverNoLeidas;
    static String BASE_URL;
    static String RUTA;
    static int TODAS=0;
    String rsp;
    String idUsuarioCredencial;
    SharedPreferences sharedPreferences;
    int totalCirculares=0;
    boolean todos=false;
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


        View v = inflater.inflate(R.layout.fragment_notificaciones, container, false);
        lstCirculares = v.findViewById(R.id.lstCirculares);

        final SwipeRefreshLayout pullToRefresh = v.findViewById(R.id.swiperefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                circulares.clear();
                getCirculares(idUsuario);// your code
                pullToRefresh.setRefreshing(false);
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
                if(!circular.getNivel().equalsIgnoreCase("null")){
                    intent.putExtra("nivel",circular.getNivel());
                }else{
                    intent.putExtra("nivel","");
                }


                getActivity().startActivity(intent);

            }
        });

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        circulares.clear();
    }




    public void leeCirculares(int idUsuario){
        circulares.clear();
        ArrayList<DBCircular> dbCirculares = new ArrayList<>();
        List<DBCircular> list = new Select().from(DBNotificacion.class).where("idUsuario=?",idUsuario).execute();
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

            //Toast.makeText(getActivity(),contenido,Toast.LENGTH_LONG).show();

            circulares.add(new Circular(idCircular,
                    "Circular No. "+idCircular,
                    nombre,"",
                    fecha1,
                    fecha2,
                    estado,
                    Integer.parseInt(leido),
                    Integer.parseInt(favorito),
                    contenido));



        } //fin del for
        Toast.makeText(getActivity(),"Se muestran las circulares almacenadas en el dispositivo",Toast.LENGTH_LONG).show();
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

                                String temaIcs = jsonObject.getString("tema_ics");
                                String fechaIcs = jsonObject.getString("fecha_ics");
                                String horaInicialIcs = jsonObject.getString("hora_inicial_ics");
                                String horaFinalIcs = jsonObject.getString("hora_final_ics");
                                String ubicacionIcs = jsonObject.getString("ubicacion_ics");
                                String adjunto = jsonObject.getString("adjunto");
                                String nivel = "";
                                try{
                                    nivel=jsonObject.getString("nivel");
                                }catch (Exception ex){
                                    nivel="";
                                }
                                //No mostrar las eliminadas
                                if(Integer.parseInt(eliminada)==0){
                                    circulares.add(new Circular(idCircular,
                                            "Circular No. "+idCircular,
                                            nombre,"",
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
                                            nivel));
                                }

                                circulares2.add(new Circular(idCircular,
                                        "Circular No. "+idCircular,
                                        nombre,"",
                                        strFecha1,
                                        strFecha2,
                                        estado,
                                        Integer.parseInt(leido),
                                        Integer.parseInt(favorito),
                                        contenido,
                                        Integer.parseInt(eliminada)));


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
                            //dbCircular.textoCircular = circulares.get(i).getTextoCircular();
                            dbCircular.contenido = circulares2.get(i).getContenido();
                            dbCircular.created_at = circulares2.get(i).getFecha1();
                            dbCircular.updated_at = circulares2.get(i).getFecha2();
                            Log.w("GUARDANDO",""+dbCircular.save());
                        }
                        try{
                            adapter = new NotificacionAdapter(getActivity(),circulares);
                            lstCirculares.setAdapter(adapter);
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

                Toast.makeText(getActivity().getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
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
