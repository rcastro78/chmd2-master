package mx.edu.chmd1;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.ArrayList;

import mx.edu.chmd1.Alumnos.AlumnosActivity;
import mx.edu.chmd1.Padres.PadresActivity;
import mx.edu.chmd1.adapter.MenuAdapter;
import mx.edu.chmd1.modelos.Menu;

public class MenuCircularesActivity extends AppCompatActivity {
    MenuAdapter menuAdapter = null;
    ListView lstPrincipal;
    ArrayList<Menu> items = new ArrayList<>();
    VideoView videoview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        videoview = findViewById(R.id.videoView);
        videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                videoview.start();
            }
        });
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.video_app);
        videoview.setVideoURI(uri);
        videoview.start();

        lstPrincipal = findViewById(R.id.lstPrincipal);
        llenarMenu();
        lstPrincipal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Menu m = (Menu)lstPrincipal.getItemAtPosition(position);
                if(m.getIdMenu()==1){
                    //Intent intent = new Intent(PrincipalActivity.this,MainActivity.class);
                    Intent intent = new Intent(MenuCircularesActivity.this, PadresActivity.class);
                    startActivity(intent);
                }
                //web
                if(m.getIdMenu()==2){
                    Intent intent = new Intent(MenuCircularesActivity.this, AlumnosActivity.class);
                    startActivity(intent);
                }

                if(m.getIdMenu()==3){
                    /*Intent intent = new Intent(MenuCircularesActivity.this, AlumnosActivity.class);
                    startActivity(intent);*/
                    Toast.makeText(getApplicationContext(),"En construcción",Toast.LENGTH_LONG).show();
                }

                if(m.getIdMenu()==4){
                    /*Intent intent = new Intent(MenuCircularesActivity.this, AlumnosActivity.class);
                    startActivity(intent);*/
                    Toast.makeText(getApplicationContext(),"En construcción",Toast.LENGTH_LONG).show();
                }

                if(m.getIdMenu()==5){
                    Intent intent = new Intent(MenuCircularesActivity.this, CircularActivity.class);
                    startActivity(intent);
                    //Toast.makeText(getApplicationContext(),"En construcción",Toast.LENGTH_LONG).show();
                }


            }
        });
    }

    private void llenarMenu(){
        items.add(new Menu(1,"Padres",R.drawable.appmenu06));
        items.add(new Menu(2,"Alumnos",R.drawable.appmenu06));
        items.add(new Menu(3,"Grupos Administrativos",R.drawable.appmenu06));
        items.add(new Menu(4,"Grupos Especiales",R.drawable.appmenu06));
        items.add(new Menu(5,"Circulares",R.drawable.appmenu06));
        menuAdapter = new MenuAdapter(MenuCircularesActivity.this,items);
        lstPrincipal.setAdapter(menuAdapter);
    }
}
