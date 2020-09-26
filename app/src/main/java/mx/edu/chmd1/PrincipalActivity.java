package mx.edu.chmd1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

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

import java.util.ArrayList;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;
import mx.edu.chmd1.adapter.MenuAdapter;
import mx.edu.chmd1.modelos.Menu;

public class PrincipalActivity extends AppCompatActivity {
    MenuAdapter menuAdapter = null;
    ListView lstPrincipal;
    ArrayList<Menu> items = new ArrayList<>();
    VideoView videoview;

    static String BASE_URL;
    static String RUTA;
    SharedPreferences sharedPreferences;
    String correo,rsp;
    static String GET_USUARIO="getUsuarioEmail.php";
    static String GET_VIGENCIA="getVigencia.php";
    static String GET_CIFRADO="cifrar.php";
    static String METODO_REG="registrarDispositivo.php";
    static String TAG=PrincipalActivity.class.getName();
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions gso;
    String idUsuarioCredencial="0";

    public boolean hayConexion() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        BASE_URL = this.getString(R.string.BASE_URL);
        RUTA = this.getString(R.string.PATH);

        videoview = findViewById(R.id.videoView);
        videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                videoview.start();
            }
        });
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.video_app);
        videoview.setVideoURI(uri);
        videoview.start();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        sharedPreferences = this.getSharedPreferences(this.getString(R.string.SHARED_PREF), 0);
        correo = sharedPreferences.getString("correoRegistrado","");
        idUsuarioCredencial = sharedPreferences.getString("idUsuarioCredencial","0");


        //idCircularNotif


        //getUsuario(correo);
        getVigencia(idUsuarioCredencial);
        getCifrado(idUsuarioCredencial);
        ShortcutBadger.applyCount(getApplicationContext(), 0);
        FirebaseInstanceId.getInstance().getInstanceId() .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "getInstanceId failed", task.getException());
                    return;
                }
                String token = task.getResult().getToken();
                new RegistrarDispositivoAsyncTask(correo,token,"Android OS",idUsuarioCredencial).execute();
            }
        });



        lstPrincipal = findViewById(R.id.lstPrincipal);
        llenarMenu();
        lstPrincipal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Menu m = (Menu)lstPrincipal.getItemAtPosition(position);
                if(m.getIdMenu()==1){
                    //Circulares
                    Intent intent = new Intent(PrincipalActivity.this, CircularActivity.class);
                    startActivity(intent);
                }
                //Web
                if(m.getIdMenu()==2){
                    Intent intent = new Intent(PrincipalActivity.this,WebCHMDActivity.class);
                    startActivity(intent);
                }

                if(m.getIdMenu()==3){
                    Intent intent = new Intent(PrincipalActivity.this,CredencialActivity.class);
                    startActivity(intent);
                }
