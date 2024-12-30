package com.example.gearfit_android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Nombre y versión de la base de datos
    private static final String DATABASE_NAME = "gearfit.db";
    private static final int DATABASE_VERSION = 2;

    // Tabla Usuarios
    public static final String TABLE_USERS = "registered_users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String TABLE_STEPS = "daily_steps";
    private static final String COLUMN_STEP_ID = "step_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_STEPS = "steps";

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_PASSWORD + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_STEPS_TABLE = "CREATE TABLE " + TABLE_STEPS + " (" +
                COLUMN_STEP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " INTEGER, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_STEPS + " INTEGER, " +
                "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_STEPS_TABLE);
    }


    // Actualizar la base de datos
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Métodos CRUD
    public boolean addUser(User usuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, usuario.getUsername());
        values.put(COLUMN_EMAIL, usuario.getEmail());
        values.put(COLUMN_PASSWORD, usuario.getPassword());
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public User getUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL, COLUMN_PASSWORD},
                COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User usuario = new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3));
            cursor.close();
            return usuario;
        }
        return null;
    }

    public User getUserById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL, COLUMN_PASSWORD},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3));
            cursor.close();
            return user;
        }
        return null;
    }

    public void saveDailySteps(int userId, String date, int steps) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("DatabaseHelper", "Guardando pasos - userId: " + userId + ", date: " + date + ", steps: " + steps);

        // Verificar si ya existen registros para esta fecha
        Cursor cursor = db.rawQuery(
                "SELECT steps FROM daily_steps WHERE user_id = ? AND date = ?",
                new String[]{String.valueOf(userId), date}
        );

        if (cursor.moveToFirst()) {
            // Si existen, actualizamos los pasos
            db.execSQL(
                    "UPDATE daily_steps SET steps = ? WHERE user_id = ? AND date = ?",
                    new Object[]{steps, userId, date}
            );
            Log.d("DatabaseHelper", "Pasos actualizados para userId: " + userId + ", date: " + date);
        } else {
            // Si no existen, insertamos un nuevo registro
            db.execSQL(
                    "INSERT INTO daily_steps (user_id, date, steps) VALUES (?, ?, ?)",
                    new Object[]{userId, date, steps}
            );
            Log.d("DatabaseHelper", "Pasos insertados para userId: " + userId + ", date: " + date);
        }

        cursor.close();
        db.close();
    }

    public int getDailySteps(int userId, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT steps FROM daily_steps WHERE user_id = ? AND date = ?",
                new String[]{String.valueOf(userId), date}
        );

        int steps = 0;
        if (cursor.moveToFirst()) {
            steps = cursor.getInt(0);
        }
        Log.d("DatabaseHelper", "getDailySteps - userId: " + userId + ", date: " + date + ", steps: " + steps);

        cursor.close();
        db.close();
        return steps;
    }




}
