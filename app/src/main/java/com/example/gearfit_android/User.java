package com.example.gearfit_android;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;

    private float weight;
    private float height;
    private int kcalObjective;

    // Constructor vacío
    public User() {
    }

    // Constructor con parámetros
    public User(int id, String username, String email, String password, float weight, float height, int kcalObjective) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.weight = weight;
        this.height = height;
        this.kcalObjective = kcalObjective;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public int getKcalObjective() {
        return kcalObjective;
    }

    public void setKcalObjective(int kcalObjective) {
        this.kcalObjective = kcalObjective;
    }
}
