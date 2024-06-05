package com.zxc.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainMenuActivity.this);
                boolean isRandomImage = sharedPref.getBoolean("is_random_image", false);

                if (isRandomImage) {
                    selectRandomImageAndStartGame();
                } else {
                    selectImageAndStartGame();
                }
            }
        });

        Button btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        Button exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity(); // Закрываем все активности и приложение
            }
        });
    }

    private void selectRandomImageAndStartGame() {
        int[] imageList = {R.drawable.sample_image, R.drawable.puzzle, R.drawable.puzzle1, R.drawable.puzzle2, R.drawable.puzzle3,
                R.drawable.puzzle4, R.drawable.puzzle5, R.drawable.puzzle6, R.drawable.puzzle7};
        Random random = new Random();
        int randomIndex = random.nextInt(imageList.length);
        int selectedImage = imageList[randomIndex];

        startGame(selectedImage);
    }

    private void selectImageAndStartGame() {
        int[] imageList = {R.drawable.sample_image, R.drawable.puzzle, R.drawable.puzzle1, R.drawable.puzzle2, R.drawable.puzzle3,
                R.drawable.puzzle4, R.drawable.puzzle5, R.drawable.puzzle6, R.drawable.puzzle7};
        int selectedImage = imageList[0]; // Default to the first image for now. You can modify this to show an image selector if needed.

        startGame(selectedImage);
    }

    private void startGame(int selectedImage) {
        Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
        intent.putExtra("selected_image", selectedImage);
        startActivity(intent);
    }
}
