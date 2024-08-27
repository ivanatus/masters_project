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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

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
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
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
        //androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

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

        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {

        private final String[] texts = new String[] {
                "Kako biste uspješno mogli koristiti aplikaciju, potrebno je imati uključenu GPS lokaciju te pristup internetu.\n" +
                "Aplikacija nema pristup trenutnim podacima o prometu, već o statistički vjerojatnim podacima.",
                "Hej",
                "hej2"
                // Add more texts here if needed
        };

        private final String[] titles = new String[] {
                "Upute za korištenje aplikacije",
                "Hej",
                "hej2"
                // Add more texts here if needed
        };

        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Create the fragment and pass the corresponding text
            return InfoFragment.newInstance(texts[position], titles[position]);
        }

        @Override
        public int getItemCount() {
            return texts.length;  // Number of fragments (equal to the number of texts)
        }
    }

}