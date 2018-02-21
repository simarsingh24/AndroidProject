package com.example.rajulnahar.smartmaps.Activity;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.constant.TransportMode;
import com.example.rajulnahar.smartmaps.Others.Constants;
import com.example.rajulnahar.smartmaps.Objects.Favourites;
import com.example.rajulnahar.smartmaps.Objects.ListedPlace;
import com.example.rajulnahar.smartmaps.Adapters.ListviewAdapter;
import com.example.rajulnahar.smartmaps.Others.PermissionUtils;
import com.example.rajulnahar.smartmaps.R;
import com.example.rajulnahar.smartmaps.Database.SmartMapsdb;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GpsStatus.Listener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    public LocationManager locationManager;
    public LocationListener locationListener;
    private GoogleApiClient mGoogleApiClient;
    Location location;
    SmartMapsdb smartMapsdb;
    double lat = 0;
    double lon = 0;
    Dialog distancedialog;
    Dialog advsearch;
    Dialog poiPopup;


    @BindView(R.id.settings)
    ImageView settings;
    @BindView(R.id.advancesearch)
    LinearLayout advancesearch;
    @BindView(R.id.likeus)
    LinearLayout likeus;
    @BindView(R.id.rateus)
    LinearLayout rateus;
    @BindView(R.id.shareit)
    LinearLayout shareit;
    @BindView(R.id.listedplace)
    LinearLayout listedplace;
    @BindView(R.id.addnew)
    LinearLayout addnew;
    @BindView(R.id.seekBar)
    SeekBar seekbar;


    double latitudePOISelected = 0;
    double longitudePOISelected = 0;
    Marker markerSelectedPoi;

    double distanceVal = 1000;
    boolean iskm = true;

    RadioButton inkm;
    RadioButton inmiles;

    Button avdSearchButton;

    LinearLayout ll_favourite;
    LinearLayout ll_share;
    TextView tvDrivingDirection, tvWalkingDirection;

    public long lastgps = 0;
    public static boolean gpsfixed = false;
    private final int LOCATION_PERMISSION = 1;
    public boolean locationPermissionAvailable = false;

    public List<com.example.rajulnahar.smartmaps.Objects.Location> locationList;


    List<ListedPlace> listedPlaceList;
    com.example.rajulnahar.smartmaps.Objects.Location loc;

    public ListView listView;
    public ListviewAdapter listviewAdapter;

    ConnectivityManager connectivityManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        requestPermissions();
        initComponent();
        onClicks();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                while (!gpsfixed) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                locationLocked();
            }
        }.execute();


        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (iskm)
                    distanceVal = progress;
                else
                    distanceVal = progress * 0.621;

                saveToSharedPrefrences(Constants.distancekey, String.valueOf(distanceVal));
                mMap.clear();
                //mMap.addMarker(new MarkerOptions().position(new LatLng(Constants.location.getLatitude(), Constants.location.getLongitude()))
                  //      .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                listedPlaceList = smartMapsdb.getListedlace();

                for (int i = 0; i < listedPlaceList.size(); i++) {
                    double distance = distance(Constants.location.getLatitude(), Constants.location.getLongitude(), Double.parseDouble(listedPlaceList.get(i).latitude), Double.parseDouble(listedPlaceList.get(i).longitude));
                    for (int j = 0; j < Constants.selectedCategories.size(); j++) {
                        if (listedPlaceList.get(i).category.contains(Constants.selectedCategories.get(j))) {
                            if (iskm) {
                                if (distance < distanceVal)
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(listedPlaceList.get(i).latitude), Double.parseDouble(listedPlaceList.get(i).longitude))));
                            } else {
                                if (distance * 0.62 < distanceVal)
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(listedPlaceList.get(i).latitude), Double.parseDouble(listedPlaceList.get(i).longitude))));
                            }
                        }
                    }


                }


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mapFragment.getMapAsync(this);

    }

    public void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionAvailable = true;
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                locationPermissionAvailable = false;
                Toast.makeText(this, "Location permissions needed!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void initComponent() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(locationPermissionAvailable == true) locationManager.addGpsStatusListener(this);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            alertToSwitchGPS();
        }
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        listedPlaceList = new ArrayList<>();
        //test purpose
        smartMapsdb = SmartMapsdb.getInstance(MapsActivity.this);
        //
        smartMapsdb.deleteTables();
        smartMapsdb.testCategory();
        listedPlaceList = smartMapsdb.getListedlace();


        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        distancedialog = new Dialog(MapsActivity.this);
        View view = LayoutInflater.from(MapsActivity.this).inflate(R.layout.distancedialog, null);
        distancedialog.setContentView(view);
        distancedialog.setTitle("Select distance in");
        inkm = (RadioButton) view.findViewById(R.id.distancekm);
        inmiles = (RadioButton) view.findViewById(R.id.distancemile);

        advsearch = new Dialog(MapsActivity.this);
        view = LayoutInflater.from(MapsActivity.this).inflate(R.layout.searchdailog, null);
        listView = (ListView) view.findViewById(R.id.categorylist);
        listviewAdapter = new ListviewAdapter(this);
        listviewAdapter.setCategories(smartMapsdb.getAllCategories());
        listView.setAdapter(listviewAdapter);
        advsearch.setContentView(view);
        advsearch.setTitle("Select Categories");
        avdSearchButton = (Button) view.findViewById(R.id.btn_search);

        poiPopup = new Dialog(MapsActivity.this);
        view = LayoutInflater.from(MapsActivity.this).inflate(R.layout.popupdialog, null);
        poiPopup.setContentView(view);
        poiPopup.setTitle("POI Popup");
        ll_favourite = (LinearLayout) view.findViewById(R.id.ll_favourite);
        ll_share = (LinearLayout) view.findViewById(R.id.ll_share);
        tvDrivingDirection = (TextView) view.findViewById(R.id.drivingdirection);
        tvWalkingDirection = (TextView) view.findViewById(R.id.walkingdirctions);

    }

    public void onClicks() {
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                distancedialog.show();
            }
        });

        advancesearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "Advance Search", Toast.LENGTH_SHORT).show();
                Constants.selectedCategories.clear();
                advsearch.show();


            }
        });

        likeus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Toast.makeText(MapsActivity.this, "Like us", Toast.LENGTH_SHORT).show();
                Uri uri = Uri.parse("https://www.facebook.com/Future-Smart-Technologies-Pvt-Ltd-993516597384645/");
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);*/
            }
        });

        rateus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "Rate us", Toast.LENGTH_SHORT).show();
               /* Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.bigduckgames.flowbridges&hl=en");
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);*/
            }
        });
        shareit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "Share", Toast.LENGTH_SHORT).show();
                /*String bla = "https://play.google.com/store/apps/details?id=com.bigduckgames.flowbridges&hl=en";
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_TEXT,bla);
                startActivity(Intent.createChooser(sharingIntent,"Select to share"));*/
            }
        });

        listedplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "List", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MapsActivity.this, ListedPlaceActivity.class));
            }
        });

        addnew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "Add new", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MapsActivity.this, AddNewActivity.class);
                startActivity(intent);
            }
        });

        inkm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inmiles.setChecked(false);
                iskm = true;
            }
        });

        inmiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inkm.setChecked(false);
                //distance miles mai count hoga
                iskm = false;
            }
        });

        avdSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                advsearch.dismiss();
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(new LatLng(Constants.location.getLatitude(), Constants.location.getLongitude()))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                listedPlaceList = smartMapsdb.getListedlace();
                for (int i = 0; i < listedPlaceList.size(); i++) {
                    double distance = distance(Constants.location.getLatitude(), Constants.location.getLongitude(), Double.parseDouble(listedPlaceList.get(i).latitude), Double.parseDouble(listedPlaceList.get(i).longitude));
                    for (int j = 0; j < Constants.selectedCategories.size(); j++) {
                        if (listedPlaceList.get(i).category.contains(Constants.selectedCategories.get(j))) {
                            if (iskm) {
                                if (distance < distanceVal)
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(listedPlaceList.get(i).latitude), Double.parseDouble(listedPlaceList.get(i).longitude))));
                            } else {
                                if (distance * 0.62 < distanceVal)
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(listedPlaceList.get(i).latitude), Double.parseDouble(listedPlaceList.get(i).longitude))));
                            }
                        }
                    }


                }

            }
        });
        ll_favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Favourites favourites = new Favourites();
                favourites.latitude = String.valueOf(latitudePOISelected);
                favourites.longitude = String.valueOf(longitudePOISelected);
                long arc = smartMapsdb.addFavourites(favourites);
                Log.e("add to fav", String.valueOf(arc));
                markerSelectedPoi.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                poiPopup.dismiss();
            }
        });
        ll_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.selectedCategories.clear();
                Intent intent = new Intent(MapsActivity.this, ShareActivity.class);
                startActivity(intent);
            }
        });
        tvDrivingDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.ModeDriving = TransportMode.DRIVING;
                startActivity(new Intent(MapsActivity.this, DrivingDirectionsActivity.class));
            }
        });

        tvWalkingDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.ModeDriving = TransportMode.WALKING;
                startActivity(new Intent(MapsActivity.this, DrivingDirectionsActivity.class));
            }
        });
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public void alertToSwitchGPS() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
        alert.setCancelable(false)
                .setMessage("Please Enable your GPS")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertToSwitchGPS();
                    }
                });
        alert.show();
    }


    private void enableMyLocation() {


        if(locationPermissionAvailable == false) return;
        mMap.setMyLocationEnabled(true);

    }



    public  void locationLocked(){

        final Location location = mMap.getMyLocation();
        if(location!=null) {
            loc = new com.example.rajulnahar.smartmaps.Objects.Location();
            loc.setLat(String.valueOf(location.getLatitude()));
            loc.setLng(String.valueOf(location.getLongitude()));
            Constants.location = location;
            Log.e("Recievedlocation", String.valueOf(location.getLongitude()));
            mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15.0f));
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.sharedPrefrencename,MODE_PRIVATE);
            final String search = sharedPreferences.getString(Constants.categorykey,"temple|atm|food|bank|airport");
            final String distan = sharedPreferences.getString(Constants.distancekey,"1000");

            for(int i=0;i<listedPlaceList.size();i++){
                double distance = distance(Constants.location.getLatitude(),Constants.location.getLongitude(),Double.parseDouble(listedPlaceList.get(i).latitude),Double.parseDouble(listedPlaceList.get(i).longitude));
                Log.e("main",String.valueOf(distance));
                if(iskm){
                    if(distance<1)
                        mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(listedPlaceList.get(i).latitude),Double.parseDouble(listedPlaceList.get(i).longitude))));
                }
                else {
                    if(distance*0.62<1)
                        mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(listedPlaceList.get(i).latitude),Double.parseDouble(listedPlaceList.get(i).longitude))));
                }

            }

        }
    }

    public String getCategories(){
        String res = "";
        for (int i  = 0;i<Constants.selectedCategories.size();i++){
            res = res+Constants.selectedCategories.get(i).toLowerCase();
            if(i != Constants.selectedCategories.size()-1 ){
                res+= "|";
            }
        }
        saveToSharedPrefrences(Constants.categorykey,res);
        return res;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
        mMap.setOnMarkerClickListener(MapsActivity.this);
        // Add a marker in Sydney and move the camera

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGpsStatusChanged(int event) {
        if(event ==1){

        }else if(event==2){

        }else if(event==3){

        }else{

            long systime = System.currentTimeMillis();
            long d = systime-lastgps;
            if(d<5000){
                gpsfixed = true;
                lastgps = systime;
            }
            else if(d>10000){
                gpsfixed = false;
                lastgps = systime;
            }
        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

       // Toast.makeText(this, "marker clicked", Toast.LENGTH_SHORT).show();
        latitudePOISelected = marker.getPosition().latitude;
        longitudePOISelected = marker.getPosition().longitude;
        markerSelectedPoi = marker;
        Constants.markerPoiSelect = marker;
        poiPopup.show();
        return false;
    }

    public  void saveToSharedPrefrences(String key, String msg){
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.sharedPrefrencename,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,msg);
        editor.commit();
    }
}
