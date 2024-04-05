package com.example.usagestatsjava;


import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;
import static android.os.Process.myUid;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);

        findViewById(R.id.fab).setOnClickListener(view -> {
            if (!checkForPermission()) {
                Log.i(TAG, "The user may not allow the access to apps usage. ");
                Toast.makeText(
                        this,
                        "Failed to retrieve app usage statistics. " +
                                "You may need to enable access for this app through " +
                                "Settings > Security > Apps with usage access",
                        Toast.LENGTH_LONG
                ).show();
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            } else {
                List<UsageStats> usageStats = getAppUsageStats();
                showAppUsageStats(usageStats);
            }
        });
    }

    private boolean checkForPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, myUid(), getPackageName());
        return mode == MODE_ALLOWED;
    }

    private void showAppUsageStats(List<UsageStats> usageStatsList) {
        usageStatsList.sort((left, right) -> Long.compare(right.getLastTimeUsed(), left.getLastTimeUsed()));

        List<String> listItems = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        for (UsageStats usageStats : usageStatsList) {
            String packageName = usageStats.getPackageName();
            String lastTimeUsed = dateFormat.format(new Date(usageStats.getLastTimeUsed()));
            long totalTimeInForeground = usageStats.getTotalTimeInForeground();
            String listItem = "Package Name: " + packageName +
                    "\nLast Time Used: " + lastTimeUsed +
                    "\nTotal Time in Foreground: " + totalTimeInForeground + " ms";
            listItems.add(listItem);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(adapter);
    }

    private List<UsageStats> getAppUsageStats() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);

        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long startTime = cal.getTimeInMillis();
        long endTime = System.currentTimeMillis();
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        return queryUsageStats;
    }



}