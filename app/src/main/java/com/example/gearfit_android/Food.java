package com.example.gearfit_android;

public class Food {
    private int id;        // ID del alimento
    private int userId;    // ID del usuario que ha registrado el alimento
    private String name;   // Nombre del alimento
    private int calories;  // Calorías por 100 gramos
    private double protein;  // Gramos de proteína por 100 gramos
    private double carbs;    // Gramos de carbohidratos por 100 gramos
    private double fat;      // Gramos de grasa por 100 gramos

    public Food() {
    }

    public Food(int id, int userId, String name, int calories, double protein, double carbs, double fat) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }
}
