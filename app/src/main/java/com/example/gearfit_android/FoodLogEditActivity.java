package com.example.gearfit_android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class FoodLogEditActivity extends AppCompatActivity {

    private int userId;
    private int foodLogId;
    private String meal, date;
    private EditText editTextGrams;
    private TextView textFoodName, textGrams, textFat, textCarbs, textProteins, textCalories;

    private TextView textFatPer100, textCarbsPer100, textProteinsPer100, textCaloriesPer100;
    private double fatPerGram, carbsPerGram, proteinPerGram, caloriesPerGram;
    private double originalQuantity;
    private double newQuantity;

    TextView warningGramsTextView;
    private LinearLayout buttonSaveFoodLog, buttonDeleteFoodLog;

    private static final int MAX_GRAMS = 1000;
    private static final int WARNING_GRAMS = 500;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_log_edit);
        setupUI();

        // Obtener referencias a los elementos de la interfaz
        textFoodName = findViewById(R.id.foodSelectedHeaderText);
        editTextGrams = findViewById(R.id.editTextGrams);
        editTextGrams.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        textGrams = findViewById(R.id.totalNutritionGramsTextView);
        textFat = findViewById(R.id.totalFatTextView);
        textCarbs = findViewById(R.id.totalCarbsTextView);
        textProteins = findViewById(R.id.totalProteinTextView);
        textCalories = findViewById(R.id.totalKcalTextView);

        textFatPer100 = findViewById(R.id.totalFatPer100TextView);
        textCarbsPer100 = findViewById(R.id.totalCarbsPer100TextView);
        textProteinsPer100 = findViewById(R.id.totalProteinPer100TextView);
        textCaloriesPer100 = findViewById(R.id.totalKcalPer100TextView);

        ImageView buttonBack = findViewById(R.id.buttonBack);
        buttonSaveFoodLog = findViewById(R.id.btnSaveFoodLog);
        buttonDeleteFoodLog = findViewById(R.id.btnDeleteFoodLog);

        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(FoodLogEditActivity.this, FoodLogActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("currentMeal", meal);
            intent.putExtra("currentMealDate", date);
            startActivity(intent);
            finish();
        });

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

        warningGramsTextView = findViewById(R.id.warningGramsTextView);

        textFatPer100.setText(formatNumber((totalFat / originalQuantity) * 100) + "g");
        textCarbsPer100.setText(formatNumber((totalCarbs / originalQuantity) * 100) + "g");
        textProteinsPer100.setText(formatNumber((totalProteins / originalQuantity) * 100) + "g");
        textCaloriesPer100.setText(formatToInteger((totalCalories / originalQuantity) * 100));

        // Calcular valores por gramo
        fatPerGram = totalFat / originalQuantity;
        carbsPerGram = totalCarbs / originalQuantity;
        proteinPerGram = totalProteins / originalQuantity;
        caloriesPerGram = totalCalories / originalQuantity;

        textFoodName.setText(foodName);
        editTextGrams.setText(formatNumber(originalQuantity));
        textGrams.setText(formatNumber(originalQuantity) + "g");

        // Actualizar valores nutricionales en la interfaz
        newQuantity = originalQuantity;
        updateNutritionalValues(newQuantity);

        LinearLayout editGramsContainer = findViewById(R.id.editGramsContainer);

        // Al hacer clic en el LinearLayout, se activa el EditText
        editGramsContainer.setOnClickListener(v -> {
            editTextGrams.requestFocus();

            // Mover el cursor al final del texto
            editTextGrams.setSelection(editTextGrams.getText().length());

            // Abrir el teclado
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(editTextGrams, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        editGramsContainer.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                editGramsContainer.setBackgroundResource(R.drawable.rounded_edit_pressed_layout);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                editGramsContainer.setBackgroundResource(R.drawable.rounded_edit_layout);
            }
            return false; // Dejar que otros eventos (como el clic) sigan funcionando
        });

        // Agregar TextWatcher para actualizar valores en tiempo real
        editTextGrams.addTextChangedListener(new TextWatcher() {
            private boolean isModifyingText = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Si isModifyingText es true, significa que el cambio de texto se está realizando internamente y no por el usuario. En ese caso, salimos del método para evitar el bucle.
                if (isModifyingText) return;

                // Indicamos que estamos a punto de modificar el texto internamente.
                isModifyingText = true;
                if (!s.toString().isEmpty()) {
                    String input = s.toString();

                    // Eliminar ceros al inicio, pero permitir un solo "0" si es el único número
                    if (input.matches("^0\\d+")) {
                        input = input.replaceFirst("^0+", ""); // Elimina ceros iniciales
                        editTextGrams.setText(input);
                        editTextGrams.setSelection(input.length()); // Mantiene el cursor al final
                        // Indicamos que hemos terminado de modificar el texto internamente.
                        isModifyingText = false;
                        return;
                    }

                    try {
                        // Muestra u oculta el botón de guardar según si hay texto o no.
                        buttonSaveFoodLog.setVisibility(editTextGrams.getText().toString().trim().length() > 0 ? View.VISIBLE : View.GONE);

                        double newQuantity = Double.parseDouble(s.toString());
                        updateNutritionalValues(newQuantity);

                        double grams = Double.parseDouble(editTextGrams.getText().toString().trim());

                        if (grams > 999) {
                            editTextGrams.setText(String.valueOf(MAX_GRAMS));
                            editTextGrams.setSelection(editTextGrams.getText().length());
                            updateNutritionalValues(MAX_GRAMS);
                            warningGramsTextView.setText("Máximo permitido: " + MAX_GRAMS + "g por temas de salud");
                            warningGramsTextView.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                        } else if (grams >= WARNING_GRAMS) {
                            warningGramsTextView.setText("Cuidado: " + WARNING_GRAMS + "g puede ser excesivo");
                            warningGramsTextView.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
                        } else {
                            warningGramsTextView.setText("");
                            warningGramsTextView.setTextColor(getResources().getColor(android.R.color.white));
                        }
                    } catch (NumberFormatException e) {
                        editTextGrams.setText("0");
                        updateNutritionalValues(originalQuantity);
                    }
                } else {
                    editTextGrams.setText("0");
                    updateNutritionalValues(originalQuantity);
                }
                // Indicamos que hemos terminado de modificar el texto internamente.
                isModifyingText = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        buttonSaveFoodLog.setOnClickListener(v -> saveChanges());

        buttonSaveFoodLog.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.rounded_add_pressed_button);
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.setBackgroundResource(R.drawable.rounded_add_button);
                }
                return false; // Permite que el evento continúe propagándose (para que siga funcionando el OnClick)
            }
        });

        buttonDeleteFoodLog.setOnClickListener(v -> {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            dbHelper.deleteFoodLog(foodLogId);

            Intent intent = new Intent(FoodLogEditActivity.this, FoodLogActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("currentMeal", meal);
            intent.putExtra("currentMealDate", date);
            startActivity(intent);
            finish();
        });

        buttonDeleteFoodLog.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.rounded_delete_pressed_button);
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.setBackgroundResource(R.drawable.rounded_delete_button);
                }
                return false; // Permite que el evento continúe propagándose (para que siga funcionando el OnClick)
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void updateNutritionalValues(double quantity) {
        textGrams.setText(formatNumber(quantity) + "g");
        textFat.setText(formatNumber(quantity * fatPerGram) + "g");
        textCarbs.setText(formatNumber(quantity * carbsPerGram) + "g");
        textProteins.setText(formatNumber(quantity * proteinPerGram) + "g");
        textCalories.setText(formatToInteger(quantity * caloriesPerGram));
    }

    private void saveChanges() {
        String newQuantityStr = editTextGrams.getText().toString().trim();

        if (!newQuantityStr.isEmpty()) {
            double newQuantity = Double.parseDouble(newQuantityStr);

            if (newQuantity == 0) {
                warningGramsTextView.setText("Introduce una cantidad válida");
                warningGramsTextView.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                return;
            }

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
            return String.format(Locale.US, "%.0f", number); // Entero sin decimales
        } else {
            return String.format(Locale.US, "%.2f", number); // Decimal con punto
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
