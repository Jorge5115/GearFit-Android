package com.example.gearfit_android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

public class FoodLogActivity extends AppCompatActivity {
    private int userId;
    private TextView mealTitleTextView;
    private TextView selectedDateTextView;

    private LinearLayout foodLogInfo;
    private LinearLayout foodLogContainer;

    // Botón para añadir alimento
    private LinearLayout addFoodLogButton;

    private Button buttonList;

    private TextView addFoodText;

    private TextView totalFoodLogFats, totalFoodLogCarbs, totalFoodLogProteins, totalFoodLogCalories;


    @SuppressLint("ClickableViewAccessibility")
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

        // Botón para ir a la lista de alimentos
        buttonList = findViewById(R.id.buttonList);
        buttonList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodLogActivity.this, FoodListActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("currentMeal", mealTitle);
                intent.putExtra("currentMealDate", selectedDate);
                startActivity(intent);
                overridePendingTransition(0, 0); // Elimina la animación de transición
                finish();
            }
        });

        foodLogInfo = findViewById(R.id.foodLogInfo);

        totalFoodLogFats = findViewById(R.id.totalFoodLogFats);
        totalFoodLogCarbs = findViewById(R.id.totalFoodLogCarbs);
        totalFoodLogProteins = findViewById(R.id.totalFoodLogProteins);
        totalFoodLogCalories = findViewById(R.id.totalFoodLogCalories);

        loadFoodLog(selectedDate, mealTitle);

        // Botón para añadir alimento (Funcional)
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

        // Botón para añadir alimento (Visual)
        addFoodText = findViewById(R.id.addFoodText);
        addFoodLogButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.add_food_log_pressed);
                    addFoodText.setTextColor(Color.WHITE);
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.setBackgroundResource(R.drawable.add_food_button);
                    addFoodText.setTextColor(Color.parseColor("#5A78A2"));
                }
                return false; // Permite que el evento continúe propagándose (para que siga funcionando el OnClick)
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

        float totalCalories = 0, totalProteins = 0, totalCarbs = 0, totalFats = 0;

        boolean validFoodLogs = false;  // Variable para verificar si hay alimentos válidos

        foodLogContainer.removeAllViews();
        for (FoodLog log : foodLogs) {
            // Obtener los valores nutricionales
            double[] nutritionalValues = log.getNutritionalValues(dbHelper);

            if (nutritionalValues != null) {
                // Actualizar los valores totales
                totalFats += nutritionalValues[0];
                totalCarbs += nutritionalValues[1];
                totalProteins += nutritionalValues[2];
                totalCalories += nutritionalValues[3];

                // Inflar la vista para cada alimento
                View foodView = getLayoutInflater().inflate(R.layout.food_item_log_button, foodLogContainer, false);

                TextView foodNameText = foodView.findViewById(R.id.food_name);
                TextView foodGramsText = foodView.findViewById(R.id.food_grams);
                TextView foodCaloriesText = foodView.findViewById(R.id.food_calories);
                TextView foodProteinsText = foodView.findViewById(R.id.food_proteins);
                TextView foodCarbsText = foodView.findViewById(R.id.food_carbs);
                TextView foodFatsText = foodView.findViewById(R.id.food_fats);

                // Establecer los valores nutricionales
                foodNameText.setText(log.getFoodName(dbHelper));
                foodGramsText.setText("Cantidad: " + formatNumber(log.getGrams()) + " gramos");
                foodFatsText.setText(formatNumber(nutritionalValues[0]));
                foodCarbsText.setText(formatNumber(nutritionalValues[1]));
                foodProteinsText.setText(formatNumber(nutritionalValues[2]));
                foodCaloriesText.setText(formatNumber(nutritionalValues[3]));

                // Agregar listener para ir a FoodLogEditActivity
                foodView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FoodLogActivity.this, FoodLogEditActivity.class);
                        intent.putExtra("userId", userId);
                        intent.putExtra("currentMeal", mealTitleTextView.getText().toString());
                        intent.putExtra("currentMealDate", date);

                        // Pasar los valores nutricionales a FoodLogEditActivity
                        intent.putExtra("foodLogId", log.getId());
                        intent.putExtra("grams", log.getGrams());
                        intent.putExtra("fats", nutritionalValues[0]);
                        intent.putExtra("carbs", nutritionalValues[1]);
                        intent.putExtra("proteins", nutritionalValues[2]);
                        intent.putExtra("calories", nutritionalValues[3]);
                        intent.putExtra("foodName", log.getFoodName(dbHelper));

                        startActivity(intent);
                        finish();
                    }
                });

                // Agregar la vista del alimento al contenedor
                foodLogContainer.addView(foodView);

                validFoodLogs = true;  // Marcar que hay al menos un alimento válido

                // Total de cada valor acumulado
                totalFoodLogFats.setText(formatNumber(totalFats));
                totalFoodLogCarbs.setText(formatNumber(totalCarbs));
                totalFoodLogProteins.setText(formatNumber(totalProteins));
                totalFoodLogCalories.setText(formatToInteger(totalCalories));
            }
        }

        if (!validFoodLogs) {
            foodLogInfo.setVisibility(View.GONE);  // Oculta la sección si hay alimentos pero su id padre fue eliminado
        } else {
            foodLogInfo.setVisibility(View.VISIBLE);  // Muestra la sección si hay alimentos
        }
    }

    public static String formatNumber(double number) {
        if (number % 1 == 0) {
            return String.format(Locale.US, "%.0f", number); // Si es un número entero, sin decimales
        } else {
            return String.format(Locale.US, "%.2f", number); // Si tiene decimales, muestra dos
        }
    }

    public static String formatToInteger(double number) {
        return String.format(Locale.US, "%.0f", number);
    }

    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#7AB8FF"));
        getSupportActionBar().hide();
    }
}
