package mx.edu.chmd1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class PoliticaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_politica);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.icon_home);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        TextView lblTextToolbar = toolbar.findViewById(R.id.lblTextoToolbar);
        lblTextToolbar.setText("CHMD - Política de Privacidad");
        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/GothamRoundedBold_21016.ttf");
        lblTextToolbar.setTypeface(tf);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(PoliticaActivity.this, PrincipalActivity.class);
            startActivity(intent);
            finish();
        });

        WebView webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setBuiltInZoomControls(true);

        String url = "https://www.chmd.edu.mx/aviso-de-privacidad/";
        String ua = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0";

        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString(ua);
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl(url);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

            }
        });

    }
}