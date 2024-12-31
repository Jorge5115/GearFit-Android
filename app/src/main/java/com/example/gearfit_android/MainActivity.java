package com.example.gearfit_android;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;

    private int userId;
    private int initialStepCount;
    private boolean isInitialCountSet = false;

    private DatabaseHelper dbHelper;
    private TextView stepsTextView;
    private String currentDate;

    // Componentes para el encabezado
    private TextView usernameTextView; // Muestra el nombre del usuario
    private TextView currentDateTextView; // Muestra la fecha actual

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();

        // Obtener los datos del intent
        userId = getIntent().getIntExtra("userId", -1);
        String username = getIntent().getStringExtra("username");

        // Inicializar componentes
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepCounterSensor == null) {
            Toast.makeText(this, "El dispositivo no tiene sensor de pasos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        stepsTextView = findViewById(R.id.stepsTextView);
        dbHelper = new DatabaseHelper(this);

        // Obtener la fecha actual
        currentDate = getCurrentDate();

        usernameTextView = findViewById(R.id.usernameTextView);
        currentDateTextView = findViewById(R.id.currentDateTextView);

        // Configurar el nombre de usuario y la fecha
        usernameTextView.setText(username);
        currentDateTextView.setText(getCurrentFormattedDate());

        // Cargar el valor inicial de pasos desde SharedPreferences
        initialStepCount = getInitialStepCount(userId);
        if (initialStepCount > 0) {
            isInitialCountSet = true;
        } else {
            Log.d(TAG, "No se encontró un valor inicial para userId: " + userId);
        }

        // Mostrar los pasos guardados para el día actual
        int savedSteps = dbHelper.getDailySteps(userId, currentDate);
        stepsTextView.setText("Pasos hoy: " + savedSteps);
    }

    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#000000"));
        getSupportActionBar().hide();
    }

    // Método para obtener la fecha actual formateada
    private String getCurrentFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM, dd 'of' yyyy", Locale.ENGLISH);
        return sdf.format(new Date());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (stepCounterSensor != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int totalSteps = (int) event.values[0];
            Log.d(TAG, "totalSteps detectados por el sensor: " + totalSteps);

            // Obtener la fecha actual
            String todayDate = getCurrentDate();

            // Si el día cambió, reinicia el contador inicial para el nuevo día
            if (!currentDate.equals(todayDate)) {
                currentDate = todayDate; // Actualizar la fecha actual
                initialStepCount = totalSteps; // Reiniciar el contador inicial
                saveInitialStepCount(userId, initialStepCount); // Guardar en SharedPreferences
                dbHelper.saveDailySteps(userId, currentDate, 0); // Reiniciar los pasos del día a 0 en la base de datos
                Log.d(TAG, "Nuevo día detectado. Reseteando pasos.");
            }

            // Calcular los pasos de hoy
            if (!isInitialCountSet) {
                initialStepCount = totalSteps;
                saveInitialStepCount(userId, initialStepCount);
                isInitialCountSet = true;
                Log.d(TAG, "initialStepCount configurado: " + initialStepCount);
            }

            int stepsToday = totalSteps - initialStepCount;
            dbHelper.saveDailySteps(userId, currentDate, stepsToday);
            Log.d(TAG, "stepsToday calculados: " + stepsToday);

            stepsTextView.setText("Pasos hoy: " + stepsToday);

            // Calcular calorías quemadas
            double caloriesBurned = calculateCalories(stepsToday);

            // Calcular distancia recorrida (en km)
            double strideLength = 0.78; // Longitud promedio de zancada en metros
            double distanceInKm = (stepsToday * strideLength) / 1000; // Convertir metros a kilómetros

            // Actualizar texto en las tarjetas
            stepsTextView.setText(String.valueOf(stepsToday)); // Actualizar pasos
            TextView caloriesTextView = findViewById(R.id.caloriesTextView);
            caloriesTextView.setText(String.format("%.0f KC", caloriesBurned)); // Actualizar calorías

            TextView distanceTextView = findViewById(R.id.distanceTextView); // Asegúrate de tener este TextView en el layout
            distanceTextView.setText(String.format("%.2f km", distanceInKm)); // Actualizar distancia

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No necesitamos manejar esto en este caso
    }

    private void saveInitialStepCount(int userId, int initialStepCount) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        prefs.edit()
                .putInt("initialStepCount_" + userId, initialStepCount)
                .apply();
        Log.d(TAG, "initialStepCount guardado: " + initialStepCount + " para userId: " + userId);
    }

    private int getInitialStepCount(int userId) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int initialStepCount = prefs.getInt("initialStepCount_" + userId, 0);
        Log.d(TAG, "initialStepCount recuperado: " + initialStepCount + " para userId: " + userId);
        return initialStepCount;
    }

    private String getCurrentDate() {
        // Aquí puedes usar un método para obtener la fecha actual en formato "yyyy-MM-dd"
        return java.time.LocalDate.now().toString();
    }

    private double calculateCalories(int steps) {
        return steps * 0.04; // Ejemplo: 0.04 KC por paso
    }

}
