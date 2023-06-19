package com.example.hyrulecompendium;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class CustomListView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_custom_list_view);
    }
}