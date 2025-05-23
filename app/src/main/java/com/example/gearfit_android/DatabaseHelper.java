package com.example.gearfit_android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Nombre y versión de la base de datos
    private static final String DATABASE_NAME = "gearfit.db";
    private static final int DATABASE_VERSION = 5;

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

            if (cursor != null) {
                cursor.close();
            }
            db.close();
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


    public boolean addFood(Food food) {
        if (isFoodNameExists(food.getUserId(), food.getName())) {
            return false;  // Nombre de alimento ya existe
        }

        // Verificación de valores negativos
        if (food.getCalories() < 0 || food.getProtein() < 0 || food.getCarbs() < 0 || food.getFat() < 0) {
            return false;  // Datos no válidos
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FOOD_USER_ID, food.getUserId());
        values.put(COLUMN_FOOD_NAME, food.getName());
        values.put(COLUMN_FOOD_CALORIES, food.getCalories());
        values.put(COLUMN_FOOD_PROTEIN, food.getProtein());
        values.put(COLUMN_FOOD_CARBS, food.getCarbs());
        values.put(COLUMN_FOOD_FAT, food.getFat());

        long result = db.insert(TABLE_FOOD, null, values);
        db.close();
        return result != -1;
    }

    public boolean isFoodNameExists(int userId, String foodName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_FOOD + " WHERE " + COLUMN_FOOD_USER_ID + " = ? AND " + COLUMN_FOOD_NAME + " = ?",
                new String[]{String.valueOf(userId), foodName}
        );

        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        db.close();
        return exists;
    }

    public List<Food> getFoodsByUserId(int userId) {
        List<Food> foodList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_FOOD + " WHERE " + COLUMN_FOOD_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                Food food = new Food();
                food.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOOD_ID)));
                food.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOOD_USER_ID)));
                food.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOOD_NAME)));
                food.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOOD_CALORIES)));
                food.setProtein(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FOOD_PROTEIN)));
                food.setCarbs(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FOOD_CARBS)));
                food.setFat(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FOOD_FAT)));

                foodList.add(food);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return foodList;
    }

    public Food getFoodById(int foodId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Food food = null;

        String query = "SELECT * FROM " + TABLE_FOOD + " WHERE " + COLUMN_FOOD_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(foodId)});

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOOD_ID));
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOOD_USER_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOOD_NAME));
            int calories = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOOD_CALORIES));
            double proteins = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FOOD_PROTEIN));
            double carbs = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FOOD_CARBS));
            double fats = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FOOD_FAT));

            food = new Food(id, userId, name, calories, proteins, carbs, fats);
        }

        cursor.close();
        db.close();
        return food;
    }


    public void insertFoodLog(FoodLog foodLog) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOG_USER_ID, foodLog.getUserId()); // Usamos COLUMN_LOG_USER_ID
        values.put(COLUMN_LOG_FOOD_ID, foodLog.getFoodId()); // Usamos COLUMN_LOG_FOOD_ID
        values.put(COLUMN_LOG_GRAMS, foodLog.getGrams());    // Usamos COLUMN_LOG_GRAMS
        values.put(COLUMN_LOG_DATE, foodLog.getDate());      // Usamos COLUMN_LOG_DATE
        values.put(COLUMN_LOG_MEAL_TYPE, foodLog.getMealType()); // Usamos COLUMN_LOG_MEAL_TYPE

        // Insertamos en la tabla correcta: TABLE_FOOD_LOG
        db.insert(TABLE_FOOD_LOG, null, values);
        db.close();
    }

    public List<FoodLog> getFoodLogsByDateAndMeal(int userId, String date, String mealType) {
        List<FoodLog> foodLogs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Definir la consulta SQL para obtener los registros de alimentos de acuerdo con el userId, fecha y tipo de comida
        String query = "SELECT * FROM " + TABLE_FOOD_LOG +
                " WHERE " + COLUMN_LOG_USER_ID + " = ? AND " +
                COLUMN_LOG_DATE + " = ? AND " +
                COLUMN_LOG_MEAL_TYPE + " = ?";

        // Ejecutar la consulta con los parámetros proporcionados
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), date, mealType});

        // Verificar si la consulta devuelve resultados
        if (cursor.moveToFirst()) {
            do {
                // Crear un objeto FoodLog para cada registro obtenido
                FoodLog foodLog = new FoodLog();
                foodLog.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LOG_ID)));
                foodLog.setFoodId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LOG_FOOD_ID)));
                foodLog.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LOG_USER_ID)));
                foodLog.setGrams(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LOG_GRAMS)));
                foodLog.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_DATE)));
                foodLog.setMealType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_MEAL_TYPE)));

                // Agregar el objeto FoodLog a la lista
                foodLogs.add(foodLog);
            } while (cursor.moveToNext());
        }

        // Cerrar el cursor y la base de datos
        cursor.close();
        db.close();

        // Retornar la lista de FoodLogs
        return foodLogs;
    }

    public void updateFoodLogQuantity(int foodLogId, double newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOG_GRAMS, newQuantity); // Actualizar la cantidad de gramos

        int rowsAffected = db.update(TABLE_FOOD_LOG, values, COLUMN_LOG_ID + " = ?", new String[]{String.valueOf(foodLogId)});

        if (rowsAffected > 0) {
            Log.d("DatabaseHelper", "Cantidad actualizada en food_log para log_id: " + foodLogId);
        } else {
            Log.d("DatabaseHelper", "No se encontró el registro en food_log con log_id: " + foodLogId);
        }

        db.close();
    }


    public void deleteFoodLog(int foodLogId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_FOOD_LOG, COLUMN_LOG_ID + " = ?", new String[]{String.valueOf(foodLogId)});

        if (rowsDeleted > 0) {
            Log.d("DatabaseHelper", "Registro eliminado de food_log con log_id: " + foodLogId);
        } else {
            Log.d("DatabaseHelper", "No se encontró el registro en food_log con log_id: " + foodLogId);
        }

        db.close();
    }

    public boolean updateFood(Food food) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Verificar si ya existe otro alimento con el mismo nombre para el usuario
        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_FOOD_ID + " FROM " + TABLE_FOOD +
                        " WHERE " + COLUMN_FOOD_USER_ID + " = ? AND " +
                        COLUMN_FOOD_NAME + " = ? AND " +
                        COLUMN_FOOD_ID + " != ?",
                new String[]{
                        String.valueOf(food.getUserId()),
                        food.getName(),
                        String.valueOf(food.getId())
                }
        );

        boolean nameExists = cursor.moveToFirst();
        cursor.close();

        if (nameExists) {
            db.close();
            return false;  // Ya existe otro alimento con el mismo nombre
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_FOOD_NAME, food.getName());
        values.put(COLUMN_FOOD_CALORIES, food.getCalories());
        values.put(COLUMN_FOOD_PROTEIN, food.getProtein());
        values.put(COLUMN_FOOD_CARBS, food.getCarbs());
        values.put(COLUMN_FOOD_FAT, food.getFat());

        int rowsAffected = db.update(
                TABLE_FOOD,
                values,
                COLUMN_FOOD_ID + " = ? AND " + COLUMN_FOOD_USER_ID + " = ?",
                new String[]{String.valueOf(food.getId()), String.valueOf(food.getUserId())}
        );

        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteFood(int foodId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int rowsDeleted = db.delete(TABLE_FOOD, COLUMN_FOOD_ID + " = ?", new String[]{String.valueOf(foodId)});

        db.close();
        return rowsDeleted > 0;
    }


    public boolean isUsernameTaken(String username, int excludeUserId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE " + COLUMN_NAME + " = ? AND " + COLUMN_ID + " != ?",
                new String[]{username, String.valueOf(excludeUserId)}
        );

        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        db.close();
        return exists;
    }

    public boolean updateUserData(int userId, String username, float height, float weight, int kcalObjective) {
        if (isUsernameTaken(username, userId)) {
            return false; // Nombre ya tomado
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, username);
        values.put(COLUMN_HEIGHT, height);
        values.put(COLUMN_WEIGHT, weight);
        values.put(COLUMN_KCAL_OBJECTIVE, kcalObjective);

        int rowsAffected = db.update(TABLE_USERS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});
        db.close();

        return rowsAffected > 0;
    }

    public int getKcalObjectiveByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int kcalObjective = -1;

        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_KCAL_OBJECTIVE}, COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            kcalObjective = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_KCAL_OBJECTIVE));
            cursor.close();
        }

        db.close();
        return kcalObjective;
    }


    public String getUsernameById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_NAME + " FROM " + TABLE_USERS + " WHERE " + COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        String username = null;
        if (cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
        }

        cursor.close();
        db.close();
        return username;
    }

}
