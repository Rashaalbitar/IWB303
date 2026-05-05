package com.example.iwb303;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class AddPurchaseActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "selected_theme";

    private TextInputEditText etItemName, etCategory, etPrice, etQuantity, etDate;
    private Button btnSave;
    private ImageButton btnClose;
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
        setContentView(R.layout.activity_add_purchase);

        dbHelper = new DatabaseHelper(this);

        etItemName = findViewById(R.id.etItemName);
        etCategory = findViewById(R.id.etCategory);
        etPrice = findViewById(R.id.etPrice);
        etQuantity = findViewById(R.id.etQuantity);
        etDate = findViewById(R.id.etDate);
        btnSave = findViewById(R.id.btnSave);
        btnClose = findViewById(R.id.btnClose);

        // برمجة حقل التاريخ ليظهر نافذة اختيار التاريخ
        etDate.setFocusable(false);
        etDate.setClickable(true);
        etDate.setOnClickListener(v -> showDatePickerDialog());

        // برمجة زر الإغلاق للعودة للصفحة الرئيسية
        btnClose.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String name = etItemName.getText().toString().trim();
            String categoryName = etCategory.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String quantityStr = etQuantity.getText().toString().trim();
            String date = etDate.getText().toString().trim();

            if (name.isEmpty() || categoryName.isEmpty() || priceStr.isEmpty() || quantityStr.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "يرجى ملء جميع الحقول", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int quantity = Integer.parseInt(quantityStr);
                
                int categoryId = dbHelper.getOrCreateCategoryId(categoryName);
                Purchase purchase = new Purchase(name, categoryId, price, quantity, date);

                long id = dbHelper.addPurchase(purchase);
                if (id > 0) {
                    Toast.makeText(this, "تم الحفظ بنجاح", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "فشل في الحفظ", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "يرجى إدخال قيم صحيحة للسعر والكمية", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // استخدام الثيم المحدد في الـ Manifest أو المطبق برمجياً
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // استخدام Locale.US لضمان حفظ التاريخ بتنسيق أرقام موحد
                    String formattedDate = String.format(Locale.US, "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    etDate.setText(formattedDate);
                }, year, month, day);

        datePickerDialog.show();
    }
}
