package com.example.gearfit_android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class FoodLogActivity extends AppCompatActivity {
    private int userId;
    private TextView mealTitleTextView;
    private TextView selectedDateTextView;
    private LinearLayout foodLogContainer;

    private LinearLayout addFoodLogButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_log);
        setupUI();

        // Obtener los datos del Intent de NutritionActivity
        userId = getIntent().getIntExtra("userId", -1);
        String mealTitle = getIntent().getStringExtra("currentMeal");
        String selectedDate = getIntent().getStringExtra("currentMealDate");

        mealTitleTextView = findViewById(R.id.currentMeal);
        selectedDateTextView = findViewById(R.id.currentMealDate);
        foodLogContainer = findViewById(R.id.foodLogContainer);

        // Mostrar la comida actual y la fecha
        mealTitleTextView.setText(mealTitle);
        String formattedDate = formatDate(selectedDate);

        // Formatear la fecha antes de mostrarla
        selectedDateTextView.setText(formattedDate);

        loadFoodLog(selectedDate, mealTitle);

        addFoodLogButton = findViewById(R.id.addFoodLogButton);
        addFoodLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodLogActivity.this, AddFoodLogActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("currentMeal", mealTitleTextView.getText().toString());
                intent.putExtra("currentMealDate", selectedDate);
                startActivity(intent);
            }
        });
    }

    private String formatDate(String dateString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

        try {
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString; // En caso de error, devuelve la fecha sin cambios
        }
    }

    private void loadFoodLog(String date, String mealType) {
        /*DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<FoodLog> foodLogs = dbHelper.getFoodLogsByDateAndMeal(userId, date, mealType);

        foodLogContainer.removeAllViews();
        for (FoodLog log : foodLogs) {
            View foodView = getLayoutInflater().inflate(R.layout.food_log_item, foodLogContainer, false);
            TextView foodNameText = foodView.findViewById(R.id.food_name);
            TextView foodGramsText = foodView.findViewById(R.id.food_grams);
            TextView foodCaloriesText = foodView.findViewById(R.id.food_calories);

            foodNameText.setText(log.getFoodName());
            foodGramsText.setText(log.getGrams() + "g");
            foodCaloriesText.setText((log.getCaloriesPer100g() * log.getGrams() / 100) + " kcal");

            foodLogContainer.addView(foodView);
        }
        */
    }

    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#7AB8FF"));
        getSupportActionBar().hide();
    }
}
