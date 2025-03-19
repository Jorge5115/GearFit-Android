package com.example.gearfit_android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class FoodLogEditActivity extends AppCompatActivity {

    private int userId;
    private String meal, date;

    private EditText editFoodQuantity;
    private TextView textFoodName, textFat, textCarbs, textProteins, textCalories;
    private Button buttonSave, buttonCancel;
    private int foodLogId;
    private double originalQuantity;
    private double newQuantity;
    private double fatPerGram, carbsPerGram, proteinPerGram, caloriesPerGram;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_log_edit);
        setupUI();

        // Obtener referencias a los elementos de la interfaz
        textFoodName = findViewById(R.id.textFoodName);
        editFoodQuantity = findViewById(R.id.editFoodQuantity);
        textFat = findViewById(R.id.textFat);
        textCarbs = findViewById(R.id.textCarbs);
        textProteins = findViewById(R.id.textProteins);
        textCalories = findViewById(R.id.textCalories);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);

        // Obtener los datos del Intent
        userId = getIntent().getIntExtra("userId", -1);
        foodLogId = getIntent().getIntExtra("foodLogId", -1);
        String foodName = getIntent().getStringExtra("foodName");
        originalQuantity = getIntent().getDoubleExtra("grams", 0);
        meal = getIntent().getStringExtra("currentMeal");
        date = getIntent().getStringExtra("currentMealDate");

        double totalFat = getIntent().getDoubleExtra("fats", 0);
        double totalCarbs = getIntent().getDoubleExtra("carbs", 0);
        double totalProteins = getIntent().getDoubleExtra("proteins", 0);
        double totalCalories = getIntent().getDoubleExtra("calories", 0);

        // **Calcular valores por gramo**
        fatPerGram = totalFat / originalQuantity;
        carbsPerGram = totalCarbs / originalQuantity;
        proteinPerGram = totalProteins / originalQuantity;
        caloriesPerGram = totalCalories / originalQuantity;

        textFoodName.setText(foodName);
        editFoodQuantity.setText(formatNumber(originalQuantity));

        // Actualizar valores nutricionales en la interfaz
        newQuantity = originalQuantity;
        updateNutritionalValues(newQuantity);

        // Agregar TextWatcher para actualizar valores en tiempo real
        editFoodQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    try {
                        double newQuantity = Double.parseDouble(s.toString());
                        updateNutritionalValues(newQuantity);
                    } catch (NumberFormatException e) {
                        updateNutritionalValues(originalQuantity);
                    }
                } else {
                    updateNutritionalValues(originalQuantity);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        buttonSave.setOnClickListener(v -> saveChanges());
        buttonCancel.setOnClickListener(v -> finish());
    }


    @SuppressLint("SetTextI18n")
    private void updateNutritionalValues(double quantity) {
        textFat.setText(formatNumber(quantity * fatPerGram) + "gr de Grasa");
        textCarbs.setText(formatNumber(quantity * carbsPerGram) + "gr de Carbohidratos");
        textProteins.setText(formatNumber(quantity * proteinPerGram) + "gr de Proteína");
        textCalories.setText(formatNumber(quantity * caloriesPerGram) + " Calorías");
    }


    private void saveChanges() {
        String newQuantityStr = editFoodQuantity.getText().toString();
        if (!newQuantityStr.isEmpty()) {
            double newQuantity = Double.parseDouble(newQuantityStr);
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            dbHelper.updateFoodLogQuantity(foodLogId, newQuantity);

            Intent intent = new Intent(FoodLogEditActivity.this, FoodLogActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("currentMeal", meal);
            intent.putExtra("currentMealDate", date);

            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Ingrese una cantidad válida", Toast.LENGTH_SHORT).show();
        }
    }

    public static String formatNumber(double number) {
        if (number % 1 == 0) {
            return String.format(Locale.getDefault(), "%.0f", number); // Si es un número entero, sin decimales
        } else {
            return String.format(Locale.getDefault(), "%.2f", number); // Si tiene decimales, muestra dos
        }
    }

    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#7AB8FF"));
        getSupportActionBar().hide();
    }
}
