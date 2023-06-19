package com.example.hyrulecompendium;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity {

    //TODO: Read from Firebase DB to fill the items in inventory
    //TODO: Make list of items a list view with custom list items
    public List<SearchedItem> searchedItems = new ArrayList<>();

    ListView listView;
    ImageView image;
    JSONObject jsonObject;
    TextView NameText, DropsText, DescriptionText, CategoryText, LocationText;
    Button back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_inventory);
        listView = findViewById(R.id.ListView);
        image = findViewById(R.id.imageView3);
        NameText = findViewById(R.id.name);
        DropsText = findViewById(R.id.drops);
        DescriptionText = findViewById(R.id.Description);
        CategoryText = findViewById(R.id.catagory);
        LocationText = findViewById(R.id.location);
        back = findViewById(R.id.backButton);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userID = user.getUid();
        DatabaseReference searchedItemsRef = FirebaseDatabase.getInstance().getReference("users").child(userID).child("searchedItems");
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InventoryActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
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

                        InventoryActivity.ApiCallTask apiCallTask = new InventoryActivity.ApiCallTask(new InventoryActivity.ApiCallback() {
                            @Override
                            public void onApiCompleted(String result) throws JSONException {
                                try {
                                    if (result == "" || result == null) {
                                        Toast.makeText(InventoryActivity.this, "please select a valid enemy", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // textView.setText(result);
                                        Log.d("JSON", result);
                                        jsonObject = new JSONObject(result);
                                        JSONObject data = jsonObject.getJSONObject("data");

                                        String category = data.getString("category");
                                        String description = data.getString("description");
                                        String image = data.getString("image");

                                        //For commonLocations
                                        JSONArray commonLocationsArray = data.getJSONArray("common_locations");
                                        ArrayList<String> commonLocations = new ArrayList<>();
                                        //Going through the commanLocations JsonArray so I can get the multiple common locations
                                        for (int i = 0; i < commonLocationsArray.length(); i++) {
                                            String location = commonLocationsArray.getString(i);
                                            commonLocations.add(location);
                                        }

                                        //Going through drops jsonarray
                                        ArrayList<String> drops = null;
                                        try {
                                            JSONArray dropsArray = data.getJSONArray("drops");
                                            drops = new ArrayList<>();
                                            for (int i = 0; i < dropsArray.length(); i++) {
                                                String drop = dropsArray.getString(i);
                                                drops.add(drop);
                                            }
                                            DropsText.setText(drops.toString());

                                        } catch (Exception e) {
                                            DropsText.setText("");
                                        }





                                        NameText.setText(searchedItems.get(position).getName());
                                        LocationText.setText(commonLocations.toString());
                                        CategoryText.setText(category);
                                        DescriptionText.setText(description);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        apiCallTask.execute("https://botw-compendium.herokuapp.com/api/v2/entry/" + searchedItems.get(position).getName());

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
    public static class ApiCallTask extends AsyncTask<String, Void, String> {
        private InventoryActivity.ApiCallback callback;

        public ApiCallTask(InventoryActivity.ApiCallback callback) {
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            HttpURLConnection connection = null;

            try {
                URL url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();

                // Set request method and headers if needed
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");

                // Get the response
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    reader.close();
                    result = stringBuilder.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                callback.onApiCompleted(result);
                //databaseRef.push().setValue(result); // Save the entire API response as the searched item
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public interface ApiCallback {
        void onApiCompleted(String result) throws JSONException;
    }

}