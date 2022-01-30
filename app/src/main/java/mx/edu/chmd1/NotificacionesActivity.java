package mx.edu.chmd1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.activeandroid.query.Update;

import java.util.ArrayList;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;
import mx.edu.chmd1.adapter.NotificacionAdapter;
import mx.edu.chmd1.modelos.Notificacion;
import mx.edu.chmd1.modelosDB.DBNotificacion;

public class NotificacionesActivity extends AppCompatActivity {
    protected ArrayList<Notificacion> items = new ArrayList<>();
    ListView lstNotificaciones;

    TextView lblEncabezado;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotificacionesActivity.this, PrincipalActivity.class);
                startActivity(intent);
                finish();
            }
        });
         Typeface t = Typeface.createFromAsset(getAssets(),"fonts/GothamRoundedBold_21016.ttf");
        lblEncabezado = toolbar.findViewById(R.id.lblTextoToolbar);
        lblEncabezado.setText("Notificaciones recibidas");
        lblEncabezado.setTypeface(t);

        lstNotificaciones = findViewById(R.id.lstNotificaciones);
        lstNotificaciones.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Obtener el id de la circular
                Notificacion n = (Notificacion)lstNotificaciones.getItemAtPosition(i);
                String idCircular = n.getIdCircular();
                //Cambiar el estado de la notificación a 0
                new Update(DBNotificacion.class).set("estado=0").where("idCircular=?",idCircular).execute();
                //Reducir el badge
                ShortcutBadger.applyCount(getApplicationContext(), -1);
                //Cargar los detalles de la circular
                Intent  intent = new Intent(NotificacionesActivity.this, CircularDetalleActivity.class);
                intent.putExtra("idCircularN",idCircular);
                intent.putExtra("viaNotif",1);
                startActivity(intent);
            }
        });


        //ShortcutBadger.applyCount(getApplicationContext(), 0);

    }


    /*
    *  public void leeCirculares(int idUsuario){

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
    * */
}
