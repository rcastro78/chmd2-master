package mx.edu.chmd1.validaciones;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import mx.edu.chmd1.AppCHMD;
import mx.edu.chmd1.CircularDetalleActivity;
import mx.edu.chmd1.InicioActivity;
import mx.edu.chmd1.PrincipalActivity;
import mx.edu.chmd1.R;
import mx.edu.chmd1.adapter.CircularesAdapter;
import mx.edu.chmd1.modelos.Circular;
import mx.edu.chmd1.modelos.Circulares;
import mx.edu.chmd1.modelosDB.DBCircular;
import mx.edu.chmd1.networking.APIUtils;
import mx.edu.chmd1.networking.ICircularesCHMD;
import retrofit2.Call;
import retrofit2.Callback;

public class ValidarPadreActivity extends AppCompatActivity {
    static String VALIDAR_CUENTA="validarEmail.php";
    static String METODO="getCirculares_iOS.php";
    public ArrayList<Circular> circulares2 = new ArrayList<>();
    static String BASE_URL;
    static String RUTA;
    TextView lblMensaje;
    SharedPreferences sharedPreferences;
    String correo,existe;
    ICircularesCHMD iCircularesCHMD;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validar_padre);
        lblMensaje = findViewById(R.id.lblMensaje);
        iCircularesCHMD = APIUtils.getCircularesService();

        BASE_URL = this.getString(R.string.BASE_URL);
        RUTA = this.getString(R.string.PATH);
        sharedPreferences = this.getSharedPreferences(this.getString(R.string.SHARED_PREF), 0);
        correo = sharedPreferences.getString("correoRegistrado","");
        String idCircularNotif = "";
        int vn = 0;
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d("idCircularN", "llave: " + key + " valor: " + value);
                if(key.equalsIgnoreCase("idCircular")){
                    idCircularNotif = value.toString();
                }
                if(key.equalsIgnoreCase("viaNotificacion")){
                    vn = Integer.parseInt(value.toString());
                }
            }

            Log.d("idCircularN", idCircularNotif);
            Log.d("idCircularN", ""+vn);

            Intent intent = new Intent(this,CircularDetalleActivity.class);
            intent.putExtra("idCircular",idCircularNotif);
            intent.putExtra("viaNotificacion",vn);
            startActivity(intent);
            finish();
        }else{

            if(hayConexion()){
                String idUsuario = sharedPreferences.getString("idUsuarioCredencial","");
                Log.d("CIRCULAR2",idUsuario);
                //getCirculares2(idUsuario);
                new CircularesAsyncTask(idUsuario).execute();


            }else{
                int cuentaValida = sharedPreferences.getInt("cuentaValida",0);
                if(cuentaValida==1){
                    Intent i = new Intent(ValidarPadreActivity.this, PrincipalActivity.class);
                    startActivity(i);
                }
            }


        }
    }


    //Retrofit

    public void getCirculares2(String id_usuario){
        Call<List<Circulares>> circ = iCircularesCHMD.getCirculares(id_usuario);
        final Date[] date1 = {new Date()};
        final Date[] date2 = { new Date() };
        final SimpleDateFormat formatoInicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final SimpleDateFormat formatoDestino = new SimpleDateFormat("HH:mm:ss");
        final SimpleDateFormat formatoDestino2 = new SimpleDateFormat("dd/MM/yyyy");
        new Delete().from(DBCircular.class).execute();

        circ.enqueue(new Callback<List<Circulares>>() {
            @Override
            public void onResponse(Call<List<Circulares>> call, retrofit2.Response<List<Circulares>> response) {
                Log.d("CIRCULAR2->R",""+response.code());
                List<Circulares> lc = response.body();
                ArrayList<Circulares> c = new ArrayList<>();
                c.addAll(lc);



                Log.d("CIRCULAR2->R",""+c.size());
                String strFecha1="",strFecha2="";
                for (Circulares circ:
                     lc) {
                    try{
                        date1[0] = formatoInicio.parse(circ.fecha);
                        date2[0] = formatoInicio.parse(circ.fecha);
                        strFecha1 = formatoDestino.format(date1[0]);
                        strFecha2 = formatoDestino2.format(date2[0]);
                    }catch (ParseException ex){

                    }

                    DBCircular dbCircular = new DBCircular();
                    dbCircular.idCircular = circ.id;
                    dbCircular.leida = Integer.parseInt(circ.leido);
                    if (Integer.parseInt(circ.leido)==1){
                        dbCircular.no_leida = 0;
                    }
                    if (Integer.parseInt(circ.leido)==0){
                        dbCircular.no_leida = 1;
                    }
                    dbCircular.idUsuario = Integer.parseInt(id_usuario);
                    dbCircular.favorita = Integer.parseInt(circ.favorito);
                    //dbCircular.compartida = circulares.get(i).getCompartida();
                    dbCircular.eliminada = Integer.parseInt(circ.eliminado);
                    dbCircular.nombre = circ.titulo;
                    String f;
                    String grados="";
                    try{
                        f = circ.fecha_ics;
                    }catch (Exception ex){
                        f="";
                    }
                    try{
                        grados = String.valueOf(circ.grados);
                    }catch (Exception ex){

                    }
                    String adm = "";
                    try {
                        adm = circ.adm;

                    }catch (Exception ex){}
                    String rts ="";
                    String para="";
                    try{
                        rts =  circ.rts;

                    }catch (Exception ex){}
                    String espec = circ.espec;


                    String nivel = "";
                    try{
                        nivel= String.valueOf(circ.nivel);
                    }catch (Exception ex){
                        nivel="";
                    }

                    if(nivel.length()>0) {nivel="nivel: "+nivel+"/";}
                    if(grados.length()>0) {grados=" para: "+grados+"/";}
                    if(espec.length()>0) {espec=espec+"/";}
                    if(adm.length()>0) {adm=adm+"/";}
                    if(rts.length()>0) {espec=rts+"/";}

                    para = nivel+grados+espec+adm+rts;
                    if(para.endsWith("/")){
                        para = para.substring(0,para.length() - 1);
                    }
                    String enviaTodos = circ.envia_todos;

                    if(enviaTodos.equalsIgnoreCase("0") && adm.equalsIgnoreCase("")
                            && rts.equalsIgnoreCase("") && espec.equalsIgnoreCase("")
                            && grados.equalsIgnoreCase("") & nivel.equalsIgnoreCase("")){
                        para = "Personal";
                    }

                    if(enviaTodos.equalsIgnoreCase("1")){para="Todos";}

                    dbCircular.created_at=strFecha1;
                    dbCircular.updated_at=strFecha2;
                    dbCircular.fecha_ics = f;
                    dbCircular.contenido = circ.contenido;
                    dbCircular.para = para;
                    dbCircular.estado=circ.estatus;
                    dbCircular.temaIcs=circ.tema_ics;
                    dbCircular.adjunto=circ.adjunto;
                    dbCircular.nivel=String.valueOf(circ.nivel);
                    try {
                        if (!circ.fecha_ics.equalsIgnoreCase(""))
                            dbCircular.recordatorio = 1;
                        else
                            dbCircular.recordatorio = 0;
                    }catch(Exception ex){
                        dbCircular.recordatorio=0;
                    }

                    Log.d("CIRCULAR2->R",":"+dbCircular.save());

                }


                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("descarga",1);
                editor.apply();
                validarCuenta(correo);


            }










            @Override
            public void onFailure(Call<List<Circulares>> call, Throwable t) {
                Log.d("CIRCULAR2->",t.getMessage());
            }
        });
    }

    public void validarCuenta(final String email){

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+RUTA+VALIDAR_CUENTA+"?correo="+email,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        try {

                                    JSONObject jsonObject = (JSONObject) response
                                        .get(0);
                                    existe = jsonObject.getString("existe");


                        }catch (JSONException e)
                        {
                            e.printStackTrace();

                            Toast.makeText(getApplicationContext(),
                                    "Error",
                                    Toast.LENGTH_LONG).show();
                        }
                        //TODO: Cambiarlo a 0 para pruebas
                        if (existe.equalsIgnoreCase("1") || existe.equalsIgnoreCase("2")) {
                        //if (existe.equalsIgnoreCase("1") || existe.equalsIgnoreCase("0")) {

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("cuentaValida",1);
                            editor.putString("email",email);
                            editor.commit();

                            lblMensaje.setText("Cuenta de correo validada correctamente");

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent i = new Intent(ValidarPadreActivity.this, PrincipalActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                            }, 5000);

                        }else{
                            lblMensaje.setText("Cuenta de correo no registrada");
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("cuentaValida",0);
                            editor.commit();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent i = new Intent(ValidarPadreActivity.this, InicioActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                            }, 3000);
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
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();

    }


    private class CircularesAsyncTask extends AsyncTask<Void,Integer,Integer> {
        String id_usuario;
        public CircularesAsyncTask(String id_usuario) {
            this.id_usuario = id_usuario;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(),"Espera, estamos recuperando las últimas circulares del servidor",Toast.LENGTH_LONG).show();

        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            Toast.makeText(getApplicationContext(),"Circulares recuperadas...",Toast.LENGTH_LONG).show();

        }

        @Override
        protected Integer doInBackground(Void... voids) {
            getCirculares2(id_usuario);
            return null;
        }
    }

}










