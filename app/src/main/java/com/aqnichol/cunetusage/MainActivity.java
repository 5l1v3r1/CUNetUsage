package com.aqnichol.cunetusage;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity {

    private MonthOverview monthOverview;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

        monthOverview = (MonthOverview)getFragmentManager().findFragmentById(R.id.month_fragment);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            monthOverview.setClient((NubbClient)extras.get("client"));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            finish();
            return true;
        } else if (id == R.id.action_refresh) {
            monthOverview.loadMonth();
        }
        return super.onOptionsItemSelected(item);
    }

}
