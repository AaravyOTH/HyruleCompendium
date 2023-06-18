package com.example.hyrulecompendium;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity {

    //TODO: Read from Firebase DB to fill the items in inventory
    //TODO: Make list of items a list view with custom list items
    public List<SearchedItem> searchedItems = new ArrayList<>();

    ListView listView;
    ImageView image;
    JSONObject jsonObject;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        listView = findViewById(R.id.ListView);
        image = findViewById(R.id.imageView3);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userID = user.getUid();
        DatabaseReference searchedItemsRef = FirebaseDatabase.getInstance().getReference("users").child(userID).child("searchedItems");
        searchedItemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //TODO: delete repeat entries
                    String name = snapshot.child("name").getValue(String.class);
                    String imageLink = snapshot.child("imageLink").getValue(String.class);
                    SearchedItem searchedItem = new SearchedItem(name, imageLink);
                    searchedItems.add(searchedItem);
                }
                // Pass the searchedItems list to your custom list view adapter and display the items.
               Log.d("tag", searchedItems.toString());
                CustomBaseAdapter customBaseAdapter = new CustomBaseAdapter(getApplicationContext(), searchedItems);
                listView.setAdapter(customBaseAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.d("clicked!", position +"");
                        Picasso.get().load(searchedItems.get(position).getImageLink()).into(image);

//                        MainActivity.ApiCallTask apiCallTask = new MainActivity.ApiCallTask(new MainActivity.ApiCallback() {
//                            @Override
//                            public void onApiCompleted(String result) throws JSONException {
//                                try {
//                                    if (result == "" || result == null) {
//                                        Toast.makeText(InventoryActivity.this, "please select a valid enemy", Toast.LENGTH_SHORT).show();
//                                    } else {
//                                        // textView.setText(result);
//                                        Log.d("JSON", result);
//                                        jsonObject = new JSONObject(result);
//                                        JSONObject data = jsonObject.getJSONObject("data");
//
//                                        String category = data.getString("category");
//                                        String description = data.getString("description");
//                                        String image = data.getString("image");
//
//                                        //For commonLocations
//                                        JSONArray commonLocationsArray = data.getJSONArray("common_locations");
//                                        ArrayList<String> commonLocations = new ArrayList<>();
//                                        //Going through the commanLocations JsonArray so I can get the multiple common locations
//                                        for (int i = 0; i < commonLocationsArray.length(); i++) {
//                                            String location = commonLocationsArray.getString(i);
//                                            commonLocations.add(location);
//                                        }
//
//                                        //Going through drops jsonarray
//                                        ArrayList<String> drops = null;
//                                        try {
//                                            JSONArray dropsArray = data.getJSONArray("drops");
//                                            drops = new ArrayList<>();
//                                            for (int i = 0; i < dropsArray.length(); i++) {
//                                                String drop = dropsArray.getString(i);
//                                                drops.add(drop);
//                                            }
//                                            DropsText.setText(drops.toString());
//
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//
//
//
//
//
//                                        NameText.setText(userInput.getText());
//                                        LocationText.setText(commonLocations.toString());
//                                        CatagoryText.setText(category);
//                                        DescriptionText.setText(description);
//                                    }
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
//                        apiCallTask.execute("https://botw-compendium.herokuapp.com/api/v2/entry/" + enemy);

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur while retrieving data.
            }
        });



    }
    public class SearchedItem {
        private String name;
        private String imageLink;



        public SearchedItem(String name, String imageLink) {
            this.name = name;
            this.imageLink = imageLink;
        }

        public String getName() {
            return name;
        }

        public String getImageLink() {
            return imageLink;
        }
        @Override
        public String toString() {
            return name + ": " + imageLink;
        }
    }

}