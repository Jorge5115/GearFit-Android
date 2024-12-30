package com.example.gearfit_android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private Button loginButton;
    private TextView registerLink;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupUI();

        dbHelper = new DatabaseHelper(this);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        registerLink = findViewById(R.id.register_link);

        loginButton.setOnClickListener(v -> handleLogin());
        registerLink.setOnClickListener(v -> navigateToRegister());
    }

    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#000000"));
        getSupportActionBar().hide();
    }

    private void handleLogin() {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();

        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            showToast("Por favor, completa todos los campos");
            return;
        }

        User user = dbHelper.getUser(userEmail, userPassword);

        if (user != null) {
            showToast("Bienvenido " + user.getUsername());
            navigateToMainActivity(user.getId());
        } else {
            showToast("Usuario o contraseña incorrectos");
        }
    }

    private void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void navigateToMainActivity(int userId) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        User user = dbHelper.getUser (email.getText().toString().trim(), password.getText().toString().trim());
        intent.putExtra("userId", userId);
        intent.putExtra("username", user.getUsername()); // Agregar el username
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
