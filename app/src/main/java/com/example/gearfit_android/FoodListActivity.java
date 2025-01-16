package com.example.gearfit_android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FoodListActivity extends AppCompatActivity {

    private TextView mealTitleTextView;
    private TextView selectedDateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        setupUI();

        mealTitleTextView = findViewById(R.id.currentMeal);
        selectedDateTextView = findViewById(R.id.currentMealDate);

        // Obtener los datos del Intent
        Intent intent = getIntent();
        String mealTitle = intent.getStringExtra("currentMeal");
        String selectedDate = intent.getStringExtra("currentMealDate");

        // Establecer los datos en los TextViews
        mealTitleTextView.setText(mealTitle);
        selectedDateTextView.setText(selectedDate); // Mostrar la fecha seleccionada

    }

    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#7AB8FF"));
        getSupportActionBar().hide();
    }
}

