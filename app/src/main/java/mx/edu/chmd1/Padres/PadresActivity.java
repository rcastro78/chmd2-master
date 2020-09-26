package mx.edu.chmd1.Padres;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import mx.edu.chmd1.AppCHMD;
import mx.edu.chmd1.MenuCircularesActivity;
import mx.edu.chmd1.R;
import mx.edu.chmd1.adapter.PadreAdapter;
import mx.edu.chmd1.modelos.Padre;

public class PadresActivity extends AppCompatActivity {
    ArrayList<Padre> padres = new ArrayList<>();
    PadreAdapter padreAdapter = null;
    ProgressDialog progressDialog;
    ListView lstPadres;
    TextView lblToolbar;
    FloatingActionButton fabNuevo;
    static String GET_PADRES="getPapa.php";
    static String BASE_URL;
    Typeface tf;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_padres);
        lstPadres = findViewById(R.id.lstPadres);
        fabNuevo = findViewById(R.id.fabNuevoPadre);
        tf = Typeface.createFromAsset(getAssets(),"fonts/GothamRoundedBold_21016.ttf");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PadresActivity.this, MenuCircularesActivity.class);
                startActivity(intent);
                finish();
            }
        });
        lblToolbar = toolbar.findViewById(R.id.lblTextoToolbar);
        lblToolbar.setText("Padres");
        lblToolbar.setTypeface(tf);
        BASE_URL = this.getString(R.string.BASE_URL);
        datosPadres();
        lstPadres.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        fabNuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public void cargar() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Cargando...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setProgress(0);
        progressDialog.show();
    }

    public void datosPadres(){
        cargar();
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+"pruebascd/app/webservices/"+GET_PADRES,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        progressDialog.dismiss();

                        try {


                            for (int i=0; i<response.length(); i++) {
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                String id = jsonObject.getString("id");
                                String familia = jsonObject.getString("Familia");
                                String nombre = jsonObject.getString("nombre");
                                String apellidos = jsonObject.getString("apellidos");
                                String rol = jsonObject.getString("Rol");
                                String correo = jsonObject.getString("correo");

                                /*
                                * {"id":"125","nombre":"ABADI TAVASHI JACOBO","apellidos":"ABADI CHAYO","Rol":"PADRE","correo":"","Familia":"1484"
                                * */
                                padres.add(new Padre(Integer.parseInt(id),Integer.parseInt(familia),nombre,apellidos,rol,correo));


                            }

                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),
                                    "Error",
                                    Toast.LENGTH_LONG).show();
                        }
                        padreAdapter = new PadreAdapter(PadresActivity.this,padres);
                        lstPadres.setAdapter(padreAdapter);


                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                VolleyLog.d("ERROR", "Error: " + error.getMessage());
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Adding request to request queue
        AppCHMD.getInstance().addToRequestQueue(req);
    }

}
