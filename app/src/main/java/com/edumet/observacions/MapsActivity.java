package com.edumet.observacions;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private double latitud;
    private double longitud;
    String[] nomFenomen;
    private int numFenomen=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.map_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.disableShiftMode(navigation);

        Resources res = getResources();
        nomFenomen = res.getStringArray(R.array.nomFenomen);

        Intent intent = getIntent();
        latitud= Double.valueOf(intent.getStringExtra(MainActivity.EXTRA_LATITUD));
        longitud= Double.valueOf(intent.getStringExtra(MainActivity.EXTRA_LONGITUD));
        numFenomen=Integer.valueOf(intent.getStringExtra(MainActivity.EXTRA_NUMFENOMEN));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_observacions:
                    intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_estacions:
                    intent = new Intent(getApplicationContext(),Estacions.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_radar:
                    intent = new Intent(getApplicationContext(),Radar.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_pronostic:
                    //intent = new Intent(getApplicationContext(),Proostic.class);
                    //startActivity(intent);
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        String etiqueta;
        if(numFenomen>0) {
            etiqueta=nomFenomen[numFenomen];
        }else {
            etiqueta = "Ubicació actual";
        }
        mMap = googleMap;
        LatLng observacio = new LatLng(latitud,longitud);
        mMap.addMarker(new MarkerOptions().position(observacio).title(etiqueta));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(observacio,15.0f));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}