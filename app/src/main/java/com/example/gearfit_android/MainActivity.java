package com.example.gearfit_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

    private ImageView userIcon;
    private TextView usernameTextView;
    private TextView currentDateTextView;

    private ImageView nutritionImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();

        // En tu onCreate o en un método adecuado
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
            }
        }

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

        // Configurar clic en userIcon
        userIcon = findViewById(R.id.userIcon);
        userIcon.setClickable(true);
        userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                intent.putExtra("userId", userId); // Pasar datos si es necesario
                startActivity(intent);
            }
        });

        // Configurar clic en nutritionImage
        nutritionImage = findViewById(R.id.nutritionImage);
        nutritionImage.setClickable(true);
        nutritionImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NutritionActivity.class);
                intent.putExtra("userId", userId); // Pasar datos si es necesario
                startActivity(intent);
            }
        });

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

    // Obtener la fecha actual formateada
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
                currentDate = todayDate;
                initialStepCount = totalSteps;
                saveInitialStepCount(userId, initialStepCount);
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

            // Asegúrate de que stepsToday no sea negativo
            if (stepsToday < 0) {
                stepsToday = 0; // Establecer a 0 si es negativo
                // También puedes considerar reiniciar initialStepCount aquí si es necesario
                initialStepCount = totalSteps; // Reiniciar el conteo inicial para evitar futuros negativos
                saveInitialStepCount(userId, initialStepCount);
            }

            dbHelper.saveDailySteps(userId, currentDate, stepsToday);
            Log.d(TAG, "stepsToday calculados: " + stepsToday);

            stepsTextView.setText("Pasos hoy: " + stepsToday);

            // Calcular calorías quemadas
            double caloriesBurned = calculateCalories(stepsToday);

            // Calcular distancia recorrida (en km)
            double strideLength = 0.78; // Longitud promedio de zancada en metros
            double distanceInKm = (stepsToday * strideLength) / 1000; // Convertir metros a kilómetros

            // Actualizar texto en las tarjetas
            stepsTextView.setText(String.valueOf(stepsToday));
            TextView caloriesTextView = findViewById(R.id.caloriesTextView);
            caloriesTextView.setText(String.format("%.0f KC", caloriesBurned));

            TextView distanceTextView = findViewById(R.id.distanceTextView);
            distanceTextView.setText(String.format("%.2f km", distanceInKm));
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
        // Aquí puedes obtener la fecha actual en formato "yyyy-MM-dd"
        return java.time.LocalDate.now().toString();
    }

    private double calculateCalories(int steps) {
        return steps * 0.04; // Ejemplo: 0.04 KC por paso
    }

}
