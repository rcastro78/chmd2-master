package mx.edu.chmd1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class CredencialActivity extends AppCompatActivity {
ImageView imgFotoPadre,imgQR,firma;
TextView lblNombrePadre,lblPadre,lblNumFam;
private static String BASE_URL_FOTO="http://chmd.chmd.edu.mx:65083/CREDENCIALES/padres/";
    private static String URL_FIRMA="https://www.chmd.edu.mx/imagenesapp/img/firma.jpg";
    static String BASE_URL;
    static String RUTA;
    SharedPreferences sharedPreferences;
    String cifrado="",vigencia="";
    int idUsuario=0;
    String fotoCargar="";

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CredencialActivity.this, PrincipalActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credencial);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.icon_home);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CredencialActivity.this, PrincipalActivity.class);
                startActivity(intent);
                finish();
            }
        });

        TextView lblEncabezado = toolbar.findViewById(R.id.lblTextoToolbar);
        lblEncabezado.setText("Credencial");
        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/GothamRoundedMedium_21022.ttf");
        Typeface tf1 = Typeface.createFromAsset(getAssets(),"fonts/GothamRoundedBold_21016.ttf");
        lblEncabezado.setTypeface(tf1);
        lblNombrePadre = findViewById(R.id.lblNombrePadre);
        lblPadre = findViewById(R.id.lblPadre);
        lblNumFam = findViewById(R.id.lblVigencia);
        imgFotoPadre =findViewById(R.id.imgFotoPadre);
        firma = findViewById(R.id.firma);
        imgQR =findViewById(R.id.imgQR);
        BASE_URL = this.getString(R.string.BASE_URL);
        RUTA = this.getString(R.string.PATH);
        sharedPreferences = getSharedPreferences(this.getString(R.string.SHARED_PREF), 0);
        cifrado = sharedPreferences.getString("cifrado","");
        vigencia = sharedPreferences.getString("vigencia","");
        String nombrePadre = sharedPreferences.getString("nombreCredencial","S/N");
        nombrePadre.toLowerCase();
        lblNombrePadre.setText(nombrePadre);
        lblNumFam.setText("Vigente hasta: "+vigencia);
        lblPadre.setText(sharedPreferences.getString("responsableCredencial","N/A"));


        lblNombrePadre.setTypeface(tf);
        lblPadre.setTypeface(tf);
        lblNumFam.setTypeface(tf);
        String fotoUrl = sharedPreferences.getString("fotoCredencial","");


        Glide.with(this)
                .load(URL_FIRMA)
                .placeholder(R.drawable.logo2)
                .error(R.drawable.logo2)
                .into(firma);

        if (fotoUrl.length()<=0){
            //No tiene foto
            fotoUrl = "http://chmd.chmd.edu.mx:65083/CREDENCIALES/padres/sinfoto.png";
            try{
                URL uri = new URL(fotoUrl);
                HttpURLConnection huc = (HttpURLConnection) uri.openConnection();
                int responseCode = huc.getResponseCode();
                Toast.makeText(getApplicationContext(),""+responseCode,Toast.LENGTH_LONG).show();
                if(responseCode>400){
                    if(fotoUrl.contains(".jpg"))
                        fotoUrl.replace(".jpg",".JPG");

                    if(fotoUrl.contains(".JPG"))
                        fotoUrl.replace(".JPG",".jpg");
                }
                Glide.with(this)
                        .load(fotoUrl)
                        .placeholder(R.drawable.icon_non_profile)
                        .error(R.drawable.icon_non_profile)
                        .into(imgFotoPadre);
            }catch (Exception ex){
                Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
            }


            try{
                Bitmap bmp = crearQR(fotoUrl);
                imgQR.setImageBitmap(bmp);
            }catch(Exception ex){
            }

        }else{

            //revisar que el response de la URL no sea 40x
            int response;



            final String finalFotoUrl = fotoUrl;

            new Thread() {

                public void run() {
                    //your "file checking code" goes here like this
                    //write your results to log cat, since you cant do Toast from threads without handlers also...
                    fotoCargar = finalFotoUrl;

                    if(!exists(BASE_URL_FOTO+fotoCargar)){
                        if(fotoCargar.contains(".JPG")) {fotoCargar = fotoCargar.replace(".JPG",".jpg");}
                        if(fotoCargar.contains(".jpg")) {fotoCargar = fotoCargar.replace(".jpg",".JPG");}
                    }

                    if(exists(BASE_URL_FOTO+fotoCargar)) {


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(CredencialActivity.this)
                                        .load(BASE_URL_FOTO + fotoCargar)
                                        .placeholder(R.drawable.icon_non_profile)
                                        .error(R.drawable.icon_non_profile)
                                        .into(imgFotoPadre);

                                try{
                                    Bitmap bmp = crearQR(cifrado);
                                    imgQR.setImageBitmap(bmp);
                                }catch(Exception ex){
                                }

                            }
                        });
                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(CredencialActivity.this)
                                        .load("http://chmd.chmd.edu.mx:65083/CREDENCIALES/padres/sinfoto.png")
                                        .placeholder(R.drawable.icon_non_profile)
                                        .error(R.drawable.icon_non_profile)
                                        .into(imgFotoPadre);


                                try{
                                    Bitmap bmp = crearQR(cifrado);
                                    imgQR.setImageBitmap(bmp);
                                }catch(Exception ex){
                                }
                            }
                        });
                    }



                    try {
                        HttpURLConnection.setFollowRedirects(false);
                        // note : you may also need
                        //HttpURLConnection.setInstanceFollowRedirects(false)

                        /*HttpURLConnection con =  (HttpURLConnection) new URL(BASE_URL_FOTO + foto.replace(".jpg",".JPG")).openConnection();
                        con.setRequestMethod("HEAD");
                        if( (con.getResponseCode() == HttpURLConnection.HTTP_OK) )
                            Glide.with(CredencialActivity.this)
                                    .load(BASE_URL_FOTO+foto.replace(".jpg",".JPG"))
                                    .placeholder(R.drawable.icon_non_profile)
                                    .error(R.drawable.icon_non_profile)
                                    .into(imgFotoPadre);
                        else

                            Glide.with(CredencialActivity.this)
                                    .load("http://chmd.chmd.edu.mx:65083/CREDENCIALES/padres/sinfoto.png")
                                    .placeholder(R.drawable.icon_non_profile)
                                    .error(R.drawable.icon_non_profile)
                                    .into(imgFotoPadre);


                        try{
                            Bitmap bmp = crearQR(cifrado);
                            imgQR.setImageBitmap(bmp);
                        }catch(Exception ex){
                        }*/


                    }
                    catch (Exception e) {
                        e.printStackTrace();

                    }
                }
            }.start();





            }




        }


        //Cargar la foto del padre

        //generar QR a partir de la URL




        /*

*  editor.putString("idUsuario",idUsuario);
                                editor.putString("nombre",nombre);
                                editor.putString("numero",numero);
                                editor.putString("telefono",telefono);
                                editor.putString("responsable",responsable);
                                editor.putString("familia",familia);
* */


    public static boolean exists(String URLName){
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //HttpURLConnection.setInstanceFollowRedirects(false)

            HttpURLConnection con =  (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    Bitmap crearQR(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, 200, 200, null);
        } catch (IllegalArgumentException iae) {

            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 200, 0, 0, w, h);
        return bitmap;
    }


    public int getResponseCode(String urlString){
        try{
            URL u = new URL(urlString);
            HttpURLConnection huc =  (HttpURLConnection)  u.openConnection();
            huc.setRequestMethod("HEAD");
            return huc.getResponseCode();
        }catch (Exception ex){
            return -1;
        }

    }


    public static byte[] encrypt(byte[] data, byte[] key, byte[] ivs) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            byte[] finalIvs = new byte[16];
            int len = ivs.length > 16 ? 16 : ivs.length;
            System.arraycopy(ivs, 0, finalIvs, 0, len);
            IvParameterSpec ivps = new IvParameterSpec(finalIvs);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivps);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }



}
