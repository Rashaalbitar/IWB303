package com.example.iwb303;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "selected_theme";
    private HorizontalBarChart barChart;

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
        setContentView(R.layout.activity_statistics);

        View rootView = findViewById(R.id.statistics_root);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

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

        barChart = findViewById(R.id.barChart);
        setupBarChart(food, cleaning, personal, baby);

        btnBack.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            finish();
        });
    }

    private void setupBarChart(double food, double cleaning, double personal, double baby) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        if (food > 0) {
            entries.add(new BarEntry(index++, (float) food));
            labels.add("Food");
        }
        if (cleaning > 0) {
            entries.add(new BarEntry(index++, (float) cleaning));
            labels.add("Cleaning");
        }
        if (personal > 0) {
            entries.add(new BarEntry(index++, (float) personal));
            labels.add("Personal");
        }
        if (baby > 0) {
            entries.add(new BarEntry(index++, (float) baby));
            labels.add("Baby");
        }

        if (entries.isEmpty()) {
            barChart.setNoDataText("No data to compare");
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Expenses by Category");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        barChart.setData(data);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size());

        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }
}
