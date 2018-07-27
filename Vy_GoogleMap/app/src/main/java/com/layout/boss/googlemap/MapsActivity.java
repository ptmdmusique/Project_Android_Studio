package com.layout.boss.googlemap;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    //UI
    private static final float DEFAULT_ZOOM = 15f;
    private EditText searchText;
        ///Drawer
    private DrawerLayout mDrawerLayout;

    //Map
    private GoogleMap mMap;
    private static final String TAG = "MapsActivity";
    private static final int locationPermissionCodeGranted = 1234;
    private Boolean locationPermissionsGranted = false;

    //Location
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location curLocation;

    //Activity
    private Activity curActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        searchText = findViewById(R.id.input_search);
        initMap();

        locationPermission();

        //Current activity
        curActivity = this;

        //Set up location stuff
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);       //Set up the manager
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                curLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };                              //Set up the listener

        //Set up drawer
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        Toast.makeText(curActivity, "Clicked on: " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });


        init();                     //Initialize the search bar
    }

    //Location stuff
    private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = searchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, "new location");
        }
    }
    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting current location");
        /*
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);                   //Create a new instance

        try{
            if(locationPermissionsGranted){
                //Get the last known location
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            //On location found
                            Log.d(TAG, "onComplete: found location!");
                            //Location currentLocation = (Location) task.getResult();

                            curLocation = (Location) task.getResult();
                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }*/

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            locationPermission();
            return;
        } else {
            locationManager.requestLocationUpdates("gps", 5000, 5, locationListener);
        }
    }

    //UI Stuff
    private void moveCamera(LatLng latLng, float zoom, String title){
        //Move to a new location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        //Add a marker to the current location
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);
        mMap.addMarker(markerOptions);
    }
    public void sendMessage(View view){
        if (view.getId() == R.id.curLocation_button){
            getDeviceLocation();
            moveCamera(new LatLng(curLocation.getLatitude(), curLocation.getLongitude()),10,"Current Location");
        }
    }       //CurLocation button


    //Initialization - On Start stuff
    private void locationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        //Request permission
        ActivityCompat.requestPermissions(this, permissions, locationPermissionCodeGranted);

        /*
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationPermissionsGranted = true;
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        locationPermissionCodeGranted);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    locationPermissionCodeGranted);
        }*/
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationPermissionsGranted = true;                                                                             //Set permission to granted
            Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();                             //Misc display
        }else{
            Toast.makeText(this, "Location access denied!", Toast.LENGTH_SHORT).show();                         //Misc display
        }
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        locationPermissionsGranted = false;

        switch(requestCode){
            case locationPermissionCodeGranted:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            locationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: failed to have permission.");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: got permission.");
                    locationPermissionsGranted = true;
                }
            }
        }
    }
    private void init(){
        Log.d(TAG, "init: initializing");

        /*TODO: why is there a check for user search input in initialization? Shouldn't it initMap and geoLocate as default?*/
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    geoLocate();
                }
                //TODO: why false condition here?
                return false;
            }
        });
    }
    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }
    @Override public void onMapReady(GoogleMap googleMap) {
        /*
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (locationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            initMap();
            init();
        }*/
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        //Default location
        LatLng oregon = new LatLng(43.8, -120.55);
        googleMap.addMarker(new MarkerOptions().position(oregon)
                .title("Marker in Oregon"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(oregon));

        //Set default location
        curLocation = new Location(LocationManager.GPS_PROVIDER);
        curLocation.setLongitude(oregon.longitude);
        curLocation.setLatitude(oregon.latitude);
    }
}

