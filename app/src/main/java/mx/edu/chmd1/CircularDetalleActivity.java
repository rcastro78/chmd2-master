package mx.edu.chmd1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.provider.CalendarContract;
import android.text.Html;
import android.util.Log;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.bitly.Bitly;
import com.bitly.Error;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import mx.edu.chmd1.modelos.Circular;
import mx.edu.chmd1.modelosDB.DBCircular;
import mx.edu.chmd1.modelosDB.DBNotificacion;
import mx.edu.chmd1.utilerias.OnSwipeTouchListener;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

public class CircularDetalleActivity extends AppCompatActivity {
    static String METODO="getCircularId6.php";
    static String METODO_CIRCULAR="getCircularId6.php";
    static String METODO2="getCirculares_iOS.php";
    static String METODO2N="getNotificaciones_iOS.php";
    static String METODO_REG="leerCircular.php";
    static String METODO_NOLEER="noleerCircular.php";
    static String METODO_DEL="eliminarCircular.php";
    static String METODO_FAV="favCircular.php";
    static String BASE_URL;
    static String RUTA;
    static int TODAS=0;
    static int FAVORITAS=1;
    static int NOLEIDAS=2;
    static int ELIMINADAS=3;
    static int NOTIFICACIONES=5;
    private OnSwipeTouchListener onSwipeTouchListener;
    SharedPreferences sharedPreferences;
    //TextView lblTitulo,lblTitulo2;
    WebView wvwDetalleCircular;
    String idCircular,contenidoCircular;
    String idUsuario,rsp;
    String temaIcs,fechaIcs,ubicacionIcs,horaInicioIcs,horaFinIcs;
    ImageView imgHome,imgEliminarSeleccionados,imgMoverFavSeleccionados;
    ImageView btnSiguiente,btnAnterior,btnCalendario,btnCompartir;
    FloatingActionButton fabReload;
    Typeface t;
    String t2;
    int pos=0;
    int tipo=0;
    ArrayList<Circular> circulares = new ArrayList<>();
    ArrayList<Circular> circularesId = new ArrayList<>();
    public boolean hayConexion() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circular_detalle);
        sharedPreferences = getSharedPreferences(this.getString(R.string.SHARED_PREF), 0);
        final int viaNotif = sharedPreferences.getInt("viaNotificacion",0);
        int vn = getIntent().getIntExtra("viaNotificacion",0);

        if(vn==1){
            idCircular = getIntent().getStringExtra("idCircular");
        }else{
            idCircular = getIntent().getStringExtra("idCircular");
        }

        //Volbt a poner vianotif a 0
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("viaNotif",0);
        editor.putInt("viaNotificacion",0);
        editor.commit();



        tipo = getIntent().getIntExtra("tipo",0);
        Toolbar toolbar = findViewById(R.id.toolbar);
        fabReload = findViewById(R.id.fabReload);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        if(hayConexion())
            //Bitly.initialize(this, "9bd1d4e87ce38e38044ff0c7c60c07c90483e2a4");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CircularDetalleActivity.this, CircularActivity.class);
                startActivity(intent);
                finish();
            }
        });
        t = Typeface.createFromAsset(getAssets(),"fonts/GothamRoundedMedium_21022.ttf");
       // lblEncabezado = toolbar.findViewById(R.id.lblTextoToolbar);
       // lblEncabezado.setText("Circular");
        //lblEncabezado.setTypeface(t);

        wvwDetalleCircular = findViewById(R.id.wvwDetalleCircular);
        BASE_URL = this.getString(R.string.BASE_URL);
        RUTA = this.getString(R.string.PATH);


        //lblTitulo = findViewById(R.id.lblTitulo);
        //lblTitulo2 = findViewById(R.id.lblTitulo2);
        //lblFecha = findViewById(R.id.lblFecha);
        //lblNivel = findViewById(R.id.lblNivel);
        //lblTitulo.setTypeface(t);
        //lblTitulo2.setTypeface(t);
        //lblFecha.setTypeface(t);
        //lblNivel.setTypeface(t);


        /*final SimpleDateFormat formatoInicio = new SimpleDateFormat("dd/MM/yyyy");
        final SimpleDateFormat formatoDestino = new SimpleDateFormat("dd 'de' MMM 'de' yyyy");
        try{
            Date date1 = formatoInicio.parse(getIntent().getStringExtra("fechaCircular"));
            String strFecha = formatoDestino.format(date1);
            lblFecha.setText(strFecha);
        }catch (Exception ex){

        }*/
        



       /* if(getIntent().getStringExtra("nivel")!="null")
            lblNivel.setText(getIntent().getStringExtra("nivel"));
        else
            lblNivel.setText("");*/

        imgMoverFavSeleccionados = findViewById(R.id.imgMovFav);
        imgEliminarSeleccionados = findViewById(R.id.imgEliminarSeleccionados);
        //imgMoverNoLeidos = findViewById(R.id.imgMoverNoLeidos);
        btnCalendario = findViewById(R.id.btnCalendario);
        imgHome = findViewById(R.id.imgHome);
        btnSiguiente = findViewById(R.id.btnSiguiente);
        btnAnterior = findViewById(R.id.btnAnterior);
        btnCompartir = findViewById(R.id.btnCompartir);
        String idUsuarioCredencial="";

        fabReload.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                wvwDetalleCircular.loadUrl(BASE_URL+RUTA+METODO+"?id="+idCircular);
                fabReload.setVisibility(View.GONE);
            }
        });





        try{
            idUsuarioCredencial = sharedPreferences.getString("idUsuarioCredencial","0");
            idUsuario = idUsuarioCredencial;
            contenidoCircular = getIntent().getStringExtra("contenidoCircular");
            temaIcs = getIntent().getStringExtra("temaIcs");
            fechaIcs = getIntent().getStringExtra("fechaIcs");
            ubicacionIcs = getIntent().getStringExtra("ubicaIcs");
            horaInicioIcs = getIntent().getStringExtra("horaInicioIcs");
            horaFinIcs = getIntent().getStringExtra("horaFinIcs");
            if(!horaInicioIcs.equalsIgnoreCase("00:00:00"))
                btnCalendario.setVisibility(View.VISIBLE);
            else
                btnCalendario.setVisibility(View.GONE);
        }catch (Exception ex){

        }

        if(hayConexion()) {
            if (tipo == TODAS)
                getCirculares(idUsuarioCredencial);
            if (tipo == FAVORITAS)
                getCircularesFavs(idUsuarioCredencial);
            if (tipo == NOLEIDAS)
                getCircularesNoLeidas(idUsuarioCredencial);
            if (tipo == ELIMINADAS)
                getCircularesEliminadas(idUsuarioCredencial);
            if (tipo == NOTIFICACIONES)
                getNotificaciones(idUsuarioCredencial);
        }else {


            if(tipo==TODAS)
                leeCirculares(Integer.parseInt(idUsuarioCredencial));
            if(tipo==FAVORITAS)
                leeCircularesFavs(Integer.parseInt(idUsuarioCredencial));
            if(tipo==NOLEIDAS)
                leeCircularesNoLeidas(Integer.parseInt(idUsuarioCredencial));
            if(tipo==ELIMINADAS)
                leeCircularesEliminadas(Integer.parseInt(idUsuarioCredencial));
            if(tipo==NOTIFICACIONES)
                leeNotificaciones(Integer.parseInt(idUsuarioCredencial));


        }
        /*imgCompartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CircularDetalleActivity.this);
                builder.setTitle("Compartir Circular");
                builder.setMessage("¿Desea compartir esta circular?");
                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       share("https://www.chmd.edu.mx/WebAdminCirculares/ws/getCircularId2.php?id=300");
                        //Compartir

                    }
                });
                builder.setNegativeButton("Cancelar", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });*/


        imgHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });




        imgEliminarSeleccionados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hayConexion()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CircularDetalleActivity.this);
                    builder.setTitle("Eliminar Circular");
                    builder.setMessage("¿Estás seguro que quieres eliminar esta circular?");
                    builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new EliminaAsyncTask(idCircular, idUsuario).execute();
                        }
                    });
                    builder.setNegativeButton("Cancelar", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else{
                    Toast.makeText(getApplicationContext(),"Esta opción solo puede utilizarse con una conexión activa a Internet",Toast.LENGTH_LONG).show();

                }
            }
        });


        imgMoverFavSeleccionados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(hayConexion()) {
                    if(tipo!=5){
                        AlertDialog.Builder builder = new AlertDialog.Builder(CircularDetalleActivity.this);
                        builder.setTitle("Mover a favoritos");
                        builder.setMessage("¿Estás seguro que quieres mover esta circular a favoritas?");
                        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new FavAsyncTask(idCircular,idUsuario).execute();
                            }
                        });
                        builder.setNegativeButton("Cancelar", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }else{
                        Toast.makeText(getApplicationContext(),"Esta opción no puede usarse con las notificaciones",Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"Esta opción solo puede utilizarse con una conexión activa a Internet",Toast.LENGTH_LONG).show();

                }


            }
        });


        /*imgMoverNoLeidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CircularDetalleActivity.this);
                builder.setTitle("Mover a favoritos");
                builder.setMessage("¿Estás seguro que quieres mover esta circular a no leídas?");
                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new NoLeerAsyncTask(idCircular,idUsuario).execute();
                    }
                });
                builder.setNegativeButton("Cancelar", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });*/


        btnCompartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(Uri.parse(BASE_URL+RUTA+METODO+"?id="+idCircular))
                        .setDomainUriPrefix("https://chmd1.page.link")

                        .buildShortDynamicLink()
                        .addOnCompleteListener(CircularDetalleActivity.this, new OnCompleteListener<ShortDynamicLink>() {
                            @Override
                            public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                if (task.isSuccessful()) {
                                    // Short link created
                                    Uri shortLink = task.getResult().getShortLink();
                                    Uri flowchartLink = task.getResult().getPreviewLink();

                                    try {
                                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                        shareIntent.setType("text/plain");
                                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "CHMD");
                                        shareIntent.putExtra(Intent.EXTRA_TEXT, "Comparto:"+ getIntent().getStringExtra("tituloCircular") +" \n" + "https://chmd1.page.link"+shortLink.getPath() +"\n\n");
                                        shareIntent.setPackage("com.whatsapp");
                                        startActivity(shareIntent);
                                    } catch(Exception e) {
                                        Toast.makeText(getApplicationContext(),"WhatsApp no está instalado",Toast.LENGTH_LONG).show();
                                    }



                                } else {
                                    // Error
                                    // ...
                                }
                            }
                        });
            }
        });


        btnCalendario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                    AlertDialog.Builder builder = new AlertDialog.Builder(CircularDetalleActivity.this);
                    builder.setTitle("Calendario");
                    builder.setMessage("¿Estás seguro que quieres agregar este evento a tu calendario?");
                    builder.setPositiveButton("Establecer", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                String startDate = fechaIcs + " " + horaInicioIcs;
                                String endDate = fechaIcs + " " + horaFinIcs;
                                String title = temaIcs;
                                String description = "";
                                String location = ubicacionIcs;
                                new Update(DBCircular.class).set("recordatorio=1").where("idCircular=?",idCircular).execute();
                                if(!fechaIcs.equalsIgnoreCase("")){

                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    Date sdate = sdf.parse(startDate);
                                    Date edate = sdf.parse(endDate);

                                    long startTime = sdate.getTime();
                                    long endTime = edate.getTime();

                                    Intent intent = new Intent(Intent.ACTION_INSERT);
                                    intent.setType("vnd.android.cursor.item/event");
                                    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime);
                                    intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);
                                    intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);

                                    intent.putExtra(CalendarContract.Events.TITLE, title);
                                    intent.putExtra(CalendarContract.Events.DESCRIPTION, "");
                                    intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);
                                    //intent.putExtra(CalendarContract.Events.RRULE, "FREQ=YEARLY");

                                    startActivity(intent);

                                    //Actualizar la circular, poner el recordatorio en 1



                                }else{
                                    Toast.makeText(getApplicationContext(),"No se puede agregar al calendario, no tiene fecha",Toast.LENGTH_LONG).show();
                                }

                            }catch (Exception e){
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                    builder.setNegativeButton("Cancelar", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();





                }

        });


        wvwDetalleCircular.getSettings().setJavaScriptEnabled(true);
        wvwDetalleCircular.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        wvwDetalleCircular.setWebViewClient(new WebViewClient() {
            @SuppressLint("RestrictedApi")
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.startsWith("intent")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                        if (fallbackUrl != null) {
                            wvwDetalleCircular.loadUrl(fallbackUrl);
                            return true;
                        }
                    } catch (URISyntaxException e) {
                        //not an intent uri
                    }
                }


                if (url.endsWith(".pdf")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(url), "application/pdf");
                    try {
                        view.getContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        //user does not have a pdf viewer installed
                    }
                } else {

                   wvwDetalleCircular.loadUrl(url);
                }
                fabReload.setVisibility(View.VISIBLE);
                return true;
            }
            });

        wvwDetalleCircular.getSettings().setSupportZoom(true);
        wvwDetalleCircular.getSettings().setBuiltInZoomControls(true);
        wvwDetalleCircular.getSettings().setDisplayZoomControls(true);
        wvwDetalleCircular.getSettings().setDomStorageEnabled(true);
        wvwDetalleCircular.getSettings().setAppCacheEnabled(true);
        wvwDetalleCircular.getSettings().setLoadsImagesAutomatically(true);
        wvwDetalleCircular.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        //cortar el título por los espacios
        /*try{
            String[] titulo = getIntent().getStringExtra("tituloCircular").split(" ");
            int totalElementos = titulo.length;
            int totalEspacios = totalElementos-1;
            if(totalElementos>2){
                lblTitulo.setText("");
                lblTitulo.setText(titulo[0]+" "+titulo[1]);
                String t="";

                for(int i=2; i<totalElementos; i++){
                    t += titulo[i]+" ";

                }
                lblTitulo2.setVisibility(View.VISIBLE);

                lblTitulo2.setText(t);
            }else{
                lblTitulo2.setVisibility(View.INVISIBLE);
                lblTitulo.setText(getIntent().getStringExtra("tituloCircular"));
            }
        }catch (Exception ex){

        }*/




        new RegistrarLecturaAsyncTask(idCircular,idUsuario).execute();
        if(hayConexion())
            wvwDetalleCircular.loadUrl(BASE_URL+RUTA+METODO+"?id="+idCircular);
        else {
            contenidoCircular = Html.fromHtml(contenidoCircular).toString();
            wvwDetalleCircular.loadData(contenidoCircular,"text/html", Xml.Encoding.UTF_8.toString());
        }
        onSwipeTouchListener = new OnSwipeTouchListener() {
            public void onSwipeRight() {
                //Toast.makeText(CircularDetalleActivity.this, "der.", Toast.LENGTH_SHORT).show();
                if(pos>0){
                    for(int i=0; i<circulares.size(); i++){
                        if(circulares.get(i).getIdCircular()==idCircular)
                            pos=i;
                    }

                    pos = pos - 1;
                    idCircular = circulares.get(pos).getIdCircular();

                    temaIcs = circulares.get(pos).getTemaIcs();
                    fechaIcs = circulares.get(pos).getFechaIcs();
                    ubicacionIcs = circulares.get(pos).getUbicacionIcs();
                    horaInicioIcs = circulares.get(pos).getHoraInicialIcs();
                    horaFinIcs = circulares.get(pos).getHoraFinalIcs();

                    new RegistrarLecturaAsyncTask(idCircular,idUsuario).execute();
                    if(viaNotif==0)
                        wvwDetalleCircular.loadUrl(BASE_URL+RUTA+METODO+"?id="+idCircular);


                    //lblTitulo.setText(circulares.get(pos).getNombre());

                    String tituloCompleto = circulares.get(pos).getNombre();
                    String[] titulo = tituloCompleto.split(" ");
                    int totalElementos = titulo.length;
                    if(totalElementos>2){
                        //lblTitulo.setText("");
                        //lblTitulo.setText(titulo[0]+" "+titulo[1]);
                        String t="";
                        //el titulo 2 tiene desde titulo[2] hasta titulo[totalElementos-1];
                        for(int i=2; i<totalElementos; i++){
                            t += titulo[i]+" ";

                        }
                        //lblTitulo2.setVisibility(View.VISIBLE);

                        //lblTitulo2.setText(t);
                    }else{
                        //lblTitulo2.setVisibility(View.INVISIBLE);
                        //lblTitulo.setText(tituloCompleto);
                    }

                }else {
                    pos = circulares.size() - 1;
                }

            }
            public void onSwipeLeft() {
                // Toast.makeText(CircularDetalleActivity.this, "izq.", Toast.LENGTH_SHORT).show();

                if(pos<circulares.size()){
                    for(int i=0; i<circulares.size(); i++){
                        if(circulares.get(i).getIdCircular()==idCircular)
                            pos=i;
                    }

                    //despues de obtenerla pasar a la siguiente circular
                    pos = pos+1;
                    idCircular = circulares.get(pos).getIdCircular();
                    temaIcs = circulares.get(pos).getTemaIcs();
                    fechaIcs = circulares.get(pos).getFechaIcs();
                    ubicacionIcs = circulares.get(pos).getUbicacionIcs();
                    horaInicioIcs = circulares.get(pos).getHoraInicialIcs();
                    horaFinIcs = circulares.get(pos).getHoraFinalIcs();
                    String nivel = circulares.get(pos).getNivel();
                    new RegistrarLecturaAsyncTask(idCircular,idUsuario).execute();
                    wvwDetalleCircular.loadUrl(BASE_URL+RUTA+METODO+"?id="+idCircular);
                    //lblNivel.setText(nivel);


                    String tituloCompleto = circulares.get(pos).getNombre();
                    String[] titulo = tituloCompleto.split(" ");
                    int totalElementos = titulo.length;
                    int totalEspacios = totalElementos-1;
                    if(totalElementos>2){
                        //lblTitulo.setText("");
                        //lblTitulo.setText(titulo[0]+" "+titulo[1]);
                        String t="";
                        //el titulo 2 tiene desde titulo[2] hasta titulo[totalElementos-1];
                        for(int i=2; i<totalElementos; i++){
                            t += titulo[i]+" ";

                        }
                        //lblTitulo2.setVisibility(View.VISIBLE);

                        //lblTitulo2.setText(t);
                    }else{
                        //lblTitulo2.setVisibility(View.INVISIBLE);
                       // lblTitulo.setText(tituloCompleto);
                    }


                }else{
                    pos=0;
                }



            }
        };

        wvwDetalleCircular.setOnTouchListener(onSwipeTouchListener);



        btnAnterior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(pos<=0){
                    return;
                }

                if(pos>0){
                    for(int i=0; i<circulares.size(); i++){
                        if(circulares.get(i).getIdCircular()==idCircular)
                            pos=i;
                    }

                    pos = pos - 1;
                    idCircular = circulares.get(pos).getIdCircular();
                    temaIcs = circulares.get(pos).getTemaIcs();
                    fechaIcs = circulares.get(pos).getFechaIcs();
                    ubicacionIcs = circulares.get(pos).getUbicacionIcs();
                    horaInicioIcs = circulares.get(pos).getHoraInicialIcs();
                    horaFinIcs = circulares.get(pos).getHoraFinalIcs();
                    String nivel = circulares.get(pos).getNivel();
                    if(!horaInicioIcs.equalsIgnoreCase("00:00:00"))
                        btnCalendario.setVisibility(View.VISIBLE);
                    else
                        btnCalendario.setVisibility(View.GONE);
                    new RegistrarLecturaAsyncTask(idCircular,idUsuario).execute();
                    wvwDetalleCircular.loadUrl(BASE_URL+RUTA+METODO+"?id="+idCircular);
                   // lblNivel.setText(nivel);

                    String tituloCompleto = circulares.get(pos).getNombre();
                    String[] titulo = tituloCompleto.split(" ");
                    int totalElementos = titulo.length;
                    if(totalElementos>2){
                        //lblTitulo.setText("");
                        //lblTitulo.setText(titulo[0]+" "+titulo[1]);
                        String t="";
                        //el titulo 2 tiene desde titulo[2] hasta titulo[totalElementos-1];
                        for(int i=2; i<totalElementos; i++){
                            t += titulo[i]+" ";

                        }
                        //lblTitulo2.setVisibility(View.VISIBLE);

                        ///lblTitulo2.setText(t);
                    }else{
                        //lblTitulo2.setVisibility(View.INVISIBLE);
                        //lblTitulo.setText(tituloCompleto);
                    }

                }else {
                    pos = circulares.size() - 1;
                }

            }
        });


        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //obtener la posición del id
                //Toast.makeText(getApplicationContext(),""+circulares.size(),Toast.LENGTH_LONG).show();
                try{
                    if(pos<circulares.size()){
                        for(int i=0; i<circulares.size(); i++){
                            if(circulares.get(i).getIdCircular()==idCircular)
                                pos=i;
                        }

                        //despues de obtenerla pasar a la siguiente circular
                        pos = pos+1;
                        idCircular = circulares.get(pos).getIdCircular();
                        temaIcs = circulares.get(pos).getTemaIcs();
                        fechaIcs = circulares.get(pos).getFechaIcs();
                        ubicacionIcs = circulares.get(pos).getUbicacionIcs();
                        horaInicioIcs = circulares.get(pos).getHoraInicialIcs();
                        horaFinIcs = circulares.get(pos).getHoraFinalIcs();
                        new RegistrarLecturaAsyncTask(idCircular,idUsuario).execute();
                        wvwDetalleCircular.loadUrl(BASE_URL+RUTA+METODO+"?id="+idCircular);
                        //lblTitulo.setText(circulares.get(pos).getNombre());
                        if(!horaInicioIcs.equalsIgnoreCase("00:00:00"))
                            btnCalendario.setVisibility(View.VISIBLE);
                        else
                            btnCalendario.setVisibility(View.GONE);

                        String tituloCompleto = circulares.get(pos).getNombre();
                        String[] titulo = tituloCompleto.split(" ");
                        int totalElementos = titulo.length;
                        int totalEspacios = totalElementos-1;
                        if(totalElementos>2){
                            //lblTitulo.setText("");
                            //lblTitulo.setText(titulo[0]+" "+titulo[1]);
                            String t="";
                            //el titulo 2 tiene desde titulo[2] hasta titulo[totalElementos-1];
                            for(int i=2; i<totalElementos; i++){
                                t += titulo[i]+" ";

                            }
                            //lblTitulo2.setVisibility(View.VISIBLE);

                            //lblTitulo2.setText(t);
                        }else{
                           // lblTitulo2.setVisibility(View.INVISIBLE);
                           // lblTitulo.setText(tituloCompleto);
                        }


                    }else{
                        pos=0;
                    }



                }catch (Exception ex){

                }




            }

        });


    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        onSwipeTouchListener.getGestureDetector().onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }


    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater findMenuItems = getMenuInflater();
        findMenuItems.inflate(R.menu.menu_zoom, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.zoomIn:
                wvwDetalleCircular.zoomIn();
                break;
            case R.id.zoomOut:
                wvwDetalleCircular.zoomOut();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
*/




    private class RegistrarLecturaAsyncTask extends AsyncTask<Void, Long, Boolean> {
        private String idCircular;
        private String idUsuario;

        public RegistrarLecturaAsyncTask(String idCircular, String idUsuario) {
            this.idCircular = idCircular;
            this.idUsuario = idUsuario;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("RESPONSE","ejecutando...");
        }

        public void registraLectura(){
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
            registraLectura();
            return null;
        }
    }
    private class NoLeerAsyncTask extends AsyncTask<Void, Long, Boolean> {
        private String idCircular;
        private String idUsuario;

        public NoLeerAsyncTask(String idCircular, String idUsuario) {
            this.idCircular = idCircular;
            this.idUsuario = idUsuario;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("RESPONSE","ejecutando...");
        }

        public void registraLectura(){
            HttpClient httpClient;
            HttpPost httppost;
            httpClient = new DefaultHttpClient();
            httppost = new HttpPost(BASE_URL+RUTA+METODO_NOLEER);
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
            registraLectura();
            return null;
        }
    }
    private class EliminaAsyncTask extends AsyncTask<Void, Long, Boolean> {
        private String idCircular;
        private String idUsuario;

        public EliminaAsyncTask(String idCircular, String idUsuario) {
            this.idCircular = idCircular;
            this.idUsuario = idUsuario;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("RESPONSE","ejecutando...");
        }

        public void registraLectura(){
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
            //Intent intent = new Intent(CircularDetalleActivity.this,CircularActivity.class);
            //startActivity(intent);
            //finish();
            Toast.makeText(getApplicationContext(),"Circular Eliminada",Toast.LENGTH_LONG).show();
            btnSiguiente.callOnClick();


        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            registraLectura();
            return null;
        }
    }
    private class FavAsyncTask extends AsyncTask<Void, Long, Boolean> {
        private String idCircular;
        private String idUsuario;

        public FavAsyncTask(String idCircular, String idUsuario) {
            this.idCircular = idCircular;
            this.idUsuario = idUsuario;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("RESPONSE","ejecutando...");
        }

        public void registraLectura(){
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
            //Intent intent = new Intent(CircularDetalleActivity.this,CircularActivity.class);
            //startActivity(intent);
            //finish();
            Toast.makeText(getApplicationContext(), "Se marcó esta circular como favorita", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            registraLectura();
            return null;
        }
    }


    public void share(String urlLargo){
        try{


            Bitly.shorten(urlLargo, new Bitly.Callback() {
                @Override
                public void onResponse(com.bitly.Response response) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, response.getBitlink());
                    sendIntent.setType("text/plain");
                    Intent shareIntent = Intent.createChooser(sendIntent, null);
                    startActivity(shareIntent);
                }

                @Override
                public void onError(Error error) {
                    Toast.makeText(getApplicationContext(),error.getErrorMessage(),Toast.LENGTH_LONG).show();
                }


            });





        }catch (Exception ex){
            Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
        }

        /**/




    }



