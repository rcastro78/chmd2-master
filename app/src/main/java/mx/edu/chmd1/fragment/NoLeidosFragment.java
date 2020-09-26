package mx.edu.chmd1.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

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
import mx.edu.chmd1.modelos.Circular;
import mx.edu.chmd1.modelosDB.DBCircular;

public class NoLeidosFragment extends Fragment {
    public ListView lstCirculares;
    public ArrayList<Circular> circulares = new ArrayList<>();
    public ArrayList<Circular> circulares2 = new ArrayList<>();
    public CircularesAdapter adapter = null;
    ArrayList<String> seleccionados = new ArrayList<String>();
    ArrayList<String> idsSeleccionados = new ArrayList<String>();
    static int NOLEIDAS=2;
    private SearchView.OnQueryTextListener queryTextListener;
    static String METODO="getCircularesUsuarios.php";
    static String METODO_REG="leerCircular.php";
    static String METODO_DEL="eliminarCircular.php";
    static String METODO_FAV="favCircular.php";
    ImageView imgMoverFavSeleccionados,imgMoverLeidos,imgEliminarSeleccionados;
    static String BASE_URL;
    static String RUTA;
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
        //Toast.makeText(getActivity().getApplicationContext(),idUsuarioCredencial,Toast.LENGTH_LONG).show();

        View v = inflater.inflate(R.layout.fragment_circulares_no_leidas, container, false);
        lstCirculares = v.findViewById(R.id.lstCirculares);
        imgMoverFavSeleccionados = v.findViewById(R.id.imgMoverFavSeleccionados);
        imgMoverLeidos = v.findViewById(R.id.imgMoverComp);
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



