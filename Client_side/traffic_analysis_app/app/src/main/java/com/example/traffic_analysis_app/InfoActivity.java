package com.example.traffic_analysis_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.apache.commons.math3.stat.inference.TTest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class InfoActivity extends AppCompatActivity {

    BottomNavigationView bottom_navigation;
    boolean showToolbarMenu = false;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    private void removeOverflowMenu(Menu menu) {
        try {
            Field field = MenuBuilder.class.getDeclaredField("mOptionalIconsVisible");
            field.setAccessible(true);
            field.set(menu, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //adding the navigation bar to the layout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);

        if (!showToolbarMenu)
            return false;
        getMenuInflater().inflate(R.menu.bottom_navigation, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //defining the action that is done after navigation bar/toolbar item selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.recording) {
            Intent recording = new Intent(getApplicationContext(), RecordingActivity.class);
            startActivity(recording);
            finish();
        } else if(id == R.id.info) {
            //userInstructions(this);
            Intent data = new Intent(getApplicationContext(), InfoActivity.class);
            startActivity(data);
            finish();
        } else if(id == R.id.settings){
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            finish();
        } else if(id == R.id.home){
            Intent home = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(home);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottom_navigation = findViewById(R.id.bottom_navigation);
        // Clear any selected item
        bottom_navigation.setOnNavigationItemSelectedListener(null);
        bottom_navigation.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottom_navigation.getMenu().size(); i++) {
            bottom_navigation.getMenu().getItem(i).setChecked(false);
        }
        bottom_navigation.getMenu().setGroupCheckable(0, true, true);
        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if(id == R.id.recording){
                    Intent recording = new Intent(getApplicationContext(), RecordingActivity.class);
                    startActivity(recording);
                    finish();
                } else if(id == R.id.home){
                    Intent home = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(home);
                    finish();
                }
                return false;
            }
        });

        /*TextView anovaRes = findViewById(R.id.anova_res);
        TextView tTestRes = findViewById(R.id.t_test_res);
        TextView wilcoxonRes = findViewById(R.id.wilcoxon_res);

        double[] group1 = {1.23, 2.34, 1.56, 2.78, 2.34};
        double[] group2 = {2.45, 3.56, 2.67, 3.78, 3.45};
        double[] group3 = {1.67, 1.78, 2.89, 2.34, 2.67};

        List<double[]> groups = new ArrayList<>();
        groups.add(group1);
        groups.add(group2);
        groups.add(group3);

        // Perform Levene's Test
        double levenePValue = performLeveneTest(groups);
        Log.d("LEVENESTATS", "Levene's Test P-value: " + levenePValue);
        anovaRes.append("\nLevene's Test P-value: " + levenePValue);

        // Prepare data for Mauchly's Test
        double[][] data = {
                {1.23, 2.45, 1.67},
                {2.34, 3.56, 1.78},
                {1.56, 2.67, 2.89},
                {2.78, 3.78, 2.34},
                {2.34, 3.45, 2.67}
        };

        // Perform Mauchly's Test
        double mauchlyPValue = performMauchlyTest(data);
        Log.d("MAUCHLYSTATS", "Mauchly's Test P-value: " + mauchlyPValue);

        if(levenePValue > 0.05 & mauchlyPValue > 0.05) {
            // Perform ANOVA
            performAnova(groups, anovaRes);
        } else {
            anovaRes.setText("Should be Friedman");
        }

        // Perform Paired T-Test
        performPairedTTest(group1, group2, tTestRes);

        // Perform Wilcoxon Signed-Rank Test
        performWilcoxonSignedRankTest(group1, group2, wilcoxonRes);*/
    }

    public static void performAnova(List<double[]> groups, TextView anovaRes) {
        OneWayAnova anova = new OneWayAnova();
        double pValue = anova.anovaPValue(groups);
        Log.d("ANOVASTATS","P-value: " + pValue);

        anovaRes.setText("P-value: " + pValue);

        // For detailed ANOVA results
        //org.apache.commons.math3.stat.inference.AnovaStats stats = anova.anovaStats(groups);
        //Log.d("STATS", "F-value: " + stats.getFValue());
        // System.out.println("P-value: " + stats.getPValue());
    }

    public static void performPairedTTest(double[] group1, double[] group2, TextView tTestRes) {
        TTest tTest = new TTest();
        double pValue = tTest.pairedTTest(group1, group2);
        Log.d("TTEST", "Paired T-Test P-value: " + pValue);

        tTestRes.setText("Paired T-Test P-value: " + pValue);

    }

    public static void performWilcoxonSignedRankTest(double[] group1, double[] group2, TextView wilcoxonRes) {
        // Wilcoxon Signed-Rank Test implementation (manual or using another library)
        double wilcoxonPValue = wilcoxonSignedRankTest(group1, group2);
        Log.d("WILCOXON", "Wilcoxon Signed-Rank Test P-value: " + wilcoxonPValue);

        wilcoxonRes.setText("Wilcoxon Signed-Rank Test P-value: " + wilcoxonPValue);
    }

    // Manual implementation or using another library
    public static double wilcoxonSignedRankTest(double[] group1, double[] group2) {
        // Calculate differences and ranks
        int n = group1.length;
        double[] differences = new double[n];
        double sumOfRanks = 0.0;

        for (int i = 0; i < n; i++) {
            differences[i] = group2[i] - group1[i];
        }

        // Calculate the sum of ranks for positive differences
        for (int i = 0; i < n; i++) {
            double absDiff = Math.abs(differences[i]);
            double rank = i + 1; // Simplified ranking, you may need a more sophisticated ranking
            if (differences[i] > 0) {
                sumOfRanks += rank;
            }
        }

        // Calculate the Wilcoxon statistic
        double wilcoxonStatistic = sumOfRanks;

        // For now, let's just return a placeholder p-value
        // You should replace this with an actual p-value calculation
        return wilcoxonStatistic / (n * (n + 1) / 2);
    }

    public static double performLeveneTest(List<double[]> groups) {
        int k = groups.size();  // Number of groups
        int N = 0;  // Total number of observations
        double grandMean = 0.0;
        double sumSquaredZ = 0.0;
        double sumSquaredGroupMeans = 0.0;
        double[] Z = new double[k];
        Mean mean = new Mean();

        for (double[] group : groups) {
            N += group.length;
        }

        for (int i = 0; i < k; i++) {
            double groupMean = mean.evaluate(groups.get(i));
            for (int j = 0; j < groups.get(i).length; j++) {
                Z[i] += Math.abs(groups.get(i)[j] - groupMean);
            }
            Z[i] /= groups.get(i).length;  // Mean of absolute deviations for group i
            sumSquaredZ += Z[i] * Z[i];
            sumSquaredGroupMeans += Z[i] * Z[i] * groups.get(i).length;
        }

        grandMean = mean.evaluate(Z);

        double numerator = (N - k) * sumSquaredGroupMeans - sumSquaredZ;
        numerator /= (k - 1);

        double denominator = sumSquaredZ - (N * grandMean * grandMean);
        denominator /= (N - k);

        double FValue = numerator / denominator;

        FDistribution fDist = new FDistribution(k - 1, N - k);
        return 1.0 - fDist.cumulativeProbability(FValue);
    }
    public static double performMauchlyTest(double[][] data) {
        int n = data.length;  // Number of samples
        int p = data[0].length;  // Number of repeated measures

        // Calculate covariance matrix
        double[][] covMatrix = new double[p][p];
        double[] means = new double[p];
        Mean meanCalculator = new Mean();

        // Calculate means
        for (int j = 0; j < p; j++) {
            double[] column = new double[n];
            for (int i = 0; i < n; i++) {
                column[i] = data[i][j];
            }
            means[j] = meanCalculator.evaluate(column);
        }

        // Calculate covariance matrix
        for (int i = 0; i < p; i++) {
            for (int j = 0; j < p; j++) {
                double cov = 0.0;
                for (int k = 0; k < n; k++) {
                    cov += (data[k][i] - means[i]) * (data[k][j] - means[j]);
                }
                covMatrix[i][j] = cov / (n - 1);
            }
        }

        // Calculate determinant and trace
        double determinant = covMatrix[0][0] * covMatrix[1][1] - covMatrix[0][1] * covMatrix[1][0];
        double trace = covMatrix[0][0] + covMatrix[1][1];

        // Calculate Mauchly's W statistic
        double W = Math.pow(determinant / Math.pow(trace / p, p), n / 2.0);

        // Calculate Chi-Square
        double chiSquare = -(n - 1) * (p + 1 - 2 / 3.0 * p) * Math.log(W);

        // Degrees of freedom
        int df = (p * (p - 1)) / 2;

        // Chi-square distribution
        ChiSquaredDistribution chiDist = new ChiSquaredDistribution(df);
        return 1.0 - chiDist.cumulativeProbability(chiSquare);
    }
}