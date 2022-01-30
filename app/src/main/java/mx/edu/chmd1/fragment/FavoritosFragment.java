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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

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
import mx.edu.chmd1.modelos.Circular;
import mx.edu.chmd1.modelosDB.DBCircular;
import mx.edu.chmd1.networking.APIUtils;
import mx.edu.chmd1.networking.ICircularesCHMD;
import retrofit2.Call;
import retrofit2.Callback;

public class FavoritosFragment extends Fragment {
    public ListView lstCirculares;
    ArrayList<Circular> circulares = new ArrayList<>();
    public CircularesAdapter adapter = null;
    static String METODO="getCirculares_iOS.php";
    static String BASE_URL;
    static String RUTA;
    static int FAVORITAS=1;
    SharedPreferences sharedPreferences;
    static String METODO_REG="leerCircular.php";
    static String METODO_DEL="eliminarCircular.php";
    static String METODO_FAV="favCircular.php";
    ICircularesCHMD iCircularesCHMD;
    ImageView imgMoverFavSeleccionados,imgMoverLeidos,imgMoverNoLeidos,imgEliminarSeleccionados;
    String rsp="";
    String idUsuarioCredencial;
    ArrayList<String> seleccionados = new ArrayList<String>();
    ArrayList<String> idsSeleccionados = new ArrayList<String>();
    int idUsuario =0;
    public boolean hayConexion() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();

    }

    @Override
    public void onPause() {
        super.onPause();
        circulares.clear();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(hayConexion())
            getCirculares(idUsuario);
        else
            leeCirculares(idUsuario);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        BASE_URL = this.getString(R.string.BASE_URL);
        RUTA = this.getString(R.string.PATH);
        iCircularesCHMD = APIUtils.getCircularesService();
        sharedPreferences = getActivity().getSharedPreferences(this.getString(R.string.SHARED_PREF), 0);
        idUsuarioCredencial = sharedPreferences.getString("idUsuarioCredencial","0");
        idUsuario = Integer.parseInt(idUsuarioCredencial);
        View v = inflater.inflate(R.layout.fragment_circulares, container, false);
        lstCirculares = v.findViewById(R.id.lstCirculares);

        imgMoverNoLeidos = v.findViewById(R.id.imgMoverNoLeidas);
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


        imgMoverNoLeidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(hayConexion()){
                seleccionados = adapter.getSeleccionados();
                if(seleccionados.size()>0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("¡Advertencia!");
                    builder.setMessage("¿Estás seguro que quieres marcar estas las circulares como no leídas?");
                    builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            for (int i = 0; i < seleccionados.size(); i++) {
                                Circular c = (Circular) adapter.getItem(Integer.parseInt(seleccionados.get(i)));
                                idsSeleccionados.add(c.getIdCircular());
                            }


                            noLeerCircular(idsSeleccionados,idUsuarioCredencial);
                            try {
                                Thread.sleep(500);
                            }catch (Exception ex){

                            }
                            leeCirculares(idUsuario);
                            /*for (int i = 0; i < seleccionados.size(); i++) {
                                Circular c = (Circular) adapter.getItem(Integer.parseInt(seleccionados.get(i)));
                                new FavAsyncTask(c.getIdCircular(),idUsuarioCredencial).execute();

                            }*/

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



        imgMoverLeidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hayConexion()){
                seleccionados = adapter.getSeleccionados();
                if(seleccionados.size()>0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("¡Advertencia!");
                    builder.setMessage("¿Estás seguro que quieres marcar estas las circulares como leídas?");
                    builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            for (int i = 0; i < seleccionados.size(); i++) {
                                Circular c = (Circular) adapter.getItem(Integer.parseInt(seleccionados.get(i)));
                                idsSeleccionados.add(c.getIdCircular());
                            }
                            leerCircular(idsSeleccionados,idUsuarioCredencial);
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
                if(hayConexion()){
                seleccionados = adapter.getSeleccionados();
                if(seleccionados.size()>0){
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
                }else{
                    Toast.makeText(getActivity(),"Debes seleccionar al menos una circular para utilizar esta opción",Toast.LENGTH_LONG).show();
                }
            }else{
                    Toast.makeText(getActivity().getApplicationContext(),"Esta función sólo está disponible con una conexión a Internet",Toast.LENGTH_LONG).show();
                }
            }
        });

        lstCirculares.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

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


        lstCirculares.setOnItemClickListener((parent, view, position, id) -> {
            //Se desplegará la circular
            Circular circular = (Circular)lstCirculares.getItemAtPosition(position);
            String idCircular = circular.getIdCircular();
            Intent intent = new Intent(getActivity(), CircularDetalleActivity.class);
            intent.putExtra("idCircular",idCircular);
            intent.putExtra("tipo",FAVORITAS);
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
            intent.putExtra("cfav",circular.getFavorita());
            getActivity().startActivity(intent);
            getActivity().finish();

        });
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        circulares.clear();
    }



    //dummy
    /*public void llenaCirculares(){
        circulares.clear();
        circulares.add(new Circular("1","Encabezado","Nueva Circular","Este es el texto de la circular","11/07/2019","11/07/2019",1));
        circulares.add(new Circular("2","Encabezado B","Nueva Circular","Este es el texto de la circular","11/07/2019","11/07/2019",1));
        circulares.add(new Circular("3","Encabezado C","Nueva Circular","Este es el texto de la circular","11/07/2019","11/07/2019",1));
        adapter = new CircularesAdapter(getActivity(),circulares);
        lstCirculares.setAdapter(adapter);
    }*/


    public void leeCirculares(int idUsuario){
        circulares.clear();
        seleccionados.clear();
        idsSeleccionados.clear();
        ArrayList<DBCircular> dbCirculares = new ArrayList<>();
        List<DBCircular> list = new Select().from(DBCircular.class).where("idUsuario=? AND favorita=1",idUsuario).execute();
        dbCirculares.addAll(list);
        //llenar el adapter
        for(int i=0; i<dbCirculares.size(); i++){
            String idCircular = dbCirculares.get(i).idCircular;
            String nombre = dbCirculares.get(i).nombre;
            String fecha1 =dbCirculares.get(i).created_at;
            String fecha2 = dbCirculares.get(i).updated_at;


            String estado = "0";
            String para =  String.valueOf(dbCirculares.get(i).para);
            String leido = String.valueOf(dbCirculares.get(i).leida);
            String contenido = String.valueOf(dbCirculares.get(i).contenido);
            String fechaIcs = dbCirculares.get(i).fecha_ics;
            //Toast.makeText(getActivity(),contenido,Toast.LENGTH_LONG).show();

            circulares.add(new Circular(idCircular,
                    "Circular No. "+idCircular,
                    nombre,"",
                    fecha1,
                    fecha2,
                    estado,
                    Integer.parseInt(leido),
                    1,
                    contenido,
                    fechaIcs,
                    para));



        } //fin del for
        //Toast.makeText(getActivity(),"Se muestran las circulares almacenadas en el dispositivo",Toast.LENGTH_LONG).show();
        adapter = new CircularesAdapter(getActivity(),circulares);
        lstCirculares.setAdapter(adapter);
        ((BaseAdapter) lstCirculares.getAdapter()).notifyDataSetChanged();
    }

    public void getCirculares(int usuario_id){

        final SimpleDateFormat formatoInicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final SimpleDateFormat formatoDestino = new SimpleDateFormat("HH:mm:ss");
        final SimpleDateFormat formatoDestino2 = new SimpleDateFormat("dd/MM/yyyy");
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+RUTA+METODO+"?usuario_id="+usuario_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {



                        try {
                            for(int i=0; i<response.length(); i++){
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


                                if(Integer.parseInt(favorito)==1){
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
                                        nivel,
                                        para));
                                //String idCircular, String encabezado, String nombre,
                                //                    String textoCircular, String fecha1, String fecha2, String estado

                            }
                            }




                        }catch (JSONException e)
                        {
                            e.printStackTrace();

                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Error: "+e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                        //TODO: Cambiarlo cuando pase a prueba en MX
                        // if (existe.equalsIgnoreCase("1")) {
                        //llenado de datos
                        //eliminar circulares y guardar las primeras 10 del registro
                        //Borra toda la tabla
                        /*new Delete().from(DBCircular.class).execute();

                        for(int i=0; i<10; i++){
                            DBCircular dbCircular = new DBCircular();
                            dbCircular.idCircular = circulares.get(i).getIdCircular();
                            dbCircular.estado = circulares.get(i).getEstado();
                            dbCircular.nombre = circulares.get(i).getNombre();
                            dbCircular.textoCircular = circulares.get(i).getTextoCircular();
                            dbCircular.save();
                        }*/
                        if(circulares.size()>0) {
                            adapter = new CircularesAdapter(getActivity(), circulares);
                            lstCirculares.setAdapter(adapter);
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



    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_circular, menu);
        MenuItem item = menu.findItem(R.id.searchBar);
        SearchView sv = new SearchView(((CircularActivity) getActivity()).getSupportActionBar().getThemedContext());
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(item, sv);
        /*sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                System.out.println("search query submit");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    adapter.getFilter().filter(newText);
                }catch (Exception ex){

                }
                return true;
            }
        });*/
    }


    private void noLeerCircular(ArrayList<String> idCirculares, String  idUsuarioCredencial) {
        for(String idCircular:idCirculares) {
            iCircularesCHMD.noLeerCircular(idCircular, idUsuarioCredencial)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                            if (response.isSuccessful()) {
                                Log.d("NO_LEER", "Se marcó como no leída");
                                //Intent intent = new Intent(getActivity(),CircularActivity.class);
                                //startActivity(intent);
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Log.d("NO_LEER", t.getMessage());
                        }
                    });

            new Update(DBCircular.class)
                    .set("leida=0 , favorita=0 , eliminada=0")
                    .where("idCircular=?",idCircular)
                    .execute();

        }
        //Intent intent = new Intent(getActivity(),CircularActivity.class);
        //startActivity(intent);

    }
    private void leerCircular(ArrayList<String> idCirculares, String  idUsuarioCredencial) {
        for(String idCircular:idCirculares) {
            iCircularesCHMD.leerCircular(idCircular, idUsuarioCredencial)
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

            //Actualizar la base de datos interna
            new Update(DBCircular.class)
                    .set("leida=1 , favorita=0 , eliminada=0")
                    .where("idCircular=?",idCircular)
                    .execute();


        }
        //Intent intent = new Intent(getActivity(),CircularActivity.class);
        //startActivity(intent);

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

            new Update(DBCircular.class)
                    .set("leida=0 , favorita=0 , eliminada=1")
                    .where("idCircular=?",idCircular)
                    .execute();

        }
        //Intent intent = new Intent(getActivity(),CircularActivity.class);
        //startActivity(intent);

    }



}
