package com.aqnichol.cunetusage;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity {

    public static NubbClient currentClient = null;
    private MonthView monthView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        monthView = (MonthView)findViewById(R.id.main_month_view);

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

    private class FetchInfoTask extends AsyncTask<Void, Void, NubbClient.GeneralMonthInfo> {
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
        protected NubbClient.GeneralMonthInfo doInBackground(Void... params) {
            try {
                return currentClient.fetchGeneralMonthInfo();
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(NubbClient.GeneralMonthInfo result) {
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
