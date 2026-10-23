package com.example.smartpantrymanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchExpiryAlerts;
    private TextView textSettingsStatus;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchExpiryAlerts = findViewById(R.id.switchExpiryAlerts);
        textSettingsStatus = findViewById(R.id.textSettingsStatus);

        Button buttonBackToPantrySettings =
                findViewById(R.id.buttonBackToPantrySettings);

        preferences = getSharedPreferences(
                "SmartPantrySettings",
                MODE_PRIVATE
        );

        boolean expiryAlertsEnabled =
                preferences.getBoolean("expiry_alerts_enabled", true);

        switchExpiryAlerts.setChecked(expiryAlertsEnabled);

        updateStatus(expiryAlertsEnabled);

        switchExpiryAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit()
                    .putBoolean("expiry_alerts_enabled", isChecked)
                    .apply();

            updateStatus(isChecked);
        });

        buttonBackToPantrySettings.setOnClickListener(v -> finish());
    }

    private void updateStatus(boolean enabled) {
        if (enabled) {
            textSettingsStatus.setText(
                    "Expiry alerts are enabled. Your preference is saved."
            );
        } else {
            textSettingsStatus.setText(
                    "Expiry alerts are disabled. Your preference is saved."
            );
        }
    }
}