/*
                if(m.getIdMenu()==4){
                    Intent intent = new Intent(PrincipalActivity.this,NotificacionesActivity.class);
                    startActivity(intent);
                }
*/
                if(m.getIdMenu()==5){
                    //Cerrar Sesión
                    try{
                        mGoogleSignInClient.signOut();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("email","");
                        editor.putString("nombre","");
                        editor.putString("userPic","");
                        editor.putString("idToken","");
                        editor.putInt("cuentaValida",0);


                        editor.commit();
                        Intent intent = new Intent(PrincipalActivity.this,InicioActivity.class);
                        startActivity(intent);
                        finish();
                    }catch (Exception ex){

                    }
                }
            }
        });
    }

    private void llenarMenu(){
        items.add(new Menu(1,"Circulares",R.drawable.circulares256));
        items.add(new Menu(2,"Mi Maguen",R.drawable.mi_maguen256));
        items.add(new Menu(3,"Mi Credencial",R.drawable.mi_credencial256));
        //items.add(new Menu(4,"Notificaciones",R.drawable.notificaciones));
        items.add(new Menu(5,"Cerrar Sesión",R.drawable.cerrar_sesion256));
        menuAdapter = new MenuAdapter(PrincipalActivity.this,items);
        lstPrincipal.setAdapter(menuAdapter);
    }






    private class RegistrarDispositivoAsyncTask extends AsyncTask<Void, Long, Boolean> {
        private String correo;
        private String device_token;
        private String plataforma;
        private String idUsuarioCredencial;


        public RegistrarDispositivoAsyncTask(String correo, String device_token, String plataforma,
                                             String idUsuarioCredencial) {
            this.correo = correo;
            this.device_token = device_token;
            this.plataforma = plataforma;
            this.idUsuarioCredencial = idUsuarioCredencial;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("RESPONSE","ejecutando...");
        }

        public void registraDispositivo(){
            HttpClient httpClient;
            HttpPost httppost;
            httpClient = new DefaultHttpClient();
            httppost = new HttpPost(BASE_URL+RUTA+METODO_REG);
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<>(4);
                nameValuePairs.add(new BasicNameValuePair("correo",correo));
                nameValuePairs.add(new BasicNameValuePair("device_token",device_token));
                nameValuePairs.add(new BasicNameValuePair("plataforma",plataforma));
                nameValuePairs.add(new BasicNameValuePair("id_usuario",idUsuarioCredencial));
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
            registraDispositivo();
            return null;
        }
    }



    public void getUsuario(String email){


        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+RUTA+GET_USUARIO+"?correo="+email,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {


                        if(response.length()<=0){
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("idUsuarioCredencial","0");
                            editor.putString("nombreCredencial","S/N");
                            editor.putString("numeroCredencial","0");
                            editor.putString("telefonoCredencial","S/T");
                            editor.putString("responsableCredencial","S/N");
                            editor.putString("familiaCredencial","S/N");
                            editor.putString("fotoCredencial","");
                            //Toast.makeText(getApplicationContext(),foto,Toast.LENGTH_LONG).show();
                            editor.commit();
                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                String idUsuario = jsonObject.getString("id");
                                String nombre = jsonObject.getString("nombre");
                                String numero = jsonObject.getString("numero");
                                String telefono = jsonObject.getString("telefono");
                                String responsable = jsonObject.getString("responsable");
                                String familia = jsonObject.getString("familia");
                                String rutaFoto = jsonObject.getString("fotografia");
                                Log.d("PrincipalActivity",rutaFoto);
                                String[] ruta;
                                String foto;
                                try{
                                 ruta  = rutaFoto.split("\\\\");
                                 foto = ruta[4];
                                }catch (Exception ex){
                                  foto="";
                                }


                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("idUsuarioCredencial",idUsuario);
                                editor.putString("nombreCredencial",nombre);
                                editor.putString("numeroCredencial",numero);
                                editor.putString("telefonoCredencial",telefono);
                                editor.putString("responsableCredencial",responsable);
                                editor.putString("familiaCredencial",familia);
                                editor.putString("fotoCredencial",foto);
                                //Toast.makeText(getApplicationContext(),foto,Toast.LENGTH_LONG).show();
                                editor.commit();


                            }





                        }catch (JSONException e)
                        {
                            e.printStackTrace();


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



                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                VolleyLog.d("ERROR", "Error: " + error.getMessage());
                /*
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                        */

            }
        });

        // Adding request to request queue
        AppCHMD.getInstance().addToRequestQueue(req);
    }
    public void getVigencia(String idUsuario){


        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+RUTA+GET_VIGENCIA+"?idUsuario="+idUsuario,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                String vigencia = jsonObject.getString("texto");
                                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("vigencia",vigencia);

                                //Toast.makeText(getApplicationContext(),foto,Toast.LENGTH_LONG).show();
                                editor.commit();


                            }





                        }catch (JSONException e)
                        {
                            e.printStackTrace();


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



                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                VolleyLog.d("ERROR", "Error: " + error.getMessage());
                /*
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                        */

            }
        });

        // Adding request to request queue
        AppCHMD.getInstance().addToRequestQueue(req);
    }
    public void getCifrado(String idUsuario){


        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+RUTA+GET_CIFRADO+"?idUsuario="+idUsuario,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                String cifrado = jsonObject.getString("cifrado");
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("cifrado",cifrado);

                                //Toast.makeText(getApplicationContext(),foto,Toast.LENGTH_LONG).show();
                                editor.commit();


                            }





                        }catch (JSONException e)
                        {
                            e.printStackTrace();


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



                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                VolleyLog.d("ERROR", "Error: " + error.getMessage());
                /*
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                        */

            }
        });

        // Adding request to request queue
        AppCHMD.getInstance().addToRequestQueue(req);
    }

}