/*
    private class CompartirAsyncTask extends AsyncTask<Void, Long, Boolean> {
        private String idCircular;
        private String idUsuario;

        public CompartirAsyncTask(String idCircular, String idUsuario) {
            this.idCircular = idCircular;
            this.idUsuario = idUsuario;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("RESPONSE","ejecutando...");

        }

        public void registraLectura(){
            HttpClient httpClient;
            HttpPost httppost;
            httpClient = new DefaultHttpClient();
            httppost = new HttpPost(BASE_URL+RUTA+METODO_COMPARTIR);
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

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/html");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT,"CHMD");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Te comparto la circular: "+wvwDetalleCircular.getUrl()+"");
            startActivity(Intent.createChooser(sharingIntent, "Compartir mediante"));


            //Intent intent = new Intent(CircularDetalleActivity.this,CircularActivity.class);
            //startActivity(intent);
            //finish();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            new RecortarUrlAsyncTask(wvwDetalleCircular.getUrl()).execute();
            registraLectura();
            return null;
        }
    }
*/
public void getCircularId(final int id){
    circularesId.clear();
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putInt("viaNotif",0);
    editor.commit();
    final SimpleDateFormat formatoInicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    final SimpleDateFormat formatoDestino = new SimpleDateFormat("HH:mm:ss");
    final SimpleDateFormat formatoDestino2 = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy");

    JsonArrayRequest req = new JsonArrayRequest(BASE_URL+RUTA+METODO_CIRCULAR+"?id="+id,
            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try {
                        for(int i=0; i<response.length(); i++){

                            JSONObject jsonObject = (JSONObject) response
                                    .get(i);
                            String idCircular = jsonObject.getString("id");
                            String nombre = jsonObject.getString("titulo");
                            t2 = nombre;
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
                            String estado = jsonObject.getString("id_estatus");
                            //String favorito = jsonObject.getString("favorito");
                            //String leido = jsonObject.getString("leido");
                            String contenido = jsonObject.getString("contenido");
                            //String eliminada = jsonObject.getString("eliminado");

                            String temaIcs = jsonObject.getString("tema_ics");
                            String fechaIcs = jsonObject.getString("fecha_ics");
                            String horaInicialIcs = jsonObject.getString("hora_inicial_ics");
                            String horaFinalIcs = jsonObject.getString("hora_final_ics");
                            String ubicacionIcs = jsonObject.getString("ubicacion_ics");
                            String adjunto = jsonObject.getString("adjunto");
                            String nivel = "";

                          /*  if(!jsonObject.getString("nivel").equalsIgnoreCase("null"))
                                lblNivel.setText(getIntent().getStringExtra("nivel"));
                            else
                                lblNivel.setText("");*/



                                circularesId.add(new Circular(idCircular,
                                        "Circular No. "+idCircular,
                                        nombre,"",
                                        strFecha1,
                                        strFecha2,
                                        estado,
                                        0,
                                        0,
                                        contenido,
                                        temaIcs,
                                        fechaIcs,
                                        horaInicialIcs,
                                        horaFinalIcs,
                                        ubicacionIcs,
                                        Integer.parseInt(adjunto),
                                        nivel,""
                                ));


                            //String idCircular, String encabezado, String nombre,
                            //                    String textoCircular, String fecha1, String fecha2, String estado

                        }


                    }catch (JSONException e)
                    {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

                    }


                    //TODO: Cambiarlo cuando pase a prueba en MX
                    // if (existe.equalsIgnoreCase("1")) {
                    //llenado de datos
                    //eliminar circulares y guardar las primeras 10 del registro
                    //Borra toda la tabla
                    /*contenidoCircular = circularesId.get(0).getTextoCircular();
                    temaIcs = circularesId.get(0).getTemaIcs();
                    fechaIcs = circularesId.get(0).getFechaIcs();
                    ubicacionIcs = circularesId.get(0).getUbicacionIcs();
                    horaInicioIcs = circularesId.get(0).getHoraInicialIcs();
                    horaFinIcs = circularesId.get(0).getHoraFinalIcs();
*/

                    /*try{
                        String[] titulo = circularesId.get(0).getNombre().split(" ");
                        int totalElementos = titulo.length;
                        int totalEspacios = totalElementos-1;
                        if(totalElementos>2){
                            lblTitulo.setText("");
                            lblTitulo.setText(titulo[0].toUpperCase()+" "+titulo[1].toUpperCase());
                            String t="";
                            //el titulo 2 tiene desde titulo[2] hasta titulo[totalElementos-1];
                            for(int i=2; i<totalElementos; i++){
                                t += titulo[i]+" ";

                            }
                            lblTitulo2.setVisibility(View.VISIBLE);

                            lblTitulo2.setText(t.toUpperCase());
                        }else{
                            lblTitulo2.setVisibility(View.INVISIBLE);
                            lblTitulo.setText(circularesId.get(0).getNombre().toUpperCase());
                        }
                    }catch (Exception ex){

                    }*/
                    wvwDetalleCircular.loadUrl(BASE_URL+RUTA+METODO+"?id="+id);


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



    public void getCirculares(String usuario_id){

        final SimpleDateFormat formatoInicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final SimpleDateFormat formatoDestino = new SimpleDateFormat("HH:mm:ss");
        final SimpleDateFormat formatoDestino2 = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy");

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+RUTA+METODO2+"?usuario_id="+usuario_id,
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
                                String eliminada = jsonObject.getString("eliminado");

                                String temaIcs = jsonObject.getString("tema_ics");
                                String fechaIcs = jsonObject.getString("fecha_ics");
                                String horaInicialIcs = jsonObject.getString("hora_inicial_ics");
                                String horaFinalIcs = jsonObject.getString("hora_final_ics");
                                String ubicacionIcs = jsonObject.getString("ubicacion_ics");
                                String adjunto = jsonObject.getString("adjunto");
                                String nivel = "";
                                String para = jsonObject.getString("espec");
                                try{
                                    nivel=jsonObject.getString("nivel");
                                }catch (Exception ex){
                                    nivel="";
                                }
                                if(eliminada!="1"){
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
                                }

                                //String idCircular, String encabezado, String nombre,
                                //                    String textoCircular, String fecha1, String fecha2, String estado

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
    public void getNotificaciones(String usuario_id){

        final SimpleDateFormat formatoInicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final SimpleDateFormat formatoDestino = new SimpleDateFormat("HH:mm:ss");
        final SimpleDateFormat formatoDestino2 = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy");

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+RUTA+METODO2N+"?usuario_id="+usuario_id,
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
                                String eliminada = jsonObject.getString("eliminado");

                                String temaIcs = jsonObject.getString("tema_ics");
                                String fechaIcs = jsonObject.getString("fecha_ics");
                                String horaInicialIcs = jsonObject.getString("hora_inicial_ics");
                                String horaFinalIcs = jsonObject.getString("hora_final_ics");
                                String ubicacionIcs = jsonObject.getString("ubicacion_ics");
                                String adjunto = jsonObject.getString("adjunto");
                                String nivel = "";
                                String para = jsonObject.getString("espec");
                                try{
                                    nivel=jsonObject.getString("nivel");
                                }catch (Exception ex){
                                    nivel="";
                                }

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


                        }catch (JSONException e)
                        {
                            e.printStackTrace();


                        }
                        //TODO: Cambiarlo cuando pase a prueba en MX
                        // if (existe.equalsIgnoreCase("1")) {
                        //llenado de datos
                        //eliminar circulares y guardar las primeras 10 del registro
                        //Borra toda la tabla




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
    public void getCircularesFavs(String usuario_id){

        final SimpleDateFormat formatoInicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final SimpleDateFormat formatoDestino = new SimpleDateFormat("HH:mm:ss");
        final SimpleDateFormat formatoDestino2 = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy");

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+RUTA+METODO2+"?usuario_id="+usuario_id,
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
                                String eliminada = jsonObject.getString("eliminado");

                                String temaIcs = jsonObject.getString("tema_ics");
                                String fechaIcs = jsonObject.getString("fecha_ics");
                                String horaInicialIcs = jsonObject.getString("hora_inicial_ics");
                                String horaFinalIcs = jsonObject.getString("hora_final_ics");
                                String ubicacionIcs = jsonObject.getString("ubicacion_ics");
                                String adjunto = jsonObject.getString("adjunto");
                                String para = jsonObject.getString("espec");
                                String nivel = "";
                                try{
                                    nivel=jsonObject.getString("nivel");
                                }catch (Exception ex){
                                    nivel="";
                                }
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
                                }

                                //String idCircular, String encabezado, String nombre,
                                //                    String textoCircular, String fecha1, String fecha2, String estado

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
    public void getCircularesNoLeidas(String usuario_id){

        final SimpleDateFormat formatoInicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final SimpleDateFormat formatoDestino = new SimpleDateFormat("HH:mm:ss");
        final SimpleDateFormat formatoDestino2 = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy");

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+RUTA+METODO2+"?usuario_id="+usuario_id,
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
                                String eliminada = jsonObject.getString("eliminado");

                                String temaIcs = jsonObject.getString("tema_ics");
                                String fechaIcs = jsonObject.getString("fecha_ics");
                                String horaInicialIcs = jsonObject.getString("hora_inicial_ics");
                                String horaFinalIcs = jsonObject.getString("hora_final_ics");
                                String ubicacionIcs = jsonObject.getString("ubicacion_ics");
                                String adjunto = jsonObject.getString("adjunto");
                                String para = jsonObject.getString("espec");
                                String nivel = "";
                                try{
                                    nivel=jsonObject.getString("nivel");
                                }catch (Exception ex){
                                    nivel="";
                                }
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
                                            nivel,
                                            para));
                                }

                                //String idCircular, String encabezado, String nombre,
                                //                    String textoCircular, String fecha1, String fecha2, String estado

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
    public void getCircularesEliminadas(String usuario_id){

        final SimpleDateFormat formatoInicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final SimpleDateFormat formatoDestino = new SimpleDateFormat("HH:mm:ss");
        final SimpleDateFormat formatoDestino2 = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy");

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+RUTA+METODO2+"?usuario_id="+usuario_id,
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
                                String eliminada = jsonObject.getString("eliminado");

                                String temaIcs = jsonObject.getString("tema_ics");
                                String fechaIcs = jsonObject.getString("fecha_ics");
                                String horaInicialIcs = jsonObject.getString("hora_inicial_ics");
                                String horaFinalIcs = jsonObject.getString("hora_final_ics");
                                String ubicacionIcs = jsonObject.getString("ubicacion_ics");
                                String adjunto = jsonObject.getString("adjunto");
                                String nivel = "";
                                String para = jsonObject.getString("espec");
                                try{
                                    nivel=jsonObject.getString("nivel");
                                }catch (Exception ex){
                                    nivel="";
                                }
                                if(Integer.parseInt(eliminada)==1){
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
                                }

                                //String idCircular, String encabezado, String nombre,
                                //                    String textoCircular, String fecha1, String fecha2, String estado

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
    public void leeCirculares(int idUsuario){

        ArrayList<DBCircular> dbCirculares = new ArrayList<>();
        List<DBCircular> list = new Select().from(DBCircular.class).where("idUsuario=? AND favorita=0",idUsuario).execute();
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
            String eliminada = String.valueOf(dbCirculares.get(i).eliminada);
            String para = dbCirculares.get(i).para;
            //Toast.makeText(getActivity(),contenido,Toast.LENGTH_LONG).show();
            if(Integer.parseInt(eliminada)==0)
            circulares.add(new Circular(idCircular,
                    "Circular No. "+idCircular,
                    nombre,"",
                    fecha1,
                    fecha2,
                    estado,
                    Integer.parseInt(leido),
                    Integer.parseInt(favorito),
                    contenido,"","","","","",0,"",para)
                    );

            }

        } //fin del for
    public void leeNotificaciones(int idUsuario){

        ArrayList<DBNotificacion> dbCirculares = new ArrayList<>();
        List<DBNotificacion> list = new Select().from(DBNotificacion.class).where("idUsuario=?",idUsuario).execute();
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
            String eliminada = String.valueOf(dbCirculares.get(i).eliminada);

            //Toast.makeText(getActivity(),contenido,Toast.LENGTH_LONG).show();
            if(Integer.parseInt(eliminada)==0)
                circulares.add(new Circular(idCircular,
                        "Circular No. "+idCircular,
                        nombre,"",
                        fecha1,
                        fecha2,
                        estado,
                        Integer.parseInt(leido),
                        Integer.parseInt(favorito),
                        contenido,"","","","","",0,"","")
                );

        }

    }
    public void leeCircularesFavs(int idUsuario){

        ArrayList<DBCircular> dbCirculares = new ArrayList<>();
        List<DBCircular> list = new Select().from(DBCircular.class).where("idUsuario=? AND favorita=0",idUsuario).execute();
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
            String eliminada = String.valueOf(dbCirculares.get(i).eliminada);
            String para = dbCirculares.get(i).para;
            //Toast.makeText(getActivity(),contenido,Toast.LENGTH_LONG).show();
            if(Integer.parseInt(favorito)==1)
            circulares.add(new Circular(idCircular,
                    "Circular No. "+idCircular,
                    nombre,"",
                    fecha1,
                    fecha2,
                    estado,
                    Integer.parseInt(leido),
                    Integer.parseInt(favorito),
                    contenido,"","","","","",0,"",para)
            );

        }

    }
    public void leeCircularesNoLeidas(int idUsuario){

        ArrayList<DBCircular> dbCirculares = new ArrayList<>();
        List<DBCircular> list = new Select().from(DBCircular.class).where("idUsuario=? AND favorita=0",idUsuario).execute();
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
            String eliminada = String.valueOf(dbCirculares.get(i).eliminada);
            String para = dbCirculares.get(i).para;
            //Toast.makeText(getActivity(),contenido,Toast.LENGTH_LONG).show();
            if(Integer.parseInt(leido)==0)
                circulares.add(new Circular(idCircular,
                        "Circular No. "+idCircular,
                        nombre,"",
                        fecha1,
                        fecha2,
                        estado,
                        Integer.parseInt(leido),
                        Integer.parseInt(favorito),
                        contenido,"","","","","",0,"",para)
                );

        }

    }
    public void leeCircularesEliminadas(int idUsuario){

        ArrayList<DBCircular> dbCirculares = new ArrayList<>();
        List<DBCircular> list = new Select().from(DBCircular.class).where("idUsuario=? AND favorita=0",idUsuario).execute();
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
            String eliminada = String.valueOf(dbCirculares.get(i).eliminada);
            String para = dbCirculares.get(i).para;
            //Toast.makeText(getActivity(),contenido,Toast.LENGTH_LONG).show();
            if(Integer.parseInt(eliminada)==1)
                circulares.add(new Circular(idCircular,
                        "Circular No. "+idCircular,
                        nombre,"",
                        fecha1,
                        fecha2,
                        estado,
                        Integer.parseInt(leido),
                        Integer.parseInt(favorito),
                        contenido,"","","","","",0,"",para)
                );

        }

    }



    private void agregarEventoCalendario(String startDate, String endDate, String title,String description, String location) {

        String stDate = startDate;
        String enDate = endDate;

        GregorianCalendar calDate = new GregorianCalendar();
        GregorianCalendar caleDate = new GregorianCalendar();
         //GregorianCalendar calEndDate = new GregorianCalendar();

        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
        Date date,edate;
        try {
            date = originalFormat.parse(startDate);
            stDate=targetFormat.format(date);
            edate = originalFormat.parse(endDate);
            enDate=targetFormat.format(edate);


        } catch (ParseException ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
        }

        long startMillis = 0;
        long endMillis = 0;
        String dates[] = stDate.split(",");
        String SD_YeaR = dates[0];
        String SD_MontH = dates[1];
        String SD_DaY = dates[2];
        String SD_HouR = dates[3];
        String SD_MinutE = dates[4];

        String edates[] = enDate.split(",");
        String E_SD_YeaR = edates[0];
        String E_SD_MontH = edates[1];
        String E_SD_DaY = edates[2];
        String E_SD_HouR = edates[3];
        String E_SD_MinutE = edates[4];






        /*Log.e("YeaR ", SD_YeaR);
        Log.e("MontH ",SD_MontH );
        Log.e("DaY ", SD_DaY);
        Log.e(" HouR", SD_HouR);
        Log.e("MinutE ", SD_MinutE);*/

        calDate.set(Integer.parseInt(SD_YeaR), Integer.parseInt(SD_MontH)-1, Integer.parseInt(SD_DaY), Integer.parseInt(SD_HouR), Integer.parseInt(SD_MinutE));
        startMillis = calDate.getTimeInMillis();

        caleDate.set(Integer.parseInt(E_SD_YeaR), Integer.parseInt(E_SD_MontH)-1, Integer.parseInt(E_SD_DaY), Integer.parseInt(E_SD_HouR), Integer.parseInt(E_SD_MinutE));
        endMillis = caleDate.getTimeInMillis();
/*
        try {
            edate = originalFormat.parse(endDate);
            enDate=targetFormat.format(edate);

        } catch (ParseException ex) {}


        String end_dates[] = endDate.split(",");

        String ED_YeaR = end_dates[0];
        String ED_MontH = end_dates[1];
        String ED_DaY = end_dates[2];

        String ED_HouR = end_dates[3];
        String ED_MinutE = end_dates[4];


        calEndDate.set(Integer.parseInt(ED_YeaR), Integer.parseInt(ED_MontH)-1, Integer.parseInt(ED_DaY), Integer.parseInt(ED_HouR), Integer.parseInt(ED_MinutE));
        endMillis = calEndDate.getTimeInMillis();*/

        try {
            ContentResolver cr = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, title);
            values.put(CalendarContract.Events.DESCRIPTION, description);
            values.put(CalendarContract.Events.EVENT_LOCATION,location);
            values.put(CalendarContract.Events.HAS_ALARM,1);
            values.put(CalendarContract.Events.CALENDAR_ID, 1);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, Calendar.getInstance()
                    .getTimeZone().getID());
            System.out.println(Calendar.getInstance().getTimeZone().getID());
            if (ActivityCompat.checkSelfPermission(CircularDetalleActivity.this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

            long eventId = Long.parseLong(uri.getLastPathSegment());
            Log.d("Ketan_Event_Id", String.valueOf(eventId));
            new Update(DBCircular.class).set("recordatorio=1").where("idCircular=?",idCircular).execute();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
}

