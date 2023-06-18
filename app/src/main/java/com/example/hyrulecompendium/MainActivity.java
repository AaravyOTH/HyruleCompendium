package com.example.hyrulecompendium;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
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

public class MainActivity extends AppCompatActivity {

    //TODO: Keyboard drop after user presses submit
    //TODO: Change default value from Name text to a 'hint' or 'subtext'. Look into InputView class you are using to see the option to add this instead of setting the inputs value.
    //TODO: Fix look of MainActivity page
    //TODO: Add to Database button
    //TODO: Add to inventory view button
    JSONObject jsonObject;

        //    //WRITE TO DB
    //        // Write a message to the database
    //
    Button save;
    Button viewCompendium;
    TextView NameText, DescriptionText, CatagoryText, DropsText, LocationText;
    String mode = "";
    Button search;
    String enemy;
    EditText userInput;
    ImageView imageView;
    DatabaseReference databaseRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NameText = findViewById(R.id.NameText);
        DropsText = findViewById(R.id.CommonDropsText);
        DescriptionText = findViewById(R.id.DescriptionText);
        CatagoryText = findViewById(R.id.CatagoryText);
        LocationText = findViewById(R.id.CommonLocationText);
        search = findViewById(R.id.SearchButton);
        userInput = findViewById(R.id.editTextTextPersonName);
        imageView = findViewById(R.id.imageView);
        save = findViewById(R.id.Save);
        viewCompendium = findViewById(R.id.button2);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userID = user.getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userID).child("searchedItems");


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                enemy = String.valueOf(userInput.getText());
                ApiCallTask apiCallTask = new ApiCallTask(new ApiCallback() {
                    @Override
                    public void onApiCompleted(String result) throws JSONException {
                        try {
                            if (result == "" || result == null) {
                                Toast.makeText(MainActivity.this, "please select a valid enemy", Toast.LENGTH_SHORT).show();
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
                                    e.printStackTrace();
                                }


                                String imageLink = jsonObject.getJSONObject("data").getString("image");

                                Picasso.get().load(imageLink).into(imageView);


                                NameText.setText(userInput.getText());
                                LocationText.setText(commonLocations.toString());
                                CatagoryText.setText(category);
                                DescriptionText.setText(description);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                apiCallTask.execute("https://botw-compendium.herokuapp.com/api/v2/entry/" + enemy);


            }

        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchedItemName = String.valueOf(userInput.getText());
                String imageLink = null;
                try {
                    imageLink = jsonObject.getJSONObject("data").getString("image");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                Item item = new Item(searchedItemName, imageLink);
                databaseRef.push().setValue(item);
            }
        });

        viewCompendium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
                startActivity(intent);
            }
        });
    }
    public class ApiCallTask extends AsyncTask<String, Void, String> {
        private ApiCallback callback;

        public ApiCallTask(ApiCallback callback) {
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
    public class Item {
        private String name;
        private String imageLink;

        public Item() {
            // Default constructor required for Firebase
        }

        public Item(String name, String imageLink) {
            this.name = name;
            this.imageLink = imageLink;
        }

        public String getName() {
            return name;
        }

        public String getImageLink() {
            return imageLink;
        }
    }

}
