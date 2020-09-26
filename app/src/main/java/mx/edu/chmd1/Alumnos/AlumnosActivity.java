package mx.edu.chmd1.Alumnos;

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
import android.widget.Spinner;
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
import mx.edu.chmd1.adapter.AlumnosAdapter;
import mx.edu.chmd1.adapter.CHMDSpinnerAdapter;
import mx.edu.chmd1.modelos.Alumno;

public class AlumnosActivity extends AppCompatActivity {
ArrayList<Alumno> alumnos = new ArrayList<>();
AlumnosAdapter alumnosAdapter = null;
ProgressDialog progressDialog;
ListView lstAlumnos;
TextView lblToolbar;
ArrayList<String> grados = new ArrayList<>(),
        grupos = new ArrayList<>(),
        niveles = new ArrayList<>();
CHMDSpinnerAdapter gruposAdapter,gradosAdapter,nivelesAdapter;
Spinner sprGrado,sprNivel,sprGrupo;
FloatingActionButton fabNuevo;
    static String GET_ALUMNOS="getAlumnos.php";
    static String GET_GRADO="getGrados.php";
    static String GET_NIVEL="getNiveles.php";
    static String GET_GRUPO="getGrupos.php";
    static String BASE_URL;
    String nv,gpo,grad;
    Typeface tf;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alumnos);
        lstAlumnos = findViewById(R.id.lstAlumnos);
        sprGrado = findViewById(R.id.sprGrado);
        sprGrupo = findViewById(R.id.sprGrupo);
        sprNivel = findViewById(R.id.sprNivel);
        fabNuevo = findViewById(R.id.fabNuevoAlumno);
        tf = Typeface.createFromAsset(getAssets(),"fonts/GothamRoundedBold_21016.ttf");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlumnosActivity.this, MenuCircularesActivity.class);
                startActivity(intent);
                finish();
            }
        });
        lblToolbar = toolbar.findViewById(R.id.lblTextoToolbar);
        lblToolbar.setText("Alumnos");
        lblToolbar.setTypeface(tf);
        BASE_URL = this.getString(R.string.BASE_URL);
        datosAlumnos();
        getGrados();
        getGrupos();
        getNiveles();

        lstAlumnos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        sprNivel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                nv = String.valueOf(sprNivel.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sprGrupo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gpo = String.valueOf(sprGrupo.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sprGrado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                grad = String.valueOf(sprGrado.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

    public void datosAlumnos(){
        cargar();
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+"pruebascd/app/webservices/"+GET_ALUMNOS,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        progressDialog.dismiss();

                        try {


                            for (int i=0; i<response.length(); i++) {
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                String id = jsonObject.getString("id");
                                String nombre = jsonObject.getString("nombre");
                                String apellido = jsonObject.getString("familia");
                                String sexo = jsonObject.getString("sexo");
                                String nivel = jsonObject.getString("nivel");
                                String grado = jsonObject.getString("grado");
                                String grupo = jsonObject.getString("grupo");
                                String estatus = jsonObject.getString("estatus");
                                alumnos.add(new Alumno(Integer.parseInt(id),nombre,"",sexo,nivel,grado,grupo,estatus));

                                //mercanciasServicio.add(new MercanciaServicio(idTm,marca+"\n"+modelo+"\n"+subtipo,peso,tipoMercancia));

                            }

                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),
                                    "Error",
                                    Toast.LENGTH_LONG).show();
                        }
                        alumnosAdapter = new AlumnosAdapter(AlumnosActivity.this,alumnos);
                        lstAlumnos.setAdapter(alumnosAdapter);
                        //mercanciaServicioAdapter = new MercanciaServicioAdapter(DatosServicioActivity.this,mercanciasServicio);
                        //lstMercancias.setAdapter(mercanciaServicioAdapter);



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

    public void getGrupos(){

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+"pruebascd/app/webservices/"+GET_GRUPO,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {


                        try {


                            for (int i=0; i<response.length(); i++) {
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                String g = jsonObject.getString("grupo");
                                grupos.add(g);
                                //mercanciasServicio.add(new MercanciaServicio(idTm,marca+"\n"+modelo+"\n"+subtipo,peso,tipoMercancia));

                            }

                        }catch (JSONException e)
                        {
                            e.printStackTrace();

                            Toast.makeText(getApplicationContext(),
                                    "Error",
                                    Toast.LENGTH_LONG).show();
                        }
                        gruposAdapter = new CHMDSpinnerAdapter(AlumnosActivity.this,grupos);
                        sprGrupo.setAdapter(gruposAdapter);


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
    public void getGrados(){

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+"pruebascd/app/webservices/"+GET_GRADO,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {


                        try {


                            for (int i=0; i<response.length(); i++) {
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                String g = jsonObject.getString("grado");
                                grados.add(g);
                                //mercanciasServicio.add(new MercanciaServicio(idTm,marca+"\n"+modelo+"\n"+subtipo,peso,tipoMercancia));

                            }

                        }catch (JSONException e)
                        {
                            e.printStackTrace();

                            Toast.makeText(getApplicationContext(),
                                    "Error",
                                    Toast.LENGTH_LONG).show();
                        }
                        gradosAdapter = new CHMDSpinnerAdapter(AlumnosActivity.this,grados);
                        sprGrado.setAdapter(gradosAdapter);


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
    public void getNiveles(){

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+"pruebascd/app/webservices/"+GET_NIVEL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {


                        try {


                            for (int i=0; i<response.length(); i++) {
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                String n = jsonObject.getString("nivel");
                                niveles.add(n);
                                //mercanciasServicio.add(new MercanciaServicio(idTm,marca+"\n"+modelo+"\n"+subtipo,peso,tipoMercancia));

                            }

                        }catch (JSONException e)
                        {
                            e.printStackTrace();

                            Toast.makeText(getApplicationContext(),
                                    "Error",
                                    Toast.LENGTH_LONG).show();
                        }
                        nivelesAdapter = new CHMDSpinnerAdapter(AlumnosActivity.this,niveles);
                        sprNivel.setAdapter(nivelesAdapter);


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

}
    /*
    * public void datosMercancia(final String svc, final String idT) {
        //cargar();

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+METODO_MERCANCIA+"?svc="+svc+"&idt="+idT,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        progressDialog.dismiss();

                        try {


                            for (int i=0; i<response.length(); i++) {
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                String idTm = jsonObject.getString("IdTM");
                                String marca = jsonObject.getString("marca");
                                String modelo = jsonObject.getString("modelo");
                                String subtipo = jsonObject.getString("SubTipoMercancia");
                                String tipoMercancia = jsonObject.getString("TipoMercancia");
                                String peso = jsonObject.getString("peso");
                                if (marca.equals("null")){marca="";}
                                if (modelo.equals("null")){modelo="";}

                                mercanciasServicio.add(new MercanciaServicio(idTm,marca+"\n"+modelo+"\n"+subtipo,peso,tipoMercancia));

                            }

                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),
                                    "Error",
                                    Toast.LENGTH_LONG).show();
                        }

                        mercanciaServicioAdapter = new MercanciaServicioAdapter(DatosServicioActivity.this,mercanciasServicio);
                        lstMercancias.setAdapter(mercanciaServicioAdapter);



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
        AppTransporte.getInstance().addToRequestQueue(req);
    }

    * */


