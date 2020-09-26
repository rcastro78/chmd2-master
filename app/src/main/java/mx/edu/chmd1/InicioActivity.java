package mx.edu.chmd1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mx.edu.chmd1.validaciones.ValidarPadreActivity;

public class InicioActivity extends AppCompatActivity {
ImageButton fabLogin;
    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    static int RC_SIGN_IN=999;
    SharedPreferences sharedPreferences;
    static String TAG = InicioActivity.class.getName();
    VideoView videoview;
    int valida;
    static String BASE_URL;
    static String RUTA;
    static String GET_USUARIO="getUsuarioEmail.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        BASE_URL = this.getString(R.string.BASE_URL);
        RUTA = this.getString(R.string.PATH);

        sharedPreferences = this.getSharedPreferences(this.getString(R.string.SHARED_PREF), 0);
        valida = sharedPreferences.getInt("cuentaValida",0);
        videoview = findViewById(R.id.videoView);
        videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                videoview.start();
            }
        });
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.video_app);
        videoview.setVideoURI(uri);
        videoview.start();

        if(hayConexion()){
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestProfile()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            fabLogin = findViewById(R.id.fabLogin);
            fabLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signIn();
                }
            });
        }else{
            Intent i = new Intent(InicioActivity.this, ValidarPadreActivity.class);
            startActivity(i);
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account!=null){

            if(valida==1) {
                Intent intent = new Intent(InicioActivity.this, ValidarPadreActivity.class);
                startActivity(intent);
            }else{
                mGoogleSignInClient.signOut();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        /*if(valida==-1){
            Toast.makeText(getApplicationContext(),"Cerraste sesión",Toast.LENGTH_LONG).show();
            mGoogleSignInClient.signOut();
        }*/
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
            }catch (Exception ex){
                Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }


    private void handleSignInResult(GoogleSignInResult result) {

        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            getUsuario(account.getEmail());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("correoRegistrado",account.getEmail());
            editor.putString("nombre",account.getDisplayName());
            String userPic = "";
            //Al venir la pic vacía daba error, se cerraba luego de escoger la cuenta.
            try{
                userPic = account.getPhotoUrl().toString();
            }catch (Exception ex){
                userPic = "";
            }
            editor.putString("userPic",userPic);
            editor.putString("idToken",account.getIdToken());

            editor.commit();

            Intent intent = new Intent(InicioActivity.this, ValidarPadreActivity.class);
            startActivity(intent);

        }else{
            //Log.w(TAG, "No se pudo iniciar sesión");
            Toast.makeText(getApplicationContext(),""+result.getStatus().getStatusMessage(),Toast.LENGTH_LONG).show();
            //Log.w(TAG, result.getStatus().getStatusMessage());
            Log.w(TAG, result.getStatus().getStatusCode()+"");
        }

    }


    public boolean hayConexion() {
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();

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






}
