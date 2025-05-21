package com.example.gearfit_android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class SelectFoodLogActivity extends AppCompatActivity {
    private LinearLayout foodListContainer;
    private DatabaseHelper dbHelper;
    private int userId;

    private String mealTitle, selectedDate;

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

        if (foodList.isEmpty()) {
            setResult(RESULT_CANCELED);
            finish();
        }

        for (Food food : foodList) {
            Button foodButton = new Button(this);
            foodButton.setText(food.getName() + " - " + food.getCalories() + " kcal");
            foodButton.setPadding(30, 0, 30, 0);
            foodButton.setAllCaps(false);

            // 🎨 Aplica estilo redondeado
            foodButton.setBackgroundResource(R.drawable.rounded_selector_button);
            foodButton.setTextColor(Color.parseColor("#f7f7f7"));

            // Extra: margen entre botones
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(70, 10, 70, 30);
            foodButton.setLayoutParams(params);

            foodButton.setOnClickListener(v -> returnSelectedFood(food));

            foodButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.setBackgroundResource(R.drawable.rounded_selector_pressed_button);
                        foodButton.setTextColor(Color.parseColor("#636363"));
                    } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        v.setBackgroundResource(R.drawable.rounded_selector_button);
                        foodButton.setTextColor(Color.parseColor("#f7f7f7"));
                    }
                    return false; // Permite que el evento continúe propagándose (para que siga funcionando el OnClick)
                }
            });

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
        getWindow().setStatusBarColor(Color.parseColor("#444444"));
        getSupportActionBar().hide();
    }
}
