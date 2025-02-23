package com.example.gearfit_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddFoodLogActivity extends AppCompatActivity {

    // Datos del usuario
    private int userId;
    private TextView mealTitleTextView;
    private TextView selectedDateTextView;

    private String selectDateUnformatted;

    // Elementos de la interfaz de usuario
    private EditText editTextGrams;
    private TextView totalKcalTextView, totalProteinTextView, totalCarbsTextView, totalFatTextView;
    private FloatingActionButton btnSaveFoodLog;

    // Alimento seleccionado
    private Food selectedFood;

    private DatabaseHelper dbHelper;

    // Elementos de la interfaz de usuario
    private LinearLayout foodSelectionHeader;
    private TextView foodSelectionHeaderText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_add);

        dbHelper = new DatabaseHelper(this);

        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no válido", Toast.LENGTH_SHORT).show();
            finish(); // Cierra la actividad si el usuario no es válido
        }


        userId = getIntent().getIntExtra("userId", -1);


        String mealTitle = getIntent().getStringExtra("currentMeal");
        String selectedDate = getIntent().getStringExtra("currentMealDate");
        selectDateUnformatted = selectedDate;

        mealTitleTextView = findViewById(R.id.mealTitleTextView);
        selectedDateTextView = findViewById(R.id.selectedDateTextView);

        editTextGrams = findViewById(R.id.editTextGrams);
        totalKcalTextView = findViewById(R.id.totalKcalTextView);
        totalProteinTextView = findViewById(R.id.totalProteinTextView);
        totalCarbsTextView = findViewById(R.id.totalCarbsTextView);
        totalFatTextView = findViewById(R.id.totalFatTextView);
        btnSaveFoodLog = findViewById(R.id.btnSaveFoodLog);

        // Mostrar la comida actual y la fecha
        mealTitleTextView.setText(mealTitle);
        String formattedDate = formatDate(selectedDate);

        // Formatear la fecha antes de mostrarla
        selectedDateTextView.setText(formattedDate);

        // Detectar cambios en los gramos y calcular en tiempo real
        /*editTextGrams.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateNutrition();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });*/


        btnSaveFoodLog.setOnClickListener(v -> saveFoodLog());

        foodSelectionHeader = findViewById(R.id.foodSelectionHeader);
        foodSelectionHeaderText = findViewById(R.id.foodSelectionHeaderText);

        foodSelectionHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddFoodLogActivity.this, SelectFoodLogActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("currentMeal", mealTitleTextView.getText().toString());
                intent.putExtra("currentMealDate", selectedDate);
                startActivityForResult(intent, 1);
            }
        });
    }

    // Recibe el resultado de la selección de alimentos
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            int selectedFoodId = data.getIntExtra("selectedFoodId", -1);
            if (selectedFoodId != -1) {
                selectedFood = dbHelper.getFoodById(selectedFoodId);
                foodSelectionHeaderText.setText(selectedFood.getName()); // Mostrar el nombre del alimento seleccionado
            }
        }
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

    private void calculateNutrition() {
        if (selectedFood == null) return;

        String gramsStr = editTextGrams.getText().toString();
        if (gramsStr.isEmpty()) {
            clearNutritionValues();
            return;
        }

        int grams = Integer.parseInt(gramsStr);
        double factor = grams / 100.0;

        totalKcalTextView.setText(String.valueOf((int) (selectedFood.getCalories() * factor)));
        totalProteinTextView.setText(String.format("%.2f", selectedFood.getProtein() * factor));
        totalCarbsTextView.setText(String.format("%.2f", selectedFood.getCarbs() * factor));
        totalFatTextView.setText(String.format("%.2f", selectedFood.getFat() * factor));
    }

    private void clearNutritionValues() {
        totalKcalTextView.setText("0");
        totalProteinTextView.setText("0.00");
        totalCarbsTextView.setText("0.00");
        totalFatTextView.setText("0.00");
    }

    private void saveFoodLog() {
        if (selectedFood == null || editTextGrams.getText().toString().isEmpty()) {
            Toast.makeText(this, "Seleccione un alimento e ingrese los gramos", Toast.LENGTH_SHORT).show();
            return;
        }

        int grams = Integer.parseInt(editTextGrams.getText().toString());

        FoodLog newLog = new FoodLog(userId, selectedFood.getId(), grams, selectDateUnformatted, mealTitleTextView.getText().toString());
        dbHelper.insertFoodLog(newLog);

        Toast.makeText(this, "Alimento agregado al historial", Toast.LENGTH_SHORT).show();
        finish(); // Cierra la actividad después de guardar
    }
}
