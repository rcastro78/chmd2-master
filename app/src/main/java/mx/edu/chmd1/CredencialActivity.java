package mx.edu.chmd1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

import mx.edu.chmd1.networking.APIUtils;
import mx.edu.chmd1.networking.ICircularesCHMD;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Response;

public class CredencialActivity extends AppCompatActivity {
ImageView imgFotoPadre,imgQR,firma;
TextView lblNombrePadre,lblPadre,lblNumFam;
    File camFile;
    private static String BASE_URL_FOTO="http://chmd.chmd.edu.mx:65083/CREDENCIALES/padres/";
    private static String BASE_URL_FOTO2="https://www.chmd.edu.mx/WebAdminCirculares/ws/";
    private static String URL_FIRMA="https://www.chmd.edu.mx/imagenesapp/img/firma.jpg";
    static String BASE_URL;
    static String RUTA;
    SharedPreferences sharedPreferences;
    String cifrado="",vigencia="";
    int idUsuario=0;
    Bitmap bmp;
    ICircularesCHMD iCircularesCHMD;
    String fotoCredencial2;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try{
            Glide.with(this)
                    .clear(imgFotoPadre);
        }catch (Exception ignored){

        }
        Intent intent = new Intent(CredencialActivity.this, PrincipalActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        try{
            Glide.with(this)
                    .clear(imgFotoPadre);
        }catch (Exception ignored){

        }



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credencial);
        sharedPreferences = getSharedPreferences(this.getString(R.string.SHARED_PREF), 0);
        fotoCredencial2 = sharedPreferences.getString("fotoCredencial2","");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.icon_home);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(CredencialActivity.this, PrincipalActivity.class);
            startActivity(intent);
            finish();
        });



        iCircularesCHMD = APIUtils.getCircularesService();
        idUsuario =  Integer.parseInt(sharedPreferences.getString("idUsuarioCredencial","0"));
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

        Picasso.with(this)
                .load(URL_FIRMA)
                .placeholder(R.drawable.logo2)
                .error(R.drawable.logo2)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(firma);

        if(fotoCredencial2.length()>0){

            Picasso.with(this)
                    .load(BASE_URL_FOTO2+fotoCredencial2)
                    .placeholder(R.drawable.icon_non_profile)
                    .error(R.drawable.icon_non_profile)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    //.transform(new Transformacion(90))
                    .into(imgFotoPadre);

            Bitmap bmp = null;
            try {
                bmp = crearQR(cifrado);
            } catch (WriterException e) {
                e.printStackTrace();
            }
            imgQR.setImageBitmap(bmp);

        }else {

            //No tiene foto nueva

            if (fotoUrl.length() <= 0) {
                //No tiene foto de carnet,
                fotoUrl = "http://chmd.chmd.edu.mx:65083/CREDENCIALES/padres/sinfoto.png";
                try {
                    URL uri = new URL(fotoUrl);
                    HttpURLConnection huc = (HttpURLConnection) uri.openConnection();
                    int responseCode = huc.getResponseCode();
                    Toast.makeText(getApplicationContext(), "" + responseCode, Toast.LENGTH_LONG).show();
                    if (responseCode > 400) {
                        if (fotoUrl.contains(".jpg"))
                            fotoUrl.replace(".jpg", ".JPG");

                        if (fotoUrl.contains(".JPG"))
                            fotoUrl.replace(".JPG", ".jpg");
                    }
                    Picasso.with(this)
                            .load(fotoUrl)
                            .placeholder(R.drawable.icon_non_profile)
                            .error(R.drawable.icon_non_profile)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .into(imgFotoPadre);
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                }


                try {
                    Bitmap bmp = crearQR(cifrado);
                    imgQR.setImageBitmap(bmp);
                } catch (Exception ex) {
                }

            } else {


                final String finalFotoUrl = fotoUrl;
                Bitmap bmp = null;
                try {
                    bmp = crearQR(cifrado);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
                imgQR.setImageBitmap(bmp);

                HttpURLConnection huc = null;
                try {
                    URL uri = new URL(fotoUrl);
                    huc = (HttpURLConnection) uri.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int responseCode = 0;
                try {
                    responseCode = huc.getResponseCode();
                } catch (Exception ex) {
                }


                if (responseCode > 400) {
                    if (fotoUrl.contains(".jpg"))
                        fotoUrl.replace(".jpg", ".JPG");

                    if (fotoUrl.contains(".JPG"))
                        fotoUrl.replace(".JPG", ".jpg");
                }


                Picasso.with(this)
                        .load(BASE_URL_FOTO + fotoUrl)
                        .placeholder(R.drawable.logo2)
                        .error(R.drawable.icon_non_profile)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .into(imgFotoPadre);


            }

        }

        }



        public boolean hayConexion() {
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();

            return netInfo != null && netInfo.isConnectedOrConnecting();

        }



    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_foto, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuCamera:
                if(hayConexion()) {
                    selectImage();
                }else{
                    Toast.makeText(getApplicationContext(), "Esta función solo se puede utilizar con internet", Toast.LENGTH_LONG).show();
                }

            case R.id.menuUpload:

                if (bmp != null){

                    Toast.makeText(getApplicationContext(), "Subiendo tu foto...No cierres esta pantalla", Toast.LENGTH_LONG).show();

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    bmp.compress(Bitmap.CompressFormat.JPEG,90,bos);

                    if(bmp.getWidth()>240) {


                        float aspectRatio = bmp.getWidth() /
                                (float) bmp.getHeight();
                        int width = 240;
                        int height = Math.round(width / aspectRatio);

                        bmp = Bitmap.createScaledBitmap(
                                bmp, width, height, false);

                    }

                    String filename=String.valueOf(idUsuario);
                    byte[] data = bos.toByteArray();
                    try{
                        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                                .addFormDataPart("usuario_id",String.valueOf(idUsuario))
                                .addFormDataPart("image",filename+".jpg",RequestBody.create(MediaType.parse("image/*.jpg"), data))
                                .build();

                        Request request = new Request.Builder()
                                .url(BASE_URL+"WebAdminCirculares/ws/actualizaFoto.php")
                                .post(requestBody)
                                .build();
                        OkHttpClient client = new OkHttpClient();
                        client.newCall(request).enqueue(new Callback() {

                            @Override
                            public void onFailure(@NonNull final okhttp3.Call call, final IOException e) {
                                Log.e("ERROR_SUBIDA",e.getMessage());
                                bmp=null;
                            }

                            @Override
                            public void onResponse(Call call, okhttp3.Response response) {
                                if (!response.isSuccessful()) {
                                    Log.e("ERROR_SUBIDA",""+response.message());
                                }else{
                                    bmp=null;
                                    Toast.makeText(getApplicationContext(), "Subida exitosa", Toast.LENGTH_LONG).show();
                                     Intent intent = new Intent(CredencialActivity.this,PrincipalActivity.class);
                                    startActivity(intent);
                                }
                            }


                        });
                    }catch (Exception ex){
                        Log.e("ERROR_SUBIDA",""+ex.getMessage());
                    }



                }else{
                    Toast.makeText(getApplicationContext(),"No puedes subir sin cambiar tu foto",Toast.LENGTH_LONG).show();
                }

            default:
            return false;

        }
    }




    private void selectImage() {
        final CharSequence[] options = { "Tomar Foto", "Seleccionar de la galería","Cancelar" };
        AlertDialog.Builder builder = new AlertDialog.Builder(CredencialActivity.this);
        builder.setTitle("Cambiar tu foto desde...");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Tomar Foto"))
            {

                Intent cameraImgIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    camFile = createImageFile();
                    if (camFile != null){
                        Uri uri = FileProvider.getUriForFile(this,"mx.edu.chmd1.provider",camFile);
                        cameraImgIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                        startActivityForResult(cameraImgIntent, 1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
            else if (options[item].equals("Seleccionar de la galería"))
            {
                Intent intent = new   Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2);
            }
            else if (options[item].equals("Cancelar")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Glide.with(this)
                        .clear(imgFotoPadre);
                Bitmap bitmap = BitmapFactory.decodeFile(camFile.getAbsolutePath());
                imgFotoPadre.setImageBitmap(bitmap);
                bmp = bitmap;

            } else if (requestCode == 2) {

                Glide.with(this)
                        .clear(imgFotoPadre);


                Uri selectedImage = data.getData();
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));

                imgFotoPadre.setImageBitmap(thumbnail);
                bmp = thumbnail;
            }
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



    File createImageFile() throws IOException {

        String imageFileName = String.valueOf(idUsuario);
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File img = File.createTempFile(imageFileName, ".jpg", dir);
        return img;
    }



    private class Transformacion extends BitmapTransformation
    {
        int mOrientation=0;
        public Transformacion(int orientation) {
            mOrientation = orientation;
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            int exifOrientationDegrees = getExifOrientationDegrees(mOrientation);
            return TransformationUtils.rotateImageExif(pool, toTransform, exifOrientationDegrees);
        }

        private int getExifOrientationDegrees(int orientation) {
            int exifInt;
            switch (orientation) {
                case 90:
                    exifInt = ExifInterface.ORIENTATION_ROTATE_90;
                    break;
                // other cases
                default:
                    exifInt = ExifInterface.ORIENTATION_NORMAL;
                    break;
            }
            return exifInt;
        }

        @Override
        public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

        }
    }


}
