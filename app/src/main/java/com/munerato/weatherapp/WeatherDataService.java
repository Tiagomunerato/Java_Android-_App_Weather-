package com.munerato.weatherapp;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WeatherDataService {

    public static final String QUERY_FOR_CITY_ID = "https://www.metaweather.com/api/location/search/?query=";
    Context context;
    String cityID = "";


    public WeatherDataService(Context context) {
        this.context = context;
    }

    //call back with volley
    public interface VolleyResponseListener {
        void onError(String message);

        void onResponse(String cityID);
    }

    public void getCityID(String cityName, final VolleyResponseListener volleyResponseListener) {
        String url = QUERY_FOR_CITY_ID + cityName;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                try {
                    JSONObject cityInfo = response.getJSONObject(0);
                    cityID = cityInfo.getString("woeid");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // its ok but not return
                // Toast.makeText(context,"City ID" + cityID,Toast.LENGTH_LONG).show();
                volleyResponseListener.onResponse(cityID);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(context,"Something wrong..",Toast.LENGTH_LONG).show();
                volleyResponseListener.onError("Something wrong...");
            }
        });
        MySingleton.getInstance(context).addToRequestQueue(jsonArrayRequest);

        // return  cityID;
    }

    //get the json obj
    //get the proprety called "consolodated_weather" wich array
    //get each item in the array and assing in to a new weatherreport

    public interface ForecastByIDResponse {
        void onError(String message);

        void onResponse(List<WeatherReportModel> weatherReportModels);
    }


    public void getCityForecastByID(String cityID, ForecastByIDResponse forecastByIDResponse) {
        List<WeatherReportModel> reportModelList = new ArrayList<>();
        String url = QUERY_FOR_CITY_ID + cityID;
        //get the json obj
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {



                //Toast.makeText(context, response.toString(), Toast.LENGTH_LONG).show();

                try {
                    JSONArray consolidate_weather_list;
                    consolidate_weather_list = response.getJSONArray("consolidated_weather");

                    //get the first item in the array

                      WeatherReportModel first_day = new WeatherReportModel();
                     for (int a = 0; a < consolidate_weather_list.length(); a++) {



                        JSONObject first_day_from_api = (JSONObject) consolidate_weather_list.get(a);

                        first_day.setId(first_day_from_api.getInt("id"));
                        first_day.setWeather_state_name(first_day_from_api.getString("weather_state_name"));
                        first_day.setWeather_state_abbr(first_day_from_api.getString("weather_state_abbr"));
                        first_day.setWind_direction_compass(first_day_from_api.getString("wind_direction_compass"));
                        first_day.setCreated(first_day_from_api.getString("created"));
                        first_day.setApplicable_date(first_day_from_api.getString("applicable_date"));
                        first_day.setMax_temp(first_day_from_api.getDouble("max_temp"));
                        first_day.setMin_temp(first_day_from_api.getDouble("min_temp"));
                        first_day.setWind_speed(first_day_from_api.getDouble("wind_speed"));
                        first_day.setWind_direction(first_day_from_api.getDouble("wind_direction"));
                        first_day.setAir_pressure(first_day_from_api.getDouble("air_pressure"));
                        first_day.setHumidity(first_day_from_api.getDouble("humidity"));
                        first_day.setVisibility(first_day_from_api.getDouble("visibility"));
                        first_day.setPredictability(first_day_from_api.getDouble("predictability"));
                        reportModelList.add(first_day);

                    }

                    forecastByIDResponse.onResponse((List<WeatherReportModel>) first_day);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public interface GetCityForecastByNameCallback{
        void onError(String message);
        void onResponse(List<WeatherReportModel> weatherReportModels);
    }
    public void getCityForecastByName(String cityName, GetCityForecastByNameCallback getCityForecastByNameCallback) {
        getCityID(cityName, new VolleyResponseListener() {
            @Override
            public void onError(String message) {

            }

            @Override
            public void onResponse(String cityID) {
                getCityForecastByID(cityID, new ForecastByIDResponse() {
                    @Override
                    public void onError(String message) {

                    }

                    @Override
                    public void onResponse(List<WeatherReportModel> weatherReportModels) {
                        getCityForecastByNameCallback.onResponse(weatherReportModels);

                    }
                });

            }
        });

    }


}
