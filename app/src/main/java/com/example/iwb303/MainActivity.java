package com.example.iwb303;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "selected_theme";
    
    private RecyclerView recyclerView;
    private PurchaseAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // تطبيق الثيم المختار قبل onCreate
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int theme = prefs.getInt(KEY_THEME, 0);
        if (theme == 1) {
            setTheme(R.style.Theme_IWB303_Accent);
        } else {
            setTheme(R.style.Theme_IWB303);
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View mainView = findViewById(R.id.main_layout);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerViewPurchases);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        loadData();

        // زر إضافة مشتريات
        Button btnAdd = findViewById(R.id.btnAddPurchaseLabel);
        btnAdd.setOnClickListener(v -> {
            startActivity(intentWithTheme(AddPurchaseActivity.class));
        });

        // زر تغيير الثيم المباشر
        findViewById(R.id.btnThemePicker).setOnClickListener(v -> showThemeMenu(v));

        // زر القائمة الرئيسي (Menu)
        TextView btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> showMainMenu(v));
    }

    private Intent intentWithTheme(Class<?> cls) {
        return new Intent(MainActivity.this, cls);
    }

    private void showMainMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add(0, 1, 0, "Show all purchases");
        popup.getMenu().add(0, 2, 1, "Show purchases by category");
        popup.getMenu().add(0, 3, 2, "Show purchases by date");
        popup.getMenu().add(0, 4, 3, "Statistics");
        popup.getMenu().add(0, 5, 4, "Change Theme");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    loadData();
                    return true;
                case 2:
                    showCategoryMenu(v);
                    return true;
                case 3:
                    showDatePicker();
                    return true;
                case 4:
                    startActivity(intentWithTheme(StatisticsActivity.class));
                    return true;
                case 5:
                    showThemeMenu(v);
                    return true;
            }
            return false;
        });
        popup.show();
    }

    private void showCategoryMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add("Food");
        popup.getMenu().add("Cleaning Supplies");
        popup.getMenu().add("Personal Care");
        popup.getMenu().add("Baby Supplies");

        popup.setOnMenuItemClickListener(item -> {
            try {
                List<Purchase> filtered = dbHelper.getPurchasesByCategory(item.getTitle().toString());
                if (adapter != null) {
                    adapter.updateData(filtered);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popup.show();
    }

    private void showDatePicker() {
        if (dbHelper == null) dbHelper = new DatabaseHelper(this);
        
        final Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);

        try {
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
                try {
                    // استخدام Locale.US لضمان توافق الأرقام (0-9)
                    String selectedDate = String.format(Locale.US, "%02d/%02d/%d", day, month + 1, year);
                    
                    List<Purchase> filteredList = dbHelper.getPurchasesByDate(selectedDate);
                    
                    if (adapter != null) {
                        adapter.updateData(filteredList);
                        if (filteredList.isEmpty()) {
                            Toast.makeText(MainActivity.this, "لا توجد مشتريات لهذا التاريخ: " + selectedDate, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // في حال كان المحول فارغاً، نقوم بتحميل البيانات من جديد
                        loadData();
                    }
                } catch (Exception e) {
                    android.util.Log.e("MainActivity", "Filter error", e);
                    Toast.makeText(MainActivity.this, "خطأ أثناء الفلترة: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }, y, m, d);
            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "تعذر فتح نافذة التاريخ", Toast.LENGTH_SHORT).show();
        }
    }

    private void showThemeMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add(0, 0, 0, "Light Mode");
        popup.getMenu().add(0, 1, 1, "Dark Mode");
        popup.getMenu().add(0, 2, 2, "Accent Theme");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 0) {
                saveTheme(0);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (item.getItemId() == 1) {
                saveTheme(0);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else if (item.getItemId() == 2) {
                saveTheme(1);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            recreate();
            return true;
        });
        popup.show();
    }

    private void loadData() {
        List<Purchase> list = dbHelper.getAllPurchases();
        if (adapter == null) {
            adapter = new PurchaseAdapter(list);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(list);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void saveTheme(int theme) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(KEY_THEME, theme);
        editor.apply();
    }
}