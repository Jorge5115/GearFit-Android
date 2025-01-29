package com.example.gearfit_android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FoodCreateActivity extends AppCompatActivity {
    private EditText etFoodName, etCalories, etProtein, etCarbs, etFat;
    private Button btnSaveFood;
    private DatabaseHelper dbHelper;
    private int userId;  // Este valor debe ser obtenido desde la actividad o sesión

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_create);
        setupUI();

        dbHelper = new DatabaseHelper(this);

        etFoodName = findViewById(R.id.etFoodName);
        etCalories = findViewById(R.id.etCalories);
        etProtein = findViewById(R.id.etProtein);
        etCarbs = findViewById(R.id.etCarbs);
        etFat = findViewById(R.id.etFat);
        btnSaveFood = findViewById(R.id.btnSaveFood);

        // Simulación de userId. Reemplaza esto con el valor real de la sesión o usuario actual.
        userId = getIntent().getIntExtra("userId", -1);

        btnSaveFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createFood();
            }
        });
    }

    private void createFood() {
        String name = etFoodName.getText().toString().trim();
        String caloriesStr = etCalories.getText().toString().trim();
        String proteinStr = etProtein.getText().toString().trim();
        String carbsStr = etCarbs.getText().toString().trim();
        String fatStr = etFat.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(caloriesStr) ||
                TextUtils.isEmpty(proteinStr) || TextUtils.isEmpty(carbsStr) ||
                TextUtils.isEmpty(fatStr)) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int calories;
        double protein, carbs, fat;

        try {
            calories = Integer.parseInt(caloriesStr);
            protein = Double.parseDouble(proteinStr);
            carbs = Double.parseDouble(carbsStr);
            fat = Double.parseDouble(fatStr);

            if (calories < 0 || protein < 0 || carbs < 0 || fat < 0) {
                Toast.makeText(this, "Los valores no pueden ser negativos", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingrese valores numéricos válidos", Toast.LENGTH_SHORT).show();
            return;
        }

        Food food = new Food(0, userId, name, calories, protein, carbs, fat);

        if (dbHelper.addFood(food)) {
            Toast.makeText(this, "Alimento guardado exitosamente", Toast.LENGTH_SHORT).show();
            finish();  // Cierra la actividad después de guardar
        } else {
            Toast.makeText(this, "El nombre del alimento ya existe", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#f7f7f7"));
        getSupportActionBar().hide();
    }
}




