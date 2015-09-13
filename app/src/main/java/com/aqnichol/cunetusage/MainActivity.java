package com.aqnichol.cunetusage;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.text.DecimalFormat;


public class MainActivity extends ActionBarActivity {

    public static NubbClient currentClient = null;

    private TextView totalUsageField;
    private TextView freeUsageField;
    private TextView billableUsageField;
    private TextView billingRateField;
    private TextView totalChargeField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        totalUsageField = (TextView)findViewById(R.id.total_usage);
        freeUsageField = (TextView)findViewById(R.id.free_usage);
        billableUsageField = (TextView)findViewById(R.id.billable_usage);
        billingRateField = (TextView)findViewById(R.id.billing_rate);
        totalChargeField = (TextView)findViewById(R.id.total_charge);

        new FetchInfoTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String formatMegabytes(long size) {
        if (size > 1024) {
            DecimalFormat f = new DecimalFormat();
            f.setMinimumFractionDigits(2);
            return f.format((double)size / 1024.0) + " GiB";
        }
        return size + " MiB";
    }

    private class FetchInfoTask extends AsyncTask<Void, Void, NubbClient.GeneralMonthInfo> {
        @Override
        protected NubbClient.GeneralMonthInfo doInBackground(Void... params) {
            try {
                return currentClient.fetchGeneralMonthInfo();
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(NubbClient.GeneralMonthInfo result) {
            if (result == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.fetch_info_error);
                builder.setPositiveButton(R.string.ok, null);
                builder.create().show();
                return;
            }
            totalUsageField.setText(formatMegabytes(result.totalUsageMB));
            freeUsageField.setText(formatMegabytes(result.freeUsageMB));
            billableUsageField.setText(formatMegabytes(result.billableUsageMB));
            billingRateField.setText(result.billingRate);
            totalChargeField.setText(result.totalCharge);
        }
    }

}
