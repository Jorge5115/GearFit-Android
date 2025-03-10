package com.example.gearfit_android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<FoodLog> foodLogs = dbHelper.getFoodLogsByDateAndMeal(userId, date, mealType);

        // Verificamos si no hay ningún log de alimento
        if (foodLogs.isEmpty()) {
            Toast.makeText(this, "No se han encontrado alimentos para esta comida.", Toast.LENGTH_SHORT).show();
        }

        foodLogContainer.removeAllViews();
        for (FoodLog log : foodLogs) {
            // Obtener los valores nutricionales usando el método getNutritionalValues
            double[] nutritionalValues = log.getNutritionalValues(dbHelper);

            if (nutritionalValues != null) {
                // Inflar la vista para cada alimento
                View foodView = getLayoutInflater().inflate(R.layout.food_item_log_button, foodLogContainer, false);

                TextView foodNameText = foodView.findViewById(R.id.food_name);
                TextView foodGramsText = foodView.findViewById(R.id.food_grams);
                TextView foodCaloriesText = foodView.findViewById(R.id.food_calories);
                TextView foodProteinsText = foodView.findViewById(R.id.food_proteins);
                TextView foodCarbsText = foodView.findViewById(R.id.food_carbs);
                TextView foodFatsText = foodView.findViewById(R.id.food_fats);

                // Establecer los valores nutricionales
                foodNameText.setText("Food ID: " + log.getFoodId()); // O puedes obtener el nombre del alimento si lo prefieres
                foodGramsText.setText(log.getGrams() + "g");
                foodProteinsText.setText(String.format(Locale.getDefault(), "%.2f g", nutritionalValues[0]));
                foodCarbsText.setText(String.format(Locale.getDefault(), "%.2f g", nutritionalValues[1]));
                foodFatsText.setText(String.format(Locale.getDefault(), "%.2f g", nutritionalValues[2]));

                // Agregar la vista del alimento al contenedor
                foodLogContainer.addView(foodView);
            }
        }
    }


    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#7AB8FF"));
        getSupportActionBar().hide();
    }
}
