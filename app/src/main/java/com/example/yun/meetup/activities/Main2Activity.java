package com.example.yun.meetup.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yun.meetup.R;
import com.example.yun.meetup.managers.NetworkManager;
import com.example.yun.meetup.models.APIResult;
import com.example.yun.meetup.models.Event;
import com.example.yun.meetup.models.UserInfo;
import com.example.yun.meetup.requests.SearchEventsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Main2Activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private GoogleMap mMap;

    private ConstraintLayout constraintLayoutMapLoading;

    private LatLng currentLocationLatLng;
    private Location currentLocation;
    private LocationManager locationManager;


    SearchView searchView;

    private double latitude;
    private double longitude;

    private int selectedDistance = 50;
    private String selectedCategory = "";

    private List<Event> mEvents;
    private Dialog dialog;

    FloatingActionButton fab;

    private String userId;


    private static final long LOCATION_REFRESH_TIME = 1;
    private static final float LOCATION_REFRESH_DISTANCE = 1;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            Main2Activity.this.saveLocation(location);
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
    };

    private final LocationListener mNetworkLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            Main2Activity.this.saveLocation(location);
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
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("MeetUs");


        constraintLayoutMapLoading = (ConstraintLayout) findViewById(R.id.constraintLayoutMapLoading);
        searchView =  findViewById(R.id.search_view);

        fab = (FloatingActionButton) findViewById(R.id.fab_add_event_main_activity);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Main2Activity.this, CreateEventActivity.class);
                startActivity(intent);
            }
        });

        showLoading();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        latitude = Double.parseDouble(sharedPref.getString("latitude", "0"));
        longitude = Double.parseDouble(sharedPref.getString("longitude", "0"));
        userId = sharedPref.getString("id", "");

        currentLocationLatLng = new LatLng(latitude, longitude);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
            return;
        }
        else{
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                buildAlertMessageNoGps();
            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mNetworkLocationListener);
            }
        }



    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        constraintLayoutMapLoading.setVisibility(View.VISIBLE);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, 15));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent(Main2Activity.this, EventDetailsActivity.class);
                intent.putExtra("eventId", marker.getSnippet());
                startActivity(intent);
                return true;
            }
        });

//        Padding needed to display the controls
        mMap.setPadding(0, 400, 0,0);

        SearchEventsRequest searchEventsRequest = new SearchEventsRequest();
        searchEventsRequest.setLatitude(currentLocationLatLng.latitude);
        searchEventsRequest.setLongitude(currentLocationLatLng.longitude);

        UiSettings uiSettings = mMap.getUiSettings();
