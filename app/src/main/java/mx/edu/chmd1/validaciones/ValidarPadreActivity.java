package mx.edu.chmd1.validaciones;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mx.edu.chmd1.AppCHMD;
import mx.edu.chmd1.CircularDetalleActivity;
import mx.edu.chmd1.InicioActivity;
import mx.edu.chmd1.PrincipalActivity;
import mx.edu.chmd1.R;
import mx.edu.chmd1.networking.APIUtils;
import mx.edu.chmd1.networking.ICircularesCHMD;
import retrofit2.Call;
import retrofit2.Callback;

public class ValidarPadreActivity extends AppCompatActivity {
    static String VALIDAR_CUENTA="validarEmail.php";

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
                if(key.equalsIgnoreCase("idCircularNotif")){
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
        /*if (getIntent().hasExtra("idCircularNotif")){
            Toast.makeText(getApplicationContext(),"Vino el body "+getIntent().getStringExtra("idCircularNotif"),Toast.LENGTH_LONG);
        }else{
            Toast.makeText(getApplicationContext(),"No viene nada",Toast.LENGTH_LONG);
        }*/

        /*if(viaNotif==1){
            Intent intent = new Intent(ValidarPadreActivity.this, CircularDetalleActivity.class);
            intent.putExtra("idCircularNotif",idCircularNotif);
            intent.putExtra("viaNotif",1);
            startActivity(intent);
        }else{*/
            if(hayConexion()){
                validarCuenta(correo);
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
                        if (existe.equalsIgnoreCase("1")) {
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
                            }, 3000);

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

                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
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
}










