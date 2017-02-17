package com.example.rajulnahar.smartmaps.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.rajulnahar.smartmaps.Objects.Categories;
import com.example.rajulnahar.smartmaps.Others.Constants;
import com.example.rajulnahar.smartmaps.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rajul Nahar on 28-01-2017.
 */

public class ListviewAdapter extends BaseAdapter {

    public  ListviewAdapter(Context context){
        this.context = context;
        holders = new ArrayList<>();
    }

    public List<Categories> categories;
    public List<Holder> holders;

    public void setCategories(List<Categories> categories) {
        this.categories = categories;
    }

    Context context;
    @Override
    public int getCount() {
        if(categories!= null){
            return categories.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(categories!=null){
            return categories.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if(categories!=null){
            return position;
        }
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder = new Holder();
        View row = LayoutInflater.from(context).inflate(R.layout.categorylistrow,null);
        holder.checkBox = (CheckBox) row.findViewById(R.id.checkbox);
        holder.checkBox.setText(categories.get(position).category);
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, String.valueOf(position), Toast.LENGTH_SHORT).show();
                if(holder.checkBox.isChecked()){
                    Constants.selectedCategories.add(categories.get(position).category);
                }else{
                    if(Constants.selectedCategories.contains(categories.get(position).category)){
                        Constants.selectedCategories.remove(categories.get(position).category);
                    }
                }

            }
        });
        holders.add(holder);
        return row;
    }



    public class  Holder{
        CheckBox checkBox;
        public void setcheck(boolean state){
            checkBox.setChecked(state);
        }
    }
}
