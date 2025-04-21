package com.example.gearfit_android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FoodCreateActivity extends AppCompatActivity {
    private EditText etFoodName, etCalories, etProtein, etCarbs, etFat;
    private Button btnSaveFood, btnCancelFood;

    private LinearLayout btnScanBarcode;

    private DatabaseHelper dbHelper;
    private int userId;

    private String mealTitle, selectDateUnformatted;

    private static final int REQUEST_CODE_SCAN = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_create);
        setupUI();

        dbHelper = new DatabaseHelper(this);

        etFoodName = findViewById(R.id.etFoodName);
        etCalories = findViewById(R.id.etCalories);
        etProtein = findViewById(R.id.etProtein);
        etCarbs = findViewById(R.id.etCarbs);
        etFat = findViewById(R.id.etFat);
        btnSaveFood = findViewById(R.id.btnSaveFood);
        btnCancelFood = findViewById(R.id.btnCancelFood);

        btnScanBarcode = findViewById(R.id.btnScanBarcode);

        // Limitar longitud de campos
        etFoodName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        etCalories.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        etFat.setFilters(new InputFilter[]{decimalDigitsInputFilter});
        etCarbs.setFilters(new InputFilter[]{decimalDigitsInputFilter});
        etProtein.setFilters(new InputFilter[]{decimalDigitsInputFilter});

        // Simulación de userId. Reemplaza esto con el valor real de la sesión o usuario actual.
        userId = getIntent().getIntExtra("userId", -1);
        mealTitle = getIntent().getStringExtra("currentMeal");
        selectDateUnformatted = getIntent().getStringExtra("currentMealDate");

        btnSaveFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createFood();
            }
        });

        btnCancelFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnScanBarcode.setOnClickListener(v -> {
            Intent intent = new Intent(this, BarcodeScannerActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SCAN);
        });
    }

    private void createFood() {
        String name = etFoodName.getText().toString().trim();
        String caloriesStr = etCalories.getText().toString().trim();
        String proteinStr = etProtein.getText().toString().trim();
        String carbsStr = etCarbs.getText().toString().trim();
        String fatStr = etFat.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(caloriesStr) ||
                TextUtils.isEmpty(proteinStr) || TextUtils.isEmpty(carbsStr) ||
                TextUtils.isEmpty(fatStr)) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int calories;
        double protein, carbs, fat;

        try {
            calories = Integer.parseInt(caloriesStr);
            protein = Double.parseDouble(proteinStr);
            carbs = Double.parseDouble(carbsStr);
            fat = Double.parseDouble(fatStr);

            if (calories < 0 || protein < 0 || carbs < 0 || fat < 0) {
                Toast.makeText(this, "Los valores no pueden ser negativos", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingrese valores numéricos válidos", Toast.LENGTH_SHORT).show();
            return;
        }

        Food food = new Food(0, userId, name, calories, protein, carbs, fat);

        if (dbHelper.addFood(food)) {
            Toast.makeText(this, "Alimento guardado exitosamente", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(FoodCreateActivity.this, FoodListActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("currentMeal", mealTitle);
            intent.putExtra("currentMealDate", selectDateUnformatted);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "El nombre del alimento ya existe", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN && resultCode == Activity.RESULT_OK) {
            String barcodeValue = data.getStringExtra("barcode");
            if (barcodeValue != null) {
                buscarAlimentoEnAPI(barcodeValue);
            }
        }
    }

    private void buscarAlimentoEnAPI(String barcode) {
        String url = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";

        new Thread(() -> {
            try {
                URL apiUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.has("product")) {
                    JSONObject product = jsonResponse.getJSONObject("product");

                    String name = product.optString("product_name", "Desconocido");
                    double calories = product.getJSONObject("nutriments").optDouble("energy-kcal_100g", 0);
                    double fat = product.getJSONObject("nutriments").optDouble("fat_100g", 0);
                    double carbs = product.getJSONObject("nutriments").optDouble("carbohydrates_100g", 0);
                    double protein = product.getJSONObject("nutriments").optDouble("proteins_100g", 0);

                    runOnUiThread(() -> {
                        etFoodName.setText(name);
                        etCalories.setText(String.valueOf(calories));
                        etFat.setText(String.valueOf(fat));
                        etCarbs.setText(String.valueOf(carbs));
                        etProtein.setText(String.valueOf(protein));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "No se encontró el producto", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private InputFilter decimalDigitsInputFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            String result = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());

            if (result.equals(".")) return "0.";

            if (result.contains(".")) {
                String[] parts = result.split("\\.");
                String integerPart = parts[0];
                String decimalPart = parts.length > 1 ? parts[1] : "";

                if (integerPart.length() > 3 || decimalPart.length() > 2) {
                    return "";
                }
            } else {
                if (result.length() > 3) {
                    return "";
                }
            }

            return null;
        }
    };

    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#7AB8FF"));
        getSupportActionBar().hide();
    }
}




