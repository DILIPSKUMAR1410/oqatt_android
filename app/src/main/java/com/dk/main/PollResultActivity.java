package com.dk.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dk.App;
import com.dk.models.Thread;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

import static com.github.mikephil.charting.components.Legend.LegendDirection.LEFT_TO_RIGHT;

public class PollResultActivity extends AppCompatActivity {
    Box<Thread> threadBox = App.getInstance().getBoxStore().boxFor(Thread.class);
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_result);
        PieChart pieChart = findViewById(R.id.chart);
        long threadId = getIntent().getLongExtra("threadID", -1);

        thread = threadBox.get(threadId);
        pieChart.setCenterText(thread.getDialogName());
        List<PieEntry> entries = new ArrayList<>();
        for (String option:thread.getOptionsList()) {
            entries.add(new PieEntry(25, option));

        }
//        entries.add(new PieEntry(18.5f, "Green"));
//        entries.add(new PieEntry(26.7f, "Yellow"));
//        entries.add(new PieEntry(24.0f, "Red"));
//        entries.add(new PieEntry(30.8f, "Blue"));
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setDirection(LEFT_TO_RIGHT);
        legend.setWordWrapEnabled(true);
        PieDataSet set = new PieDataSet(entries, "Election Results");
        PieData data = new PieData(set);
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        pieChart.setData(data);
//        pieChart.
        pieChart.invalidate(); // refresh
    }
}
