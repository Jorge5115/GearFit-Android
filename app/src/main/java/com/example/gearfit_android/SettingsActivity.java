package com.example.gearfit_android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

    private int userId;

    private DatabaseHelper dbHelper;

    private TextView usernameTextView, emailTextView;

    private EditText editUsername, editHeight, editWeight, editKcalObjective;

    private MaterialButton saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupUI();

        // Puedes recibir el userId si es necesario
        userId = getIntent().getIntExtra("userId", -1);

        // Inicializar los componentes
        usernameTextView = findViewById(R.id.usernameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        editUsername = findViewById(R.id.editUsername);
        editHeight = findViewById(R.id.editHeight);
        editWeight = findViewById(R.id.editWeight);
        editKcalObjective = findViewById(R.id.editKcalObjective);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });

        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveUserData());

        // Inicializar DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Cargar los datos del usuario
        loadUserData();
    }

    private void loadUserData() {
        User user = dbHelper.getUserById(userId); // Obtener el usuario por ID
        if (user != null) {
            // Establecer los datos en los EditText
            usernameTextView.setText(user.getUsername());
            emailTextView.setText(user.getEmail());
            editUsername.setText(user.getUsername());
            editHeight.setText(String.valueOf(user.getHeight()));
            editWeight.setText(String.valueOf(user.getWeight()));
            editKcalObjective.setText(String.valueOf(user.getKcalObjective()));
        } else {
            Toast.makeText(this, "No se encontró el usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserData() {
        String username = editUsername.getText().toString().trim();
        String heightStr = editHeight.getText().toString().trim();
        String weightStr = editWeight.getText().toString().trim();
        String kcalStr = editKcalObjective.getText().toString().trim();

        if (username.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty() || kcalStr.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        float height = Float.parseFloat(heightStr);
        float weight = Float.parseFloat(weightStr);
        int kcalObjective = Integer.parseInt(kcalStr);

        boolean updated = dbHelper.updateUserData(userId, username, height, weight, kcalObjective);
        if (updated) {
            Toast.makeText(this, "Datos actualizados correctamente.", Toast.LENGTH_SHORT).show();
            loadUserData();

            // Navegar a la actividad principal
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "El nombre de usuario ya está en uso.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#7AB8FF"));
        getSupportActionBar().hide();
    }
}
