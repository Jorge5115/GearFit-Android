package com.example.gearfit_android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Nombre y versión de la base de datos
    private static final String DATABASE_NAME = "gearfit.db";
    private static final int DATABASE_VERSION = 4;

    // Tabla Usuarios
    public static final String TABLE_USERS = "registered_users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    private static final String COLUMN_WEIGHT = "weight";
    private static final String COLUMN_HEIGHT = "height";
    private static final String COLUMN_KCAL_OBJECTIVE = "kcalObjective";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String TABLE_STEPS = "daily_steps";
    private static final String COLUMN_STEP_ID = "step_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_STEPS = "steps";

    // Definiciones para la tabla de alimentos
    private static final String TABLE_FOOD = "food";
    private static final String COLUMN_FOOD_ID = "food_id";
    private static final String COLUMN_FOOD_NAME = "name";
    private static final String COLUMN_FOOD_CALORIES = "calories";
    private static final String COLUMN_FOOD_PROTEIN = "protein";
    private static final String COLUMN_FOOD_CARBS = "carbs";
    private static final String COLUMN_FOOD_FAT = "fat";
    private static final String COLUMN_FOOD_USER_ID = "user_id";

    // Definiciones para la tabla de registros de alimentos
    private static final String TABLE_FOOD_LOG = "food_log";
    private static final String COLUMN_LOG_ID = "log_id";
    private static final String COLUMN_LOG_FOOD_ID = "food_id";
    private static final String COLUMN_LOG_USER_ID = "user_id";
    private static final String COLUMN_LOG_GRAMS = "grams";
    private static final String COLUMN_LOG_DATE = "date";

    private static final String COLUMN_LOG_MEAL_TYPE = "meal_type";


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
                COLUMN_PASSWORD + " TEXT NOT NULL, " +
                COLUMN_WEIGHT + " REAL DEFAULT 0, " +
                COLUMN_HEIGHT + " REAL DEFAULT 0, " +
                COLUMN_KCAL_OBJECTIVE + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_STEPS_TABLE = "CREATE TABLE " + TABLE_STEPS + " (" +
                COLUMN_STEP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " INTEGER, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_STEPS + " INTEGER, " +
                "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_STEPS_TABLE);

        String CREATE_FOOD_TABLE = "CREATE TABLE " + TABLE_FOOD + " (" +
                COLUMN_FOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FOOD_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_FOOD_NAME + " TEXT NOT NULL, " +
                COLUMN_FOOD_CALORIES + " INTEGER NOT NULL, " +
                COLUMN_FOOD_PROTEIN + " REAL NOT NULL, " +
                COLUMN_FOOD_CARBS + " REAL NOT NULL, " +
                COLUMN_FOOD_FAT + " REAL NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_FOOD_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_FOOD_TABLE);

        String CREATE_FOOD_LOG_TABLE = "CREATE TABLE " + TABLE_FOOD_LOG + " (" +
                COLUMN_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_LOG_FOOD_ID + " INTEGER NOT NULL, " +
                COLUMN_LOG_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_LOG_GRAMS + " REAL NOT NULL, " +
                COLUMN_LOG_DATE + " TEXT NOT NULL, " +
                COLUMN_LOG_MEAL_TYPE + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_LOG_FOOD_ID + ") REFERENCES " + TABLE_FOOD + "(" + COLUMN_FOOD_ID + "), " +
                "FOREIGN KEY (" + COLUMN_LOG_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_FOOD_LOG_TABLE);
    }


    // Actualizar la base de datos
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STEPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD_LOG);

        onCreate(db);
    }

    // Métodos CRUD
    public boolean addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, user.getUsername());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_WEIGHT, user.getWeight());
        values.put(COLUMN_HEIGHT, user.getHeight());
        values.put(COLUMN_KCAL_OBJECTIVE, user.getKcalObjective());

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public User getUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL, COLUMN_PASSWORD, COLUMN_WEIGHT, COLUMN_HEIGHT, COLUMN_KCAL_OBJECTIVE},
                COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();

            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
            user.setWeight(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT)));
            user.setHeight(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_HEIGHT)));
            user.setKcalObjective(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_KCAL_OBJECTIVE)));

            cursor.close();
            return user;
        }
        return null;
    }

    public User getUserById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL, COLUMN_PASSWORD, COLUMN_WEIGHT, COLUMN_HEIGHT, COLUMN_KCAL_OBJECTIVE},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();

            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
            user.setWeight(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT))); // Nuevo campo
            user.setHeight(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_HEIGHT))); // Nuevo campo
            user.setKcalObjective(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_KCAL_OBJECTIVE))); // Nuevo campo

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
