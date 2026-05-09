package com.example.iwb303;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

//   استخدام SQLiteOpenHelper لإنشاء الجداول وإدارتها
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "PurchasesDB";
    private static final int DATABASE_VERSION = 1;

    // أسماء الجداول
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_PURCHASES = "purchases";

    // أعمدة جدول الفئات
    public static final String COL_CAT_ID = "id";
    public static final String COL_CAT_NAME = "name";

    // أعمدة جدول المشتريات
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
        // إنشاء جدول الفئات
        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " (" +
                COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CAT_NAME + " TEXT NOT NULL)");

        // إنشاء جدول المشتريات مع الربط بجدول الفئات
        db.execSQL("CREATE TABLE " + TABLE_PURCHASES + " (" +
                COL_PUR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PUR_ITEM_NAME + " TEXT NOT NULL, " +
                COL_PUR_CAT_ID + " INTEGER, " +
                COL_PUR_PRICE + " REAL, " +
                COL_PUR_QUANTITY + " INTEGER, " +
                COL_PUR_TOTAL_COST + " REAL, " +
                COL_PUR_DATE + " TEXT)");

        // إدخال بيانات أولية
        insertInitialData(db);
    }

    private void insertInitialData(SQLiteDatabase db) {
        long foodId = insertCategory(db, "Food");
        long cleaningId = insertCategory(db, "Cleaning Supplies");
        long personalId = insertCategory(db, "Personal Care");
        long babyId = insertCategory(db, "Baby Supplies");

        insertPurchase(db, "Tomatoes", foodId, 500, 2, "20/04/2026");
        insertPurchase(db, "Eggs", foodId, 1500, 1, "20/04/2026");
        insertPurchase(db, "Milk", foodId, 1200, 2, "21/04/2026");
        insertPurchase(db, "Floor Cleaner", cleaningId, 2500, 1, "22/04/2026");
        insertPurchase(db, "Shampoo", personalId, 3500, 1, "23/04/2026");
        insertPurchase(db, "Baby Powder", babyId, 2500, 1, "25/04/2026");
    }

    private long insertCategory(SQLiteDatabase db, String name) {
        ContentValues values = new ContentValues();
        values.put(COL_CAT_NAME, name);
        return db.insert(TABLE_CATEGORIES, null, values);
    }

    private void insertPurchase(SQLiteDatabase db, String name, long catId, double price, int qty, String date) {
        ContentValues values = new ContentValues();
        values.put(COL_PUR_ITEM_NAME, name);
        values.put(COL_PUR_CAT_ID, catId);
        values.put(COL_PUR_PRICE, price);
        values.put(COL_PUR_QUANTITY, qty);
        values.put(COL_PUR_TOTAL_COST, price * qty);
        values.put(COL_PUR_DATE, date);
        db.insert(TABLE_PURCHASES, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PURCHASES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        onCreate(db);
    }

    // ميثود لجلب كل المشتريات
    public List<Purchase> getAllPurchases() {
        return getPurchasesWithFilter(null, null);
    }

    // ميثود للفلترة حسب الفئة
    public List<Purchase> getPurchasesByCategory(String categoryName) {
        // نستخدم c.name للبحث في جدول الفئات المربوط
        return getPurchasesWithFilter("c." + COL_CAT_NAME + " = ?", new String[]{categoryName});
    }

    // ميثود للفلترة حسب التاريخ
    public List<Purchase> getPurchasesByDate(String date) {
        return getPurchasesWithFilter("p." + COL_PUR_DATE + " = ?", new String[]{date});
    }

    // ميثود عامة للبحث والترتيب
    private List<Purchase> getPurchasesWithFilter(String selection, String[] selectionArgs) {
        List<Purchase> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // استعلام لربط جدول المشتريات مع جدول الفئات
        String query = "SELECT p.*, c." + COL_CAT_NAME + 
                      " FROM " + TABLE_PURCHASES + " p " +
                      " JOIN " + TABLE_CATEGORIES + " c ON p." + COL_PUR_CAT_ID + " = c." + COL_CAT_ID;

        if (selection != null) {
            query += " WHERE " + selection;
        }

        // الترتيب التنازلي حسب التاريخ (الأحدث أولا)
        query += " ORDER BY SUBSTR(p." + COL_PUR_DATE + ", 7, 4) DESC, " +
                 "SUBSTR(p." + COL_PUR_DATE + ", 4, 2) DESC, " +
                 "SUBSTR(p." + COL_PUR_DATE + ", 1, 2) DESC";

        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // استخراج البيانات من الكرسر
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PUR_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PUR_ITEM_NAME));
                int catId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PUR_CAT_ID));
                String catName = cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PUR_PRICE));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PUR_QUANTITY));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PUR_TOTAL_COST));
                String pDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_PUR_DATE));

                list.add(new Purchase(id, name, catId, catName, price, qty, total, pDate));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // ميثود لإضافة طلب شراء جديد
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

    // ميثود لحذف طلب شراء
    public void deletePurchase(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PURCHASES, COL_PUR_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // ميثود لتعديل طلب شراء
    public int updatePurchase(Purchase p) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_PUR_ITEM_NAME, p.getItemName());
        v.put(COL_PUR_CAT_ID, p.getCategoryId());
        v.put(COL_PUR_PRICE, p.getPrice());
        v.put(COL_PUR_QUANTITY, p.getQuantity());
        v.put(COL_PUR_TOTAL_COST, p.getTotalCost());
        v.put(COL_PUR_DATE, p.getDate());
        return db.update(TABLE_PURCHASES, v, COL_PUR_ID + " = ?", new String[]{String.valueOf(p.getId())});
    }

    // ميثود لجلب كل أسماء الفئات لتعبئة القوائم
    public List<String> getAllCategoryNames() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_CAT_NAME + " FROM " + TABLE_CATEGORIES, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // جلب المعرف للفئة أو إنشاؤه إذا لم يوجد (لشاشة الإضافة)
    public int getOrCreateCategoryId(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{COL_CAT_ID}, COL_CAT_NAME + "=?", new String[]{name}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        ContentValues v = new ContentValues();
        v.put(COL_CAT_NAME, name);
        return (int) db.insert(TABLE_CATEGORIES, null, v);
    }

    // ميثود مخصصة لجلب أسماء المواد بناء على الفئة (للتكملة التلقائية)
    public List<String> getItemsByCategory(String categoryName) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT DISTINCT p." + COL_PUR_ITEM_NAME + 
                      " FROM " + TABLE_PURCHASES + " p " +
                      " JOIN " + TABLE_CATEGORIES + " c ON p." + COL_PUR_CAT_ID + " = c." + COL_CAT_ID +
                      " WHERE c." + COL_CAT_NAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{categoryName});
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // حساب إجمالي المصاريف للشاشة الإحصائية
    public double getTotalExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COL_PUR_TOTAL_COST + ") FROM " + TABLE_PURCHASES, null);
        double total = 0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }

    // حساب الإجمالي حسب الفئة
    public double getTotalByCategory(String categoryName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(p." + COL_PUR_TOTAL_COST + ") FROM " + TABLE_PURCHASES + 
                      " p JOIN " + TABLE_CATEGORIES + " c ON p." + COL_PUR_CAT_ID + " = c." + COL_CAT_ID + 
                      " WHERE c." + COL_CAT_NAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{categoryName});
        double total = 0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }
}
