package com.example.traffic_analysis_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class CustomBarChart extends BarChart {
    private Paint errorLinePaint;
    private List<Float> deviations; // Store deviations

    public CustomBarChart(Context context) {
        super(context);
        init(context, null);
    }

    public CustomBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomBarChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        errorLinePaint = new Paint();
        errorLinePaint.setStyle(Paint.Style.STROKE);
        errorLinePaint.setColor(0xFF000000); // Red color for error lines
        errorLinePaint.setStrokeWidth(Utils.convertDpToPixel(1f)); // Increased line width for visibility
        deviations = new ArrayList<>(); // Initialize deviations list
    }

    public void setDeviations(List<Float> deviations) {
        this.deviations = deviations;
        invalidate(); // Refresh the chart when deviations are updated
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawErrorBars(canvas); // Ensure this is called
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawErrorBars(canvas); // Ensure this is called
    }

    public void updateChartData(BarData barData, List<Float> deviations) {
        setDeviations(deviations); // Update deviations
        setData(barData);
        adjustYAxis();
        invalidate(); // Refresh the chart view
    }

    private void adjustYAxis() {
        BarData barData = getBarData();
        if (barData == null || barData.getDataSetCount() == 0) {
            Log.d("CustomBarChart", "No barData or dataSet found");
            return;
        }

        BarDataSet dataSet = (BarDataSet) barData.getDataSetByIndex(0); // Assuming the first dataset is the mean data
        if (dataSet == null) {
            Log.d("CustomBarChart", "No dataSet found");
            return;
        }

        // Get the highest value in the dataset
        float maxEntryValue = getMaxEntryValue(dataSet);
        float yAxisMaxValue = maxEntryValue + 5; // Add 3 to the highest value

        // Set the y-axis maximum value
        YAxis leftAxis = getAxisLeft();
        leftAxis.setAxisMaximum(yAxisMaxValue);
        leftAxis.setAxisMinimum(0f); // Optional: Start y-axis from zero
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularity(1f); // Ensure that values are shown in steps of 1
        leftAxis.setGranularityEnabled(true);
        leftAxis.setDrawLabels(true);
        leftAxis.setLabelCount(5, true); // Enforces the number of labels displayed
    }

    private void drawErrorBars(Canvas canvas) {
        BarData barData = getBarData();
        if (barData == null || barData.getDataSetCount() == 0) {
            Log.d("CustomBarChart", "No barData or dataSet found");
            return;
        }

        BarDataSet dataSet = (BarDataSet) barData.getDataSetByIndex(0); // Assuming the first dataset is the mean data
        if (dataSet == null) {
            Log.d("CustomBarChart", "No dataSet found");
            return;
        }

        // Get the highest value in the dataset
        float maxEntryValue = getMaxEntryValue(dataSet);
        float yAxisMaxValue = maxEntryValue + 3; // Add 3 to the highest value

        // Set the y-axis maximum value
        YAxis leftAxis = getAxisLeft();
        leftAxis.setAxisMaximum(yAxisMaxValue);
        leftAxis.setAxisMinimum(0f); // Optional: Start y-axis from zero
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularity(1f); // Ensure that values are shown in steps of 1
        leftAxis.setGranularityEnabled(true);

        // Disable auto-calculation of the y-axis maximum
        leftAxis.setAxisMaximum(yAxisMaxValue);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawLabels(true);
        leftAxis.setLabelCount(5, true); // Enforces the number of labels displayed

        float barWidth = getBarData().getBarWidth();
        float space = barWidth / 2f;

        for (int i = 0; i < dataSet.getEntryCount(); i++) {
            BarEntry entry = dataSet.getEntryForIndex(i);
            float x = entry.getX();
            float y = entry.getY();
            float error = deviations.get(i); // Get the error value from deviations list

            // Get the bounds of the bar
            RectF barRect = new RectF();
            getBarBounds(entry, barRect);
            float barHeight = barRect.height(); // Use the bar height
            float barTop = barRect.top; // Top of the bar in the chart

            // Calculate scaling factor
            float yAxisMax = getAxisLeft().getAxisMaximum();
            float scaleFactor = getHeight() / yAxisMax; // Scaling factor for the error bar

            // Calculate the top and bottom positions of the error lines, scaled appropriately
            float top = barTop - error * scaleFactor;
            float bottom = barTop + error * scaleFactor;

            float middle = barRect.centerX();

            // Draw the top error line
            canvas.drawLine(barRect.left, top, barRect.right, top, errorLinePaint);

            // Draw the bottom error line
            canvas.drawLine(barRect.left, bottom, barRect.right, bottom, errorLinePaint);

            // Draw the vertical line connecting the top and bottom error lines
            canvas.drawLine(middle, top, middle, bottom, errorLinePaint);
        }
    }

    private float getMaxEntryValue(BarDataSet dataSet) {
        float max = Float.MIN_VALUE;
        for (int i = 0; i < dataSet.getEntryCount(); i++) {
            float value = dataSet.getEntryForIndex(i).getY();
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private float getErrorForIndex(int index) {
        // Implement your logic to get the error value for a given index
        Log.d("CustomBarChart", "In getErrorForIndex");
        return 10; // Example error value, replace with actual calculation
    }
}