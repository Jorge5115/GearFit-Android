package com.example.gearfit_android;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class FoodListEditActivity extends AppCompatActivity {

    private EditText etFoodName, etCalories, etFat, etCarbs, etProtein;
    private Button btnSaveFood, btnDeleteFood, btnCancelFood;

    private int foodId, userId;

    private String mealTitle, selectedDate;

    private DatabaseHelper dbHelper;
    private Food currentFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list_edit);
        setupUI();

        dbHelper = new DatabaseHelper(this);

        // Vincular vistas
        etFoodName = findViewById(R.id.etFoodName);
        etCalories = findViewById(R.id.etCalories);
        etFat = findViewById(R.id.etFat);
        etCarbs = findViewById(R.id.etCarbs);
        etProtein = findViewById(R.id.etProtein);
        btnSaveFood = findViewById(R.id.btnSaveFood);
        btnDeleteFood = findViewById(R.id.btnDeleteFood);
        btnCancelFood = findViewById(R.id.btnCancelFood);

        // Obtener IDs del intent
        foodId = getIntent().getIntExtra("foodId", -1);
        userId = getIntent().getIntExtra("userId", -1);
        mealTitle = getIntent().getStringExtra("currentMeal");
        selectedDate = getIntent().getStringExtra("currentMealDate");

        etFoodName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        etCalories.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        etFat.setFilters(new InputFilter[]{decimalDigitsInputFilter});
        etCarbs.setFilters(new InputFilter[]{decimalDigitsInputFilter});
        etProtein.setFilters(new InputFilter[]{decimalDigitsInputFilter});

        // Cargar alimento
        currentFood = dbHelper.getFoodById(foodId);

        if (currentFood != null) {
            etFoodName.setText(currentFood.getName());
            etCalories.setText(String.valueOf(currentFood.getCalories()));
            etFat.setText(String.valueOf(currentFood.getFat()));
            etCarbs.setText(String.valueOf(currentFood.getCarbs()));
            etProtein.setText(String.valueOf(currentFood.getProtein()));
        } else {
            Toast.makeText(this, "Alimento no encontrado", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Botón GUARDAR
        btnSaveFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFood();
            }
        });

        // Botón ELIMINAR
        btnDeleteFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });

        // Botón CANCELAR
        btnCancelFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodListEditActivity.this, FoodListActivity.class);
                intent.putExtra("userId", userId); // si querés seguir pasando también el userId
                intent.putExtra("foodId", foodId); // pasás el ID del alimento
                intent.putExtra("currentMeal", mealTitle);
                intent.putExtra("currentMealDate", selectedDate);
                startActivity(intent);
                finish();
            }
        });
    }

    private void saveFood() {
        String name = etFoodName.getText().toString().trim();
        String caloriesStr = etCalories.getText().toString().trim();
        String fatStr = etFat.getText().toString().trim();
        String carbsStr = etCarbs.getText().toString().trim();
        String proteinStr = etProtein.getText().toString().trim();

        if (name.isEmpty() || caloriesStr.isEmpty() || fatStr.isEmpty() || carbsStr.isEmpty() || proteinStr.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        currentFood.setName(name);
        currentFood.setCalories(Integer.parseInt(caloriesStr));
        currentFood.setFat(Double.parseDouble(fatStr));
        currentFood.setCarbs(Double.parseDouble(carbsStr));
        currentFood.setProtein(Double.parseDouble(proteinStr));

        boolean updated = dbHelper.updateFood(currentFood);
        if (updated) {
            Intent intent = new Intent(FoodListEditActivity.this, FoodListActivity.class);
            intent.putExtra("userId", userId); // si querés seguir pasando también el userId
            intent.putExtra("foodId", foodId); // pasás el ID del alimento
            intent.putExtra("currentMeal", mealTitle);
            intent.putExtra("currentMealDate", selectedDate);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar alimento")
                .setMessage("¿Estás seguro de que quieres eliminar este alimento?")
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteFood(foodId);
                        Intent intent = new Intent(FoodListEditActivity.this, FoodListActivity.class);
                        intent.putExtra("userId", userId); // si querés seguir pasando también el userId
                        intent.putExtra("foodId", foodId); // pasás el ID del alimento
                        intent.putExtra("currentMeal", mealTitle);
                        intent.putExtra("currentMealDate", selectedDate);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private InputFilter decimalDigitsInputFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            String result = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());

            if (result.equals(".")) return "0.";

            if (result.contains(".")) {
                String[] parts = result.split("\\.");
                String integerPart = parts[0];
                String decimalPart = parts.length > 1 ? parts[1] : "";

                if (integerPart.length() > 3 || decimalPart.length() > 2) {
                    return "";
                }
            } else {
                if (result.length() > 3) {
                    return "";
                }
            }

            return null;
        }
    };

    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#7AB8FF"));
        getSupportActionBar().hide();
    }
}
