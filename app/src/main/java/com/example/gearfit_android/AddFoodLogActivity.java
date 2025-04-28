package com.example.gearfit_android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
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

    private LinearLayout editGramsLayout;
    private EditText editTextGrams;

    private TextView totalNutritionGramsTextView;
    private TextView totalKcalTextView, totalProteinTextView, totalCarbsTextView, totalFatTextView;
    private TextView totalKcalPer100TextView, totalProteinPer100TextView, totalCarbsPer100TextView, totalFatPer100TextView;
    private LinearLayout btnSaveFoodLog;

    // Alimento seleccionado
    private Food selectedFood;

    private DatabaseHelper dbHelper;

    // Elementos de la interfaz de usuario

    private ImageView backButton;
    private LinearLayout foodSelectionHeader;
    private TextView foodSelectionHeaderText;

    TextView warningGramsTextView;

    TextView btnSaveFoodTextView;

    private static final int MAX_GRAMS = 1000;
    private static final int WARNING_GRAMS = 500;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_add);
        setupUI();

        dbHelper = new DatabaseHelper(this);

        userId = getIntent().getIntExtra("userId", -1);
        String mealTitle = getIntent().getStringExtra("currentMeal");
        String selectedDate = getIntent().getStringExtra("currentMealDate");

        selectDateUnformatted = selectedDate;

        backButton = findViewById(R.id.buttonBack);
        mealTitleTextView = findViewById(R.id.currentMeal);
        selectedDateTextView = findViewById(R.id.currentMealDate);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddFoodLogActivity.this, FoodLogActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("currentMeal", mealTitleTextView.getText().toString());
                intent.putExtra("currentMealDate", selectDateUnformatted);

                startActivity(intent);
                finish();
            }
        });

        editGramsLayout = findViewById(R.id.editGramsLayout);
        editGramsLayout.setVisibility(View.GONE);

        editTextGrams = findViewById(R.id.editTextGrams);

        totalNutritionGramsTextView = findViewById(R.id.totalNutritionGramsTextView);
        warningGramsTextView = findViewById(R.id.warningGramsTextView);

        warningGramsTextView.setText("Selecciona un alimento para añadir!");
        warningGramsTextView.setTextColor(getResources().getColor(android.R.color.holo_red_light));

        totalKcalPer100TextView = findViewById(R.id.totalKcalPer100TextView);
        totalProteinPer100TextView = findViewById(R.id.totalProteinPer100TextView);
        totalCarbsPer100TextView = findViewById(R.id.totalCarbsPer100TextView);
        totalFatPer100TextView = findViewById(R.id.totalFatPer100TextView);

        totalKcalTextView = findViewById(R.id.totalKcalTextView);
        totalProteinTextView = findViewById(R.id.totalProteinTextView);
        totalCarbsTextView = findViewById(R.id.totalCarbsTextView);
        totalFatTextView = findViewById(R.id.totalFatTextView);

        btnSaveFoodLog = findViewById(R.id.btnSaveFoodLog);
        btnSaveFoodTextView = findViewById(R.id.btnSaveFoodTextView);

        btnSaveFoodLog.setOnTouchListener(new View.OnTouchListener() {
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

        // Mostrar la comida actual y la fecha formateada
        mealTitleTextView.setText(mealTitle);
        String formattedDate = formatDate(selectedDate);
        selectedDateTextView.setText(formattedDate);

        LinearLayout editGramsContainer = findViewById(R.id.editGramsContainer);

        // Al hacer clic en el LinearLayout, se activa el EditText
        editGramsContainer.setOnClickListener(v -> {
            editTextGrams.requestFocus();

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

        // Detectar cambios en los gramos y calcular en tiempo real
        editTextGrams.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    String input = s.toString();

                    // Eliminar ceros al inicio, pero permitir un solo "0" si es el único número
                    if (input.matches("^0\\d+")) {
                        input = input.replaceFirst("^0+", ""); // Elimina ceros iniciales
                        editTextGrams.setText(input);
                        editTextGrams.setSelection(input.length()); // Mantiene el cursor al final
                        return;
                    }

                    try {
                        int grams = Integer.parseInt(input);

                        // Ocultar o mostrar el botón según el valor de gramos
                        btnSaveFoodLog.setVisibility(grams > 0 ? View.VISIBLE : View.GONE);

                        // Actualizar el texto del botón con los gramos seleccionados
                        btnSaveFoodTextView.setText("Añadir " + grams + "g a " + mealTitleTextView.getText().toString());

                        if (grams >= MAX_GRAMS) {
                            editTextGrams.setText(String.valueOf(MAX_GRAMS));
                            editTextGrams.setSelection(editTextGrams.getText().length());
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
                        editTextGrams.setSelection(editTextGrams.getText().length());
                    }
                } else {
                    editTextGrams.setText("0");
                    editTextGrams.setSelection(editTextGrams.getText().length());
                }
                calculateNutrition();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


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

        foodSelectionHeader.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundColor(Color.parseColor("#505050"));
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.setBackgroundColor(Color.parseColor("#6D6D6D"));
                }
                return false; // Permite que el evento continúe propagándose (para que siga funcionando el OnClick)
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

                // Mostrar el campo de gramos
                editGramsLayout.setVisibility(View.VISIBLE);

                // Actualizar los valores nutricionales totales por cada 100 gramos
                totalKcalPer100TextView.setText(String.valueOf(selectedFood.getCalories()));
                totalProteinPer100TextView.setText((formatNumber(selectedFood.getProtein())) + "g");
                totalCarbsPer100TextView.setText((formatNumber(selectedFood.getCarbs())) + "g");
                totalFatPer100TextView.setText((formatNumber(selectedFood.getFat())) + "g");

                // Reiniciar el campo de gramos
                editTextGrams.setText("");

                // Reiniciar el mensaje de advertencias
                warningGramsTextView.setText("");
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
            totalNutritionGramsTextView.setText("0g");
            return;
        }

        int grams = Integer.parseInt(gramsStr);
        double factor = grams / 100.0;

        totalKcalTextView.setText(String.valueOf((int) (selectedFood.getCalories() * factor)));
        totalProteinTextView.setText((formatNumber(selectedFood.getProtein() * factor)) + "g");
        totalCarbsTextView.setText((formatNumber(selectedFood.getCarbs() * factor)) + "g");
        totalFatTextView.setText((formatNumber(selectedFood.getFat() * factor)) + "g");

        totalNutritionGramsTextView.setText((String.format(editTextGrams.getText() + "")) + "g");
    }

    private String formatNumber(double value) {
        if (value % 1 == 0) {
            return String.valueOf((int) value); // Si es un número entero, se muestra sin decimales
        } else {
            return String.format("%.2f", value); // Si tiene decimales, se muestra con dos decimales
        }
    }

    private void clearNutritionValues() {
        totalKcalTextView.setText("0");
        totalProteinTextView.setText("0g");
        totalCarbsTextView.setText("0g");
        totalFatTextView.setText("0g");
    }

    private void saveFoodLog() {
        if (selectedFood == null || editTextGrams.getText().toString().isEmpty()) {
            Toast.makeText(this, "Seleccione un alimento e ingrese los gramos", Toast.LENGTH_SHORT).show();
            return;
        }

        int grams = Integer.parseInt(editTextGrams.getText().toString());

        FoodLog newLog = new FoodLog(selectedFood.getId(), userId, grams, selectDateUnformatted, mealTitleTextView.getText().toString());
        Toast.makeText(this, "Guardando alimento del usuario " + userId + " " + newLog.getUserId(), Toast.LENGTH_SHORT).show();

        dbHelper.insertFoodLog(newLog);

        //Se realiza la devolucion aqui, ya que el log ya ha sido guardado.
        Intent intent = new Intent(AddFoodLogActivity.this, FoodLogActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("currentMeal", mealTitleTextView.getText().toString());
        intent.putExtra("currentMealDate", selectDateUnformatted);

        startActivity(intent);
        finish();
    }

    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#7AB8FF"));
        getSupportActionBar().hide();
    }
}