        imgMoverFavSeleccionados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hayConexion()) {
                    seleccionados = adapter.getSeleccionados();
                    if (seleccionados.size() > 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("¡Advertencia!");
                        builder.setMessage("¿Estás seguro que quieres mover estas circulares a favoritas?");
                        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (int i = 0; i < seleccionados.size(); i++) {
                                    Circular c = (Circular) adapter.getItem(Integer.parseInt(seleccionados.get(i)));
                                    idsSeleccionados.add(c.getIdCircular());
                                }

                                new FavAsyncTask(idsSeleccionados, idUsuarioCredencial).execute();



                            }
                        });
                        builder.setNegativeButton("Cancelar", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        Toast.makeText(getActivity(), "Debes seleccionar al menos una circular para utilizar esta opción", Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(getActivity().getApplicationContext(),"Esta función sólo está disponible con una conexión a Internet",Toast.LENGTH_LONG).show();
                }
            }
        });



        imgMoverLeidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hayConexion()){
                    seleccionados = adapter.getSeleccionados();
                    if(seleccionados.size()>0){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("¡Advertencia!");
                        builder.setMessage("¿Estás seguro que quieres mover estas circulares a leídas?");
                        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (int i = 0; i < seleccionados.size(); i++) {
                                    Circular c = (Circular) adapter.getItem(Integer.parseInt(seleccionados.get(i)));
                                    idsSeleccionados.add(c.getIdCircular());
                                }

                                new RegistrarLecturaAsyncTask(idsSeleccionados,idUsuarioCredencial).execute();



                            }
                        });
                        builder.setNegativeButton("Cancelar", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }else{
                        Toast.makeText(getActivity(),"Debes seleccionar al menos una circular para utilizar esta opción",Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getActivity().getApplicationContext(),"Esta función sólo está disponible con una conexión a Internet",Toast.LENGTH_LONG).show();

                }
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
                        builder.setMessage("¿Estás seguro que quieres eliminar estas circulares?");
                        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (int i = 0; i < seleccionados.size(); i++) {
                                    Circular c = (Circular) adapter.getItem(Integer.parseInt(seleccionados.get(i)));
                                    idsSeleccionados.add(c.getIdCircular());
                                }

                                new EliminaAsyncTask(idsSeleccionados, idUsuarioCredencial).execute();



                            }
                        });
                        builder.setNegativeButton("Cancelar", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        Toast.makeText(getActivity(), "Debes seleccionar al menos una circular para utilizar esta opción", Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(getActivity().getApplicationContext(),"Esta función sólo está disponible con una conexión a Internet",Toast.LENGTH_LONG).show();
                }

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
                intent.putExtra("tipo",NOLEIDAS);
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
        List<DBCircular> list = new Select().from(DBCircular.class).where("idUsuario=?",idUsuario).execute();
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
        adapter = new CircularesAdapter(getActivity(),circulares);
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
                                //No mostrar las eliminadas ni favoritas
                                if(Integer.parseInt(leido)==0 && Integer.parseInt(favorito)==0){
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
                        new Delete().from(DBCircular.class).execute();
                        int maxRecuento = totalCirculares;

                        for(int i=0; i<maxRecuento; i++){
                            DBCircular dbCircular = new DBCircular();
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
                            adapter = new CircularesAdapter(getActivity(),circulares);
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


    private class FavAsyncTask extends AsyncTask<Void, Long, Boolean> {
        private ArrayList<String> idCircular;
        private String idUsuario;

        public FavAsyncTask(ArrayList<String> idCircular, String idUsuario) {
            this.idCircular = idCircular;
            this.idUsuario = idUsuario;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("RESPONSE","ejecutando...");
        }

        public void hacerFavorita(String idCircular){
            HttpClient httpClient;
            HttpPost httppost;
            httpClient = new DefaultHttpClient();
            httppost = new HttpPost(BASE_URL+RUTA+METODO_FAV);
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<>(2);
                nameValuePairs.add(new BasicNameValuePair("circular_id",idCircular));
                nameValuePairs.add(new BasicNameValuePair("usuario_id",idUsuario));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpClient.execute(httppost);
                int responseCode = response.getStatusLine().getStatusCode();
                Log.d("RESPONSE", ""+responseCode);
                switch(responseCode) {
                    case 200:
                        HttpEntity entity = response.getEntity();
                        if(entity != null) {
                            String responseBody = EntityUtils.toString(entity);
                            rsp=responseBody;
                        }
                        break;
                }
                Log.d("RESPONSE", rsp);




            }catch (Exception e){
                Log.d("RESPONSE",e.getMessage());
            }



        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Log.d("RESPONSE","ejecutado.-");
            Intent intent = new Intent(getActivity(),CircularActivity.class);
            startActivity(intent);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            for (String c: idCircular){
                hacerFavorita(c);
            }

            return null;
        }
    }

    private class RegistrarLecturaAsyncTask extends AsyncTask<Void, Long, Boolean> {
        private ArrayList<String> idCircular;
        private String idUsuario;

        public RegistrarLecturaAsyncTask(ArrayList<String> idCircular, String idUsuario) {
            this.idCircular = idCircular;
            this.idUsuario = idUsuario;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("RESPONSE","ejecutando...");
        }

        public void registraLectura(String idCircular){
            HttpClient httpClient;
            HttpPost httppost;
            httpClient = new DefaultHttpClient();
            httppost = new HttpPost(BASE_URL+RUTA+METODO_REG);
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<>(2);
                nameValuePairs.add(new BasicNameValuePair("circular_id",idCircular));
                nameValuePairs.add(new BasicNameValuePair("usuario_id",idUsuario));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpClient.execute(httppost);
                int responseCode = response.getStatusLine().getStatusCode();
                Log.d("RESPONSE", ""+responseCode);
                switch(responseCode) {
                    case 200:
                        HttpEntity entity = response.getEntity();
                        if(entity != null) {
                            String responseBody = EntityUtils.toString(entity);
                            rsp=responseBody;
                        }
                        break;
                }
                Log.d("RESPONSE", rsp);




            }catch (Exception e){
                Log.d("RESPONSE",e.getMessage());
            }



        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Log.d("RESPONSE","ejecutado.-");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            for(String c:idCircular)
                registraLectura(c);
            return null;
        }
    }
    private class EliminaAsyncTask extends AsyncTask<Void, Long, Boolean> {
        private ArrayList<String> idCircular;
        private String idUsuario;

        public EliminaAsyncTask(ArrayList<String> idCircular, String idUsuario) {
            this.idCircular = idCircular;
            this.idUsuario = idUsuario;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("RESPONSE","ejecutando...");
        }

        public void registraLectura(String idCircular){
            HttpClient httpClient;
            HttpPost httppost;
            httpClient = new DefaultHttpClient();
            httppost = new HttpPost(BASE_URL+RUTA+METODO_DEL);
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<>(2);
                nameValuePairs.add(new BasicNameValuePair("circular_id",idCircular));
                nameValuePairs.add(new BasicNameValuePair("usuario_id",idUsuario));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpClient.execute(httppost);
                int responseCode = response.getStatusLine().getStatusCode();
                Log.d("RESPONSE", ""+responseCode);
                switch(responseCode) {
                    case 200:
                        HttpEntity entity = response.getEntity();
                        if(entity != null) {
                            String responseBody = EntityUtils.toString(entity);
                            rsp=responseBody;
                        }
                        break;
                }
                Log.d("RESPONSE", rsp);




            }catch (Exception e){
                Log.d("RESPONSE",e.getMessage());
            }



        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Log.d("RESPONSE","ejecutado.-");
            Intent intent = new Intent(getActivity(),CircularActivity.class);
            startActivity(intent);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            for(String c:idCircular)
                registraLectura(c);
            return null;
        }
    }
}
