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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

public class FoodLogActivity extends AppCompatActivity {
    private int userId;
    private TextView mealTitleTextView;
    private TextView selectedDateTextView;
    private LinearLayout foodLogContainer;

    private LinearLayout addFoodLogButton;

    private PieChart pieChartCalories, pieChartProteins, pieChartCarbs, pieChartFats;


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

        // Referencias a los gráficos
        pieChartCalories = findViewById(R.id.pieChartCalories);
        pieChartProteins = findViewById(R.id.pieChartProteins);
        pieChartCarbs = findViewById(R.id.pieChartCarbs);
        pieChartFats = findViewById(R.id.pieChartFats);

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

    private void setupPieChart(PieChart chart, String label, float value, float maxValue) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        if (value > maxValue) {
            // Si se excede el límite, llenar todo el círculo de rojo
            entries.add(new PieEntry(1f, ""));
        } else {
            entries.add(new PieEntry(value, "")); // Porción rellena
            entries.add(new PieEntry(maxValue - value, "")); // Porción vacía
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        if (value > maxValue) {
            dataSet.setColors(Color.RED); // Todo rojo si se pasa
        } else {
            dataSet.setColors(new int[]{Color.BLUE, Color.parseColor("#f7f7f7")}); // Azul para progreso, gris para el resto
        }

        dataSet.setDrawValues(false); // Ocultar valores dentro del gráfico

        PieData data = new PieData(dataSet);
        chart.setData(data);

        // **Estilo de dona**
        chart.setUsePercentValues(false);
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleRadius(60f); // Tamaño del agujero central
        chart.setTransparentCircleRadius(45f); // Círculo semitransparente alrededor
        chart.setHoleColor(Color.parseColor("#f7f7f7")); // Color del círculo interior

        // **Hacer que empiece en las 12 (90°) y se llene en sentido horario**
        chart.setRotationAngle(-90); // Comienza desde arriba (posición de las 12)
        chart.setRotationEnabled(false); // Evita que el usuario lo gire

        // **Quitar las leyendas**
        chart.getLegend().setEnabled(false);

        // **Quitar el efecto de selección al tocar**
        chart.setHighlightPerTapEnabled(false);

        chart.invalidate(); // Refrescar el gráfico
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
        float dailyLimit = 4000; // Esto lo cambiarás luego para que sea configurable

        foodLogContainer.removeAllViews();
        for (FoodLog log : foodLogs) {
            // Obtener los valores nutricionales usando el método getNutritionalValues
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
                foodGramsText.setText(String.format(Locale.getDefault(), "%.2f g", log.getGrams()));
                foodFatsText.setText(String.format(Locale.getDefault(), "%.2f g", nutritionalValues[0]));
                foodCarbsText.setText(String.format(Locale.getDefault(), "%.2f g", nutritionalValues[1]));
                foodProteinsText.setText(String.format(Locale.getDefault(), "%.2f g", nutritionalValues[2]));
                foodCaloriesText.setText(String.format(Locale.getDefault(), "%.2f kcal", nutritionalValues[3]));

                // Agregar la vista del alimento al contenedor
                foodLogContainer.addView(foodView);
            }
        }

        setupPieChart(pieChartFats, "", totalFats, 100);
        setupPieChart(pieChartCarbs, "", totalCarbs, 300);
        setupPieChart(pieChartProteins, "", totalProteins, 200);
        setupPieChart(pieChartCalories, "", totalCalories, dailyLimit);
    }


    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#7AB8FF"));
        getSupportActionBar().hide();
    }
}
