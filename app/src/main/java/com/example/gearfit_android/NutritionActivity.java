package com.example.gearfit_android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NutritionActivity extends AppCompatActivity {

    private MaterialCardView[] dayButtons;
    private TextView[] dayTexts;
    private Calendar[] dayDates;
    private MaterialCardView activeButton;
    private int userId;

    private Calendar selectedDate;

    private LinearLayout btnDesayuno, btnComida, btnMerienda, btnCena;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);
        setupUI();

        selectedDate = Calendar.getInstance();

        // Inicializa los botones
        btnDesayuno = findViewById(R.id.btnDesayuno);
        btnComida = findViewById(R.id.btnComida);
        btnMerienda = findViewById(R.id.btnMerienda);
        btnCena = findViewById(R.id.btnCena);

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
    }

    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#f7f7f7"));
        getSupportActionBar().hide();
    }

    private void setupMealButtons() {
        btnDesayuno.setOnClickListener(view -> onMealButtonClick("Desayuno"));
        btnComida.setOnClickListener(view -> onMealButtonClick("Comida"));
        btnMerienda.setOnClickListener(view -> onMealButtonClick("Merienda"));
        btnCena.setOnClickListener(view -> onMealButtonClick("Cena"));
    }

    private void onMealButtonClick(String meal) {
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());

        Intent intent = new Intent(NutritionActivity.this, FoodListActivity.class);
        intent.putExtra("currentMeal", meal);
        intent.putExtra("currentMealDate", formattedDate);
        startActivity(intent);

    }

    private void populateDaysWithDates() {
        Calendar today = Calendar.getInstance();
        int currentDayOfWeek = today.get(Calendar.DAY_OF_WEEK) - 1;  // Adjust for array index
        if (currentDayOfWeek == 0) currentDayOfWeek = 7;  // Make Sunday the last day

        dayDates = new Calendar[7];
        for (int i = 0; i < 7; i++) {
            Calendar date = (Calendar) today.clone();
            date.add(Calendar.DAY_OF_YEAR, i - currentDayOfWeek + 1);  // Offset to get dates for this week
            dayDates[i] = date;
            dayTexts[i].setText(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
        }
    }

    private void setTodayButtonActive() {
        int todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;  // Adjust for Monday as first
        if (todayIndex < 0) todayIndex = 6;  // Handle Sunday
        highlightButton(todayIndex);
        showNutritionLogForDate(dayDates[todayIndex]);
    }

    private void setupDayButtons() {
        for (int i = 0; i < dayButtons.length; i++) {
            final int dayIndex = i;
            dayButtons[i].setOnClickListener(view -> {
                highlightButton(dayIndex);
                showNutritionLogForDate(dayDates[dayIndex]);
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
            activeButton.setCardBackgroundColor(getResources().getColor(R.color.light_grey));
            activeButton.setStrokeColor(Color.parseColor("#0018AF2B"));
        }

        activeButton = dayButtons[dayIndex];
        activeButton.setCardBackgroundColor(getResources().getColor(R.color.green));
        activeButton.setStrokeColor(Color.parseColor("#0018AF2B"));

        // Cambiar el color de los TextView del nuevo botón activo
        dayTexts[dayIndex].setTextColor(getResources().getColor(R.color.white)); // Color para resaltar
        TextView dayLabel = (TextView) ((LinearLayout) activeButton.getChildAt(0)).getChildAt(0);
        dayLabel.setTextColor(getResources().getColor(R.color.white));


    }


    private void showNutritionLogForDate(Calendar date) {
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.getTime());
        // TODO: Use formattedDate to fetch or store nutrition logs
    }
}
