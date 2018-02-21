package com.example.rajulnahar.smartmaps.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.rajulnahar.smartmaps.Adapters.ListviewAdapter;
import com.example.rajulnahar.smartmaps.Database.SmartMapsdb;
import com.example.rajulnahar.smartmaps.Objects.Categories;
import com.example.rajulnahar.smartmaps.Objects.ListedPlace;
import com.example.rajulnahar.smartmaps.Others.Constants;
import com.example.rajulnahar.smartmaps.Others.PermissionUtils;
import com.example.rajulnahar.smartmaps.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddNewActivity extends AppCompatActivity implements GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,GoogleMap.OnMarkerDragListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    static final int REQUEST_IMAGE_CAPTURE = 365;

    private boolean mPermissionDenied = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public Marker mark;
    public SmartMapsdb smartMapsdb;
    @BindView(R.id.btnSaveAndShare)
    public Button btnSaveAndShare;
    @BindView(R.id.btnSave)
    public Button btnSave;
    public File image;
    @BindView(R.id.btnAddPhoto)
    public Button btnAddPhoto;
    @BindView(R.id.etComment)
    public EditText etComment;
    public List<Categories> categoryList;
    @BindView(R.id.lv_category)
    public ListView listView;
    ListviewAdapter listviewAdapter;
    public List<Categories> selectedCategories;
    public String finalCategories = "";

    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.rajulnahar.smartmaps.R.layout.activity_add_new);
        ButterKnife.bind(this);

        initComponents();
        onClicks();

    }

    public void initComponents(){
        smartMapsdb = SmartMapsdb.getInstance(this);
        Constants.selectedCategories.clear();
        categoryList = smartMapsdb.getAllCategories();
        listviewAdapter = new ListviewAdapter(this);
        listviewAdapter.setCategories(smartMapsdb.getAllCategories());
        listView.setAdapter(listviewAdapter);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void onClicks(){
        btnSaveAndShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListedPlace listedPlace = new ListedPlace();
                listedPlace.comments = etComment.getText().toString();
                listedPlace.longitude = String.valueOf(mark.getPosition().longitude);
                listedPlace.latitude = String.valueOf(mark.getPosition().latitude);
                //if(getSelectedCategories()!=null)
                    listedPlace.category = getSelectedCategories();
                    Log.d("selected category",getSelectedCategories());
                if(image != null){
                    listedPlace.image = image.getAbsolutePath();
                }
                Log.e("Database","Databse inset id: "  + String.valueOf(smartMapsdb.addListedPlace(listedPlace)));
                Log.e("selected category:",listedPlace.category);
                Toast.makeText(AddNewActivity.this,"Saved!",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddNewActivity.this,ShareActivity.class);
                startActivity(intent);
                finish();


            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListedPlace listedPlace = new ListedPlace();
                listedPlace.comments = etComment.getText().toString();
                listedPlace.longitude = String.valueOf(mark.getPosition().longitude);
                listedPlace.latitude = String.valueOf(mark.getPosition().latitude);
                if(getSelectedCategories()!=null) listedPlace.category = getSelectedCategories();
                Log.e("Database","Databse inset id: "  + String.valueOf(smartMapsdb.addListedPlace(listedPlace)));
                Log.e("selected category:",listedPlace.category);
                Toast.makeText(AddNewActivity.this,"Saved!",Toast.LENGTH_SHORT).show();
                finish();

            }
        });
        btnAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    public String getSelectedCategories() {
            String result = "";
            Log.d("harsimarSingh", "size is " + Constants.selectedCategories.size());
            for (int i = 0; i < Constants.selectedCategories.size(); i++) {
                result += String.valueOf(Constants.selectedCategories.get(i));
                Log.e("", String.valueOf(Constants.selectedCategories.get(i)));
                if (i != Constants.selectedCategories.size() - 1) {
                    result += ",";
                }
            }
            Constants.selectedCategories.clear();
            return result;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                image = File.createTempFile(imageFileName,".jpg",storageDir);
                Toast.makeText(this, "Image Saved Successfully", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                image = null;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
        FusedLocationProviderClient mFusedLocationClient ;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(AddNewActivity.this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(AddNewActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mark = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
                                    .position(new LatLng(location.getLatitude(), location.getLongitude())).draggable(true));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13.0f));
                        }
                    }
                });
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
            //Log.e("permission","missing");
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            mMap.setOnMarkerDragListener(this);
            //mMap.addMarker(mMap.setMyLocationEnabled(true));
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        Location loc = mMap.getMyLocation();
        if(loc!=null) {
            //Log.e("latitude", String.valueOf(loc.getLatitude()));
            mark.remove();
           mark= mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
                   .position(new LatLng(loc.getLatitude(),loc.getLongitude())).draggable(true));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 13.0f));
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            enableMyLocation();
        } else {
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mark.setTitle(String.valueOf(mark.getPosition().longitude));
        mark.showInfoWindow();
        Constants.markerPoiSelect = marker;

    }
}
