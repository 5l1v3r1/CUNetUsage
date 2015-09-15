package com.aqnichol.cunetusage;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;


/**
 * MonthView displays an overview for a given month of data usage.
 */
public class MonthView extends RelativeLayout {

    private TextView totalUsageField;
    private TextView freeUsageField;
    private TextView billableUsageField;
    private TextView billingRateField;
    private TextView totalChargeField;
    private GeneralMonthInfo monthInfo = null;

    public MonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(getContext(), R.layout.month_view, this);

        totalUsageField = (TextView)findViewById(R.id.total_usage);
        freeUsageField = (TextView)findViewById(R.id.free_usage);
        billableUsageField = (TextView)findViewById(R.id.billable_usage);
        billingRateField = (TextView)findViewById(R.id.billing_rate);
        totalChargeField = (TextView)findViewById(R.id.total_charge);
    }

    public GeneralMonthInfo getGeneralMonthInfo() {
        return monthInfo;
    }

    public void setGeneralMonthInfo(GeneralMonthInfo info) {
        monthInfo = info;
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

}
