package com.example.gearfit_android;

public class FoodLog {
    private int id;        // ID único de la entrada
    private int foodId;    // ID del alimento referenciado
    private int userId;    // ID del usuario
    private double grams;  // Cantidad consumida
    private String date;   // Fecha de consumo
    private String mealType;  // Tipo de comida: Breakfast, Lunch, Snack, Dinner

    // Constructor por defecto
    public FoodLog() {
    }

    // Constructor con todos los campos
    public FoodLog(int id, int foodId, int userId, double grams, String date, String mealType) {
        this.id = id;
        this.foodId = foodId;
        this.userId = userId;
        this.grams = grams;
        this.date = date;
        this.mealType = mealType;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getGrams() {
        return grams;
    }

    public void setGrams(double grams) {
        this.grams = grams;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }
}
