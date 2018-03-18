package com.example.yun.meetup.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yun.meetup.R;
import com.example.yun.meetup.managers.NetworkManager;
import com.example.yun.meetup.models.APIResult;
import com.example.yun.meetup.models.Event;
import com.example.yun.meetup.requests.SearchEventsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class SearchEventsActivity extends AppCompatActivity  implements OnMapReadyCallback {

    private GoogleMap mMap;

    private ConstraintLayout constraintLayoutMapLoading;

    private LatLng currentLocation;

    SearchView searchView;

    private double latitude;
    private double longitude;

    private int selectedDistance = 50;
    private String selectedCategory = "";

    private List<Event> mEvents;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_search_event);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        constraintLayoutMapLoading = (ConstraintLayout) findViewById(R.id.constraintLayoutMapLoading);
        searchView =  findViewById(R.id.search_view);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        latitude = Double.parseDouble(sharedPref.getString("latitude", "0"));
        longitude = Double.parseDouble(sharedPref.getString("longitude", "0"));

        currentLocation = new LatLng(latitude, longitude);
//        currentLocation = new LatLng(43.684201, -79.318706);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        constraintLayoutMapLoading.setVisibility(View.VISIBLE);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent(SearchEventsActivity.this, EventDetailsActivity.class);
                intent.putExtra("eventId", marker.getSnippet());
                startActivity(intent);
                return true;
            }
        });

//        Padding needed to display the controls
        mMap.setPadding(0, 400, 0,0);

        SearchEventsRequest searchEventsRequest = new SearchEventsRequest();
        searchEventsRequest.setLatitude(currentLocation.latitude);
        searchEventsRequest.setLongitude(currentLocation.longitude);

        UiSettings uiSettings = mMap.getUiSettings();
//        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
//        Checking authorization to get my location
        if (ActivityCompat.checkSelfPermission(SearchEventsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SearchEventsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SearchEventsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }else{
            mMap.setMyLocationEnabled(true);
        }

        new SearchEventsTask().execute(searchEventsRequest);

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (currentLocation != null){
//            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                    .findFragmentById(R.id.map);
//            mapFragment.getMapAsync(this);
//        }
//    }

    public void hideViews(){
        constraintLayoutMapLoading.setVisibility(View.GONE);
    }

    public void openDialog(View view) {
        dialog = new Dialog(SearchEventsActivity.this);
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
            Location.distanceBetween(currentLocation.latitude, currentLocation.longitude, event.getLatitude(), event.getLongitude(), results);
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

    private class SearchEventsTask extends AsyncTask<SearchEventsRequest, Void, APIResult>{

        @Override
        protected APIResult doInBackground(SearchEventsRequest... searchEventsRequests) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.searchEvents(searchEventsRequests[0]);
        }

        @Override
        protected void onPostExecute(APIResult apiResult) {

            hideViews();

            if (apiResult.getResultEntity() == null){
                Toast.makeText(SearchEventsActivity.this, apiResult.getResultMessage(), Toast.LENGTH_LONG);
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
            }
        }
    }
}
