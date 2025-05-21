package com.example.gearfit_android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NutritionActivity extends AppCompatActivity {

    private int userId;

    private TextView textSelectedDateHeader;
    private MaterialCardView[] dayButtons;
    private TextView[] dayTexts;
    private Calendar[] dayDates;
    private MaterialCardView activeButton;
    private Calendar selectedDate;

    private ProgressBar progressCaloriesBar;
    private double totalCalories, totalFats, totalCarbohydrates, totalProteins;
    private LinearLayout layoutDailyRemainingCalories;
    private LinearLayout layoutRegisterDailyCalories, btnRegisterDailyCalories;
    private LinearLayout btnDesayuno, btnComida, btnMerienda, btnCena;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);
        setupUI();

        selectedDate = Calendar.getInstance();

        // Inicializa las vistas
        textSelectedDateHeader = findViewById(R.id.layoutSelectedDateHeader);
        layoutDailyRemainingCalories = findViewById(R.id.layoutDailyRemainingCalories);
        layoutRegisterDailyCalories = findViewById(R.id.layoutRegisterDailyCalories);
        btnRegisterDailyCalories = findViewById(R.id.btnRegisterDailyCalories);
        progressCaloriesBar = findViewById(R.id.progressCaloriesBar);

        showNutritionLogForDate(selectedDate);

        btnRegisterDailyCalories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NutritionActivity.this, SettingsActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            }
        });

        btnRegisterDailyCalories.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundColor(Color.parseColor("#A5251C"));
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.setBackgroundColor(Color.parseColor("#C2342A"));
                }
                return false;
            }
        });

        // Inicializa los botones
        btnDesayuno = findViewById(R.id.btnBreakfast);
        btnComida = findViewById(R.id.btnLunch);
        btnMerienda = findViewById(R.id.btnSnack);
        btnCena = findViewById(R.id.btnDinner);

        setupMealButtons();

        userId = getIntent().getIntExtra("userId", -1);

        dayButtons = new MaterialCardView[]{
                findViewById(R.id.cardMonday), findViewById(R.id.cardTuesday),
                findViewById(R.id.cardWednesday), findViewById(R.id.cardThursday),
                findViewById(R.id.cardFriday), findViewById(R.id.cardSaturday),
                findViewById(R.id.cardSunday)
        };

        dayTexts = new TextView[]{
                findViewById(R.id.textDayNumberMonday), findViewById(R.id.textDayNumberTuesday),
                findViewById(R.id.textDayNumberWednesday), findViewById(R.id.textDayNumberThursday),
                findViewById(R.id.textDayNumberFriday), findViewById(R.id.textDayNumberSaturday),
                findViewById(R.id.textDayNumberSunday)
        };
        setupDayButtons();
        populateDaysWithDates();
        setTodayButtonActive();

        checkAndShowKcalObjective();

        loadFoodLogs();
    }

    private void loadFoodLogs() {
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        // Reiniciar totales al cargar un nuevo día
        totalCalories = 0;
        totalFats = 0;
        totalCarbohydrates = 0;
        totalProteins = 0;

        // Inicializar las vistas
        TextView textTotalFats = findViewById(R.id.textTotalFats);
        TextView textTotalCarbohydrates = findViewById(R.id.textTotalCarbs);
        TextView textTotalProteins = findViewById(R.id.textTotalProteins);
        TextView textTotalCalories = findViewById(R.id.textTotalCalories);

        // Obtener los alimentos del desayuno
        List<FoodLog> foodBreakfastLogs = dbHelper.getFoodLogsByDateAndMeal(userId, formattedDate, "Desayuno");

        TextView textTotalCaloriesBreakfast = findViewById(R.id.textCaloriesBreakfast);
        TextView textCountBreakfast = findViewById(R.id.textSubtitleBreakfast);

        int totalCaloriesBreakfast = 0;
        for (FoodLog foodLog : foodBreakfastLogs) {

            double[] nutritionalValues = foodLog.getNutritionalValues(dbHelper);

            if (nutritionalValues != null) {
                totalCaloriesBreakfast += Math.round(nutritionalValues[3]);

                // Actualizar los valores totales
                totalFats += nutritionalValues[0];
                totalCarbohydrates += nutritionalValues[1];
                totalProteins += nutritionalValues[2];
                totalCalories += nutritionalValues[3];
            }
        }
        textTotalCaloriesBreakfast.setText(String.valueOf(totalCaloriesBreakfast));
        if (foodBreakfastLogs.size() == 1) {
            textCountBreakfast.setText(String.valueOf(foodBreakfastLogs.size() + " Alimento agregado"));
        } else {
            textCountBreakfast.setText(String.valueOf(foodBreakfastLogs.size() + " Alimentos agregados"));
        }

        // Obtener los alimentos de la cena
        List<FoodLog> foodLunchLogs = dbHelper.getFoodLogsByDateAndMeal(userId, formattedDate, "Comida");

        TextView textTotalCaloriesLunch = findViewById(R.id.textCaloriesLunch);
        TextView textCountLunch = findViewById(R.id.textSubtitleLunch);

        int totalCaloriesLunch = 0;
        for (FoodLog foodLog : foodLunchLogs) {

            double[] nutritionalValues = foodLog.getNutritionalValues(dbHelper);

            if (nutritionalValues != null) {
                totalCaloriesLunch += Math.round(nutritionalValues[3]);

                // Actualizar los valores totales
                totalFats += nutritionalValues[0];
                totalCarbohydrates += nutritionalValues[1];
                totalProteins += nutritionalValues[2];
                totalCalories += nutritionalValues[3];
            }
        }
        textTotalCaloriesLunch.setText(String.valueOf(totalCaloriesLunch));
        if (foodLunchLogs.size() == 1) {
            textCountLunch.setText(String.valueOf(foodLunchLogs.size() + " Alimento agregado"));
        } else {
            textCountLunch.setText(String.valueOf(foodLunchLogs.size() + " Alimentos agregados"));
        }

        // Obtener los alimentos de la merienda
        List<FoodLog> foodSnackLogs = dbHelper.getFoodLogsByDateAndMeal(userId, formattedDate, "Merienda");

        TextView textTotalCaloriesSnack = findViewById(R.id.textCaloriesSnack);
        TextView textCountSnack = findViewById(R.id.textSubtitleSnack);

        int totalCaloriesSnack = 0;
        for (FoodLog foodLog : foodSnackLogs) {

            double[] nutritionalValues = foodLog.getNutritionalValues(dbHelper);

            if (nutritionalValues != null) {
                totalCaloriesSnack += Math.round(nutritionalValues[3]);

                // Actualizar los valores totales
                totalFats += nutritionalValues[0];
                totalCarbohydrates += nutritionalValues[1];
                totalProteins += nutritionalValues[2];
                totalCalories += nutritionalValues[3];
            }
        }
        textTotalCaloriesSnack.setText(String.valueOf(totalCaloriesSnack));
        if (foodSnackLogs.size() == 1) {
            textCountSnack.setText(String.valueOf(foodSnackLogs.size() + " Alimento agregado"));
        } else {
            textCountSnack.setText(String.valueOf(foodSnackLogs.size() + " Alimentos agregados"));
        }

        // Obtener los alimentos de la cena
        List<FoodLog> foodDinnerLogs = dbHelper.getFoodLogsByDateAndMeal(userId, formattedDate, "Cena");

        TextView textTotalCaloriesDinner = findViewById(R.id.textCaloriesDinner);
        TextView textCountDinner = findViewById(R.id.textSubtitleDinner);

        int totalCaloriesDinner = 0;
        for (FoodLog foodLog : foodDinnerLogs) {

            double[] nutritionalValues = foodLog.getNutritionalValues(dbHelper);

            if (nutritionalValues != null) {
                totalCaloriesDinner += Math.round(nutritionalValues[3]);

                // Actualizar los valores totales
                totalFats += nutritionalValues[0];
                totalCarbohydrates += nutritionalValues[1];
                totalProteins += nutritionalValues[2];
                totalCalories += nutritionalValues[3];
            }
        }
        textTotalCaloriesDinner.setText(String.valueOf(totalCaloriesDinner));
        if (foodDinnerLogs.size() == 1) {
            textCountDinner.setText(String.valueOf(foodDinnerLogs.size() + " Alimento agregado"));
        } else {
            textCountDinner.setText(String.valueOf(foodDinnerLogs.size() + " Alimentos agregados"));
        }

        int kcalObjective = dbHelper.getKcalObjectiveByUserId(userId);
        if (kcalObjective > 0) {
            progressCaloriesBar.setMax(kcalObjective);
            progressCaloriesBar.setProgress((int) Math.round(totalCalories));

            textTotalFats.setText(String.valueOf(Math.round(totalFats)) + " gramos");
            textTotalCarbohydrates.setText(String.valueOf(Math.round(totalCarbohydrates)) + " gramos");
            textTotalProteins.setText(String.valueOf(Math.round(totalProteins)) + " gramos");
            textTotalCalories.setText(String.valueOf(Math.round(totalCalories)) + " de " + kcalObjective + " calorias diarias completadas");

            // Cambiar el color si se supera el objetivo
            if (totalCalories > kcalObjective) {
                progressCaloriesBar.setProgressDrawable(
                        getResources().getDrawable(R.drawable.progress_bar_over_calories)
                );
            } else {
                progressCaloriesBar.setProgressDrawable(
                        getResources().getDrawable(R.drawable.progress_bar_calories)
                );
            }
        }

    }

    public void checkAndShowKcalObjective() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        int kcalObjective = dbHelper.getKcalObjectiveByUserId(userId);

        if (kcalObjective == 0) {
            layoutDailyRemainingCalories.setVisibility(View.GONE);
            layoutRegisterDailyCalories.setVisibility(View.VISIBLE);
        } else {
            layoutDailyRemainingCalories.setVisibility(View.VISIBLE);
            layoutRegisterDailyCalories.setVisibility(View.GONE);
        }
    }


    private void setupMealButtons() {
        btnDesayuno.setOnClickListener(view -> onMealButtonClick("Desayuno"));
        btnComida.setOnClickListener(view -> onMealButtonClick("Comida"));
        btnMerienda.setOnClickListener(view -> onMealButtonClick("Merienda"));
        btnCena.setOnClickListener(view -> onMealButtonClick("Cena"));

        // Aplicar el efecto táctil a todos los botones
        applyTouchEffect(btnDesayuno);
        applyTouchEffect(btnComida);
        applyTouchEffect(btnMerienda);
        applyTouchEffect(btnCena);
    }

    private void applyTouchEffect(View button) {
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.rounded_nutrition_pressed_button);
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.setBackgroundResource(R.drawable.rounded_nutrition_button);
                }
                return false; // permite que se ejecute el onClick
            }
        });
    }

    private void onMealButtonClick(String meal) {
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());

        Intent intent = new Intent(NutritionActivity.this, FoodLogActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("currentMeal", meal);
        intent.putExtra("currentMealDate", formattedDate);
        startActivity(intent);
    }

    private void populateDaysWithDates() {
        Calendar today = Calendar.getInstance();
        int currentDayOfWeek = today.get(Calendar.DAY_OF_WEEK) - 1;
        if (currentDayOfWeek == 0)
            currentDayOfWeek = 7;  // Ajustar para que el Domingo sea el último botón

        dayDates = new Calendar[7];
        for (int i = 0; i < 7; i++) {
            Calendar date = (Calendar) today.clone();
            date.add(Calendar.DAY_OF_YEAR, i - currentDayOfWeek + 1);  // Obtener las fechas de esta semana
            dayDates[i] = date;
            dayTexts[i].setText(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
        }
    }

    private void setTodayButtonActive() {
        int todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;  // Ajustar para que salga el Lunes el primer botón

        if (todayIndex < 0) todayIndex = 6;
        highlightButton(todayIndex);
        showNutritionLogForDate(dayDates[todayIndex]);
    }

    private void setupDayButtons() {
        for (int i = 0; i < dayButtons.length; i++) {
            final int dayIndex = i;
            dayButtons[i].setOnClickListener(view -> {
                highlightButton(dayIndex);
                showNutritionLogForDate(dayDates[dayIndex]);
                loadFoodLogs();
            });
        }
    }

    private void highlightButton(int dayIndex) {
        selectedDate = dayDates[dayIndex];  // Actualiza la fecha seleccionada

        if (activeButton != null) {
            int previousIndex = java.util.Arrays.asList(dayButtons).indexOf(activeButton);
            if (previousIndex >= 0) {
                // Restaurar el color original de los TextView del botón previamente activo
                dayTexts[previousIndex].setTextColor(getResources().getColor(R.color.black)); // Restablecer color original
                TextView previousDayLabel = (TextView) ((LinearLayout) activeButton.getChildAt(0)).getChildAt(0);
                previousDayLabel.setTextColor(getResources().getColor(R.color.grey));
            }
            activeButton.setCardBackgroundColor(getResources().getColor(R.color.medium_grey));
            activeButton.setStrokeColor(Color.parseColor("#0018AF2B"));
        }

        activeButton = dayButtons[dayIndex];
        activeButton.setCardBackgroundColor(getResources().getColor(R.color.blue));
        activeButton.setStrokeColor(Color.parseColor("#0018AF2B"));

        // Cambiar el color de los TextView del nuevo botón activo
        dayTexts[dayIndex].setTextColor(getResources().getColor(R.color.white)); // Color para resaltar
        TextView dayLabel = (TextView) ((LinearLayout) activeButton.getChildAt(0)).getChildAt(0);
        dayLabel.setTextColor(getResources().getColor(R.color.white));
    }

    private void showNutritionLogForDate(Calendar date) {
        Locale spanish = new Locale("es", "ES");

        String dayOfWeek = new SimpleDateFormat("EEEE", spanish).format(date.getTime());
        String day = new SimpleDateFormat("d", spanish).format(date.getTime());
        String month = new SimpleDateFormat("MMMM", spanish).format(date.getTime());

        String formattedDate = String.format("%s %s de %s", capitalize(dayOfWeek), day, capitalize(month));

        if (textSelectedDateHeader != null) {
            textSelectedDateHeader.setText(formattedDate);
        }
    }

    // Para poner la primera letra en mayúscula (Lunes, Martes, etc.)
    private String capitalize(String input) {
        if (input == null || input.length() == 0) return "";
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }


    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#444444"));
        getSupportActionBar().hide();
    }

}
