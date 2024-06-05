package com.zxc.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup puzzleSizeGroup, timerGroup;
    private RadioButton size3x3, size4x4, size5x5, size6x6;
    private RadioButton timer30s, timer60s, timer120s, timer180s;
    private CheckBox timedCheckBox, randomImageCheckBox;
    private TextView timerText;
    private Button backButton, applyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        puzzleSizeGroup = findViewById(R.id.puzzle_size_group);
        size3x3 = findViewById(R.id.size_3x3);
        size4x4 = findViewById(R.id.size_4x4);
        size5x5 = findViewById(R.id.size_5x5);
        size6x6 = findViewById(R.id.size_6x6);
        timedCheckBox = findViewById(R.id.timed_checkbox);
        randomImageCheckBox = findViewById(R.id.random_image_checkbox);
        timerGroup = findViewById(R.id.timer_group);
        timer30s = findViewById(R.id.timer_30s);
        timer60s = findViewById(R.id.timer_60s);
        timer120s = findViewById(R.id.timer_120s);
        timer180s = findViewById(R.id.timer_180s);
        timerText = findViewById(R.id.timer_text);
        backButton = findViewById(R.id.button_back);
        applyButton = findViewById(R.id.button_apply);

        // Load preferences
        loadPreferences();

        timedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                timerGroup.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                timerText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();
                Toast.makeText(SettingsActivity.this, "Настройки сохранены", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadPreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String puzzleSize = sharedPref.getString("puzzle_size", "4x4");
        boolean isTimed = sharedPref.getBoolean("is_timed", false);
        int timerDuration = sharedPref.getInt("timer_duration", 60);
        boolean isRandomImage = sharedPref.getBoolean("is_random_image", false);

        switch (puzzleSize) {
            case "3x3":
                size3x3.setChecked(true);
                break;
            case "4x4":
                size4x4.setChecked(true);
                break;
            case "5x5":
                size5x5.setChecked(true);
                break;
            case "6x6":
                size6x6.setChecked(true);
                break;
        }

        switch (timerDuration) {
            case 30:
                timer30s.setChecked(true);
                break;
            case 60:
                timer60s.setChecked(true);
                break;
            case 120:
                timer120s.setChecked(true);
                break;
            case 180:
                timer180s.setChecked(true);
                break;
        }

        timedCheckBox.setChecked(isTimed);
        timerGroup.setVisibility(isTimed ? View.VISIBLE : View.GONE);
        timerText.setVisibility(isTimed ? View.VISIBLE : View.GONE);
        randomImageCheckBox.setChecked(isRandomImage);
    }

    private void savePreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();

        int selectedSizeId = puzzleSizeGroup.getCheckedRadioButtonId();
        String puzzleSize = "4x4"; // default
        if (selectedSizeId == R.id.size_3x3) {
            puzzleSize = "3x3";
        } else if (selectedSizeId == R.id.size_5x5) {
            puzzleSize = "5x5";
        } else if (selectedSizeId == R.id.size_6x6) {
            puzzleSize = "6x6";
        }

        boolean isTimed = timedCheckBox.isChecked();
        boolean isRandomImage = randomImageCheckBox.isChecked();

        int selectedTimerId = timerGroup.getCheckedRadioButtonId();
        int timerDuration = 60; // default
        if (selectedTimerId == R.id.timer_30s) {
            timerDuration = 30;
        } else if (selectedTimerId == R.id.timer_120s) {
            timerDuration = 120;
        } else if (selectedTimerId == R.id.timer_180s) {
            timerDuration = 180;
        }

        editor.putString("puzzle_size", puzzleSize);
        editor.putBoolean("is_timed", isTimed);
        editor.putInt("timer_duration", timerDuration);
        editor.putBoolean("is_random_image", isRandomImage);
        editor.apply();
    }
}

