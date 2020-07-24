package com.example.wewe_clone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //declare necessary variables
    private static final String TAG = "Info";
    private EditText queryVal;
    public static final String mypreference = "mypref";
    SharedPreferences sharedpreferences;
    TextView main_temp_tv;
    TextView temp_tv;
    TextView condition_tv;
    TextView city_name_tv;
    TextView humidity_tv;
    TextView wind_tv;
    TextView visibility_tv;
    TextView date_tv;
    ImageView ic_weather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //call all views
        main_temp_tv = findViewById(R.id.main_temp_tv);
        temp_tv = findViewById(R.id.temp_tv);
        condition_tv = findViewById(R.id.condition_tv);
        city_name_tv = findViewById(R.id.city_tv);
        humidity_tv = findViewById(R.id.humidity_tv);
        wind_tv = findViewById(R.id.wind_tv);
        visibility_tv = findViewById(R.id.visibility_tv);
        date_tv = findViewById(R.id.date);
        ic_weather = findViewById(R.id.weather_condition_ic);
        queryVal = findViewById(R.id.et_query);
        ImageButton darkMmodeToggleButton = findViewById(R.id.toggle_darkmode_btn);

        //call method to handle search click
        handleSearchClick(queryVal);

        //create sharedPref to persist dark mode
        String themeSharedPrefs = "sharedPrefs";
        SharedPreferences sharedPreferences
                = getSharedPreferences(
                themeSharedPrefs, MODE_PRIVATE);
        final SharedPreferences.Editor editor
                = sharedPreferences.edit();
        final boolean isDarkModeOn
                = sharedPreferences
                .getBoolean(
                        "isDarkModeOn", false);


        if (isDarkModeOn) {
            AppCompatDelegate
                    .setDefaultNightMode(
                            AppCompatDelegate
                                    .MODE_NIGHT_YES);

        }
        else {
            AppCompatDelegate
                    .setDefaultNightMode(
                            AppCompatDelegate
                                    .MODE_NIGHT_NO);
        }

        // handle dark mode button click
        darkMmodeToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate
                            .setDefaultNightMode(
                                    AppCompatDelegate
                                            .MODE_NIGHT_NO);

                    editor.putBoolean(
                            "isDarkModeOn", false);
                    editor.apply();
                }
                else {
                    AppCompatDelegate
                            .setDefaultNightMode(
                                    AppCompatDelegate
                                            .MODE_NIGHT_YES);
                    editor.putBoolean(
                            "isDarkModeOn", true);
                    editor.apply();
                }
            }

        });
        // all data is lost on mode change, so call the displayToLayout method again
        //but this same data will persist and loads up on app launch (BUG!)
        displayToLayout();
    }

    //listen to the 'search' or 'enter' button click from the keyboard
    public void handleSearchClick(final EditText query){
        query.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    executeQuery(query);
                    return true;
                }
                return false;
            }
        });
    }

    //execute the search
    public void executeQuery(EditText query){
        try{
            URL dataUrl = ApiUtil.buildUrl(query.getText().toString());
            new DataQuaryTask().execute(dataUrl);
        }
        catch (Exception e){
            Log.e("error", e.getMessage());
        }

    }

    //store search results as sharedPref
    public void storeSearchPref(String date, String formattedTemp,
                                String cityName, String windSpeed,
                                String visibility, String humidity,
                                String iconUrl, String description){
        sharedpreferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("temperature", formattedTemp);
        editor.putString("date", date);
        editor.putString("cityName", cityName);
        editor.putString("windSpeed", windSpeed);
        editor.putString("visibility", visibility);
        editor.putString("humidity", humidity);
        editor.putString("iconUrl", iconUrl);
        editor.putString("description", description);
        editor.apply();
    }

    //handle parsing data to the UI
    public void displayToLayout(){
        sharedpreferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);
        main_temp_tv.setText(String.format("%s°", sharedpreferences.getString("temperature", "0")));
        city_name_tv.setText(sharedpreferences.getString("cityName", ""));
        temp_tv.setText(String.format("%s°", sharedpreferences.getString("temperature", "0")));
        humidity_tv.setText(String.format("%s %%", sharedpreferences.getString("humidity", "0")));
        wind_tv.setText(String.format("%s m/h", sharedpreferences.getString("windSpeed", "0")));
        visibility_tv.setText(String.format("%s km", sharedpreferences.getString("visibility", "0")));
        date_tv.setText(sharedpreferences.getString("date", ""));
        condition_tv.setText(sharedpreferences.getString("description", ""));
        if (sharedpreferences.contains("iconUrl")){
            Picasso.get().load(sharedpreferences.getString("iconUrl", ""))
                    .resize(70, 70)
                    .into(ic_weather);
        }

    }

    //Async Task
    public class DataQuaryTask extends AsyncTask<URL, Void, String>{
        @Override
        protected String doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            String result = null;
            try {
                result = ApiUtil.getJson(searchUrl);
            }
            catch (IOException e){
                Log.e("Error", e.getMessage());
            }
            return result;
        }
        @Override
        protected void onPostExecute(String result) {
            //create Data object
            Data data = new Data(result);
            storeSearchPref(data.dateStamp, data.temperature,
                    data.cityName, data.windSpeed,
                    data.visibility, data.humidity,
                    data.iconUrl,
                    data.description);
            displayToLayout();
        }

    }

}