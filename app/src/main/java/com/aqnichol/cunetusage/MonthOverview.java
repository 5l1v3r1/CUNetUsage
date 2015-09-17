package com.aqnichol.cunetusage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.text.DecimalFormat;


/**
 * MonthOverview shows general information about a given month.
 */
public class MonthOverview extends Fragment {
    private ProgressBar progressBar;
    private TextView errorView;
    private View mainContent;
    private TextView totalUsageField;
    private TextView freeUsageField;
    private TextView billableUsageField;
    private TextView billingRateField;
    private TextView totalChargeField;
    private NubbClient client = null;
    private GeneralMonthInfo monthInfo = null;
    private FetchInfoTask currentTask = null;

    static MonthOverview create(NubbClient client) {
        MonthOverview o = new MonthOverview();
        o.setArguments(new Bundle());
        o.getArguments().putParcelable("client", client);
        return o;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            client = (NubbClient)getArguments().get("client");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);

        progressBar = (ProgressBar)getView().findViewById(R.id.progress);
        errorView = (TextView)getView().findViewById(R.id.error_message);
        mainContent = getView().findViewById(R.id.month_main_content);
        totalUsageField = (TextView)getView().findViewById(R.id.total_usage);
        freeUsageField = (TextView)getView().findViewById(R.id.free_usage);
        billableUsageField = (TextView)getView().findViewById(R.id.billable_usage);
        billingRateField = (TextView)getView().findViewById(R.id.billing_rate);
        totalChargeField = (TextView)getView().findViewById(R.id.total_charge);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("monthInfo")) {
                monthInfo = (GeneralMonthInfo) savedInstanceState.get("monthInfo");
                showMonthInfo(monthInfo);
            }
        }
        if (monthInfo == null) {
            loadMonth();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_month_overview, container, false);
    }

    @Override
    public void onDestroy() {
        if (currentTask != null) {
            currentTask.cancel(false);
        }
        super.onDestroy();
    }

    public NubbClient getClient() {
        return client;
    }

    public void setClient(NubbClient c) {
        client = c;
        if (monthInfo == null) {
            loadMonth();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        if (monthInfo != null) {
            b.putParcelable("monthInfo", monthInfo);
        }
    }

    /**
     * Load or re-load the latest month and display it here.
     */
    public void loadMonth() {
        if (client == null || progressBar == null) {
            return;
        }
        if (currentTask == null) {
            currentTask = new FetchInfoTask();
            currentTask.execute();
        }
    }

    private void showMonthInfo(GeneralMonthInfo info) {
        mainContent.setVisibility(View.VISIBLE);
        totalUsageField.setText(formatMegabytes(info.totalUsageMB));
        freeUsageField.setText(formatMegabytes(info.freeUsageMB));
        billableUsageField.setText(formatMegabytes(info.billableUsageMB));
        billingRateField.setText(info.billingRate);
        totalChargeField.setText(info.totalCharge);
    }

    private String formatMegabytes(long size) {
        if (size > 1024) {
            DecimalFormat f = new DecimalFormat();
            f.setMinimumFractionDigits(2);
            return f.format((double)size / 1024.0) + " GiB";
        }
        return size + " MiB";
    }

    private class FetchInfoTask extends AsyncTask<Void, Void, GeneralMonthInfo> {
        private String errorMessage = null;
        private NubbClient useClient = null;

        @Override
        protected void onPreExecute() {
            // NOTE: we have a separate variable for this because client could be changed if the
            // activity was re-created. We want to avoid race conditions.
            useClient = client;

            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected GeneralMonthInfo doInBackground(Void... params) {
            try {
                return useClient.fetchGeneralMonthInfo();
            } catch (IOException e) {
                errorMessage = e.getLocalizedMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(GeneralMonthInfo result) {
            currentTask = null;
            progressBar.setVisibility(View.GONE);
            if (result == null) {
                errorView.setText(errorMessage);
                errorView.setVisibility(View.VISIBLE);
            } else {
                errorView.setVisibility(View.GONE);
                monthInfo = result;
                showMonthInfo(result);
            }
        }
    }

}
