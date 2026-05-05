package com.example.iwb303;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "PurchasesDB";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_PURCHASES = "purchases";

    public static final String COL_CAT_ID = "id";
    public static final String COL_CAT_NAME = "name";
    public static final String COL_CAT_DESC = "description";

    public static final String COL_PUR_ID = "id";
    public static final String COL_PUR_ITEM_NAME = "item_name";
    public static final String COL_PUR_CAT_ID = "category_id";
    public static final String COL_PUR_PRICE = "price";
    public static final String COL_PUR_QUANTITY = "quantity";
    public static final String COL_PUR_TOTAL_COST = "total_cost";
    public static final String COL_PUR_DATE = "date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " (" +
                COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CAT_NAME + " TEXT NOT NULL, " +
                COL_CAT_DESC + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_PURCHASES + " (" +
                COL_PUR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PUR_ITEM_NAME + " TEXT NOT NULL, " +
                COL_PUR_CAT_ID + " INTEGER, " +
                COL_PUR_PRICE + " REAL, " +
                COL_PUR_QUANTITY + " INTEGER, " +
                COL_PUR_TOTAL_COST + " REAL, " +
                COL_PUR_DATE + " TEXT)");

        insertInitialData(db);
    }

    private void insertInitialData(SQLiteDatabase db) {
        long foodId = insertCategory(db, "Food", "Nutrition");
        long cleaningId = insertCategory(db, "Cleaning Supplies", "Household cleaning");
        long personalId = insertCategory(db, "Personal Care", "Hygiene");
        long babyId = insertCategory(db, "Baby Supplies", "Baby products");

        insertPurchase(db, "Tomatoes", foodId, 500, 2, "20/05/2024");
        insertPurchase(db, "Potatoes", foodId, 400, 3, "20/05/2024");
        insertPurchase(db, "Eggs", foodId, 1500, 1, "20/05/2024");
        insertPurchase(db, "Milk", foodId, 1200, 2, "21/05/2024");
        insertPurchase(db, "Sugar", foodId, 800, 5, "21/05/2024");
        insertPurchase(db, "White Rice", foodId, 1000, 4, "21/05/2024");
        insertPurchase(db, "Floor Cleaner", cleaningId, 2500, 1, "22/05/2024");
        insertPurchase(db, "Dishwashing Liquid", cleaningId, 1800, 1, "22/05/2024");
        insertPurchase(db, "Glass Cleaner", cleaningId, 1500, 1, "22/05/2024");
        insertPurchase(db, "Shampoo", personalId, 3500, 1, "23/05/2024");
        insertPurchase(db, "Body Wash", personalId, 3000, 1, "23/05/2024");
        insertPurchase(db, "Soap", personalId, 500, 4, "23/05/2024");
        insertPurchase(db, "Hand Cream", personalId, 2000, 1, "24/05/2024");
        insertPurchase(db, "Baby Shampoo", babyId, 4000, 1, "24/05/2024");
        insertPurchase(db, "Baby Powder", babyId, 2500, 1, "25/05/2024");
        insertPurchase(db, "Baby Lotion", babyId, 4500, 1, "25/05/2024");
        insertPurchase(db, "Baby Wipes", babyId, 1200, 2, "25/05/2024");
    }

    private long insertCategory(SQLiteDatabase db, String name, String desc) {
        ContentValues v = new ContentValues();
        v.put(COL_CAT_NAME, name);
        v.put(COL_CAT_DESC, desc);
        return db.insert(TABLE_CATEGORIES, null, v);
    }

    private void insertPurchase(SQLiteDatabase db, String name, long catId, double price, int qty, String date) {
        ContentValues v = new ContentValues();
        v.put(COL_PUR_ITEM_NAME, name);
        v.put(COL_PUR_CAT_ID, catId);
        v.put(COL_PUR_PRICE, price);
        v.put(COL_PUR_QUANTITY, qty);
        v.put(COL_PUR_TOTAL_COST, price * qty);
        v.put(COL_PUR_DATE, date);
        db.insert(TABLE_PURCHASES, null, v);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PURCHASES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        onCreate(db);
    }

    public List<Purchase> getAllPurchases() {
        return getPurchasesWithFilter(null, null);
    }

    public List<Purchase> getPurchasesByCategory(String categoryName) {
        return getPurchasesWithFilter("c.name = ?", new String[]{categoryName});
    }

    public List<Purchase> getPurchasesByDate(String date) {
        // حماية اسم العمود بالأقواس لتجنب تعارضه مع الكلمات المحجوزة
        return getPurchasesWithFilter("p.[" + COL_PUR_DATE + "] = ?", new String[]{date});
    }

    private List<Purchase> getPurchasesWithFilter(String selection, String[] selectionArgs) {
        List<Purchase> list = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            // استخدام أسماء مستعارة (AS) لكل الأعمدة لضمان عدم حدوث خطأ عند قراءة البيانات من Cursor
            String query = "SELECT " +
                    "p." + COL_PUR_ID + " AS res_id, " +
                    "p." + COL_PUR_ITEM_NAME + " AS res_name, " +
                    "p." + COL_PUR_CAT_ID + " AS res_cat_id, " +
                    "p." + COL_PUR_PRICE + " AS res_price, " +
                    "p." + COL_PUR_QUANTITY + " AS res_qty, " +
                    "p." + COL_PUR_TOTAL_COST + " AS res_total, " +
                    "p." + COL_PUR_DATE + " AS res_date, " +
                    "c." + COL_CAT_NAME + " AS res_cat_name" +
                    " FROM " + TABLE_PURCHASES + " p " +
                    " JOIN " + TABLE_CATEGORIES + " c ON p." + COL_PUR_CAT_ID + " = c." + COL_CAT_ID;

            if (selection != null) {
                query += " WHERE " + selection;
            }
            query += " ORDER BY p." + COL_PUR_ID + " DESC";

            cursor = db.rawQuery(query, selectionArgs);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(new Purchase(
                            cursor.getInt(cursor.getColumnIndexOrThrow("res_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("res_name")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("res_cat_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("res_cat_name")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("res_price")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("res_qty")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("res_total")),
                            cursor.getString(cursor.getColumnIndexOrThrow("res_date"))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error fetching purchases", e);
        } finally {
            if (cursor != null) cursor.close();
            // لا نغلق db هنا لأن getReadableDatabase قد يعيد نفس الكائن
        }
        return list;
    }

    public double getTotalExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(total_cost) FROM purchases", null);
        double total = 0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }

    public double getTotalByCategory(String categoryName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(p.total_cost) FROM purchases p JOIN categories c ON p.category_id = c.id WHERE c.name = ?";
        Cursor cursor = db.rawQuery(query, new String[]{categoryName});
        double total = 0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }

    public long addPurchase(Purchase p) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_PUR_ITEM_NAME, p.getItemName());
        v.put(COL_PUR_CAT_ID, p.getCategoryId());
        v.put(COL_PUR_PRICE, p.getPrice());
        v.put(COL_PUR_QUANTITY, p.getQuantity());
        v.put(COL_PUR_TOTAL_COST, p.getTotalCost());
        v.put(COL_PUR_DATE, p.getDate());
        return db.insert(TABLE_PURCHASES, null, v);
    }

    public int getOrCreateCategoryId(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{COL_CAT_ID}, COL_CAT_NAME + "=?", new String[]{name}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        if (cursor != null) cursor.close();

        ContentValues v = new ContentValues();
        v.put(COL_CAT_NAME, name);
        return (int) db.insert(TABLE_CATEGORIES, null, v);
    }
}
