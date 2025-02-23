package com.example.gearfit_android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class SelectFoodLogActivity extends AppCompatActivity {
    private LinearLayout foodListContainer;
    private DatabaseHelper dbHelper;
    private int userId;

    private String mealTitle;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_select);
        setupUI();

        userId = getIntent().getIntExtra("userId", -1);
        mealTitle = getIntent().getStringExtra("currentMeal");
        selectedDate = getIntent().getStringExtra("currentMealDate");

        dbHelper = new DatabaseHelper(this);
        foodListContainer = findViewById(R.id.foodListContainer);

        loadFoodButtons();
    }

    private void loadFoodButtons() {
        List<Food> foodList = dbHelper.getFoodsByUserId(userId);

        for (Food food : foodList) {
            Button foodButton = new Button(this);
            foodButton.setText(food.getName() + " - " + food.getCalories() + " kcal");
            foodButton.setPadding(10, 20, 10, 20);
            foodButton.setAllCaps(false);

            foodButton.setOnClickListener(v -> returnSelectedFood(food));

            foodListContainer.addView(foodButton);
        }
    }

    private void returnSelectedFood(Food food) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selectedFoodId", food.getId());
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#7AB8FF"));
        getSupportActionBar().hide();
    }
}
