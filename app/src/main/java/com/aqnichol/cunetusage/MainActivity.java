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

    private NubbClient currentClient;
    private MonthView monthView;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

        monthView = (MonthView)findViewById(R.id.main_month_view);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentClient = (NubbClient)extras.get("client");
        } else {
            Log.e("MainActivity", "started without extras");
            return;
        }

        if (state != null) {
            if (state.containsKey("currentMonth")) {
                monthView.setGeneralMonthInfo((GeneralMonthInfo)state.get("currentMonth"));
            }
        }

        if (monthView.getGeneralMonthInfo() == null) {
            new FetchInfoTask().execute();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        if (monthView.getGeneralMonthInfo() != null) {
            bundle.putParcelable("currentMonth", monthView.getGeneralMonthInfo());
        }
        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onDestroy() {
        new SessionSaver(this).clear();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
            new FetchInfoTask().execute();
        }
        return super.onOptionsItemSelected(item);
    }

    private class FetchInfoTask extends AsyncTask<Void, Void, GeneralMonthInfo> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(MainActivity.this);
            progress.setMessage(getString(R.string.fetching_info));
            progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(false);
                }
            });
            progress.show();
        }

        @Override
        protected GeneralMonthInfo doInBackground(Void... params) {
            try {
                return currentClient.fetchGeneralMonthInfo();
            } catch (IOException e) {
                Log.e("MainActivity", "fetching general month info failed: " + e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(GeneralMonthInfo result) {
            progress.dismiss();
            if (result == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.fetch_info_error);
                builder.setPositiveButton(R.string.ok, null);
                builder.create().show();
                return;
            } else {
                monthView.setGeneralMonthInfo(result);
            }
        }
    }

}
