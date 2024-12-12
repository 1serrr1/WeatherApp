package com.example.myapplication;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.ImageView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {
    private EditText editTextCity;
    private Button buttonGetWeather, buttonGetLocation;
    private TextView textViewWeather, textViewForecast, textViewTemperatureUnit;
    private ToggleButton toggleTemperature;
    private final String API_KEY = "df109a4a699e651b7f52c2ff0a8c8c7d";
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isCelsius = true;
    private Button buttonSettings;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "app_settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        buttonSettings = findViewById(R.id.buttonSettings);

        if (sharedPreferences.getBoolean("dark_theme", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        isCelsius = sharedPreferences.getBoolean("temperature_unit_celsius", true);

        buttonSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        editTextCity = findViewById(R.id.editTextCity);
        buttonGetWeather = findViewById(R.id.buttonGetWeather);
        buttonGetLocation = findViewById(R.id.buttonGetLocation);
        textViewWeather = findViewById(R.id.textViewWeather);
        textViewForecast = findViewById(R.id.textViewForecast);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        buttonGetWeather.setOnClickListener(v -> {
            String city = editTextCity.getText().toString();
            if (!city.isEmpty()) {
                getWeatherData(city);
            } else {
                Toast.makeText(MainActivity.this, "Введите город", Toast.LENGTH_SHORT).show();
            }
        });

        buttonGetLocation.setOnClickListener(v -> getLocation());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isCelsius = sharedPreferences.getBoolean("temperature_unit_celsius", true);

        String city = editTextCity.getText().toString();
        if (!city.isEmpty()) {
            getWeatherData(city);
        }
    }

    private void getWeatherData(String city) {
        String units = isCelsius ? "metric" : "imperial";
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY + "&units=" + units;

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    displayWeatherData(response);
                    try {
                        double latitude = response.getJSONObject("coord").getDouble("lat");
                        double longitude = response.getJSONObject("coord").getDouble("lon");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(MainActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(jsonObjectRequest);
    }



    private void displayWeatherData(JSONObject response) {
        try {
            String weatherInfo = response.getJSONArray("weather").getJSONObject(0).getString("description");
            double temperature = response.getJSONObject("main").getDouble("temp");
            double feelsLike = response.getJSONObject("main").getDouble("feels_like");
            double humidity = response.getJSONObject("main").getDouble("humidity");
            double windSpeed = response.getJSONObject("wind").getDouble("speed");
            String windDirection = response.getJSONObject("wind").getString("deg") + "°";

            textViewWeather.setText("Температура: " + temperature + (isCelsius ? "°C" : "°F") + "\n" +
                    "Ощущается как: " + feelsLike + (isCelsius ? "°C" : "°F") + "\n" +
                    "Влажность: " + humidity + "%\n" +
                    "Скорость ветра: " + windSpeed + " м/с\n" +
                    "Направление ветра: " + windDirection + "°\n" +
                    "Погода: " + weatherInfo);

            ImageView weatherIcon = findViewById(R.id.imageViewWeatherIcon);
            if (weatherInfo.contains("clear")) {
                weatherIcon.setImageResource(R.drawable.sun);
            } else if (weatherInfo.contains("clouds")) {
                weatherIcon.setImageResource(R.drawable.cloud);
            } else if (weatherInfo.contains("rain")) {
                weatherIcon.setImageResource(R.drawable.rain);
            } else if (weatherInfo.contains("snow")) {
                weatherIcon.setImageResource(R.drawable.snow);
            } else if (weatherInfo.contains("fog")) {
                weatherIcon.setImageResource(R.drawable.fog);
            } else if (weatherInfo.contains("wind")) {
                weatherIcon.setImageResource(R.drawable.wind);
            } else {
                weatherIcon.setImageResource(R.drawable.defualt);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Ошибка получения данных", Toast.LENGTH_SHORT).show();
        }
    }

    private String convertUnixToTime(long unixTime) {
        java.util.Date date = new java.util.Date(unixTime * 1000);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        return sdf.format(date);
    }

    private void getLocation() {
        // Проверяем, включено ли разрешение на местоположение
        if (sharedPreferences.getBoolean("location_permission", false)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            getWeatherDataByCoordinates(location.getLatitude(), location.getLongitude());
                        } else {
                            Toast.makeText(MainActivity.this, "Не удалось получить местоположение", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Если разрешение отключено, показываем сообщение и не запрашиваем местоположение
            Toast.makeText(MainActivity.this, "Местоположение отключено в настройках", Toast.LENGTH_SHORT).show();
        }
    }

    private void getWeatherDataByCoordinates(double latitude, double longitude) {
        String units = isCelsius ? "metric" : "imperial";
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY + "&units=" + units;

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    displayWeatherData(response);
                    try {
                        double lat = response.getJSONObject("coord").getDouble("lat");
                        double lon = response.getJSONObject("coord").getDouble("lon");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(MainActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Разрешение на доступ к местоположению отклонено", Toast.LENGTH_SHORT).show();
            }
        }
    }
}