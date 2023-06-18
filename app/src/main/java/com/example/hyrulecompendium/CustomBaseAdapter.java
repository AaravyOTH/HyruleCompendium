package com.example.hyrulecompendium;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CustomBaseAdapter extends BaseAdapter {
    List<InventoryActivity.SearchedItem> searchedItems = new ArrayList<>();
    LayoutInflater inflater;
    Context context;
    public CustomBaseAdapter(Context context, List<InventoryActivity.SearchedItem> searchedItems){
        this.context = context;
        this.searchedItems = searchedItems;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return searchedItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.activity_custom_list_view, null);
        TextView itemText = (TextView) convertView.findViewById(R.id.textView3);
        ImageView itemImage = (ImageView) convertView.findViewById(R.id.imageView2);
        itemText.setText(searchedItems.get(position).getName());
        Picasso.get().load(searchedItems.get(position).getImageLink()).into(itemImage);
        return convertView;
    }
}
