package com.example.hyrulecompendium;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.AsyncTask;
import android.widget.Toast;

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
        //    //WRITE TO DB
    //        // Write a message to the database
    //        FirebaseDatabase database = FirebaseDatabase.getInstance();
    //        String userId = "aaravyadav";
    //        DatabaseReference myRef = database.getReference(userId);
    //
    //        myRef.setValue("User Java Object");
    //
    //        // Read from the database
    //        myRef.addValueEventListener(new ValueEventListener() {
    //            @Override
    //            public void onDataChange(DataSnapshot dataSnapshot) {
    //                // This method is called once with the initial value and again
    //                // whenever data at this location is updated.
    //                String value = dataSnapshot.getValue(String.class);
    //                Log.d(TAG, "Value is: " + value);
    //            }
    //
    //            @Override
    //            public void onCancelled(DatabaseError error) {
    //                // Failed to read value
    //                Log.w(TAG, "Failed to read value.", error.toException());
    //            }
    //        });
    TextView NameText, DescriptionText, CatagoryText, DropsText, LocationText;
    String mode = "";
    Button search;
    String enemy;
    EditText userInput;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NameText = findViewById(R.id.NameText);
        DropsText = findViewById(R.id.CommonDropsText);
        DescriptionText = findViewById(R.id.DescriptionText);
        CatagoryText = findViewById(R.id.CatagoryText);
        LocationText = findViewById(R.id.CommonLocationText);
        search = findViewById(R.id.button);
        userInput = findViewById(R.id.editTextTextPersonName);
        imageView = findViewById(R.id.imageView);
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
                                JSONObject jsonObject = new JSONObject(result);
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
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public interface ApiCallback {
        void onApiCompleted(String result) throws JSONException;
    }
}
