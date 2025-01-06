package com.example.gearfit_android;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {


    private int userId;

    private DatabaseHelper dbHelper;

    private TextView usernameTextView;
    private TextView emailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupUI();

        // Puedes recibir el userId si es necesario
        int userId = getIntent().getIntExtra("userId", -1);

        // Inicializar los componentes
        usernameTextView = findViewById(R.id.usernameTextView);
        emailTextView = findViewById(R.id.emailTextView);

        // Inicializar DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Cargar los datos del usuario
        loadUserData(userId);
    }

    private void loadUserData(int userId) {
        User user = dbHelper.getUserById(userId); // Obtener el usuario por ID
        if (user != null) {
            // Establecer los datos en los EditText
            usernameTextView.setText(user.getUsername());
            emailTextView.setText(user.getEmail());
        } else {
            Toast.makeText(this, "No se encontró el usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#CBE5FF"));
        getSupportActionBar().hide();
    }
}