//        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
//        Checking authorization to get my location
        if (ActivityCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Main2Activity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }else{
            mMap.setMyLocationEnabled(true);
        }

        new SearchEventsTask().execute(searchEventsRequest);

    }

    public void hideViews(){
        constraintLayoutMapLoading.setVisibility(View.GONE);
    }

    public void openDialog(View view) {
        dialog = new Dialog(Main2Activity.this);
        dialog.setContentView(R.layout.dialog_filter);
        dialog.setTitle("Hello");
        Spinner spinner = dialog.findViewById(R.id.spinner_category);
        SeekBar seekBar = dialog.findViewById(R.id.seek_bar_distance);
        final TextView textViewProgress = dialog.findViewById(R.id.text_distance);
        dialog.show();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewProgress.setText(progress + "Km");
                selectedDistance = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void handleOnClickApplyFilters(View view) {
        mMap.clear();
        final List<Event> filteredEvents = new ArrayList<>();
        for(Event  event: mEvents){
            float[] results = new float[1];
            Location.distanceBetween(currentLocationLatLng.latitude, currentLocationLatLng.longitude, event.getLatitude(), event.getLongitude(), results);
            float distanceInMeters = results[0];

            if (distanceInMeters < (selectedDistance * 1000)){

                if (selectedCategory == ""){
                    LatLng location = new LatLng(event.getLatitude(), event.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(location).title(event.getTitle()).snippet(event.get_id()));
                    filteredEvents.add(event);
                }
                else if (event.getCategory().equals(selectedCategory)){
                    LatLng location = new LatLng(event.getLatitude(), event.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(location).title(event.getTitle()).snippet(event.get_id()));
                    filteredEvents.add(event);
                }
            }
        }

        dialog.dismiss();
    }

    private class SearchEventsTask extends AsyncTask<SearchEventsRequest, Void, APIResult> {

        @Override
        protected APIResult doInBackground(SearchEventsRequest... searchEventsRequests) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.searchEvents(searchEventsRequests[0]);
        }

        @Override
        protected void onPostExecute(APIResult apiResult) {

            if (apiResult.getResultEntity() == null){
                Toast.makeText(Main2Activity.this, apiResult.getResultMessage(), Toast.LENGTH_LONG);
            }
            else{
                mEvents = (List<Event>) apiResult.getResultEntity();

                for(Event event : mEvents){
                    LatLng location = new LatLng(event.getLatitude(), event.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(location).title(event.getTitle()).snippet(event.get_id()));
                }

//                SearchView Listener

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        if(!query.isEmpty()){
                            mMap.clear();
                            final List<Event> filteredEvents = new ArrayList<>();
                            for(Event  event: mEvents){
                                if(event.getTitle().contains(query) || event.getSubtitle().contains(query) || event.getDescription().contains(query)){
                                    LatLng location = new LatLng(event.getLatitude(), event.getLongitude());
                                    mMap.addMarker(new MarkerOptions().position(location).title(event.getTitle()).snippet(event.get_id()));
                                    filteredEvents.add(event);
                                }
                            }
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });

                new GetUserInfoTask().execute(userId);
            }
        }
    }

    private class GetUserInfoTask extends AsyncTask<String, Void, APIResult>{

        @Override
        protected APIResult doInBackground(String... strings) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.getUserById(strings[0]);
        }

        @Override
        protected void onPostExecute(APIResult apiResult) {

            if (apiResult.getResultEntity() == null){
                hideViews();

                Toast.makeText(Main2Activity.this, apiResult.getResultMessage(), Toast.LENGTH_LONG);
            }
            else{
                UserInfo userInfo = (UserInfo) apiResult.getResultEntity();

                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                View headerview = navigationView.getHeaderView(0);

                LinearLayout layoutDrawerHeader = (LinearLayout) headerview.findViewById(R.id.layout_drawer_header);
                CircleImageView imgUserDrawer = (CircleImageView) headerview.findViewById(R.id.img_user_drawer);
                TextView txtDrawerUsername = (TextView) headerview.findViewById(R.id.txt_drawer_user_name);
                TextView txtDrawerUserEmail = (TextView) headerview.findViewById(R.id.txt_drawer_user_email);

                txtDrawerUsername.setText(userInfo.getName());
                txtDrawerUserEmail.setText(userInfo.getEmail());

                layoutDrawerHeader.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Main2Activity.this, UserProfileActivity.class);
                        startActivity(intent);
                        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                        drawer.closeDrawer(GravityCompat.START);
                    }
                });

                new DownloadImageTask().execute("https://meet-us-server1.herokuapp.com/api/user/photo/?user_id=" + userId);
            }

        }

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            String urldisplay = strings[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            View headerview = navigationView.getHeaderView(0);

            CircleImageView imgUserDrawer = (CircleImageView) headerview.findViewById(R.id.img_user_drawer);
            imgUserDrawer.setImageBitmap(bitmap);

            hideViews();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mMap != null){
            constraintLayoutMapLoading.setVisibility(View.VISIBLE);

            SearchEventsRequest searchEventsRequest = new SearchEventsRequest();
            searchEventsRequest.setLatitude(currentLocationLatLng.latitude);
            searchEventsRequest.setLongitude(currentLocationLatLng.longitude);

            UiSettings uiSettings = mMap.getUiSettings();
//        uiSettings.setZoomControlsEnabled(true);
            uiSettings.setMyLocationButtonEnabled(true);
//        Checking authorization to get my location
            if (ActivityCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Main2Activity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }else{
                mMap.setMyLocationEnabled(true);
            }

            new SearchEventsTask().execute(searchEventsRequest);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_my_subscribed_events) {
            Intent intent = new Intent(this, MySubscribedEventsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_create_event) {
            Intent intent = new Intent(this, CreateEventActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_my_hosted_events) {
            Intent intent = new Intent(this, EventListActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_logout) {
            SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.commit();

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);

            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 200: {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
                        return;
                    }
                    else{
                        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                        if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                            buildAlertMessageNoGps();
                        }
                        else{
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mNetworkLocationListener);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onRestart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
            return;
        }
        else{
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                buildAlertMessageNoGps();
            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mNetworkLocationListener);
            }
        }
        super.onRestart();
    }

    public void showLoading(){
        constraintLayoutMapLoading.setVisibility(View.VISIBLE);
        fab.setClickable(false);
    }

    public void hideLoading(){
        constraintLayoutMapLoading.setVisibility(View.GONE);
        fab.setClickable(true);
    }

    public void saveLocation(Location location){
        hideLoading();
        currentLocation = new Location(location);
        SharedPreferences sharedPref = Main2Activity.this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("latitude", "" + location.getLatitude());
        editor.putString("longitude", "" + location.getLongitude());
        editor.commit();
    }
}
