package mx.edu.chmd1;

import android.content.Intent;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import mx.edu.chmd1.adapter.TabAdapter;
import mx.edu.chmd1.fragment.CompartidosFragment;
import mx.edu.chmd1.fragment.EliminadasFragment;
import mx.edu.chmd1.fragment.FavoritosFragment;
import mx.edu.chmd1.fragment.NoLeidosFragment;
import mx.edu.chmd1.fragment.NotificacionesFragment;
import mx.edu.chmd1.fragment.TodasCircularesFragment;
import mx.edu.chmd1.modelos.Circular;
import mx.edu.chmd1.utilerias.CustomTabLayout;

public class CircularActivity extends AppCompatActivity {


    private SectionsPagerAdapter mSectionsPagerAdapter;


    private ViewPager mViewPager;
    private TabAdapter adapter;
    public static TabLayout tabLayout;
    private SearchView searchView;
    private Fragment f;
    TextView lblEncabezado;
    Typeface tf;
    private static int TODAS=0;
    private static int NO_LEIDAS=1;
    private static int FAVORITAS=2;
    private static int NOTIFICACIONES=3;
    private static int PAPELERA=4;
    SharedPreferences sharedPreferences;
    TodasCircularesFragment tfragment;
    NoLeidosFragment nlfragment;
    FavoritosFragment favoritosFragment;
    EliminadasFragment eliminadasFragment;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CircularActivity.this, PrincipalActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circular);
        sharedPreferences = this.getSharedPreferences(this.getString(R.string.SHARED_PREF), 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("viaNotif",0);
        editor.commit();
        tf = Typeface.createFromAsset(getAssets(),"fonts/GothamRoundedBold_21016.ttf");
        Toolbar toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabs);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CircularActivity.this, PrincipalActivity.class);
                startActivity(intent);
                finish();
            }
        });

        lblEncabezado = toolbar.findViewById(R.id.lblTextoToolbar);
        searchView = findViewById(R.id.searchView);
        lblEncabezado.setText("Circulares");
        lblEncabezado.setTypeface(tf);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        CustomTabLayout tabLayout = findViewById(R.id.tabs);
        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new TodasCircularesFragment(), "Todas");
        adapter.addFragment(new NoLeidosFragment(), "No Leídas");
        adapter.addFragment(new FavoritosFragment(), "Favoritas");
        //adapter.addFragment(new CompartidosFragment(), "Compartidas");
        adapter.addFragment(new NotificacionesFragment(), "Notificaciones");
        adapter.addFragment(new EliminadasFragment(), "Papelera");

        mViewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(mViewPager);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {


                    if(mViewPager.getCurrentItem()==0){
                        tfragment = (TodasCircularesFragment)mViewPager
                                .getAdapter()
                                .instantiateItem(mViewPager, mViewPager.getCurrentItem());
                        tfragment.adapter.getFilter().filter(newText);
                        tfragment.adapter.notifyDataSetChanged();
                        tfragment.lstCirculares.setAdapter(tfragment.adapter);
                    }


                    if(mViewPager.getCurrentItem()==1){
                        nlfragment = (NoLeidosFragment)mViewPager
                                .getAdapter()
                                .instantiateItem(mViewPager, mViewPager.getCurrentItem());
                        nlfragment.adapter.getFilter().filter(newText);
                        nlfragment.adapter.notifyDataSetChanged();
                        nlfragment.lstCirculares.setAdapter(nlfragment.adapter);
                    }

                    if(mViewPager.getCurrentItem()==2){
                        favoritosFragment = (FavoritosFragment)mViewPager
                                .getAdapter()
                                .instantiateItem(mViewPager, mViewPager.getCurrentItem());
                        favoritosFragment.adapter.getFilter().filter(newText);
                        favoritosFragment.adapter.notifyDataSetChanged();
                        favoritosFragment.lstCirculares.setAdapter(favoritosFragment.adapter);
                    }

                    if(mViewPager.getCurrentItem()==3){
                        NotificacionesFragment fragment = (NotificacionesFragment)mViewPager
                                .getAdapter()
                                .instantiateItem(mViewPager, mViewPager.getCurrentItem());
                        fragment.adapter.getFilter().filter(newText);
                        fragment.adapter.notifyDataSetChanged();
                        fragment.lstCirculares.setAdapter(fragment.adapter);
                    }


                    if(mViewPager.getCurrentItem()==4){
                        eliminadasFragment = (EliminadasFragment)mViewPager
                                .getAdapter()
                                .instantiateItem(mViewPager, mViewPager.getCurrentItem());
                        eliminadasFragment.adapter.getFilter().filter(newText);
                        eliminadasFragment.adapter.notifyDataSetChanged();
                        eliminadasFragment.lstCirculares.setAdapter(eliminadasFragment.adapter);
                    }

                }catch (Exception ex){
                    Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
                }






                return true;
            }
        });

        int tabSelecc = getIntent().getIntExtra("tabSelecc",TODAS);
        if(tabSelecc==NO_LEIDAS){
            TabLayout.Tab tab = tabLayout.getTabAt(NO_LEIDAS);
            tab.select();
        }
        if(tabSelecc==FAVORITAS){
            TabLayout.Tab tab = tabLayout.getTabAt(FAVORITAS);
            tab.select();
        }
        if(tabSelecc==PAPELERA){
            TabLayout.Tab tab = tabLayout.getTabAt(PAPELERA);
            tab.select();
        }
        if(tabSelecc==NOTIFICACIONES){
            TabLayout.Tab tab = tabLayout.getTabAt(NOTIFICACIONES);
            tab.select();
        }

    }





    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }


        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_circular, container, false);

            return rootView;
        }
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 4;
        }
    }



}
