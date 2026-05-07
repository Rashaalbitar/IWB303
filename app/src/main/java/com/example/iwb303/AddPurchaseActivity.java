package com.example.iwb303;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.util.TypedValue;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AddPurchaseActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "selected_theme";

    private TextInputEditText etPrice, etQuantity, etDate;
    private AutoCompleteTextView etCategory, etItemName;
    private TextView tvActivityTitle;
    private Button btnSave;
    private ImageButton btnClose;
    private DatabaseHelper dbHelper;
    private int purchaseId = -1; // -1 تعني إضافة طلب جديد، أي قيمة أخرى تعني تعديل

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
        setContentView(R.layout.activity_add_purchase);

        View rootView = findViewById(R.id.add_purchase_root);
        if (rootView != null) {
            int padding16 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left + padding16, systemBars.top + padding16, systemBars.right + padding16, systemBars.bottom + padding16);
                return insets;
            });
        }

        dbHelper = new DatabaseHelper(this);

        etItemName = findViewById(R.id.etItemName);
        etCategory = findViewById(R.id.etCategory);
        etPrice = findViewById(R.id.etPrice);
        etQuantity = findViewById(R.id.etQuantity);
        etDate = findViewById(R.id.etDate);
        tvActivityTitle = findViewById(R.id.tvActivityTitle);
        btnSave = findViewById(R.id.btnSave);
        btnClose = findViewById(R.id.btnClose);

        // إعداد قائمة الفئات المنسدلة
        setupCategoryDropdown();

        // تحقق مما إذا كنا في وضع التعديل
        if (getIntent().hasExtra("PURCHASE_ID")) {
            purchaseId = getIntent().getIntExtra("PURCHASE_ID", -1);
            etItemName.setText(getIntent().getStringExtra("ITEM_NAME"));
            etCategory.setText(getIntent().getStringExtra("CATEGORY_NAME"), false);
            etPrice.setText(String.valueOf(getIntent().getDoubleExtra("PRICE", 0)));
            etQuantity.setText(String.valueOf(getIntent().getIntExtra("QUANTITY", 0)));
            etDate.setText(getIntent().getStringExtra("DATE"));
            
            tvActivityTitle.setText("Edit Purchase");
            btnSave.setText(R.string.save);
            etItemName.setEnabled(true); // تفعيل حقل المادة عند التعديل
        } else {
            tvActivityTitle.setText("Add Purchase");
        }

        // إعداد قائمة المواد بناءً على الفئة المختارة
        etCategory.setOnItemClickListener((parent, view, position, id) -> {
            SoundManager.getInstance(this).playClick();
            String selectedCategory = (String) parent.getItemAtPosition(position);
            updateItemDropdown(selectedCategory);
        });

        etItemName.setOnItemClickListener((parent, view, position, id) -> {
            SoundManager.getInstance(this).playClick();
        });

        // برمجة حقل التاريخ ليظهر تقويم MaterialDatePicker
        etDate.setInputType(InputType.TYPE_NULL);
        etDate.setFocusable(false);
        etDate.setClickable(true);
        etDate.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            showMaterialDatePicker();
        });

        // برمجة زر الإغلاق للعودة للصفحة الرئيسية
        btnClose.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            finish();
        });

        btnSave.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            String name = etItemName.getText().toString().trim();
            String categoryName = etCategory.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String quantityStr = etQuantity.getText().toString().trim();
            String date = etDate.getText().toString().trim();

            if (name.isEmpty() || categoryName.isEmpty() || priceStr.isEmpty() || quantityStr.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int quantity = Integer.parseInt(quantityStr);
                
                int categoryId = dbHelper.getOrCreateCategoryId(categoryName);
                Purchase purchase = new Purchase(name, categoryId, price, quantity, date);

                if (purchaseId == -1) {
                    // إضافة جديد
                    long id = dbHelper.addPurchase(purchase);
                    if (id > 0) {
                        Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    // تحديث موجود
                    // نحتاج لاستخدام constructor الـ ID للتعديل
                    Purchase updatedPurchase = new Purchase(purchaseId, name, categoryId, categoryName, price, quantity, price * quantity, date);
                    int rows = dbHelper.updatePurchase(updatedPurchase);
                    if (rows > 0) {
                        Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid price and quantity", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateItemDropdown(String categoryName) {
        List<String> items = dbHelper.getItemsByCategory(categoryName);
        if (items.isEmpty()) {
            etItemName.setText("");
            etItemName.setAdapter(null);
            etItemName.setEnabled(false);
            Toast.makeText(this, "No items found for: " + categoryName, Toast.LENGTH_SHORT).show();
        } else {
            etItemName.setEnabled(true);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, items);
            etItemName.setAdapter(adapter);
            // مسح النص القديم مع تعطيل الفلترة لضمان ظهور كل القائمة عند الضغط
            etItemName.setText("", false);
        }
    }

    private void setupCategoryDropdown() {
        List<String> categories = dbHelper.getAllCategoryNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        etCategory.setAdapter(adapter);
    }

    private void showMaterialDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Purchase Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SoundManager.getInstance(this).playClick();
            // تحويل التاريخ المختار إلى تنسيق DD/MM/YYYY
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            etDate.setText(sdf.format(calendar.getTime()));
        });

        datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
    }
}
