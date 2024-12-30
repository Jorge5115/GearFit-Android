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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();

        // Obtener los datos del intent
        userId = getIntent().getIntExtra("userId", -1);
        String username = getIntent().getStringExtra("username");

        if (userId == -1 || username == null) {
            Toast.makeText(this, "Error: Usuario no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toast.makeText(this, "Bienvenido " + username, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Usuario logueado - userId: " + userId + ", username: " + username);

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
        }
    }



    private void checkForNewDay() {
        String today = getCurrentDate();
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String lastDate = prefs.getString("lastDate_" + userId, "");

        // Si ha cambiado el día
        if (!today.equals(lastDate)) {
            Log.d(TAG, "Nuevo día detectado: " + today);

            // Guardar la nueva fecha en SharedPreferences
            prefs.edit().putString("lastDate_" + userId, today).apply();

            // Resetear los pasos iniciales para el nuevo día
            initialStepCount = 0;
            saveInitialStepCount(userId, initialStepCount);

            // Guardar 0 pasos en la base de datos para el nuevo día
            dbHelper.saveDailySteps(userId, today, 0);

            // Actualizar la variable global de la fecha actual
            currentDate = today;

            Log.d(TAG, "Pasos reiniciados para el nuevo día: " + today);
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
}
