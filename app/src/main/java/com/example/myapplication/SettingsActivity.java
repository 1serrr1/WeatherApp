package com.example.myapplication;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SettingsActivity extends AppCompatActivity {


    private Switch switchLocationPermission, switchDarkTheme, switchTemperatureUnit;
    private Button buttonBack;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "app_settings";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);


        switchLocationPermission = findViewById(R.id.switchLocationPermission);
        switchDarkTheme = findViewById(R.id.switchDarkTheme);
        switchTemperatureUnit = findViewById(R.id.switchTemperatureUnit);
        buttonBack = findViewById(R.id.buttonBack);

       // switchPushNotifications.setChecked(sharedPreferences.getBoolean("push_notifications", true));
        switchLocationPermission.setChecked(sharedPreferences.getBoolean("location_permission", false));
        switchDarkTheme.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
        switchTemperatureUnit.setChecked(sharedPreferences.getBoolean("temperature_unit_celsius", true));

        switchTemperatureUnit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("temperature_unit_celsius", isChecked);
            editor.apply();
            Toast.makeText(SettingsActivity.this, "Единицы температуры: " + (isChecked ? "Цельсий" : "Фаренгейт"), Toast.LENGTH_SHORT).show();
        });
        // Включение/выключение темной темы
        switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_theme", isChecked);
            editor.apply();
        });

       // switchPushNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
       //     SharedPreferences.Editor editor = sharedPreferences.edit();
      //      editor.putBoolean("push_notifications", isChecked);
      //      editor.apply();
        //    Toast.makeText(SettingsActivity.this, "Push уведомления: " + (isChecked ? "Включены" : "Отключены"), Toast.LENGTH_SHORT).show();
       // });

        // Разрешение на доступ к местоположению
        switchLocationPermission.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("location_permission", isChecked);
            editor.apply();
            if (isChecked) {

                requestLocationPermission();
            } else {

                Toast.makeText(SettingsActivity.this, "Местоположение отключено", Toast.LENGTH_SHORT).show();
            }
        });



        buttonBack.setOnClickListener(v -> finish());
    }

    // Запрос разрешения на доступ к местоположению
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("location_permission", true);
            editor.apply();
            Toast.makeText(SettingsActivity.this, "Разрешение на местоположение: Включено", Toast.LENGTH_SHORT).show();
        }
    }

    // Обработка результата запроса разрешения
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("location_permission", true);
                editor.apply();
                Toast.makeText(SettingsActivity.this, "Разрешение на местоположение: Включено", Toast.LENGTH_SHORT).show();
            } else {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("location_permission", false);
                editor.apply();
                Toast.makeText(SettingsActivity.this, "Разрешение на местоположение: Отклонено", Toast.LENGTH_SHORT).show();
            }
        }
    }

}