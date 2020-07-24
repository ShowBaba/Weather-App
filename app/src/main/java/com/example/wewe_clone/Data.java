package com.example.wewe_clone;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Data {
    public String temperature;
    public String humidity;
    public String windSpeed;
    public String visibility;
    public String description;
    String dateStamp;
    public String cityName;
    public String mainCondition;
    public String iconUrl;
    String result;


    public Data(String result){
        this.result = result;
        getData();
    }

    //convert time stamp returned by the API to needed format
    private String convertTimeStamp(long timeStamp){
        long dv = timeStamp *1000;
        Date df = new java.util.Date(dv);
        return new SimpleDateFormat("yyyy-MM-dd").format(df);
    }

    public void getData(){
        if (result != null){
            try {
                JSONObject json = new JSONObject(result);
                JSONArray weatherArray = json.getJSONArray("weather");
                for (int i=0; i <weatherArray.length(); i++){
                    JSONObject weatherObj = weatherArray.getJSONObject(i);
                    mainCondition = weatherObj.getString("main");
                    description = weatherObj.getString("description");
                    iconUrl = weatherObj.getString("icon");
                }

                JSONObject tempObj = json.getJSONObject("main");
                temperature = tempObj.getString("temp");
                humidity = tempObj.getString("humidity");
                visibility = json.getString("visibility");
                JSONObject windObj = json.getJSONObject("wind");
                windSpeed = windObj.getString("speed");
                cityName = json.getString("name");
                dateStamp = json.getString("dt");

                dateStamp = convertTimeStamp(Long.parseLong(dateStamp));
                temperature = String.valueOf(new BigDecimal(String.valueOf( Math.round((Double.parseDouble(temperature) - 273.15) * 10)/10.0)).stripTrailingZeros());

                iconUrl = "http://openweathermap.org/img/w/" + iconUrl + ".png";

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            Log.e("returned null", "onPostExecute: ", null);
        }

    }


}



//<!--wind-->
//<!--temp-->
//<!--visibility-->
//<!--humidity-->
//<!--description-->
//<!--name-->