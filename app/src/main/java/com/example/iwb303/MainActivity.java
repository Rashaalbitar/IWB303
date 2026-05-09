package com.example.iwb303;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "selected_theme";
    
    private RecyclerView recyclerView;
    private View layoutEmptyState;
    private PurchaseAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // استرجاع الثيم المختار من الإعدادات قبل البدء
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int theme = prefs.getInt(KEY_THEME, 0);
        if (theme == 1) {
            setTheme(R.style.Theme_IWB303_Accent);
        } else {
            setTheme(R.style.Theme_IWB303);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ربط العناصر بواجهة المستخدم
        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerViewPurchases);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // تحميل البيانات وعرضها
        loadData();

        //  زر الإضافة الموجود فوق الجدول
        findViewById(R.id.btnAddPurchaseText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundManager.getInstance(MainActivity.this).playClick();
                Intent intent = new Intent(MainActivity.this, AddPurchaseActivity.class);
                startActivity(intent);
            }
        });

        // زر تغيير الثيم
        findViewById(R.id.btnThemePicker).setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            showThemeMenu(v);
        });

        // زر القائمة
        findViewById(R.id.btnMenu).setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            showMainMenu(v);
        });
    }

    // عرض القائمة الرئيسية (كل المشتريات، حسب الفئة، حسب التاريخ، الإحصائيات)
    private void showMainMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add(0, 1, 0, "Show all purchases");
        popup.getMenu().add(0, 2, 1, "Show purchases by category");
        popup.getMenu().add(0, 3, 2, "Show purchases by date");
        popup.getMenu().add(0, 4, 3, "Statistics");
        popup.getMenu().add(0, 5, 4, "Change Theme");

        popup.setOnMenuItemClickListener(item -> {
            SoundManager.getInstance(this).playClick();
            int id = item.getItemId();
            if (id == 1) {
                loadData();
            } else if (id == 2) {
                showCategoryMenu(v);
            } else if (id == 3) {
                showDatePicker();
            } else if (id == 4) {
                startActivity(new Intent(this, StatisticsActivity.class));
            } else if (id == 5) {
                showThemeMenu(v);
            }
            return true;
        });
        popup.show();
    }

    // عرض الفئات للفلترة
    private void showCategoryMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        List<String> categories = dbHelper.getAllCategoryNames();
        for (String cat : categories) {
            popup.getMenu().add(cat);
        }

        popup.setOnMenuItemClickListener(item -> {
            SoundManager.getInstance(this).playClick();
            List<Purchase> filtered = dbHelper.getPurchasesByCategory(item.getTitle().toString());
            updateUI(filtered);
            return true;
        });
        popup.show();
    }

    // اختيار تاريخ للفلترة
    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SoundManager.getInstance(this).playClick();
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            String date = sdf.format(calendar.getTime());

            List<Purchase> filtered = dbHelper.getPurchasesByDate(date);
            updateUI(filtered);
        });
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    // تحديث الجدول وإظهار رسالة إذا كان فارغا
    private void updateUI(List<Purchase> list) {
        if (adapter != null) {
            adapter.updateData(list);
            if (list.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                layoutEmptyState.setVisibility(View.GONE);
            }
        }
    }

    // تغيير شكل التطبيق (الثيم)
    private void showThemeMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add(0, 0, 0, "Light Mode");
        popup.getMenu().add(0, 1, 1, "Dark Mode");
        popup.getMenu().add(0, 2, 2, "Accent Theme");

        popup.setOnMenuItemClickListener(item -> {
            SoundManager.getInstance(this).playClick();
            int choice = item.getItemId();
            if (choice == 0) {
                saveTheme(0);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (choice == 1) {
                saveTheme(0);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else if (choice == 2) {
                saveTheme(1);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            recreate(); // إعادة تحميل الشاشة لتغيير الثيم
            return true;
        });
        popup.show();
    }

    // ميثود لجلب كل البيانات وتحديث الجدول
    private void loadData() {
        List<Purchase> list = dbHelper.getAllPurchases();
        if (adapter == null) {
            adapter = new PurchaseAdapter(list, (purchase, view) -> {
                SoundManager.getInstance(this).playClick();
                showActionMenu(purchase, view);
            });
            recyclerView.setAdapter(adapter);
        }
        updateUI(list);
    }

    // خيارات التعديل والحذف لكل عنصر
    private void showActionMenu(Purchase purchase, View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add(0, 1, 0, "Edit");
        popup.getMenu().add(0, 2, 1, "Delete");

        popup.setOnMenuItemClickListener(item -> {
            SoundManager.getInstance(this).playClick();
            if (item.getItemId() == 1) {
                Intent intent = new Intent(this, AddPurchaseActivity.class);
                intent.putExtra("PURCHASE_ID", purchase.getId());
                intent.putExtra("ITEM_NAME", purchase.getItemName());
                intent.putExtra("CATEGORY_NAME", purchase.getCategoryName());
                intent.putExtra("PRICE", purchase.getPrice());
                intent.putExtra("QUANTITY", purchase.getQuantity());
                intent.putExtra("DATE", purchase.getDate());
                startActivity(intent);
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Delete")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            dbHelper.deletePurchase(purchase.getId());
                            loadData();
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
            return true;
        });
        popup.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundManager.getInstance(this).release();
    }

    private void saveTheme(int theme) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putInt(KEY_THEME, theme).apply();
    }
}
