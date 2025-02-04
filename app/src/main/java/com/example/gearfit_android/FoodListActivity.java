package com.example.gearfit_android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FoodListActivity extends AppCompatActivity {

    private int userId;
    private TextView mealTitleTextView;
    private TextView selectedDateTextView;
    private LinearLayout foodItemContainer;
    private LinearLayout addFoodButton;
    private EditText searchFoodEditText;

    private List<Food> foodList; // Lista original de alimentos
    private List<Food> filteredFoodList; // Lista filtrada de alimentos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        setupUI();

        mealTitleTextView = findViewById(R.id.currentMeal);
        selectedDateTextView = findViewById(R.id.currentMealDate);
        addFoodButton = findViewById(R.id.addFoodButton);
        searchFoodEditText = findViewById(R.id.editSearchFood);

        // Obtener los datos del Intent de NutritionActivity
        userId = getIntent().getIntExtra("userId", -1);
        String mealTitle = getIntent().getStringExtra("currentMeal");
        String selectedDate = getIntent().getStringExtra("currentMealDate");

        // Mostrar la comida actual y la fecha
        mealTitleTextView.setText(mealTitle);
        selectedDateTextView.setText(selectedDate);

        foodItemContainer = findViewById(R.id.foodItemContainer);
        loadUserFoods();

        addFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodListActivity.this, FoodCreateActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });

        // Agregar el TextWatcher al EditText
        searchFoodEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se necesita implementar
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFoods(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No se necesita implementar
            }
        });
    }

    private void loadUserFoods() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        foodList = dbHelper.getFoodsByUserId(userId); // Cargar la lista original de alimentos
        filteredFoodList = new ArrayList<>(foodList); // Inicializar la lista filtrada

        displayFoods(filteredFoodList); // Mostrar todos los alimentos inicialmente
    }

    private void filterFoods(String query) {
        filteredFoodList.clear(); // Limpiar la lista filtrada

        if (query.isEmpty()) {
            filteredFoodList.addAll(foodList); // Si la consulta está vacía, mostrar todos los alimentos
        } else {
            for (Food food : foodList) {
                if (food.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredFoodList.add(food); // Agregar alimentos que coincidan con la consulta
                }
            }
        }

        displayFoods(filteredFoodList); // Mostrar los alimentos filtrados
    }

    private void displayFoods(List<Food> foods) {
        foodItemContainer.removeAllViews(); // Limpiar el contenedor antes de agregar nuevos elementos

        for (Food food : foods) {
            // Infla el layout food_item_button.xml
            View foodView = getLayoutInflater().inflate(R.layout.food_item_button, foodItemContainer, false);

            // Referencia los TextViews que están dentro del layout inflado
            TextView foodNameText = foodView.findViewById(R.id.food_name);
            TextView foodCaloriesText = foodView.findViewById(R.id.food_calories);

            // Asigna el nombre del alimento y las calorías
            foodNameText.setText(food.getName());
            foodCaloriesText.setText(food.getCalories()+ " kcal/100g");

            // Configura un click listener (si es necesario)
            foodView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Acción cuando se presiona un alimento (opcional)
                }
            });

            foodView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.setBackgroundResource(R.drawable.rounded_pressed_food_button); // Fondo al presionar
                    } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        v.setBackgroundResource(R.drawable.rounded_food_button); // Fondo normal al soltar
                    }
                    return false;
                }
            });

            // Agrega la vista inflada al contenedor
            foodItemContainer.addView(foodView);
        }
    }

    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#7AB8FF"));
        getSupportActionBar().hide();
    }
}

