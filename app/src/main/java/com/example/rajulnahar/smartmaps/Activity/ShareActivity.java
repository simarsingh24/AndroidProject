package com.example.rajulnahar.smartmaps.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.rajulnahar.smartmaps.Objects.Categories;
import com.example.rajulnahar.smartmaps.Others.Constants;
import com.example.rajulnahar.smartmaps.Adapters.ListviewAdapter;
import com.example.rajulnahar.smartmaps.R;
import com.example.rajulnahar.smartmaps.Database.SmartMapsdb;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShareActivity extends AppCompatActivity implements OnMapReadyCallback {
    @BindView(R.id.lv_left)
    ListView listViewleft;
    @BindView(R.id.lv_right)
    ListView listViewright;
    @BindView(R.id.btn_share)
    Button btnShare;
    @BindView(R.id.etComment)
    EditText etComments;
    @BindView(R.id.selectall)
    Button selectAll;
    @BindView(R.id.clearall)
    Button clearAll;

    ListviewAdapter listviewAdapterleft;
    ListviewAdapter listviewAdapterright;

    public List<Categories> categories;
    public List<Categories> categoriesleft;
    public List<Categories> categoriesright;
    SmartMapsdb smartMapsdb;

    SupportMapFragment supportMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        ButterKnife.bind(this);
        initComponent();
        onClicks();
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShare();
            }
        });
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

    }

    public void initComponent(){
        smartMapsdb = SmartMapsdb.getInstance(this);
        listviewAdapterleft = new ListviewAdapter(this);
        listviewAdapterright = new ListviewAdapter(this);
        categories = smartMapsdb.getAllCategories();
        categoriesleft = categories.subList(0,categories.size()/2);
        categoriesright = categories.subList(categories.size()/2,categories.size());
        listviewAdapterleft.setCategories(categoriesleft);
        listviewAdapterright.setCategories(categoriesright);
        listViewleft.setAdapter(listviewAdapterleft);
        listViewright.setAdapter(listviewAdapterright);

    }

    public void onClicks(){
        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setAllCheckBox(true);
            }
        });

        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllCheckBox(false);
                Constants.selectedCategories.clear();
            }
        });
    }


    public void setAllCheckBox(boolean state){
        for(int i = 0; i < listviewAdapterleft.getCount(); i++){
            View view = ((LinearLayout)listViewleft.getChildAt(i));
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
            checkBox.setChecked(state);
            if(state){
                if(!Constants.selectedCategories.contains(listviewAdapterleft.categories.get(i).category)){
                    Constants.selectedCategories.add(listviewAdapterleft.categories.get(i).category);
                }
            }
                
        }
        for(int i = 0; i < listviewAdapterright.getCount(); i++){
            View view = ((LinearLayout)listViewright.getChildAt(i));
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
            checkBox.setChecked(state);
            if(state){
                if(!Constants.selectedCategories.contains(listviewAdapterright.categories.get(i).category)){
                    Constants.selectedCategories.add(listviewAdapterright.categories.get(i).category);
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMap.addMarker(new MarkerOptions().position(new LatLng(Constants.location.getLatitude(),Constants.location.getLongitude())));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Constants .location.getLatitude(),
                Constants.location.getLongitude()), 15.0f));
    }

    public  void onShare(){
        String shareString = "https://www.google.co.in/maps/@"+Constants.location.getLatitude()+","+Constants.location.getLongitude()+",15z\n";
        String bla = "\nCategories: ";

        for(int i = 0; i < Constants.selectedCategories.size(); i++){
            bla += Constants.selectedCategories.get(i);
            if(i != Constants.selectedCategories.size()-1);
                bla += ",";
        }
        String comment = etComments.getText().toString();
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT,shareString+"\n"+comment+"\n"+bla);
        startActivity(Intent.createChooser(sharingIntent,"Select to share"));
    }
}
