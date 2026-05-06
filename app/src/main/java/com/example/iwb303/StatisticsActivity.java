package com.example.iwb303;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private PieChart pieChart;

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
        pieChart = findViewById(R.id.pieChart);

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

        setupPieChart(food, cleaning, personal, baby);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupPieChart(double food, double cleaning, double personal, double baby) {
        List<PieEntry> entries = new ArrayList<>();
        if (food > 0) entries.add(new PieEntry((float) food, "Food"));
        if (cleaning > 0) entries.add(new PieEntry((float) cleaning, "Cleaning"));
        if (personal > 0) entries.add(new PieEntry((float) personal, "Personal"));
        if (baby > 0) entries.add(new PieEntry((float) baby, "Baby"));

        if (entries.isEmpty()) {
            pieChart.setNoDataText("No expenses recorded yet");
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expenses by Category");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setCenterText("Expenses");
        pieChart.setCenterTextSize(16f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }
}