package com.example.iwb303;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
                // بالنسبة للشاشة الرئيسية، نترك الهوامش الجانبية والسفلية للـ FAB والبطاقات،
                // ونضيف فقط Insets للنظام مع الحفاظ على التنسيق.
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerViewPurchases);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        loadData();

        // زر إضافة مشتريات
        View btnAdd = findViewById(R.id.btnAddPurchaseFab);
        btnAdd.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            startActivity(intentWithTheme(AddPurchaseActivity.class));
        });

        // زر تغيير الثيم المباشر
        findViewById(R.id.btnThemePicker).setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            showThemeMenu(v);
        });

        // زر القائمة الرئيسي (Menu)
        TextView btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            showMainMenu(v);
        });
    }

    private Intent intentWithTheme(Class<?> cls) {
        return new Intent(MainActivity.this, cls);
    }

    private void showMainMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        // استخدام معرفات واضحة بدلاً من الأرقام المجردة
        final int MENU_ALL = 1;
        final int MENU_CATEGORY = 2;
        final int MENU_DATE = 3;
        final int MENU_STATS = 4;
        final int MENU_THEME = 5;

        popup.getMenu().add(0, MENU_ALL, 0, "Show all purchases");
        popup.getMenu().add(0, MENU_CATEGORY, 1, "Show purchases by category");
        popup.getMenu().add(0, MENU_DATE, 2, "Show purchases by date");
        popup.getMenu().add(0, MENU_STATS, 3, "Statistics");
        popup.getMenu().add(0, MENU_THEME, 4, "Change Theme");

        popup.setOnMenuItemClickListener(item -> {
            SoundManager.getInstance(this).playClick();
            int id = item.getItemId();
            if (id == MENU_ALL) {
                loadData();
                return true;
            } else if (id == MENU_CATEGORY) {
                showCategoryMenu(v);
                return true;
            } else if (id == MENU_DATE) {
                showDatePicker();
                return true;
            } else if (id == MENU_STATS) {
                startActivity(intentWithTheme(StatisticsActivity.class));
                return true;
            } else if (id == MENU_THEME) {
                showThemeMenu(v);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showCategoryMenu(View v) {
        if (dbHelper == null) dbHelper = new DatabaseHelper(this);
        PopupMenu popup = new PopupMenu(this, v);
        
        // جلب الفئات الفعلية من قاعدة البيانات لضمان دقة الفلترة
        List<String> categories = dbHelper.getAllCategoryNames();
        if (categories.isEmpty()) {
            Toast.makeText(this, "No categories found in database", Toast.LENGTH_SHORT).show();
            return;
        }
        for (String cat : categories) {
            popup.getMenu().add(cat);
        }

        popup.setOnMenuItemClickListener(item -> {
            SoundManager.getInstance(this).playClick();
            try {
                String selectedCategory = item.getTitle().toString();
                android.util.Log.d("FilterDebug", "Filtering by category: [" + selectedCategory + "]");
                List<Purchase> filtered = dbHelper.getPurchasesByCategory(selectedCategory);
                android.util.Log.d("FilterDebug", "Found: " + filtered.size() + " items");
                
                if (adapter != null) {
                    adapter.updateData(filtered);
                    updateVisibility(filtered.isEmpty());
                    if (filtered.isEmpty()) {
                        Toast.makeText(this, "No purchases found for: " + selectedCategory, Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("FilterDebug", "Error filtering category", e);
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popup.show();
    }

    private void showDatePicker() {
        if (dbHelper == null) dbHelper = new DatabaseHelper(this);

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SoundManager.getInstance(this).playClick();
            try {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                String selectedDate = sdf.format(calendar.getTime());
                android.util.Log.d("FilterDebug", "Filtering by date: [" + selectedDate + "]");

                List<Purchase> filteredList = dbHelper.getPurchasesByDate(selectedDate);
                android.util.Log.d("FilterDebug", "Found: " + filteredList.size() + " items");

                if (adapter != null) {
                    adapter.updateData(filteredList);
                    updateVisibility(filteredList.isEmpty());
                    if (filteredList.isEmpty()) {
                        Toast.makeText(MainActivity.this, "No purchases found for: " + selectedDate, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    loadData();
                }
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Filter error", e);
                Toast.makeText(MainActivity.this, "Error during filtering: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        datePicker.show(getSupportFragmentManager(), "FILTER_DATE_PICKER");
    }

    private void updateVisibility(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void showThemeMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add(0, 0, 0, "Light Mode");
        popup.getMenu().add(0, 1, 1, "Dark Mode");
        popup.getMenu().add(0, 2, 2, "Accent Theme");

        popup.setOnMenuItemClickListener(item -> {
            SoundManager.getInstance(this).playClick();
            
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
            
            // تأخير بسيط لإعطاء فرصة لتشغيل الصوت قبل إعادة بناء الواجهة
            v.postDelayed(this::recreate, 100);
            return true;
        });
        popup.show();
    }

    private void loadData() {
        List<Purchase> list = dbHelper.getAllPurchases();
        updateVisibility(list.isEmpty());

        if (adapter == null) {
            adapter = new PurchaseAdapter(list, (purchase, view) -> {
                SoundManager.getInstance(this).playClick();
                showActionMenu(purchase, view);
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(list);
        }
    }

    private void showActionMenu(Purchase purchase, View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add(0, 1, 0, "Edit Purchase");
        popup.getMenu().add(0, 2, 1, "Delete Purchase");

        popup.setOnMenuItemClickListener(item -> {
            SoundManager.getInstance(this).playClick();
            if (item.getItemId() == 1) {
                // تعديل
                Intent intent = new Intent(MainActivity.this, AddPurchaseActivity.class);
                intent.putExtra("PURCHASE_ID", purchase.getId());
                intent.putExtra("ITEM_NAME", purchase.getItemName());
                intent.putExtra("CATEGORY_NAME", purchase.getCategoryName());
                intent.putExtra("PRICE", purchase.getPrice());
                intent.putExtra("QUANTITY", purchase.getQuantity());
                intent.putExtra("DATE", purchase.getDate());
                startActivity(intent);
                return true;
            } else if (item.getItemId() == 2) {
                // حذف - مع رسالة تأكيد
                new AlertDialog.Builder(this)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this purchase?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            dbHelper.deletePurchase(purchase.getId());
                            loadData();
                            Toast.makeText(this, "Purchase deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }
            return false;
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
        // تحرير موارد الصوت عند إغلاق التطبيق لمنع تسريب الذاكرة
        SoundManager.getInstance(this).release();
    }

    private void saveTheme(int theme) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(KEY_THEME, theme);
        editor.apply();
    }
}