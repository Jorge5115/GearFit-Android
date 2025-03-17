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

    // Constructor sin el ID (para cuando insertamos un nuevo registro)
    public FoodLog(int foodId, int userId, double grams, String date, String mealType) {
        this.foodId = foodId;
        this.userId = userId;
        this.grams = grams;
        this.date = date;
        this.mealType = mealType;
    }

    // Métodos para obtener los valores nutricionales como un arreglo de valores
    public double[] getNutritionalValues(DatabaseHelper dbHelper) {
        // Obtener el alimento correspondiente al foodId
        Food food = dbHelper.getFoodById(this.foodId);
        if (food != null) {
            // Calcular los valores nutricionales en base a los gramos
            double totalFats = (food.getFat() * this.grams) / 100.0;
            double totalCarbs = (food.getCarbs() * this.grams) / 100.0;
            double totalProteins = (food.getProtein() * this.grams) / 100.0;
            double totalCalories = (food.getCalories() * this.grams) / 100.0;

            // Devolver un arreglo con los valores nutricionales
            return new double[]{totalFats, totalCarbs, totalProteins, totalCalories};
        }
        return null; // En caso de que no se encuentre el alimento
    }

    public String getFoodName(DatabaseHelper dbHelper) {
        Food food = dbHelper.getFoodById(this.foodId);
        if (food != null) {
            // Obtener el nombre del alimento
            String foodName = food.getName();

            // Devolver el nombre del alimento
            return new String (foodName);
        }
        return null; // En caso de que no se encuentre el alimento
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
