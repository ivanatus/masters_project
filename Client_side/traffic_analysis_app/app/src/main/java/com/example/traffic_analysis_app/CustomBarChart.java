package com.example.traffic_analysis_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.utils.Utils;

public class CustomBarChart extends BarChart {
    private Paint errorLinePaint;

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
        errorLinePaint.setColor(0xFFFF0000); // Red color for error lines
        errorLinePaint.setStrokeWidth(Utils.convertDpToPixel(2f)); // Increased line width for visibility
        Log.d("CustomBarChart", "In init");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("CustomBarChart", "In onDraw");
        drawErrorBars(canvas); // Ensure this is called
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        Log.d("CustomBarChart", "In dispatchDraw");
        drawErrorBars(canvas); // Ensure this is called
    }

    private void drawErrorBars(Canvas canvas) {
        Log.d("CustomBarChart", "In drawErrorBars");
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

        float barWidth = getBarData().getBarWidth();
        float space = barWidth / 2f;

        for (int i = 0; i < dataSet.getEntryCount(); i++) {
            BarEntry entry = dataSet.getEntryForIndex(i);
            float x = entry.getX();
            float y = entry.getY();
            float error = getErrorForIndex(i); // Ensure this returns a non-zero value

            RectF barRect = new RectF();
            getBarBounds(entry, barRect);
            float left = barRect.left;
            float right = barRect.right;
            float errorTop = y + error;
            float errorBottom = y - error; // Ensure this is within chart bounds

            // Draw the top error line
            canvas.drawLine(left, errorTop, right, errorTop, errorLinePaint);
            Log.d("CustomBarChart", "Top Error Line: " + errorTop);

            // Draw the bottom error line
            canvas.drawLine(left, errorBottom, right, errorBottom, errorLinePaint);
            Log.d("CustomBarChart", "Bottom Error Line: " + errorBottom);
        }
    }

    private float getErrorForIndex(int index) {
        // Implement your logic to get the error value for a given index
        Log.d("CustomBarChart", "In getErrorForIndex");
        return 10; // Example error value, replace with actual calculation
    }
}


/*package com.example.traffic_analysis_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Utils;

import java.text.AttributedCharacterIterator;

public class CustomBarChart extends BarChart {
    private Paint errorLinePaint;

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

    public void init(Context context, AttributeSet attrs) {
        errorLinePaint = new Paint();
        errorLinePaint.setStyle(Paint.Style.STROKE);
        errorLinePaint.setColor(0xFFFF0000); // Red color for error lines
        errorLinePaint.setStrokeWidth(Utils.convertDpToPixel(1f)); // Line width
        Log.d("CustomBarChart", "In init");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("CustomBarChart", "In onDraw");
        drawErrorBars(canvas); // Ensure error bars are drawn
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        Log.d("CustomBarChart", "In dispatchDraw");
        drawErrorBars(canvas); // Call this after the chart is drawn
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d("CustomBarChart", "In onAttachedToWindow");
        postInvalidate(); // Force a redraw to ensure drawing
    }


    private void drawErrorBars(Canvas canvas) {
        Log.d("CustomBarChart", "In drawErrorBars");
        BarData barData = getBarData();
        if (barData == null || barData.getDataSetCount() == 0) return;

        BarDataSet dataSet = (BarDataSet) barData.getDataSetByIndex(0); // Assuming the first dataset is the mean data
        if (dataSet == null) return;

        float barWidth = getBarData().getBarWidth();
        float space = barWidth / 2f;

        for (int i = 0; i < dataSet.getEntryCount(); i++) {
            BarEntry entry = dataSet.getEntryForIndex(i);
            float x = entry.getX();
            float y = entry.getY();
            float error = getErrorForIndex(i); // You need to implement this method to retrieve the error value

            RectF barRect = new RectF();
            getBarBounds(entry, barRect);
            float left = barRect.left;
            float right = barRect.right;
            float errorTop = y + error;
            float errorBottom = y - error; // Height of the bar minus deviation

            // Draw the top error line
            canvas.drawLine(left, errorTop, right, errorTop, errorLinePaint);
            Log.d("CustomBarChart", String.valueOf(errorTop));
            // Draw the bottom error line
            canvas.drawLine(left, errorBottom, right, errorBottom, errorLinePaint);
            Log.d("CustomBarChart", String.valueOf(errorBottom));
        }
    }

    private float getErrorForIndex(int index) {
        // Implement your logic to get the error value for a given index
        Log.d("CustomBarChart", "In getErrorForIndex");
        return 0; // Placeholder
    }
}*/