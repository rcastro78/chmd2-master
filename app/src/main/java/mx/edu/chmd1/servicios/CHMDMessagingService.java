package mx.edu.chmd1.servicios;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;
import mx.edu.chmd1.CircularDetalleActivity;
import mx.edu.chmd1.InicioActivity;
import mx.edu.chmd1.R;
import mx.edu.chmd1.modelosDB.DBNotificacion;
import mx.edu.chmd1.validaciones.ValidarPadreActivity;

public class CHMDMessagingService extends FirebaseMessagingService {
    private static final String NOTIFICATION_ID_EXTRA = "notificationId";
    private static final String IMAGE_URL_EXTRA = "imageUrl";
    private static final String ADMIN_CHANNEL_ID ="admin_channel";
    private NotificationManager notificationManager;
    boolean notificacionRecibida=false;
    private static String CHAT="chat";
    private static String FEED="noticias";
    private static String CIRCULAR="circular";

    static String BASE_URL;
    static String RUTA;
    static String METODO_REG="registrarDispositivo.php";
    SharedPreferences sharedPreferences;

    String title="",body="",idCircular="",tituloCircular="",token="";
    String strTipo="",rsp="",correoRegistrado="",fechaCircularNotif="";

    @Override
    public void onCreate() {
        super.onCreate();
        BASE_URL = this.getString(R.string.BASE_URL);
        RUTA = this.getString(R.string.PATH);
        sharedPreferences = getSharedPreferences(this.getString(R.string.SHARED_PREF), 0);
        correoRegistrado = sharedPreferences.getString("correoRegistrado","");
    }



    @SuppressLint("WrongThread")
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("TOKEN",s);
        String versionRelease = Build.VERSION.RELEASE;
        token = s;
        new RegistrarDispositivoAsyncTask(correoRegistrado,token,"Android OS "+versionRelease).execute();
    }

    @Override
    public void onMessageReceived (RemoteMessage remoteMessage) {
        super .onMessageReceived(remoteMessage);
        Intent notificationIntent = new Intent(getApplicationContext() , ValidarPadreActivity.class ) ;
        /*Map<String, String> data = remoteMessage.getData();


        String idCircular1= data.get("idCircular");
        if(idCircular1.length()>0){
            Intent intent = new Intent(this,CircularDetalleActivity.class);
            intent.putExtra("idCircularN",idCircular1);
            intent.putExtra("viaNotif",1);
            startActivity(intent);
        }*/

        notificationIntent.putExtra("idCircularN" , remoteMessage.getData().get("idCircular")) ;
        notificationIntent.putExtra("viaNotif" , 1);

        PendingIntent resultIntent = PendingIntent. getActivity (getApplicationContext() , 0 , notificationIntent , 0 ) ;

        //Esto trabaja con la app abierta
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext() ,
                ADMIN_CHANNEL_ID )
                .setSmallIcon(R.drawable.icono_notificaciones)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("body"))
                .setAutoCancel(true)
                .setContentIntent(resultIntent) ;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context. NOTIFICATION_SERVICE ) ;
        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
            int importance = NotificationManager. IMPORTANCE_HIGH ;
            NotificationChannel notificationChannel = new
                    NotificationChannel( ADMIN_CHANNEL_ID , "NOTIFICATION_CHANNEL_NAME" , importance) ;
            mBuilder.setChannelId( ADMIN_CHANNEL_ID ) ;
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(notificationChannel) ;
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(( int ) System. currentTimeMillis () ,
                mBuilder.build()) ;
        //Se dispara con la app cerrada
        String idCircularNt = remoteMessage.getData().get("idCircular");
        //sendNotification(remoteMessage.getData().get("body"),remoteMessage.getData().get("title"),remoteMessage.getData().get("idCircular"));

        if (remoteMessage.getData().size() > 0) {
            try{
                String viaNotificacion = remoteMessage.getData().get("viaNotificacion");
                String idCircular = remoteMessage.getData().get("idCircular");
                String messageBody = remoteMessage.getData().get("body");
                String title = remoteMessage.getData().get("title");
                notificationIntent = new Intent(getApplicationContext(), ValidarPadreActivity.class);
                notificationIntent.putExtra("idCircularN" , remoteMessage.getData().get("idCircular")) ;
                notificationIntent.putExtra("viaNotif" , 1);
                notificationIntent.putExtra("idCircular" , remoteMessage.getData().get("idCircular")) ;
                notificationIntent.putExtra("viaNotificacion" , 1);
                //showNotificationMessage(title, messageBody, time, resultIntent, null);
                sendNotification(remoteMessage,idCircular);
            } catch (Exception e){
                Log.e("idCircularNotif", "Exception: " + e.getMessage());
            }
        }


    }

/*
    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels();
        }
        int tipo=0;
        //el strTipo viene en el data de la notificación
        strTipo = message.getData().get("strTipo");
        title = message.getData().get("title");
        body = message.getData().get("body");
        idCircular = message.getData().get("idCircular");
        tituloCircular = message.getData().get("tituloCircular");
        fechaCircularNotif = message.getData().get("fechaCircularNotif");


        if(message.getNotification()!=null){
            notificacionRecibida = true;
            strTipo = message.getData().get("strTipo");
            title = message.getData().get("title");
            body = message.getData().get("body");
            idCircular = message.getData().get("idCircular");
            fechaCircularNotif = message.getData().get("fechaCircularNotif");
            tituloCircular = message.getData().get("tituloCircular");

        }
        sendNotification(message,strTipo);



    }
*/


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(){
        CharSequence adminChannelName = ADMIN_CHANNEL_ID;
        String adminChannelDescription = ADMIN_CHANNEL_ID;

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_LOW);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null)
            notificationManager.createNotificationChannel(adminChannel);
    }


    private void sendNotification(RemoteMessage r, String action) {

        Intent intent = null;
        PendingIntent pendingIntent = null;

        if(r.getData()!=null){
            intent = new Intent(this, ValidarPadreActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("idCircularNotif",r.getData().get("idCircular"));
            intent.putExtra("viaNotificacion",r.getData().get("viaNotificacion"));

            Log.d("idCircularN",r.getData().get("idCircular"));

            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            //Con esto colocamos el badge
            ShortcutBadger.applyCount(getApplicationContext(), 1);


        }else{

            intent = new Intent(this, ValidarPadreActivity.class);
            intent.putExtra("idCircularNotif",idCircular);
            intent.putExtra("fechaCircularNotif",idCircular);

            intent.putExtra("tituloCircular",title);
            intent.putExtra("viaNotif",1);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Calendar c = Calendar.getInstance();
            String hoy=sdf.format(c.getTime());
            intent.putExtra("hoy",hoy);

            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            ShortcutBadger.applyCount(getApplicationContext(), 1);

        }




        Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icono_notificaciones)//notification icon
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.icono_notificaciones))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
                .setNumber(1)

                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());

    }


    private class RegistrarDispositivoAsyncTask extends AsyncTask<Void, Long, Boolean> {
        private String correo;
        private String device_token;
        private String plataforma;


        public RegistrarDispositivoAsyncTask(String correo, String device_token, String plataforma) {
            this.correo = correo;
            this.device_token = device_token;
            this.plataforma = plataforma;
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
                List<NameValuePair> nameValuePairs = new ArrayList<>(3);
                nameValuePairs.add(new BasicNameValuePair("correo",correo));
                nameValuePairs.add(new BasicNameValuePair("device_token",device_token));
                nameValuePairs.add(new BasicNameValuePair("plataforma",plataforma));
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


}