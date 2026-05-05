package com.example.iwb303;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        DatabaseHelper dbHelper = new DatabaseHelper(this);

        TextView tvTotal = findViewById(R.id.tvStatTotal);
        TextView tvFood = findViewById(R.id.tvStatFood);
        TextView tvCleaning = findViewById(R.id.tvStatCleaning);
        TextView tvPersonal = findViewById(R.id.tvStatPersonal);
        TextView tvBaby = findViewById(R.id.tvStatBaby);
        Button btnBack = findViewById(R.id.btnBack);

        double total = dbHelper.getTotalExpenses();
        double food = dbHelper.getTotalByCategory("Food");
        double cleaning = dbHelper.getTotalByCategory("Cleaning Supplies");
        double personal = dbHelper.getTotalByCategory("Personal Care");
        double baby = dbHelper.getTotalByCategory("Baby Supplies");

        tvTotal.setText(String.format(Locale.getDefault(), "%.2f SYP", total));
        tvFood.setText(String.format(Locale.getDefault(), "%.2f SYP", food));
        tvCleaning.setText(String.format(Locale.getDefault(), "%.2f SYP", cleaning));
        tvPersonal.setText(String.format(Locale.getDefault(), "%.2f SYP", personal));
        tvBaby.setText(String.format(Locale.getDefault(), "%.2f SYP", baby));

        btnBack.setOnClickListener(v -> finish());
    }
}