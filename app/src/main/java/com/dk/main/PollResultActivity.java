package com.dk.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.dk.App;
import com.dk.models.Thread;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_result);
        TextView textView = findViewById(R.id.topic);
        PieChart pieChart = findViewById(R.id.chart);
        long threadId = getIntent().getLongExtra("threadID", -1);
        Thread thread = threadBox.get(threadId);
        textView.setText(thread.getDialogName());
        List<PieEntry> entries = new ArrayList<>();
        String[] results = thread.getResultString().split(",");
        for (String result:results) {
            entries.add(new PieEntry(Float.parseFloat(result), ""));
        }
        List<LegendEntry> legend_entries = new ArrayList<>();
        ArrayList<String> options = thread.getOptionsList();
        int i = 0;
        for (String option:options) {
            LegendEntry l = new LegendEntry();
            l.label = option;
            l.formColor = ColorTemplate.MATERIAL_COLORS[i];
            legend_entries.add(l);
            i++;
        }
        Legend legend = pieChart.getLegend();
        legend.setCustom(legend_entries);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setDirection(LEFT_TO_RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setTextSize(15);
        legend.setWordWrapEnabled(true);
        PieDataSet set = new PieDataSet(entries, "");
        PieData data = new PieData(set);
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        pieChart.setData(data);
        pieChart.setCenterTextSize(0);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(0);
        pieChart.invalidate(); // refresh
    }
}
