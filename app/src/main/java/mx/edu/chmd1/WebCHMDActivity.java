package mx.edu.chmd1;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.webkit.WebViewClient;

public class WebCHMDActivity extends AppCompatActivity {
TextView lblTextToolbar;
WebView webView;
    SharedPreferences sharedPreferences;

    private String CLIENT_ID="144850677714-gv1gkasv7i5t5284v6ftn7npijemp0ks.apps.googleusercontent.com";
    private static String REDIRECT_URI="http://www.chmd.edu.mx";
    private static String GRANT_TYPE="authorization_code";
    private static String TOKEN_URL ="https://accounts.google.com/o/oauth2/token";
    private static String OAUTH_URL ="https://accounts.google.com/o/oauth2/auth";
    private static String OAUTH_SCOPE = "email%20profile";
    private static String CLIENT_SECRET = "aegzGWfkW19XGfIArXxM22qE";


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(WebCHMDActivity.this, PrincipalActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_chmd);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.icon_home);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        lblTextToolbar = toolbar.findViewById(R.id.lblTextoToolbar);
        lblTextToolbar.setText("CHMD - Web");
        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/GothamRoundedBold_21016.ttf");
        lblTextToolbar.setTypeface(tf);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WebCHMDActivity.this, PrincipalActivity.class);
                startActivity(intent);
                finish();
            }
        });
        sharedPreferences = this.getSharedPreferences(this.getString(R.string.SHARED_PREF), 0);

        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setBuiltInZoomControls(true);
        //webView.setWebViewClient(new WebViewClient());
        String url = "https://www.chmd.edu.mx/pruebascd/icloud/";
        String ua = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0";
        Log.w("TOKEN",sharedPreferences.getString("idToken",""));
        //webView.loadUrl(OAUTH_URL+"?redirect_uri="+REDIRECT_URI+"&response_type=code&client_id="+CLIENT_ID+"&scope="+OAUTH_SCOPE);
        //webView.loadUrl(OAUTH_URL+"?redirect_uri="+REDIRECT_URI+"&response_type=code&client_id="+CLIENT_ID+"&scope="+OAUTH_SCOPE);


        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString(ua);
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl(url);
        //webView.loadUrl(OAUTH_URL+"?redirect_uri="+REDIRECT_URI+"&response_type=code&client_id="+client_id+"&scope="+OAUTH_SCOPE);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                super.onPageStarted(view, url, favicon);

            }
            String authCode;
            boolean authComplete=false;
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url.contains("?code=") && authComplete != true) {
                    Uri uri = Uri.parse(url);
                    authCode = uri.getQueryParameter("code");
                    //Log.i("", "CODE : " + authCode);
                    //Toast.makeText(getApplicationContext(),"CODE:"+authCode,Toast.LENGTH_LONG).show();
                    authComplete = true;
                }else{
                    //Toast.makeText(getApplicationContext(),"ERROR:"+url.toString(),Toast.LENGTH_LONG).show();
                }
            }
        });
        //webView.loadUrl(url, map);


    }






}
